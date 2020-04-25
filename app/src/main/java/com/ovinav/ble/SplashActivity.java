package com.ovinav.ble;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        img = findViewById(R.id.img);
        Animation fadein = AnimationUtils.loadAnimation(this, R.anim.fadein);
        final Animation fadeout = AnimationUtils.loadAnimation(this, R.anim.fadeout);
        img.startAnimation(fadein);
        img.postOnAnimationDelayed(new Runnable() {
            @Override
            public void run() {
                img.startAnimation(fadeout);
            }
        }, 1500);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }
        }, 3000);
    }
}
