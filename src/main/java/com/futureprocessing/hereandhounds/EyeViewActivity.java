package com.futureprocessing.hereandhounds;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import android.view.GestureDetector;
import android.view.MotionEvent;

import com.futureprocessing.hereandhounds.model.PointsModel;
import com.futureprocessing.hereandhounds.util.augmentedreality.appunta.android.location.LocationFactory;
import com.futureprocessing.hereandhounds.util.augmentedreality.appunta.android.orientation.Orientation;
import com.futureprocessing.hereandhounds.util.augmentedreality.appunta.android.orientation.OrientationManager;
import com.futureprocessing.hereandhounds.util.augmentedreality.appunta.android.orientation.OrientationManager.OnOrientationChangedListener;
import com.futureprocessing.hereandhounds.util.augmentedreality.appunta.android.point.Point;
import com.futureprocessing.hereandhounds.util.augmentedreality.appunta.android.point.renderer.PointRenderer;
import com.futureprocessing.hereandhounds.util.augmentedreality.appunta.android.point.renderer.impl.EyeViewRenderer;
import com.futureprocessing.hereandhounds.util.augmentedreality.appunta.android.point.renderer.impl.SimplePointRenderer;
import com.futureprocessing.hereandhounds.util.augmentedreality.appunta.android.ui.AppuntaView.OnPointPressedListener;
import com.futureprocessing.hereandhounds.util.augmentedreality.appunta.android.ui.CameraView;
import com.futureprocessing.hereandhounds.util.augmentedreality.appunta.android.ui.EyeView;
import com.futureprocessing.hereandhounds.util.augmentedreality.appunta.android.ui.RadarView;

import java.io.ByteArrayOutputStream;
import java.util.List;

import pl.itraff.TestApi.ItraffApi.ItraffApi;

public class EyeViewActivity extends Activity implements OnOrientationChangedListener, OnPointPressedListener, GestureDetector.OnGestureListener {

    private static final int MAX_DISTANCE = 4000;
    private EyeView eyeView;
    private RadarView radarView;
    private CameraView camera;
    private FrameLayout cameraFrame;
    private TextView tvLocationOutput;
    private OrientationManager compass;

    private List<Point> augmentedRealityPoints;
    private List<Point> radarPoints;

    private LocationManager locationManager;
    private Location currentLocation;

    private double DEFAULT_LATITUDE = 41.383873;
    private double DEFAULT_LONGITUDE = 2.156574;
    private double DEFAULT_ALTITUDE = 12;

    private boolean pointIsNearEnough = false;

    private GestureDetector gestureDetector;

    private String CLIENT_API_KEY = ""; // get it from https://www.recognize.im/
    private Integer CLIENT_API_ID = 0;  // get it from https://www.recognize.im/

    private static final int RESULT_BMP_DAMAGED = 128;

