package musil.adam.trackingiron;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import java.io.IOException;
import java.lang.ref.WeakReference;

class ProcessAsync extends AsyncTask<Void,Void,Void> {

    final private WeakReference<ProgressBar> progressBarRef;
    final private WeakReference<VideoProcessor> videoProcessRef;
    final private WeakReference<VideoViewModel> videoViewModelRef;
    final private WeakReference<Context> contextRef;

    ProcessAsync(VideoProcessor vp, ProgressBar pb, VideoViewModel vvm, Context context){
        videoProcessRef = new WeakReference<>(vp);
        progressBarRef = new WeakReference<>(pb);
        videoViewModelRef = new WeakReference<>(vvm);
        contextRef = new WeakReference<>(context);
    }

    @Override
    protected void onPreExecute() {

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

        Video processedVideo = new Video(videoProcessRef.get().getProcessedVid());
        videoViewModelRef.get().insert(processedVideo);

        Intent videoIntent = new Intent(contextRef.get().getApplicationContext(), VideoActivity.class);
        videoIntent.setData(processedVideo.getVideoUri());
        videoIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        contextRef.get().startActivity(videoIntent);
    }
}
