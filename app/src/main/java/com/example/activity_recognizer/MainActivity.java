package com.example.activity_recognizer;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import weka.classifiers.trees.REPTree;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instances;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;

    // -----Initializing the sensors---------------------------------------
    private Sensor magnetometer;
    private Sensor accelerometer;
    private Sensor gyroscopeSensor;
    private Sensor linearAccelerometer;

    // ------filtering sample rate ---------------------------------------
    private static final int SAMPLE_RATE = 50;

    // for RAW data collection
    Map<String, ArrayList<Double>> acc = new HashMap<>();
    Map<String, ArrayList<Double>> gyr = new HashMap<>();
    Map<String, ArrayList<Double>> mag = new HashMap<>();
    Map<String, ArrayList<Double>> linAcc = new HashMap<>();
    // -----for FILTERED data --------------------------------------------
    double[] magnetic = new double[3];
    double[] acceleration = new double[3];
    double[] gyroscopeData = new double[3];
    double[] lAcceleration = new double[3];

    // ------timelapse of Activities ----------------------------------------
    ArrayList<ArrayList<String>> timelapse = new ArrayList<>();
    ActivitiesAdapter adapter = new ActivitiesAdapter(timelapse);


    // -------Sensor manager and listener------------------------------------
    private SensorManager sensorManager;
    Instances instances;
    REPTree model;
    //J48 model;
    RecyclerView rvActivity;
    private ImageView currentActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        model = new REPTree();
        loadModel();
        System.out.println("--------MODEL LOADED--------");
        //RecyclerView
        rvActivity = findViewById(R.id.rvActivity);
        rvActivity.setAdapter(adapter);
        rvActivity.setLayoutManager(new LinearLayoutManager(this));


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        magnetometer        = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer       = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroscopeSensor     = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        linearAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(MainActivity.this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(MainActivity.this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(MainActivity.this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(MainActivity.this, linearAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        // make space for sensor data collection
        acc.put("x" , new ArrayList<Double>());
        acc.put("y" , new ArrayList<Double>());
        acc.put("z" , new ArrayList<Double>());
        mag.put("x" , new ArrayList<Double>());
        mag.put("y" , new ArrayList<Double>());
        mag.put("z" , new ArrayList<Double>());
        gyr.put("x" , new ArrayList<Double>());
        gyr.put("y" , new ArrayList<Double>());
        gyr.put("z" , new ArrayList<Double>());
        linAcc.put("x" , new ArrayList<Double>());
        linAcc.put("y" , new ArrayList<Double>());
        linAcc.put("z" , new ArrayList<Double>());

        counter.put("walking", 0);
        counter.put("standing", 0);
        counter.put("jogging", 0);
        counter.put("sitting", 0);
        counter.put("biking", 0);
        counter.put("upstairs", 0);
        counter.put("downstairs", 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onSensorChanged(SensorEvent event) {
        // the values to be added to the relevant sensor collection
        double x, y, z;
        x = event.values[0];
        y = event.values[1];
        z = event.values[2];

        Map<String, ArrayList<Double>> type  = new HashMap<>();
        int intType = event.sensor.getType();

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            type = mag;
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            type = acc;
        } else if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            type = gyr;
        } else if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {
            type = linAcc;
        }
        filter(type, intType, x, y, z);

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void updateMap (Map<String, ArrayList<Double>> sensor ,double x, double y, double z){
        sensor.get("x").add(x);
        sensor.get("y").add(y);
        sensor.get("z").add(z);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void computeAverage(Map < String, ArrayList < Double >> sensor, int type){
        int size = sensor.get("x").size();
        double avgX = 0;
        double avgY = 0;
        double avgZ = 0;
        if (size >= SAMPLE_RATE) {

            for (int i = 0; i < size; i++) {
                avgX += sensor.get("x").get(i);
                avgY += sensor.get("y").get(i);
                avgZ += sensor.get("z").get(i);
            }
            clearMap(sensor);
            //System.out.println("Type: " + type +" X: "+ avgX + " Y: " + avgY + " Z: "+ avgZ);

            if (type == Sensor.TYPE_MAGNETIC_FIELD) {
                magnetic[0] = avgX / size;
                magnetic[1] = avgY / size;
                magnetic[2] = avgZ / size;
                //System.out.println("----Magnetic Values: "+ "X: "+ magnetic[0] + " Y: " + magnetic[1] + " Z: "+ magnetic[2]+ "-----");
            } else if (type == Sensor.TYPE_ACCELEROMETER) {
                acceleration[0] = avgX / size;
                acceleration[1] = avgY / size;
                acceleration[2] = avgZ / size;
                //System.out.println("----Accelerometer Values: "+ "X: "+ acceleration[0] + " Y: " + acceleration[1] + " Z: "+ acceleration[2]+ "-----");
            } else if (type == Sensor.TYPE_GYROSCOPE) {
                gyroscopeData[0] = avgX / size;
                gyroscopeData[1] = avgY / size;
                gyroscopeData[2] = avgZ / size;
                //System.out.println("----Gyroscope Values: "+ "X: "+ gyroscopeData[0] + " Y: " + gyroscopeData[1] + " Z: "+ gyroscopeData[2]+ "-----");
            } else if (type == Sensor.TYPE_LINEAR_ACCELERATION) {
                lAcceleration[0] = avgX / size;
                lAcceleration[1] = avgY / size;
                lAcceleration[2] = avgZ / size;
                //System.out.println("----Linear Acceleration Values: "+ "X: "+ lAcceleration[0] + " Y: " + lAcceleration[1] + " Z: "+ lAcceleration[2]+ "-----");
            }
            if(magnetic != null && gyroscopeData != null && lAcceleration != null && acceleration != null) {
                makeInstance();
                classify();
            }
        }

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void filter(Map < String, ArrayList < Double >> sensor, int type, double x, double y, double z){
        updateMap(sensor, x, y, z);
        computeAverage(sensor, type);
    }

    private void clearMap (Map < String, ArrayList < Double >> sensor){
        sensor.get("x").remove(0);
        sensor.get("y").remove(0);
        sensor.get("z").remove(0);
    }


    public void loadModel() {
        try {
            model = (REPTree) weka.core.SerializationHelper.read(getAssets().open("model2.model"));
        }
        catch (Exception e) {
            // Given the cast, a ClassNotFoundException must be caught along with the IOException
            System.out.println("Problem found when reading: " + e);
        }
    }

    public void makeInstance() {
        ArrayList<String> atts = new ArrayList<>(7);
        atts.add("walking");
        atts.add("standing");
        atts.add("jogging");
        atts.add("sitting");
        atts.add("biking");
        atts.add("upstairs");
        atts.add("downstairs");

        // Create the attributes, class and text
        Attribute Right_pocket_time_stamp = new Attribute("Right_pocket_time_stamp");
        Attribute Right_pocket_Ax = new Attribute("Right_pocket_Ax");
        Attribute Right_pocket_Ay = new Attribute("Right_pocket_Ay");
        Attribute Right_pocket_Az = new Attribute("Right_pocket_Az");
        Attribute Right_pocket_Lx = new Attribute("Right_pocket_Lx");
        Attribute Right_pocket_Ly = new Attribute("Right_pocket_Ly");
        Attribute Right_pocket_Lz = new Attribute("Right_pocket_Lz");
        Attribute Right_pocket_Gx = new Attribute("Right_pocket_Gx");
        Attribute Right_pocket_Gy = new Attribute("Right_pocket_Gy");
        Attribute Right_pocket_Gz = new Attribute("Right_pocket_Gz");
        Attribute Right_pocket_Mx = new Attribute("Right_pocket_Mx");
        Attribute Right_pocket_My = new Attribute("Right_pocket_My");
        Attribute Right_pocket_Mz = new Attribute("Right_pocket_Mz");
        Attribute classes = new Attribute("class", atts);
        ArrayList<Attribute> fvWekaAttributes = new ArrayList<Attribute>(13);

        fvWekaAttributes.add(Right_pocket_time_stamp);
        fvWekaAttributes.add(Right_pocket_Ax);
        fvWekaAttributes.add(Right_pocket_Ay);
        fvWekaAttributes.add(Right_pocket_Az);
        fvWekaAttributes.add(Right_pocket_Lx);
        fvWekaAttributes.add(Right_pocket_Ly);
        fvWekaAttributes.add(Right_pocket_Lz);
        fvWekaAttributes.add(Right_pocket_Gx);
        fvWekaAttributes.add(Right_pocket_Gy);
        fvWekaAttributes.add(Right_pocket_Gz);
        fvWekaAttributes.add(Right_pocket_Mx);
        fvWekaAttributes.add(Right_pocket_My);
        fvWekaAttributes.add(Right_pocket_Mz);
        fvWekaAttributes.add(classes);

        instances = new Instances("SensorData", fvWekaAttributes, 13);
        instances.setClassIndex(13);
        DenseInstance instance = new DenseInstance(13);
        instance.setValue(Right_pocket_time_stamp,  System.currentTimeMillis());
        instance.setValue(Right_pocket_Ax, acceleration[0]);
        instance.setValue(Right_pocket_Ay, acceleration[1]);
        instance.setValue(Right_pocket_Az, acceleration[2]);
        instance.setValue(Right_pocket_Lx, lAcceleration[0]);
        instance.setValue(Right_pocket_Ly, lAcceleration[1]);
        instance.setValue(Right_pocket_Lz, lAcceleration[2]);
        instance.setValue(Right_pocket_Gx, gyroscopeData[0]);
        instance.setValue(Right_pocket_Gy, gyroscopeData[1]);
        instance.setValue(Right_pocket_Gz, gyroscopeData[2]);
        instance.setValue(Right_pocket_Mx, magnetic[0]);
        instance.setValue(Right_pocket_My, magnetic[1]);
        instance.setValue(Right_pocket_Mz, magnetic[2]);

        instances.add(instance);
    }

    String current = "";
    long lastTime = System.currentTimeMillis();
    Map<String, Integer> counter = new HashMap<>();

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void classify() {
        try {
            double pred = model.classifyInstance(instances.instance(0));
            current = instances.classAttribute().value((int) pred);

            displayCurrentActivity(instances.classAttribute().value((int) pred));
            counter.replace(current, counter.get(current) + 1);

            if (System.currentTimeMillis() - lastTime >= 5000){
                String key = Collections.max(counter.entrySet(), Map.Entry.comparingByValue()).getKey();
                evaluteList(key);
                counter.replace("walking", 0);
                counter.replace("standing", 0);
                counter.replace("jogging", 0);
                counter.replace("sitting", 0);
                counter.replace("biking", 0);
                counter.replace("upstairs", 0);
                counter.replace("downstairs", 0);
                lastTime = System.currentTimeMillis();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("Problem found when classifying the text" + e);
        }
    }

    String lastActivity = "";
    long timer = 5;

    public void evaluteList(String currentActivity){
        if (!lastActivity.equalsIgnoreCase("")) {
            if (currentActivity.equalsIgnoreCase(lastActivity)) {
                timer = timer + 5;

            } else {
                ArrayList<String> item = new ArrayList<>();
                item.add(lastActivity);
                item.add(timer + " sec.");
                timelapse.add(item);
                adapter.notifyItemInserted(timelapse.size() -1);
                timer = 5;
            }
        }
        System.out.println("Activity: "+currentActivity+ " Timer: "+timer+"sec");
        lastActivity = currentActivity;
    }

    /**
     * Check if it has the permissions for location and the state of wifi,
     *   then it returns the MAC address of this device
     * @param context of this application
     * @return the MAC address of this device
     */
    public String getMacAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_DENIED) {

                Log.d("permission", "permission denied to get location - requesting it");
                String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_WIFI_STATE};
                requestPermissions(permissions, PERMISSION_REQUEST_FINE_LOCATION );
                wifiManager.setWifiEnabled(true);

                @SuppressLint("HardwareIds")
                String macAddress = wifiManager.getConnectionInfo().getMacAddress();
                if (macAddress == null) {
                    macAddress = "Device don't have mac address or wi-fi is disabled";
                }
                return macAddress;
            }
        }
        return getMacAddress(context);
    }


    /**
     * Display a frame in the screen which selects the current activity
     * @param activity recognized by the model
     */
    public void displayCurrentActivity(String activity){
        ImageView frame = null;
        if (activity.equalsIgnoreCase("walking")){
            frame = findViewById(R.id.walkingFrame);
        } else if (activity.equalsIgnoreCase("jogging")){
            frame = findViewById(R.id.joggingFrame);
        } else if (activity.equalsIgnoreCase("standing")){
            frame = findViewById(R.id.standingFrame);
        } else if (activity.equalsIgnoreCase("sitting")){
            frame = findViewById(R.id.sittingFrame);
        } else if (activity.equalsIgnoreCase("upstairs")){
            frame = findViewById(R.id.wUpFrame);
        } else if (activity.equalsIgnoreCase("downstairs")){
            frame = findViewById(R.id.wDownFrame);
        } else if (activity.equalsIgnoreCase("biking")){
            frame = findViewById(R.id.bikeFrame);
        } else {
            Log.i("# Error", "Wrong activity provided");
        }
        if (currentActivity == null){
            currentActivity = frame;
            frame.setVisibility(View.VISIBLE);
        } else if (frame != null){
            currentActivity.setVisibility(View.GONE);
            currentActivity = frame;
            frame.setVisibility(View.VISIBLE);
        } else {
            Log.i("# Error", "Wrong activity provided");
        }
    }
}