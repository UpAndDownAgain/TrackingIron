package musil.adam.trackingiron;

import android.content.res.Resources;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    /**
     * funkce zkopiruje data z res/raw do slozky applikace a vrati File
     */
    public static File loadFromRaw(Resources res, String packageName, String file, String suffix, File directory){
        InputStream is;
        OutputStream os;
        File newFile = null;
        try{
            is = res.openRawResource(
                    res.getIdentifier(file, "raw", packageName));
            newFile = new File(directory, file + "." + suffix);
            os = new FileOutputStream(newFile);

            byte[] buffer = new byte[4096];
            int bytesRead;

            while((bytesRead = is.read(buffer)) != -1){
                os.write(buffer);
            }

            is.close();
            os.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile;
    }
}
