package com.example.bletest;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity implements Networkback{
    TextInputEditText e1,e2,e3,e4,e5,e6,e7;
    TextInputLayout l1,l2,l3,l4,l5;
    String deviceId,matId;

    private boolean validEmail = true;
    private boolean validPassword = true;
    private boolean validPhone = true;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        deviceId = getIntent().getStringExtra("devId");
        matId = getIntent().getStringExtra("matId");
        getSupportActionBar().setTitle("REGISTRATION");
        getSupportActionBar().setDefaultDisplayHomeAsUpEnabled(true);
        e1 = findViewById(R.id.fname);
        e2 = findViewById(R.id.lname);
        e3 = findViewById(R.id.email);
        e4 = findViewById(R.id.mobile);
        e5 = findViewById(R.id.password);
        e6 = findViewById(R.id.d_uid);
        e7 = findViewById(R.id.m_uid);

        l1 = findViewById(R.id.l1);
        l2 = findViewById(R.id.l2);
        l3 = findViewById(R.id.l3);
        l4 = findViewById(R.id.l4);
        l5 = findViewById(R.id.l5);

        e6.setText(deviceId);

        e7.setText(matId);
        e3.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if (TextUtils.isEmpty(e3.getText().toString())) {
                        l3.setError("Email Required");
                        validEmail = false;
                        return;
                    }
                    else if(!Patterns.EMAIL_ADDRESS.matcher(e3.getText().toString()).matches()){
                        l3.setError("Enter a valid Email");
                        validEmail = false;
                        return;
                    }
                    else {
                        l3.setError(null);
                        validEmail = true;
                    }
                }
            }
        });
        e4.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    String t = e4.getText().toString();
                    if (TextUtils.isEmpty(e4.getText().toString())) {
                        l4.setError("Phone Number Required");
                        validPhone = false;
                        return;
                    }
                    else if(!Pattern.matches("[a-zA-Z]+",t) && t.length()!=10){
                        l4.setError("Enter a valid Phone Number");
                        validPhone = false;
                        return;
                    }
                    else {
                        l4.setError(null);
                        validPhone = true;
                    }
                }
            }
        });
        e5.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus){
                    if (TextUtils.isEmpty(e5.getText().toString())) {
                        l5.setError("Password Required");
                        validPassword = false;
                        return;
                    }
                    else if(!Pattern.compile("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z])(?!.*\\s).{8,32}$").matcher(e5.getText().toString()).matches()){
                        l5.setError("Password Must Contain one Upper,Lower,digit and Special Symbol.Length should between 8 and 32.");
                        validPassword = false;
                        return;
                    }
                    else {
                        l5.setError(null);
                        validPassword = true;
                    }
                }
            }
        });

    }
    public void register(View v){
        String fname,lname,email,mobile,pass;
        fname = e1.getText().toString();
        lname = e2.getText().toString();
        email = e3.getText().toString();
        mobile = e4.getText().toString();
        pass = e5.getText().toString();
        if(TextUtils.isEmpty(fname) && fname.length()<2){
            l1.setError("First Name Required");
            return;
        }
        else {
            l1.setError(null);
        }
        if(TextUtils.isEmpty(lname) && lname.length()<2){
            l2.setError("Last Name Required");
            return;
        }
        else {
            l2.setError(null);
        }

        if(!validEmail || !validPassword || !validPhone){
            Toast.makeText(this, "Invalid Email or Password or Phone", Toast.LENGTH_SHORT).show();
            return;
        }
            User u = new User(this,this,fname,lname,email,pass,mobile);
        try {
            u.register(deviceId,matId);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void postTak(String s) {
        JSONObject o = null;
        try {
            o = new JSONObject(s);
            o.getString("Id");
            Toast.makeText(this, "Register Successful.", Toast.LENGTH_SHORT).show();
            finish();
        } catch (JSONException ex) {
            try {
                JSONArray errors = o.getJSONArray("Errors");
                for(int i = 0;i<errors.length();i++){
                    Toast.makeText(this, errors.getString(i), Toast.LENGTH_SHORT).show();
                }
            } catch (JSONException e) {
                Toast.makeText(this, "Try Again", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
