package com.example.bletest;
import android.os.AsyncTask;
import android.provider.SyncStateContract;
import android.util.Log;
import android.util.Pair;
import android.webkit.HttpAuthHandler;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class Networkutil extends AsyncTask<String,String,String>{
    Networkback n;
    public Networkutil(Networkback n){
        this.n = n;
    }
    @Override
    protected String doInBackground(String... args) {
          /* URL url = new URL(args[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization",args[1]);
            connection.setDoInput(true);
            connection.setDoOutput(true); */
            HttpClient client = new DefaultHttpClient();
            HttpPost post = new HttpPost(args[0]);
             HttpResponse res =null;
             String data = "";
        try {
            post.setEntity(new StringEntity(args[2]));
            post.setHeader("Content-Type", "application/json");
            post.setHeader("Accept", "application/json");
            post.setHeader("Authorization",args[1]);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            Log.d("NO ENCODING",""+e);
        }

        try {
             res  = client.execute(post);
             BufferedReader reader = new BufferedReader(new InputStreamReader(res.getEntity().getContent()));
             String line = reader.readLine();
             while (line != null){
                 data+=line;
                 line = reader.readLine();
             }
             Log.d("RES",data);
             Log.d("HEADER", String.valueOf(post.getHeaders("Authorization")[0]));
            Log.d("CODE", String.valueOf(res.getStatusLine().getStatusCode()));

        } catch (IOException e) {
            e.printStackTrace();
            Log.d("ERROR",""+e);
        }
        return data;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        n.postTak(s);
    }
}
