package musil.adam.trackingiron;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;

/**
 * trida ke zpracovavani videa
 */

class VideoProcessor {

    final private Uri inputVideo;
    private File outputVideo;
    final private File outputDir;
    final private ContentResolver resolver;

    final private AndroidFrameConverter converter;

    final private String sourceFormat;
    private int scaledWidth;
    private int scaledHeight;
    final private int scaleTo;
    final private String rotation;


    VideoProcessor(ContentResolver resolver, Uri inputVideo, File directory, String format, int scaleTo, String rotation){

        converter = new AndroidFrameConverter();
        this.resolver = resolver;
        this.inputVideo = inputVideo;
        this.sourceFormat = format;
        this.scaleTo = scaleTo;
        this.outputDir = directory;
        this.rotation = rotation;
    }

    File getProcessedVid(){
        return outputVideo;
    }

    void processVideo() throws IOException {
        //resetoveni trackeru v jni aby byl v korektnim stavu
        resetTracker_jni();

        //input stream pro inicializaci framegrabberu
        InputStream is = resolver.openInputStream(inputVideo);

        //inicializace a spusteni framegrabberu
        FFmpegFrameGrabber grabber = new FFmpegFrameGrabber(is);
        grabber.setFormat(sourceFormat);
        grabber.start();

        //skalovani videa aby se nacitalo v nizsim rozliseni -> rychleji
        scaleResolutions(grabber.getImageWidth(), grabber.getImageHeight());
        grabber.setImageHeight(scaledHeight);
        grabber.setImageWidth(scaledWidth);
        //viz https://github.com/bytedeco/javacpp-presets/blob/master/ffmpeg/src/gen/java/org/bytedeco/ffmpeg/global/swscale.java#L76-L86
        //grabber.setImageScalingFlags(SWS_AREA);

        //soubor pro zapis noveho videa
        File out = new File(outputDir, Calendar.getInstance().getTimeInMillis() + ".mp4");

        //inicializace a spusteni framerecorderu, musi probehnout po spusteni framegrabberu
        FFmpegFrameRecorder recorder = FFmpegFrameRecorder.createDefault(out, scaledWidth, scaledHeight);
        recorder.setAudioChannels(0); //ignorujeme zvuk
        int fps = (int)grabber.getFrameRate();
        String rot = grabber.getVideoMetadata("rotation");
        recorder.setFrameRate(fps); //stejny fps jako origo video
        //nastaveni video kvality, cim horsi tim rychlejsi
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoOption("crf", "28");
        recorder.setVideoBitrate(1000000);
        recorder.setFormat("mp4");
        //nastaveni rotace puvodniho videa
        recorder.setVideoMetadata("rotate", rotation);
        recorder.start();

        Bitmap bmp;

        Mat mat = new Mat();
        Frame frame;
        int counter = 0;


        while(true){
            frame = grabber.grabImage();
            if(frame == null){
                //konec video souboru
                clearBarPath_jni();
                break;
            }
            Log.i("Processing", "Frame " + ++counter);
            bmp = converter.convert(frame);
            Utils.bitmapToMat(bmp, mat);

           //provedeni detekce a zakresleni vysledku do snimku
            detectAndDraw_jni(mat.getNativeObjAddr());

            Utils.matToBitmap(mat, bmp);
            frame = converter.convert(bmp);
            recorder.record(frame);
        }

        //uklid
        recorder.stop();
        recorder.release();
        grabber.stop();
        grabber.release();
        if (is != null) {
            is.close();
        }

        outputVideo = out;
    }

    private void scaleResolutions(int sourceWidth, int sourceHeight){
        double scale;
        if (sourceHeight > sourceWidth) {
            //portraid mode
            scale = (double) scaleTo / (double) sourceHeight;
        } else {
            //landscape mode
            scale = (double) scaleTo / (double) sourceWidth;
        }

        scaledWidth = (int)(sourceWidth * scale);
        scaledHeight = (int)(sourceHeight * scale);
    }
    //nativni metody
    private native void detectAndDraw_jni(long matAddress);
    private native void clearBarPath_jni();
    private native void resetTracker_jni();
}
