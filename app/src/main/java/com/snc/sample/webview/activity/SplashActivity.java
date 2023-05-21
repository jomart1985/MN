package com.snc.sample.webview.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.snc.sample.webview.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_splash);


        new Handler().postDelayed(new Runnable() {


            @Override
            public void run() {


                //throw new RuntimeException("Test Crash"); // Force a crash


                Intent i = new Intent(SplashActivity.this, LauncherActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(i);
                finish();


            }
        }, 1500);




    }
}