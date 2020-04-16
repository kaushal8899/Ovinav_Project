package com.example.bletest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity implements Networkback{

    TextInputEditText e1, e2, e3, e4;
    TextInputLayout l1, l2;
    CheckBox check;
    SharedPreferences s;
    String deviceId = "",matId="";
    LinearLayout parent;
    boolean validEmail=true,validPassword=true;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        matId = getIntent().getStringExtra("matId");
        s = getSharedPreferences("ble_login", MODE_PRIVATE);
        e1 = findViewById(R.id.email);
        e2 = findViewById(R.id.password);
        e3 = findViewById(R.id.d_uid);
        e4 = findViewById(R.id.m_uid);
        l2 = findViewById(R.id.l2);
        l1 = findViewById(R.id.l1);
        e4.setText(matId);
        parent = findViewById(R.id.lin_login);
        getSupportActionBar().setTitle("LOGIN");

        if (Build.VERSION.SDK_INT >= 26) {
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},100);

                return;
            }
            deviceId = getSystemService(TelephonyManager.class).getImei();

        }else{
            if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

                requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE},100);
                return;
            }
            deviceId = getSystemService(TelephonyManager.class).getDeviceId();
        }

        check = findViewById(R.id.checkbox);

        e3.setText(deviceId);
        e1.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                String e = e1.getText().toString().trim();
                if(!hasFocus){
                    if (TextUtils.isEmpty(e)) {
                        l1.setError("Email Required");
                        validEmail = false;
                        return;
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(e).matches()) {
                        l1.setError("Enter a valid Email");
                        validEmail = false;
                        return;
                    }
                    else {
                        l1.setError(null);
                        validEmail = true;
                    }
                }
            }
        });

        e3.setShowSoftInputOnFocus(false);
        e4.setShowSoftInputOnFocus(false);

        e2.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if (TextUtils.isEmpty(e2.getText().toString())) {
                        l2.setError("Password Required");
                        validPassword = false;
                        return;
                    }
                    else if(!Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\\s).{8,32}$").matcher(e2.getText().toString()).matches()){
                        l2.setError("Enter a Valid Password");
                        validPassword = false;
                        return;
                    }
                    else {
                        l2.setError(null);
                        validPassword = true;
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode==100){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "PERMISSION REQUIRED", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void login(View v) {
        deviceId = getSystemService(TelephonyManager.class).getDeviceId();
        String email, password;
        email = e1.getText().toString().trim();
        password = e2.getText().toString().trim();
        if(!validEmail || !validPassword){
            Toast.makeText(this, "Invalid Email or Password", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            User u = new User(this,this);
            u.login(email.trim(), password.trim(), deviceId);
        } catch (JSONException ex) {
            ex.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void signup(View v) {
        deviceId = getSystemService(TelephonyManager.class).getDeviceId();
        Intent i = new Intent(this, RegisterActivity.class);
        i.putExtra("devId",deviceId);
        i.putExtra("matId",matId);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {

        Toast.makeText(this, "Login Required", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void postTak(String s) {
        JSONObject o = null;
        try {
            o = new JSONObject(s);
            String token = o.getString("token");
            String expire = o.getString("expiry");
            SharedPreferences sp = getSharedPreferences("user_login", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sp.edit();
            editor.putString("token",token);
            editor.putString("email", e1.getText().toString().trim());
            editor.putString("password", e2.getText().toString().trim());
            editor.putLong("expiry", Long.parseLong(expire));
            editor.putString("deviceId", deviceId);
            if (check.isChecked()) {
                editor.putString("matId", matId);
            }
            editor.commit();
            Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
            finish();
        } catch (JSONException ex) {
            try {
                JSONArray errors = o.getJSONArray("Errors");
                for(int i = 0;i<errors.length();i++){
                    Toast.makeText(this, errors.getString(i), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(this, "Something Went Wrong Try again", Toast.LENGTH_SHORT).show();
            }
        }

    }
}
