package musil.adam.trackingiron;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class VideoViewModel extends AndroidViewModel {

    private VideoRepository repository;
    private LiveData<List<Video>> allVids;


    public VideoViewModel(@NonNull Application application) {
        super(application);

        repository = new VideoRepository(application);
        allVids = repository.getAllVids();

    }

    LiveData<List<Video>> getAllVids(){
        return allVids;
    }

    void insert(Video vid){
        repository.insert(vid);
    }

    void delete(Video vid){
        repository.delete(vid);
    }

    void changeName(String newName, String name){
        repository.changeName(newName, name);
    }
}
