package musil.adam.trackingiron;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    final int SELECT_VIDEO_CODE = 101;
    final int SCALE_RESOLUTION = 640;
    final String TAG = "MainActivity";

    Button addButton;

    Uri videoFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try{
            loadResourcesFromRaw();
        } catch (AssertionError e) {
            //TODO vyresit
        }
        /*
        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO permission
                Intent selectVideoIntent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(selectVideoIntent, SELECT_VIDEO_CODE);
            }
        });
        */

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == SELECT_VIDEO_CODE && data != null){
            videoFileUri = data.getData();

            Uri processed = processVideo(videoFileUri);
            //todo pridani videa do seznamu

            //prehrani zpracovaneho videa
            Intent playVideoIntent = new Intent(getApplicationContext(), VideoActivity.class);
            playVideoIntent.setData(processed);
            startActivity(playVideoIntent);
        }
    }

    /**
     * provedeni detekci na videu
     * nacte jednotlive snimky ze souboru preda je do native kde probehne detekce a vykresleni drahy
     * vysledek ulozi do noveho video souboru
     * @param video original
     * @return zpracovane video s vykreslenou drahou
     */
    private Uri processVideo(Uri video){
        Uri processedVid = null;
        //TODO zpracovat video

        return processedVid;
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
