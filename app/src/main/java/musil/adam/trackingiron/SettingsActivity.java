package musil.adam.trackingiron;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREFERENCE_DRAWBOX = "drawBoxSwitch";
    public static final String PREFERENCE_BOX_SIZE = "BoxLineSize";
    public static final String PREFERENCE_PATH_SIZE = "PathLineSize";
    public static final String PREFERENCE_BOX_COLOR = "BoxLineColor";
    public static final String PREFERENCE_PATH_COLOR = "PathLineColor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportFragmentManager().beginTransaction().replace(
                android.R.id.content, new SettingsFragment()).commit();
    }
}
