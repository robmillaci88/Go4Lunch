package com.example.robmillaci.go4lunch.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.robmillaci.go4lunch.R;

public class SettingsActivity extends AppCompatActivity {
    private static final int MAX_ZOOM = 21;

    public static final String DEFAULT_ZOOM_KEY = "dzoom";
    public static final String PLACES_SEARCH_ZOOM_KEY = "pzoom";

    private static int defaultZoomVal = 0;
    private static int defaultPlacesSearchZoomVal = 0;


    private TextView mapZoomVal;
    private TextView placesSearchZoomVal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_NOSENSOR);
        setTitle(getString(R.string.settings_activity_title));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); //adds the home button to the action bar to navigate back from this activity
        }

        SeekBar mapZoomProgBar = findViewById(R.id.map_zoom_prog_bar);
        mapZoomVal = findViewById(R.id.map_zoom_val);

        SeekBar placesSearchZoomProgBar = findViewById(R.id.places_search_zoom_progress);
        placesSearchZoomVal = findViewById(R.id.places_search_zoom_val);


        mapZoomProgBar.setMax(MAX_ZOOM);
        placesSearchZoomProgBar.setMax(MAX_ZOOM);

        int restoredMapZoom = restoreDefaultZoom();
        mapZoomProgBar.setProgress(restoredMapZoom);
        mapZoomVal.setText(String.valueOf(restoredMapZoom));
        mapZoomVal.setTextColor(getResources().getColor(R.color.colorPrimary));


        int restoredPlacesZoom = restorePlacesSearchZoom();
        placesSearchZoomProgBar.setProgress(restoredPlacesZoom);
        placesSearchZoomVal.setText(String.valueOf(restoredPlacesZoom));
        placesSearchZoomVal.setTextColor(getResources().getColor(R.color.colorPrimary));


        mapZoomProgBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                defaultZoomVal = progress;
                mapZoomVal.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mapZoomVal.setTextColor(getResources().getColor(R.color.colorAccent));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mapZoomVal.setTextColor(getResources().getColor(R.color.colorPrimary));
                saveToPrefs();
            }
        });


        placesSearchZoomProgBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                defaultPlacesSearchZoomVal = progress;
                placesSearchZoomVal.setText(String.valueOf(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                placesSearchZoomVal.setTextColor(getResources().getColor(R.color.colorAccent));
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                placesSearchZoomVal.setTextColor(getResources().getColor(R.color.colorPrimary));
                saveToPrefs();
            }
        });
    }


    @SuppressLint("ApplySharedPref")
    private void saveToPrefs() {
        SharedPreferences.Editor spEditor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        spEditor.putInt(DEFAULT_ZOOM_KEY, defaultZoomVal);
        spEditor.putInt(PLACES_SEARCH_ZOOM_KEY, defaultPlacesSearchZoomVal);
        spEditor.commit();
    }

    private int restoreDefaultZoom() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getInt(DEFAULT_ZOOM_KEY, 13);
    }

    private int restorePlacesSearchZoom() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        return sp.getInt(PLACES_SEARCH_ZOOM_KEY, 18);
    }

}
