package musil.adam.trackingiron;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

import static org.bytedeco.ffmpeg.global.swscale.SWS_AREA;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    final static int MY_READ_PERMISSION_CODE  = 10001;
    final static int MY_WRITE_PERMISSION_CODE = 10002;
    final static int SELECT_VIDEO_CODE        = 20001;
    final static int SCALE_RESOLUTION         = 640;
    final static String TAG = "MainActivity";

    FloatingActionButton addButton;

    Uri videoFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkMyPermission(MY_READ_PERMISSION_CODE);

        try{
            loadResourcesFromRaw();
        } catch (AssertionError e) {
            //TODO vyresit
        }

        addButton = findViewById(R.id.fab);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkMyPermission(MY_WRITE_PERMISSION_CODE);
                //TODO permission
                Intent selectVideoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(selectVideoIntent, SELECT_VIDEO_CODE);
            }
        });

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SELECT_VIDEO_CODE && data != null){
            videoFileUri = data.getData();

                File directory = getMyAppDirectory();
                Uri processed = processVideo(videoFileUri, directory);

                Intent playVideoIntent = new Intent(getApplicationContext(), VideoActivity.class);
                playVideoIntent.setData(processed);
                //startActivity(playVideoIntent);

            //todo pridani videa do seznamu

            //prehrani zpracovaneho videa

        }
    }

    /**
     * provedeni detekci na videu
     * nacte jednotlive snimky ze souboru preda je do native kde probehne detekce a vykresleni drahy
     * vysledek ulozi do noveho video souboru
     * @param video original
     * @return zpracovane video s vykreslenou drahou
     */
    private Uri processVideo(Uri video, File directory){
        Uri processedVid = null;

        try{
            final ContentResolver resolver = getApplicationContext().getContentResolver();
            final InputStream inputStream = resolver.openInputStream(video);

            assert(inputStream != null);
            final FFmpegFrameGrabber frameGrabber = new FFmpegFrameGrabber(inputStream);
            final FFmpegFrameRecorder frameRecorder;

            final AndroidFrameConverter converter = new AndroidFrameConverter();
            final String filename = Calendar.getInstance().getTimeInMillis() + ".mp4";
            final File outFile = new File(directory, filename);

            final String format = "mp4"; //todo set programatically
            frameGrabber.setFormat(format);
            //frameGrabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            frameGrabber.start();

            int sourceHeight = frameGrabber.getImageHeight();
            int sourceWidth = frameGrabber.getImageWidth();
            double scale;

            if(sourceHeight > sourceWidth){
                //portraid mode
                scale = (double)SCALE_RESOLUTION / (double)sourceHeight;
            }else{
                //landscape mode
                scale =  (double) SCALE_RESOLUTION / (double) sourceWidth;
            }

            int scaledHeight = (int)(sourceHeight * scale);
            int scaledWidth = (int)(sourceWidth * scale);


            frameGrabber.setImageHeight(scaledHeight);
            frameGrabber.setImageWidth(scaledWidth);
            //viz https://github.com/bytedeco/javacpp-presets/blob/master/ffmpeg/src/gen/java/org/bytedeco/ffmpeg/global/swscale.java#L76-L86
            frameGrabber.setImageScalingFlags(SWS_AREA);


            //inicializace recorderu musi probehnout az po spusteni grabberu
            frameRecorder = FFmpegFrameRecorder.createDefault(outFile, scaledWidth, scaledHeight);
            frameRecorder.setAudioChannels(0);
            frameRecorder.setVideoOption("preset", "ultrafast");
            frameRecorder.setVideoOption("crf", "28");
            frameRecorder.setVideoBitrate(500000);
            frameRecorder.setFormat("mp4");
            frameRecorder.start();

            final Point point = new Point(30,30);
            final Scalar textColor = new Scalar(0,71,179);

            Bitmap bmp;
            Mat mat = new Mat();
            Frame frame;
            int counter = 0;

            while(true){
                frame = frameGrabber.grabImage();
                if(frame == null){
                    //konec videa
                    break;
                }
                Log.i("PROCESSING", "processing frame " + ++counter);
                bmp = converter.convert(frame);
                Utils.bitmapToMat(bmp, mat);

                //todo call detection

                Imgproc.putText(mat, "TrackingIron", point,
                        Imgproc.FONT_HERSHEY_COMPLEX, 1, textColor, 2);

                Utils.matToBitmap(mat, bmp);
                frame = converter.convert(bmp);
                frameRecorder.record(frame);
            }

            frameRecorder.stop();
            frameRecorder.release();
            frameGrabber.stop();
            frameGrabber.release();
            inputStream.close();

            processedVid = Uri.fromFile(outFile);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        } catch (FrameRecorder.Exception e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        return processedVid;
    }

    private File getMyAppDirectory()/* throws IOException*/{
        File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File myAppDir = new File(dcimDir, "TrackingIron");

        if(!myAppDir.exists() ){
            myAppDir.mkdirs();
            //throw new IOException("ERROR CREATING DIRECTORY");
        }

        return myAppDir;
    }

    private void checkMyPermission(int permissionCode){
        switch (permissionCode) {
            case MY_READ_PERMISSION_CODE: {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        //todo
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                MY_READ_PERMISSION_CODE);
                    }
                }
                break;
            }
            case MY_WRITE_PERMISSION_CODE: {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        //todo
                    } else {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                MY_WRITE_PERMISSION_CODE);
                    }
                }
                break;
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_READ_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Read Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("PERMISSION", "read permission denied");
                    this.finishAndRemoveTask();
                }
                break;
            }
            case MY_WRITE_PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Write Permission Granted", Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("PERMISSION", "write permission denied");
                    this.finishAndRemoveTask();
                }
                break;
            }
        }

    }

    /**
     * zkopiruje yolo config a weights do slozky aplikace
     * a s jejich pomoci inicializuje nativni tridy
     */
    private void loadResourcesFromRaw() throws AssertionError{
        File dir = getDir("Resources", Context.MODE_PRIVATE);
        File cfg = loadFromRaw("yolo_tiny_config", "cfg", dir);
        File weights = loadFromRaw("yolo_tiny_weights", "weights", dir);


        assert cfg != null;
        assert weights != null;
        init_jni(cfg.getAbsolutePath(), weights.getAbsolutePath());
    }

    private File loadFromRaw( String file, String suffix, File directory){
        InputStream is;
        OutputStream os;
        File newFile = null;
        try{
            is = getResources().openRawResource(
                    getResources().getIdentifier(file, "raw", getPackageName()));
            newFile = new File(directory, file + "." + suffix);
            os = new FileOutputStream(newFile);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while((bytesRead = is.read(buffer)) != -1){
                os.write(buffer);
            }

            is.close();
            os.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile;
    }

    /**
     * hlavicky nativnich metod pro volani s jni
     */
    public native void init_jni(String cfg, String weights);
}
