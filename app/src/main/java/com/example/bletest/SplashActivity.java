package com.example.bletest;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {
  //  SharedPreferences s;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
    //            s = getSharedPreferences("ble_login",MODE_PRIVATE);
      //          String mail = s.getString("email","null");
        //        String password = s.getString("password","null");
//                Class c;
//                if(!mail.equals("null") && !password.equals("null"))
//                    c = MainActivity.class;
//                else
//                    c= LoginActivity.class;
                startActivity(new Intent(SplashActivity.this,MainActivity.class));
                finish();
            }
        },3000);
    }
}
