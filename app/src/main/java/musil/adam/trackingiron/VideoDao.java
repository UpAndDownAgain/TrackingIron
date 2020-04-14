package musil.adam.trackingiron;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * interface slouzici k obhospodarovani db
 */

@Dao
public interface VideoDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Video video);

    @Query("DELETE FROM video_table")
    void deleteAll();

    @Delete
    void delete(Video video);

    @Query("SELECT * FROM video_table ORDER BY name ASC")
    LiveData<List<Video>> getAllVideos();

    @Query("UPDATE video_table SET display_name = :newName WHERE name= :name")
    void changeName(String newName, String name);

}
