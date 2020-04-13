package musil.adam.trackingiron;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class VideoActivity extends AppCompatActivity {

    private VideoView videoView;
    private MediaController controller;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Intent intent = getIntent();
        Uri videoUri = intent.getData();

        //nastaveni videoview a kontroleru
        videoView = findViewById(R.id.Video);
        controller = new MediaController(this);
        controller.setAnchorView(videoView);
        videoView.setMediaController(controller);
        videoView.setVideoURI(videoUri);
        videoView.start();
    }
}
