package com.example.myapplication;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

public class GameActivity extends AppCompatActivity {

    private Gameview gameview;
    private View decorView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // New Point
        Point point = new Point();
        getWindowManager().getDefaultDisplay().getSize(point);

        // Initialize variable "gameview"
        gameview = new Gameview(this, point.x, point.y);
        // You will see the contents of the "gameview" variable
        setContentView(gameview);
        decorView = getWindow().getDecorView();
        decorView.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onSystemUiVisibilityChange(int visibility) {
                if (visibility == 0) {
                    decorView.setSystemUiVisibility(hideSystemBars());
                }
            }
        });

    }

    // The "pause" method of the "gameview" variable is enabled
    @Override
    protected void onPause() {
        super.onPause();
        try {
            gameview.pause();

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    // The "resume" method of the "gameview" variable is enabled
    @Override
    protected void onPostResume() {
        super.onPostResume();
        gameview.resume();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            decorView.setSystemUiVisibility(hideSystemBars());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private int hideSystemBars() {
        return View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
    }
}
