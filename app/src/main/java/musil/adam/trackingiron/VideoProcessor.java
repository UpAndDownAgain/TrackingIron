package musil.adam.trackingiron;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.net.Uri;
import android.renderscript.ScriptGroup;
import android.util.Log;

import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.Frame;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.EventListener;
import java.util.Set;
import java.util.concurrent.Executor;

import static org.bytedeco.ffmpeg.global.swscale.SWS_AREA;

class VideoProcessor {

    private Uri inputVideo;
    private Uri outputVideo;
    private File outputDir;
    private ContentResolver resolver;

    private AndroidFrameConverter converter;
    private FFmpegFrameRecorder recorder;
    private FFmpegFrameGrabber grabber;


    private String sourceFormat;
    private double scale;
    private int scaledWidth;
    private int scaledHeight;
    private int scaleTo;


    VideoProcessor(ContentResolver resolver, Uri inputVideo, File directory, String format, int scaleTo){

        converter = new AndroidFrameConverter();
        this.resolver = resolver;
        this.inputVideo = inputVideo;
        this.sourceFormat = format;
        this.scaleTo = scaleTo;
        this.outputDir = directory;
    }

    Uri getProcessedVid(){
        return outputVideo;
    }

    void processVideo() throws IOException {
        InputStream is = resolver.openInputStream(inputVideo);
        grabber = new FFmpegFrameGrabber(is);
        grabber.setFormat(sourceFormat);
        grabber.start();

        scaleResolutions(grabber.getImageWidth(), grabber.getImageHeight());

        grabber.setImageHeight(scaledHeight);
        grabber.setImageWidth(scaledWidth);
        //viz https://github.com/bytedeco/javacpp-presets/blob/master/ffmpeg/src/gen/java/org/bytedeco/ffmpeg/global/swscale.java#L76-L86
        grabber.setImageScalingFlags(SWS_AREA);

        File out = new File(outputDir, Calendar.getInstance().getTimeInMillis() + ".mp4");
        recorder = FFmpegFrameRecorder.createDefault(out, scaledWidth, scaledHeight);
        recorder.setAudioChannels(0);
        recorder.setFrameRate(grabber.getFrameRate());
        recorder.setVideoOption("preset", "ultrafast");
        recorder.setVideoOption("crf", "28");
        recorder.setVideoBitrate(500000);
        recorder.setFormat("mp4");
        recorder.start();

        Bitmap bmp;
        Mat mat = new Mat();
        Frame frame;
        int counter = 0;

        while(true){
            frame = grabber.grabImage();
            if(frame == null){
                clearBarPath_jni();
                break;
            }
            Log.i("Processing", "Frame " + ++counter);
            bmp = converter.convert(frame);
            Utils.bitmapToMat(bmp, mat);

            detectAndDraw_jni(mat.getNativeObjAddr());

            Utils.matToBitmap(mat, bmp);
            frame = converter.convert(bmp);
            recorder.record(frame);
        }
        recorder.stop();
        recorder.release();
        grabber.stop();
        grabber.release();
        if (is != null) {
            is.close();
        }

        outputVideo = Uri.fromFile(out);
    }

    private void scaleResolutions(int sourceWidth, int sourceHeight){
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

    private native void detectAndDraw_jni(long matAddress);
    private native void clearBarPath_jni();
}
