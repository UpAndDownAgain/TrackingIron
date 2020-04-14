package musil.adam.trackingiron;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // nacteni nativnich knihoven
    static {
        System.loadLibrary("native-lib");
    }

    private final static int MY_READ_PERMISSION_CODE  = 10001;
    private final static int MY_WRITE_PERMISSION_CODE = 10002;
    private final static int SELECT_VIDEO_CODE        = 20001;
    private final static int SCALE_RESOLUTION         = 640;
    private static String PACKAGE_NAME;

    private ProgressBar spinner;

    private VideoViewModel videoViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PACKAGE_NAME = getPackageName();

        checkMyPermission(MY_READ_PERMISSION_CODE);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final VideoListAdapter adapter = new VideoListAdapter(this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        videoViewModel = new ViewModelProvider(this).get(VideoViewModel.class);
        videoViewModel.getAllVids().observe(this, new Observer<List<Video>>() {
            @Override
            public void onChanged(List<Video> videos) {
                adapter.setVids(videos);
            }
        });

        try {
            //rozbali yolo cfg a weights, inicializuje tridy v native
            loadResources();
        } catch (FileNotFoundException e) {
            //extrakce se nezdarila, zobrazi se informacni dialog
            new AlertDialog.Builder(this)
                    .setTitle("Error loading assets")
                    .setMessage("Application wasn't able to extract it's assets, " +
                            "please check there is enough space on device")
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finishAndRemoveTask();
                        }
                    }).setIcon(android.R.drawable.ic_dialog_alert).show();

        }
        spinner = findViewById(R.id.spinner);
        spinner.setVisibility(View.GONE);

        //FAB na pridani pridani noveho videa
        FloatingActionButton addButton = findViewById(R.id.fab);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkMyPermission(MY_WRITE_PERMISSION_CODE);
                Intent selectVideoIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(selectVideoIntent, SELECT_VIDEO_CODE);
            }
        });

        //nastavi hodnoty v settings na default pri prvnim spusteni, neprepisuje uzivatelske nastaveni
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        //nacteni nastaveni z Shared Preferences
        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        //vyklresleni ohranicujiciho boxu
        boolean drawBox = sharedPreferences.getBoolean(
                SettingsActivity.PREFERENCE_DRAWBOX, false);

        int boxLineSize;
        int pathLineSize;
        try {
            boxLineSize = Integer.parseInt(
                    sharedPreferences.getString(SettingsActivity.PREFERENCE_BOX_SIZE, "1"));

            pathLineSize = Integer.parseInt(
                    sharedPreferences.getString(SettingsActivity.PREFERENCE_PATH_SIZE, "1"));
        }catch (Exception ex){
            boxLineSize = 1;
            pathLineSize = 1;
        }
        String boxColor = sharedPreferences.getString(
                SettingsActivity.PREFERENCE_BOX_COLOR, "red");
        String pathColor = sharedPreferences.getString(
                SettingsActivity.PREFERENCE_PATH_COLOR, "red");

        setDrawBox_jni(drawBox);
        setBoxSize_jni(boxLineSize);
        setBarPathSize_jni(pathLineSize);

        switch (boxColor.toLowerCase()){
            case "red":
                setBoxColor_jni(255,0,0);
                break;
            case "green":
                setBoxColor_jni(0,255,0);
                break;
            case "blue":
                setBoxColor_jni(0,0,255);
                break;
        }
        switch (pathColor.toLowerCase()){
            case "red":
                setBarPathColor_jni(255,0,0);
                break;
            case "green":
                setBarPathColor_jni(0,255,0);
                break;
            case "blue":
                setBarPathColor_jni(0,0,255);
                break;
        }

        //pridani dotykove funkcionality pro smazani
        ItemTouchHelper helper = new ItemTouchHelper(
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView,
                                          @NonNull RecyclerView.ViewHolder viewHolder,
                                          @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    //smazat zaznam pri odsunuti
                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder,
                                         int direction) {
                        int position = viewHolder.getAdapterPosition();
                        Video video = adapter.getVideoAtPosition(position);
                        Toast.makeText(MainActivity.this, "Deleting " + video.getName(),
                                Toast.LENGTH_LONG).show();

                        videoViewModel.delete(video);
                        video.getVideoFile().delete();
                    }
                });
        //pripojeni touch helperu k recycler view
        helper.attachToRecyclerView(recyclerView);

        //prehrani videa v nove aktivite pri doteku
        adapter.setOnItemClickListener(new VideoListAdapter.ClickListener(){
            @Override
            public void onItemClick(View v, int position){
                Video video = adapter.getVideoAtPosition(position);
                launchPlayVideoActivity(video);
            }

        });
        //zobrazi dialog pro prejmenovani polozky pri dlouhem dotyku
        adapter.setLongClickListener(new VideoListAdapter.LongClickListener() {
            @Override
            public void onItemClick(View v, int position) {
                Video video = adapter.getVideoAtPosition(position);
                EditText input = new EditText(getApplicationContext());
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Change name")
                        .setMessage("Enter new name")
                        .setView(input)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String newName = input.getText().toString();
                                Log.i("NAME CHANGE", "changing " + video.getName()
                                        + " to " + newName);
                                videoViewModel.changeName(newName, video.getName());
                            }
                        })
                        .setNegativeButton("Cancel", null).show();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.action_settings){
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECT_VIDEO_CODE && data != null) {
            Uri videoFileUri = data.getData();

            try {
                //slozka ulozeni videa
                File directory = Utilities.getMyAppDirectory();
                //vytvoreni noveho video souboru se zakreslenou detekci

                String format = Utilities.getFileExtensionFromUri(getApplicationContext(), videoFileUri);

                final VideoProcessor processor = new VideoProcessor(
                        getContentResolver(), videoFileUri, directory, format, SCALE_RESOLUTION);

                ProcessAsync processAsync = new ProcessAsync(processor, spinner, videoViewModel, getApplicationContext());
                processAsync.execute();



                //todo pridani videa do seznamu
            }catch (IOException e){
                e.printStackTrace();
                new AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage(e.toString())
                        .setPositiveButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).setIcon(android.R.drawable.ic_dialog_alert).show();
            }
        }
    }

    public void launchPlayVideoActivity(Video video){
        Intent intent = new Intent(this, VideoActivity.class);
        intent.setData(video.getVideoUri());
        startActivity(intent);
    }

    //runtime kontrola permission
    private void checkMyPermission(int permissionCode) {
        switch (permissionCode) {
            case MY_READ_PERMISSION_CODE: {
                if (ContextCompat.checkSelfPermission(MainActivity.this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
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
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_READ_PERMISSION_CODE: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Read Permission Granted",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Log.i("PERMISSION", "read permission denied");
                    this.finishAndRemoveTask();
                }
                break;
            }
            case MY_WRITE_PERMISSION_CODE: {
                if (grantResults.length > 0 &&
                        grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Write Permission Granted",
                            Toast.LENGTH_SHORT).show();
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
    private void loadResources() throws FileNotFoundException {
        File dir = getDir("Resources", Context.MODE_PRIVATE);
        File cfg = Utilities.loadFromRaw(getResources(), PACKAGE_NAME,
                "yolo_tiny_config", "cfg", dir);
        File weights = Utilities.loadFromRaw(getResources(), PACKAGE_NAME,
                "yolo_tiny_weights", "weights", dir);

        if (cfg == null || weights == null) {
            throw new FileNotFoundException("Failed to load resources");
        }
        init_jni(cfg.getAbsolutePath(), weights.getAbsolutePath());
    }

    @Override
    protected void onDestroy() {
        cleanUp_jni();
        super.onDestroy();
    }

    /**
     * hlavicky nativnich metod jni
     */
    //inicializace detektoru a trackeru
    public native void init_jni(String cfg, String weights);

    public native void setDrawBox_jni(boolean drawBox);

    public native void setBoxSize_jni(int size);

    public native void setBoxColor_jni(int r, int g, int b);

    public native void setBarPathSize_jni(int size);

    public native void setBarPathColor_jni(int r, int g, int b);

    public native void cleanUp_jni();
}
