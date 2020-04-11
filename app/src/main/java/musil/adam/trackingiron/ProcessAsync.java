package musil.adam.trackingiron;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import java.io.IOException;
import java.lang.ref.WeakReference;

public class ProcessAsync extends AsyncTask<Void,Void,Void> {

    private WeakReference<ProgressBar> progressBarRef;
    private WeakReference<VideoProcessor> videoProcessRef;
    private WeakReference<Context> contextRef;

    ProcessAsync(VideoProcessor vp, ProgressBar pb, Context context){
        videoProcessRef = new WeakReference<>(vp);
        progressBarRef = new WeakReference<>(pb);
        contextRef = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {
        System.gc();
        progressBarRef.get().setVisibility(View.VISIBLE);
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try{
            videoProcessRef.get().processVideo();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        progressBarRef.get().setVisibility(View.GONE);
        final Uri processedVid = videoProcessRef.get().getProcessedVid();
        Intent videoIntent = new Intent(contextRef.get().getApplicationContext(), VideoActivity.class);
        videoIntent.setData(processedVid);
        videoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        contextRef.get().startActivity(videoIntent);
    }
}
