package musil.adam.trackingiron;

import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.File;

@Entity(tableName = "video_table")
public class Video {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    private String path;

    @Ignore
    private Uri videoUri;

    @Ignore
    private File videoFile;

    public Video(File file){
        this.videoFile = file;
        this.videoUri = Uri.fromFile(file);
        this.name = file.getName();
        this.path = file.getPath();
    }


    //setters
    public void setName(@NonNull String name) {
        this.name = name;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setVideoUri(Uri videoUri) {
        this.videoUri = videoUri;
    }

    public void setVideoFile(File videoFile) {
        this.videoFile = videoFile;
    }
    //getters
    @NonNull
    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public Uri getVideoUri() {
        return videoUri;
    }

    public File getVideoFile() {
        return videoFile;
    }



}
