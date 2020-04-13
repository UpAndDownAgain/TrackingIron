package musil.adam.trackingiron;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Video.class}, version = 1)
public abstract class VideoRoomDatabase extends RoomDatabase {

    public abstract VideoDao videoDao();

    private static volatile VideoRoomDatabase INSTANCE;
    private static final int THREAD_NUM = 4;
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(THREAD_NUM);

    static VideoRoomDatabase getDB(final Context context){
        if(INSTANCE == null){
            synchronized (VideoRoomDatabase.class){
                if(INSTANCE == null){
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            VideoRoomDatabase.class, "video_database").build();
                }
            }
        }
        return INSTANCE;
    }
}
