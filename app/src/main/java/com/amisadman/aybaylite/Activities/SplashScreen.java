package com.amisadman.aybaylite.Activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.amisadman.aybaylite.R;

@SuppressLint("CustomSplashScreen")
public class SplashScreen extends AppCompatActivity
{
    private Handler handler;
    private Runnable runnable;
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        handler = new Handler();

        runnable = new Runnable()
        {
            @Override
            public void run()
            {
                Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        };
        handler.postDelayed(runnable, 3500);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (handler != null)
        {
            handler.removeCallbacks(runnable);
        }
    }
}