    ProgressDialog waitDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eyeview);

        gestureDetector = new GestureDetector(this, this);

        compass = new OrientationManager(this);
        compass.setAxisMode(OrientationManager.MODE_AR);
        compass.setOnOrientationChangeListener(this);
        compass.startSensor(this);

        eyeView = (EyeView) findViewById(R.id.augmentedView);
        radarView = (RadarView) findViewById(R.id.radarView);
        tvLocationOutput = (TextView) findViewById(R.id.tv_location_output);

        eyeView.setMaxDistance(MAX_DISTANCE);
        radarView.setMaxDistance(MAX_DISTANCE);

        eyeView.setOnPointPressedListener(this);
        radarView.setOnPointPressedListener(this);

        PointRenderer arRenderer = new EyeViewRenderer(getResources(), R.drawable.circle_selected, R.drawable.circle_unselected);

        augmentedRealityPoints = PointsModel.getPoints(arRenderer);
        radarPoints = PointsModel.getPoints(new SimplePointRenderer());

        eyeView.setPoints(augmentedRealityPoints);
        eyeView.setPosition(LocationFactory.createLocation(DEFAULT_LATITUDE, DEFAULT_LONGITUDE, DEFAULT_ALTITUDE));// BCN
        eyeView.setOnPointPressedListener(this);
        radarView.setPoints(radarPoints);
        radarView.setPosition(LocationFactory.createLocation(DEFAULT_LATITUDE, DEFAULT_LONGITUDE, DEFAULT_ALTITUDE));// BCN
        radarView.setRotableBackground(R.drawable.arrow);

        cameraFrame = (FrameLayout) findViewById(R.id.cameraFrame);
        camera = new CameraView(this);
        cameraFrame.addView(camera);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        setMockLocationListener();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onPause() {
        super.onPause();
        compass.stopSensor();
    }

    @Override
    protected void onResume() {
        super.onStart();
        compass.startSensor(this);
    }

    @Override
    public void onOrientationChanged(Orientation orientation) {
        eyeView.setOrientation(orientation);
        eyeView.setPhoneRotation(OrientationManager.getPhoneRotation(this));
        radarView.setOrientation(orientation);
    }

    @Override
    public void onPointPressed(Point p) {
        Toast.makeText(this, p.getName(), Toast.LENGTH_SHORT).show();
        unselectAllPoints();
        p.setSelected(true);
    }

    private void unselectAllPoints() {
        for (Point point : augmentedRealityPoints) {
            point.setSelected(false);
        }
    }

    private void setLocationListener() {
        LocationListener locationListener = new LocationListener() {
            @Override

            public void onLocationChanged(Location location) {
                currentLocation = location;
                eyeView.setPosition(LocationFactory.createLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), currentLocation.getAltitude()));// BCN
                radarView.setPosition(LocationFactory.createLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), currentLocation.getAltitude()));// BCN
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);
    }

    private void setMockLocationListener() {

        currentLocation = new Location("");
        currentLocation.setLatitude(DEFAULT_LATITUDE);
        currentLocation.setLongitude(DEFAULT_LONGITUDE);
        currentLocation.setAltitude(DEFAULT_ALTITUDE);

        for (Point point : augmentedRealityPoints) {

            Log.d("EyeViewActivity", "distanceTo: " + point.getLocation().distanceTo(currentLocation));

            if (point.getLocation().distanceTo(currentLocation) < 800) {
                tvLocationOutput.setVisibility(View.VISIBLE);
                tvLocationOutput.setText("Location: " + point.getName() + " found. Take a picture");
                this.pointIsNearEnough = true;
                return;
            }
        }
        tvLocationOutput.setVisibility(View.GONE);
        this.pointIsNearEnough = false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("EyeViewActivity", "onBackPressed event");
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d("EyeViewActivity", "onDown event");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d("Gesture Test", "onSingleTapUp event");
        if (pointIsNearEnough) {
            takePicture();
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d("Gesture Example", "onFling: velocityX:" + velocityX + " velocityY:" + velocityY);
//        if (velocityX < -3500) {
//            Toast.makeText(getApplicationContext(), "Fling Right", Toast.LENGTH_SHORT).show();
//        } else if (velocityX > 3500) {
//            Toast.makeText(getApplicationContext(), "Fling Left", Toast.LENGTH_SHORT).show();
//        }

        if (pointIsNearEnough) {
            takePicture();
        }


        return true;
    }

    private void takePicture() {
        if (CLIENT_API_KEY != null && CLIENT_API_KEY.length() > 0 && CLIENT_API_ID != null && CLIENT_API_ID > 0) {

            // Intent to take a photo
//            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            takePictureIntent.putExtra(MediaStore.EXTRA_FULL_SCREEN, true);
//            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, true);
//            takePictureIntent.putExtra(MediaStore.EXTRA_SHOW_ACTION_ICONS, false);
//            startActivityForResult(takePictureIntent, 1234);

//            camera.getCamera().stopPreview();

            camera.getCamera().takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    Log.d("EyeViewActivity", "takePicture()");
                    camera.startPreview();

                    if (ItraffApi.isOnline(getApplicationContext())) {
                        showWaitDialog();
//                        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                        // send photo
                        ItraffApi api = new ItraffApi(CLIENT_API_ID, CLIENT_API_KEY, "EyeViewActivity", true);
                        Log.v("KEY", CLIENT_API_ID.toString());
//                        if (prefs.getString("mode", "single").equals("multi")) {
//                            api.setMode(ItraffApi.MODE_MULTI);
//                        } else {
                        api.setMode(ItraffApi.MODE_SINGLE);
//                        }

//                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                        image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                        byte[] pictureData = stream.toByteArray();
                        byte[] pictureData = bytes;
//                        api.sendPhoto(pictureData, itraffApiHandler, prefs.getBoolean("allResults", true));
                        api.sendPhoto(pictureData, itraffApiHandler, false);
                    } else {
                        // show message: no internet connection
                        // available.

                        Toast.makeText(getApplicationContext(), "not connected", Toast.LENGTH_LONG).show();
                    }
                }
            });


        } else {
            Toast.makeText(getApplicationContext(), "Fill in Your Client Id and API Key", Toast.LENGTH_SHORT).show();
        }


    }

    @SuppressLint("HandlerLeak")
    private Handler itraffApiHandler = new Handler() {
        // callback from api
        @Override
        public void handleMessage(Message msg) {
            dismissWaitDialog();
            Bundle data = msg.getData();
            if (data != null) {
                Integer status = data.getInt(ItraffApi.STATUS, -1);
                String response = data.getString(ItraffApi.RESPONSE);
                // status ok
                if (status == 0) {
                    Toast.makeText(getApplicationContext(), response.toString(), Toast.LENGTH_LONG).show();
                    // application error (for example timeout)
                } else if (status == -1) {
                    Toast.makeText(getApplicationContext(), "API error", Toast.LENGTH_LONG).show();
                    // error from api
                } else {
                    Toast.makeText(getApplicationContext(), "error: " + response, Toast.LENGTH_LONG).show();
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (data != null) {
                Bundle bundle = data.getExtras();
                if (bundle != null) {

                    // byte[] pictureData = bundle.getByteArray("pictureData");
                    Bitmap image = (Bitmap) bundle.get("data");
                    if (image != null) {
                        Log.d("EyeViewActivity", "image != null");

                        // chceck internet connection
                        if (ItraffApi.isOnline(getApplicationContext())) {
                            showWaitDialog();
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            // send photo
                            ItraffApi api = new ItraffApi(CLIENT_API_ID, CLIENT_API_KEY, "EyeViewActivity", true);
                            Log.v("KEY", CLIENT_API_ID.toString());
                            if (prefs.getString("mode", "single").equals("multi")) {
                                api.setMode(ItraffApi.MODE_MULTI);
                            } else {
                                api.setMode(ItraffApi.MODE_SINGLE);
                            }

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compress(Bitmap.CompressFormat.JPEG, 100,
                                    stream);
                            byte[] pictureData = stream.toByteArray();
                            api.sendPhoto(pictureData, itraffApiHandler,
                                    prefs.getBoolean("allResults", true));
                        } else {
                            // show message: no internet connection
                            // available.

                            Toast.makeText(getApplicationContext(), "not connected", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        } else if (resultCode == RESULT_BMP_DAMAGED) {
            Log.d("EyeViewActivity", "RESULT_BMP_DAMAGEDl");
        }
    }

    private void showWaitDialog() {
        if (waitDialog != null) {
            if (!waitDialog.isShowing()) {
                waitDialog.show();
            }
        } else {
            waitDialog = new ProgressDialog(this);
            waitDialog.setMessage("loading...");
            waitDialog.show();
        }
    }

    private void dismissWaitDialog() {
        try {
            if (waitDialog != null && waitDialog.isShowing()) {
                waitDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_MENU && pointIsNearEnough) {
            takePicture();
            return true;
        }
        return super.onKeyDown(keycode, event);
    }

}
