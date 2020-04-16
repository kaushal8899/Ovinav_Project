package com.example.bletest;


import android.app.Activity;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    Activity actvity;
    Networkback n;
    String[] status = {new String()};
    String fname,lname,email,password,mobile;
    public User(Activity activity,Networkback n){
        this.actvity = activity;
        this.n = n;
    }
    public User(Activity activity,Networkback n,String fname, String lname, String email, String password, String mobile) {
        this.actvity = activity;
        this.n = n;
        this.fname = fname;
        this.lname = lname;
        this.email = email;
        this.password = password;
        this.mobile = mobile;
    }

    public void login(String email, String password, String d_id) throws JSONException {
        JSONObject j = new JSONObject();
        j.put("userName",email);
        j.put("password",password);
        j.put("deviceId",d_id);
        //  j.put("matId",m_id);

        new Networkutil(n).execute("http://analytics.ovinav.com/auth/token","",j.toString());
    }


    public void register(String d_id,String m_id) throws JSONException {
        final String[] status = {" "};
        JSONObject j = new JSONObject();
        j.put("firstName",fname);
        j.put("lastName",lname);
        j.put("userName",email);
        j.put("password",password);
        j.put("mobileNum",mobile);
        j.put("deviceId",d_id);
        j.put("matId",m_id);
        new Networkutil(n).execute("http://analytics.ovinav.com/auth/register","Basic SN2yQEcQqDERKhYnkTGgUv3NDGgHjRppn2t8QN2zn6QdGuShjFjXPtMbnkPSvpL4GUvRFWBZPYaWh7c2Pac32ghz8mijSKyDWmmMvLMPCHYtggBKhtKnjFc4GyD2gFCa",j.toString());
    }

}
