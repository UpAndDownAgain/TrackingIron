package musil.adam.trackingiron;

import android.os.Environment;

import java.io.File;

public class Utilities {
    public static File getMyAppDirectory()/* throws IOException*/{
        File dcimDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File myAppDir = new File(dcimDir, "TrackingIron");

        if(!myAppDir.exists() ){
            myAppDir.mkdirs();
            //throw new IOException("ERROR CREATING DIRECTORY");
        }

        return myAppDir;
    }
}
