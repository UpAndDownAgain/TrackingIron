package musil.adam.trackingiron;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

/**
 * obsluha db
 */
class VideoRepository {

    private VideoDao videoDao;
    private LiveData<List<Video>> allVids;

    VideoRepository(Application app){
        VideoRoomDatabase db = VideoRoomDatabase.getDB(app);
        videoDao = db.videoDao();
        allVids = videoDao.getAllVideos();
    }

    LiveData<List<Video>> getAllVids(){
        return allVids;
    }

    void insert(Video video){
        VideoRoomDatabase.databaseWriteExecutor.execute(()->videoDao.insert(video));
    }

    void delete(Video video){
        VideoRoomDatabase.databaseWriteExecutor.execute(()->videoDao.delete(video));
    }

    void changeName(String newName, String name){
        VideoRoomDatabase.databaseWriteExecutor.execute(()->videoDao.changeName(newName, name));
    }
}
