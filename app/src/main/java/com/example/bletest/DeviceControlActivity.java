package com.example.bletest;



import android.app.Dialog;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.Viewport;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.PointsGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.UUID;

/**
 * For a given BLE device, this Activity provides the user interface to connect, display data,
 * and display GATT services and characteristics supported by the device.  The Activity
 * communicates with {@code BluetoothLeService}, which in turn interacts with the
 * Bluetooth LE API.
 */
public class DeviceControlActivity extends AppCompatActivity {
    private final static String TAG = DeviceControlActivity.class.getSimpleName();
    Dialog graphdialog;
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";
    private int[] RGBFrame = {0, 0, 0};
    private TextView isSerial;
    String uid= "";
    private TextView mConnectionState;
    private TextView mDataField;
    private TextView textView;
    SQLiteDatabase db;
    //private SeekBar mRed,mGreen,mBlue;
    private String mDeviceName;
    ContentValues cv;
    private String mDeviceAddress;
    //private ExpandableListView mGattServicesList;
    private BluetoothLeService mBluetoothLeService;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;
    boolean isUID = false;
    private Button btn_Instant;
    private Button btn_RealTime;
    private Button btn_sync;
    boolean start = false;
    SharedPreferences sp;
    SharedPreferences.Editor editor;
    GraphView graph;
    String datetime = "";
    ArrayList<String> dataList = new ArrayList<>();
    public final static UUID HM_RX_TX =
            UUID.fromString(GattAttributes.HM_RX_TX);

    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";

