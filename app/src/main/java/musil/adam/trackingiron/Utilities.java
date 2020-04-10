package musil.adam.trackingiron;

import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

class Utilities {
    static File getMyAppDirectory() throws IOException{
        @SuppressWarnings("deprecation") File dcimDir = Environment.
                getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        File myAppDir = new File(dcimDir, "TrackingIron");

        if(!myAppDir.exists() || myAppDir.mkdirs() ){
            throw new IOException("ERROR CREATING DIRECTORY");
        }

        return myAppDir;
    }

    /**
     * funkce zkopiruje data z res/raw do slozky applikace a vrati File
     */
    static File loadFromRaw(Resources res, String packageName, String file, String suffix, File directory){
        InputStream is;
        OutputStream os;
        File newFile = null;
        try{
            is = res.openRawResource(
                    res.getIdentifier(file, "raw", packageName));
            newFile = new File(directory, file + "." + suffix);
            os = new FileOutputStream(newFile);

            byte[] buffer = new byte[4096];

            while((is.read(buffer)) != -1){
                os.write(buffer);
            }

            is.close();
            os.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return newFile;
    }

    static String getFileExtensionFromUri(Uri uri){
        String mimeType = new MediaMetadataRetriever().extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_MIMETYPE);

        return mimeType.split("/")[1];
    }
}
