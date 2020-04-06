package musil.adam.trackingiron;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    final int SELECT_VIDEO_CODE = 101;

    Button addButton;

    Uri videoFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addButton = findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
            //todo zpracovani videa
            //todo pridani videa do seznamu
            Uri processed;

            //prehrani zpracovaneho videa
            Intent playVideoIntent = new Intent(getApplicationContext(), VideoActivity.class);
            playVideoIntent.setData(processed);
            startActivity(playVideoIntent);
        }
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