    private String filename = "Pressure.txt";
    private String filepath = "PressureResult";
    File myExternalFile;
    String myData = "";
    PointsGraphSeries<DataPoint> series;

    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();

            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(R.string.connected);
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(R.string.disconnected);
                invalidateOptionsMenu();
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(mBluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void clearUI() {
        mDataField.setText(R.string.no_data);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
      //  SendValueToDevice("3");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device2);

        db = openOrCreateDatabase("BLE", MODE_PRIVATE, null);
        String q = "create table if not exists pressure(dataDateTime varchar(255),matrix varchar(255))";
        db.execSQL(q);
        final Intent intent = getIntent();
        mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // Sets up UI references.
        ((TextView) findViewById(R.id.device_address)).setText(mDeviceAddress);
        mConnectionState = (TextView) findViewById(R.id.connection_state);
        // is serial present?
        isSerial = (TextView) findViewById(R.id.isSerial);

        mDataField = (TextView) findViewById(R.id.data_value);

        // textView = (TextView) findViewById(R.id.textView);

/*        mRed = (SeekBar) findViewById(R.id.seekRed);
        mGreen = (SeekBar) findViewById(R.id.seekGreen);
        mBlue = (SeekBar) findViewById(R.id.seekBlue);*/

        btn_Instant = (Button) findViewById(R.id.Instant_btn);
        btn_RealTime = findViewById(R.id.real_btn);
        btn_sync = findViewById(R.id.sync_btn);
        sp = getSharedPreferences("MODE", MODE_PRIVATE);
        editor = sp.edit();
        boolean isreal = sp.getBoolean("isReal", false);
        if (isreal) {
            btn_RealTime.setText("Stop Real Time Mode");
        }

/*        readSeek(mRed,0);
        readSeek(mGreen,1);
        readSeek(mBlue,2);*/

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            Toast.makeText(getApplicationContext(), "External Storage Not Available. Result File Cannot be Created.", Toast.LENGTH_LONG).show();
        } else {
            myExternalFile = new File(getExternalFilesDir(filepath), filename);
        }

        // getActionBar().setTitle(mDeviceName);
        // getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result = " + result);
            Calendar c ;
            c = GregorianCalendar.getInstance();

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.logout:
                Intent i = new Intent(this,LoginActivity.class);
                i.putExtra("matId",uid);
                mBluetoothLeService.disconnect();
                startActivity(i);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateConnectionState(final int resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mConnectionState.setText(resourceId);
            }
        });
    }

    Queue<PointsGraphSeries> stack = new LinkedList<>();

    private void displayData(String data) {
       // Toast.makeText(DeviceControlActivity.this, data, Toast.LENGTH_SHORT).show();
        if(!isUID){
            uid = data;
            isUID = true;
            Intent i = new Intent(this,LoginActivity.class);
            i.putExtra("matId",uid);
            mBluetoothLeService.disconnect();
            startActivity(i);
        }

        //mDataField.setText(data);

        if (data != null && data.contains("DataStart")) {
            start = true;
            dataList.clear();
            datetime = String.valueOf(Calendar.getInstance().getTimeInMillis());
            return;            //Toast.makeText(DeviceControlActivity.this, datetime, Toast.LENGTH_SHORT).show();
            //Calendar c = Calendar.getInstance();
            //c.setTimeInMillis(Long.parseLong(datetime));
            //Toast.makeText(mBluetoothLeService, ""+c.getTime().toString(), Toast.LENGTH_SHORT).show();

        }
        else if(data != null && data.contains("DataEnd")){
            if(dataList.size()>0 && start){
                cv = new ContentValues();
                cv.put("dataDateTime", datetime);
                cv.put("matrix", covertList(dataList));
                db.insert("pressure", null, cv);
            }
            start =false;
            return;
        }

        if(start){
            // WriteToFile(data);
            mDataField.setText(data);
            // textView.append(data);
            if(!data.equals("") || data != null || data.length()==0){
                String d[] = data.split(":");
                dataList.add(data);
                series = new PointsGraphSeries<>();
                series.setShape(PointsGraphSeries.Shape.POINT);
                int col;

                int r, b, g, a;
                a = 128;
                r = g = 0;
                b = 255;
                try {
                    float pressure = Float.parseFloat(d[2]);
                    if (pressure > 215) {
                        r = 255;
                        b = 0;
                        a = 200;
                    } else if (pressure > 180) {
                        g = 255;
                        b = 0;
                        a = 170;
                    } else if (pressure > 158) {
                        r = 255;
                        g = 255;
                        b = 0;
                        a = 150;
                    }


                    col = Color.argb(a, r, g, b);
                    series.setSize(pressure / 4);
                    series.setColor(col);
                    //Toast.makeText(mBluetoothLeService, ""+d[0]+":"+d[1], Toast.LENGTH_SHORT).show();
                    series.appendData(new DataPoint(new Double(d[0]), new Double(d[1])), false, 100);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            //graph.removeAllSeries();
                            graph.removeSeries(stack.remove());
                        }
                    }, 1500);
                    stack.add(series);
                    graph.addSeries(series);
                } catch (Exception ex) {
                    Log.d("EXCEPTION",ex.toString());
                }
            }
        }
    }

    private String covertList(ArrayList<String> dataList) {
        StringBuilder temp = new StringBuilder();
        for(String s:dataList){
            temp.append(s);
            temp.append(",");
        }
        temp.deleteCharAt(temp.length()-1);
        return String.valueOf(temp);
    }

    // Demonstrates how to iterate through the supported GATT Services/Characteristics.
    // In this sample, we populate the data structure that is bound to the ExpandableListView
    // on the UI.
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();


        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, GattAttributes.lookup(uuid, unknownServiceString));

            // If the service exists for HM 10 Serial, say so.
            if (GattAttributes.lookup(uuid, unknownServiceString) == "HM 10 Serial") {
                isSerial.setText("Yes, serial :-)");
            } else {
                isSerial.setText("No, serial :-(");
            }
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            // get characteristic when UUID matches RX/TX UUID
            characteristicTX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
            characteristicRX = gattService.getCharacteristic(BluetoothLeService.UUID_HM_RX_TX);
            if (characteristicRX != null && characteristicTX != null) {
                Toast.makeText(DeviceControlActivity.this, "BLE connected...!!!", Toast.LENGTH_SHORT).show();
                if(!isUID)
                    SendValueToDevice("U");
                btn_Instant.setClickable(true);
                btn_Instant.setAlpha((float)1.0);
                btn_RealTime.setAlpha((float)1.0);
                btn_RealTime.setClickable(true);
                btn_sync.setAlpha((float)1.0);
                btn_sync.setClickable(true);

            }

        }

    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

