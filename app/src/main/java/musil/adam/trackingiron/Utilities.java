package musil.adam.trackingiron;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * funkce vrati File objekt slozky DCIM/TrackingIron
 * pokud slozka neexistuje funkce ji vytvori
 */
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
     * funkce zkopiruje data z res/raw do slozky applikace a vrati File zkopirovanych dat
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

    /**
     * funkce vrati pripomu souboru z uri, tzn mp4 a pod
     * @param context context aplikace
     * @param uri uri souboru
     * @return retezec pripomy (mp4)
     */
    static String getFileExtensionFromUri(Context context, Uri uri){
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(context, uri);
        String mimeType = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);

        return mimeType.split("/")[1];
    }

    /**
     * funkce nacte hodnoty uzivatelskeho nastaveni z shared preferences,
     * preda je do nativni casti
     * @param sharedPreferences instance sharedpreferences
     */

    static void setUserSettings(SharedPreferences sharedPreferences){
        boolean drawBox;
        int boxLineSize;
        int pathLineSize;
        String boxColor;
        String pathColor;

        //nacteni hodnot z sharedpreferences
        drawBox = sharedPreferences.getBoolean(SettingsActivity.PREFERENCE_DRAWBOX, false);
        boxLineSize = Integer.parseInt(
                sharedPreferences.getString(SettingsActivity.PREFERENCE_BOX_SIZE, "2"));
        pathLineSize = Integer.parseInt(
                sharedPreferences.getString(SettingsActivity.PREFERENCE_PATH_SIZE, "2"));
        boxColor = sharedPreferences.getString(SettingsActivity.PREFERENCE_BOX_COLOR, "red");
        pathColor = sharedPreferences.getString(SettingsActivity.PREFERENCE_PATH_COLOR, "red");

        //nastaveni nactenych hodnot
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

    }


    static public native void setDrawBox_jni(boolean drawBox);

    static public native void setBoxSize_jni(int size);

    static public native void setBoxColor_jni(int r, int g, int b);

    static public native void setBarPathSize_jni(int size);

    static public native void setBarPathColor_jni(int r, int g, int b);
}
