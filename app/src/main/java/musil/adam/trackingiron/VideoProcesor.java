package musil.adam.trackingiron;

import android.content.ContentResolver;
import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import org.bytedeco.ffmpeg.global.avcodec;
import org.bytedeco.javacv.AndroidFrameConverter;
import org.bytedeco.javacv.FFmpegFrameGrabber;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameGrabber;
import org.bytedeco.javacv.FrameRecorder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;

public class VideoProcesor {
    private InputStream is;
    private OutputStream os;
    private ContentResolver resolver;
    private FFmpegFrameGrabber grabber;
    private FFmpegFrameRecorder recorder;
    private AndroidFrameConverter convertor;


    private File directory;
    private File output;


    int videoWidth;
    int videoHeight;
    private double fps;
    private int totalFrames;
    private String fileFormat;

    public VideoProcesor(Uri video, Context appContext, File directory){
        this.resolver = appContext.getContentResolver();
        this.directory = directory;

        MediaMetadataRetriever metadataRetriever = new MediaMetadataRetriever();
        metadataRetriever.setDataSource(appContext, video);

        totalFrames = Integer.parseInt(
                metadataRetriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT));
        fps = Integer.parseInt(
                metadataRetriever.extractMetadata(
                        MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE));

        fileFormat =  MimeTypeMap.getFileExtensionFromUrl(video.toString());
        try{
            initGrabber(video);
            initRecorder();
        } catch (FrameRecorder.Exception e) {
            //todo
            e.printStackTrace();
        } catch (FrameGrabber.Exception e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void scaleResolution(){

    }

    private void initGrabber(Uri video) throws FileNotFoundException, FrameGrabber.Exception {
            is = resolver.openInputStream(video);
            grabber = new FFmpegFrameGrabber(is);
            grabber.setVideoCodec(avcodec.AV_CODEC_ID_H264);
            grabber.setFormat(fileFormat);
            grabber.start();
    }

    private void initRecorder() throws FrameRecorder.Exception {
        if(grabber == null){
            throw new NullPointerException();
        }
        String fileName = Long.toString(Calendar.getInstance().getTimeInMillis()) + "." +fileFormat;
        output = new File(directory, fileName);

        recorder = FFmpegFrameRecorder.createDefault(output, videoWidth, videoHeight);
        recorder.start();
    }





}