/*    private void readSeek(SeekBar seekBar, final int pos) {
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                RGBFrame[pos] = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                makeChange();
            }
        });
    }*/

    public void Instant_btn(View v) {
        //Toast.makeText(DeviceControlActivity.this, "Instant Mode", Toast.LENGTH_SHORT).show();
        SendValueToDevice("2");
        graphdialog = new Dialog(this);
        graphdialog.setContentView(R.layout.graph);
        graph = graphdialog.findViewById(R.id.graph);
        graphdialog.setCanceledOnTouchOutside(false);
        graphdialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                SendValueToDevice("5");
            }
        });
        graphdialog.show();
        Viewport port = graph.getViewport();
        port.setXAxisBoundsManual(true);
        port.setYAxisBoundsManual(true);
        port.setMaxX(30);
        port.setMaxY(30);
        port.setMinX(0);
        port.setMinY(0);
        // port.setBackgroundColor(Color.rgb(255,255,0));
        port.setScalable(false);
        port.setScrollable(false);

    }


    private void SendValueToDevice(String Msg) {
        String str = Msg;
        Log.d(TAG, "Sending result=" + str);
        final byte[] tx = str.getBytes();
        if (mConnected) {
            characteristicTX.setValue(tx);
            characteristicTX.setWriteType(2);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
            Toast.makeText(this, "' " + str + " ' Send.", Toast.LENGTH_SHORT).show();
        }
    }

    public void read(View view) {
        String btn_text = btn_RealTime.getText().toString();
        if (btn_text.startsWith("Start")) {
            btn_Instant.setClickable(false);
            SendValueToDevice("1");
            editor.putBoolean("isReal", true);
            editor.apply();
            btn_RealTime.setText("Stop Real Time Mode");
            Toast.makeText(DeviceControlActivity.this, "Real Time Mode Started.", Toast.LENGTH_LONG).show();
        } else {
            btn_Instant.setClickable(true);
            SendValueToDevice("3");
            editor.putBoolean("isReal", false);
            editor.apply();
            btn_RealTime.setText("Start Real Time Mode");
            Toast.makeText(DeviceControlActivity.this, "Real Time Mode Stopped.", Toast.LENGTH_LONG).show();
        }
    }

    public void WriteToFile(String _Data) {
        try {
            FileOutputStream fos = new FileOutputStream(myExternalFile, true);
            OutputStreamWriter myOutWriter = new OutputStreamWriter(fos);
            myOutWriter.append(_Data);
            myOutWriter.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }
    }

    public void ReadFromFile() {
        try {
            FileInputStream fis = new FileInputStream(myExternalFile);
            DataInputStream in = new DataInputStream(fis);
            BufferedReader br =
                    new BufferedReader(new InputStreamReader(in));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                myData = myData + strLine;
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG, e.toString());
        }
        textView.setText(myData);
    }

    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    public void sync(View view) {

        JSONArray jsondata = new JSONArray();
        try {
            Cursor c = null;

            c = db.rawQuery("select * from pressure",null);
            c.moveToFirst();
            for (int i=0;i<c.getCount();i++){
                JSONObject o = new JSONObject();
                o.put(c.getColumnName(0),c.getString(0));
                JSONArray array = new JSONArray();
                array.put(c.getString(1));
                o.put(c.getColumnName(1),array);
                jsondata.put(o);
                c.moveToNext();
            }
        }
        catch (Exception e1) {
            e1.printStackTrace();
            Log.d("JSONSYNC",e1.toString());
        }

        JSONObject o = new JSONObject();
        try {
            o.put("syncStartDateTime",""+Calendar.getInstance().getTimeInMillis());
            o.put("data",jsondata);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Log.d("SYNC",o.toString());
        SharedPreferences sp = getSharedPreferences("user_login", Context.MODE_PRIVATE);
        long exp = Long.parseLong(sp.getString("expiry","0"));
        long current = Calendar.getInstance().getTimeInMillis();
        if(exp>current){
            new Networkutil(new Networkback() {
                @Override
                public void postTak(String s) {
                    Toast.makeText(DeviceControlActivity.this, "Data has been sent.", Toast.LENGTH_SHORT).show();
                    db.execSQL("delete from pressure");
                }
            }).execute("http://analytics.ovinav.com/data/sync","Bearer "+sp.getString("token",""),o.toString());
        }

    }

    // on change of bars write char
/*    private void makeChange() {
        String str = RGBFrame[0] + "," + RGBFrame[1] + "," + RGBFrame[2] + "\n";
        Log.d(TAG, "Sending result=" + str);
        final byte[] tx = str.getBytes();
        if (mConnected) {
            characteristicTX.setValue(tx);
            mBluetoothLeService.writeCharacteristic(characteristicTX);
            mBluetoothLeService.setCharacteristicNotification(characteristicRX, true);
        }
    }*/

}