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
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.futureprocessing.hereandhounds.augmentedreality.AppuntaBuilder;
import com.futureprocessing.hereandhounds.augmentedreality.AppuntaUtils;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.orientation.Orientation;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.orientation.OrientationManager;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.orientation.OrientationManager.OnOrientationChangedListener;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.point.Point;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.point.renderer.PointRenderer;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.point.renderer.impl.EyeViewRenderer;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.point.renderer.impl.SimplePointRenderer;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.ui.AppuntaView.OnPointPressedListener;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.ui.CameraView;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.ui.EyeView;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.ui.RadarView;
import com.futureprocessing.hereandhounds.location.LocationHelper;
import com.futureprocessing.hereandhounds.model.PointsModel;
import com.futureprocessing.hereandhounds.ui.DialogHelper;

import java.io.ByteArrayOutputStream;
import java.util.List;

import pl.itraff.TestApi.ItraffApi.ItraffApi;

public class EyeViewActivity extends Activity implements OnOrientationChangedListener, OnPointPressedListener, GestureDetector.OnGestureListener {

    private static final String EMPTY_STRING = "";
    private static final String LOG_TAG = "EyeViewActivity";
    private boolean pointIsNearEnough = false;

    private EyeView eyeView;
    private RadarView radarView;
    private CameraView camera;
    private FrameLayout cameraFrame;
    private TextView tvLocationOutput;

    private List<Point> eyeViewPoints;
    private List<Point> radarPoints;

    private OrientationManager compass;
    private LocationManager locationManager;
    private Location currentLocation;
    private LocationHelper locationHelper;

    private GestureDetector gestureDetector;

    private DialogHelper dialogHelper;
    private ProgressDialog waitDialog;

    private String CLIENT_API_KEY = ""; // get it from https://www.recognize.im/
    private Integer CLIENT_API_ID = 0; // get it from https://www.recognize.im/

    private static final int RESULT_BMP_DAMAGED = 128;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eyeview);
        setPoints();
        initializeViews();
        initializeProgressDialog();
        initializeGestureDetector();
        initializeCompass();
        initializeAugmentedReality();
        initializeCamera();
        initializeLocationHelper();
        initializeCurrentLocation();
        initializeLocationManager();
        setLocationListenerMock();
    }

    private void initializeViews() {
        eyeView = (EyeView) findViewById(R.id.augmentedView);
        radarView = (RadarView) findViewById(R.id.radarView);
        tvLocationOutput = (TextView) findViewById(R.id.tv_location_output);
        cameraFrame = (FrameLayout) findViewById(R.id.cameraFrame);
    }

    private void initializeGestureDetector() {
        gestureDetector = new GestureDetector(this, this);
    }

    private void initializeLocationManager() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    private void initializeCompass() {
        compass = new OrientationManager(this);
    }

    private void setPoints() {
        PointRenderer eyeViewRenderer = new EyeViewRenderer(getResources(), R.drawable.circle_selected, R.drawable.circle_unselected);
        eyeViewPoints = PointsModel.getPoints(eyeViewRenderer);
        radarPoints = PointsModel.getPoints(new SimplePointRenderer());
    }

    private void initializeAugmentedReality() {
        AppuntaBuilder appuntaBuilder = new AppuntaBuilder();
        appuntaBuilder.activity(this).compass(compass).eyeView(eyeView).radarView(radarView).eyeViewPoints(eyeViewPoints).radarPoints(radarPoints).build();
    }

    private void initializeCamera() {
        camera = new CameraView(this);
        cameraFrame.addView(camera);
    }

    private void initializeCurrentLocation() {
        currentLocation = new Location(EMPTY_STRING);
    }

    private void initializeProgressDialog() {
        dialogHelper = new DialogHelper(this, waitDialog);
    }

    private void initializeLocationHelper() {
        locationHelper = new LocationHelper();
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public EyeView getEyeView() {
        return eyeView;
    }

    public RadarView getRadarView() {
        return radarView;
    }

    private void setLocationListener() {
        locationHelper.setLocationListener(currentLocation, locationManager, this);
    }

    private void setLocationListenerMock() {
        this.pointIsNearEnough = locationHelper.setLocationListenerMock(currentLocation, eyeViewPoints, tvLocationOutput, this);
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
    public void onPointPressed(Point point) {
        Toast.makeText(this, point.getName(), Toast.LENGTH_SHORT).show();
        AppuntaUtils.unselectAllPoints(eyeViewPoints);
        point.setSelected(true);
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
        Log.d(LOG_TAG, "onBackPressed - Glass: swipe down");
    }

    @Override
    public boolean onDown(MotionEvent e) {
        Log.d(LOG_TAG, "onDown");
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
        Log.d(LOG_TAG, "onShowPress");
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.d(LOG_TAG, "onSingleTapUp - Glass: tap");
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
        Log.d(LOG_TAG, "onLongPress");
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        Log.d("Gesture Example", "onFling: velocityX:" + velocityX + " velocityY:" + velocityY);
        if (velocityX < -3500) {
            Log.d(LOG_TAG, "Fling Right - Glass: swipe right");
        } else if (velocityX > 3500) {
            Log.d(LOG_TAG, "Fling Left - Glass: swipe left");
        }
        return true;
    }

    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_MENU && pointIsNearEnough) {
            takePicture();
            return true;
        }
        return super.onKeyDown(keycode, event);
    }

    private void takePicture() {
        boolean clientIdAndApiKeyAreFilledIn = CLIENT_API_KEY != null && CLIENT_API_KEY.length() > 0 && CLIENT_API_ID != null && CLIENT_API_ID > 0;
        if (clientIdAndApiKeyAreFilledIn) {
            camera.getCamera().takePicture(null, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] bytes, Camera camera) {
                    Log.d(LOG_TAG, "takePicture()");
                    camera.startPreview();
                    if (ItraffApi.isOnline(getApplicationContext())) {
                        dialogHelper.showWaitDialog();
                        ItraffApi api = new ItraffApi(CLIENT_API_ID, CLIENT_API_KEY, LOG_TAG, true);
                        api.setMode(ItraffApi.MODE_SINGLE);
                        api.sendPhoto(bytes, itraffApiHandler, false);
                    } else {
                        Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_LONG).show();
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
            dialogHelper.dismissWaitDialog();
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
                    Toast.makeText(getApplicationContext(), "Error: " + response, Toast.LENGTH_LONG).show();
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
                    Bitmap image = (Bitmap) bundle.get("data");
                    if (image != null) {
                        if (ItraffApi.isOnline(getApplicationContext())) {
                            dialogHelper.showWaitDialog();
                            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                            ItraffApi api = new ItraffApi(CLIENT_API_ID, CLIENT_API_KEY, LOG_TAG, true);
                            if (prefs.getString("mode", "single").equals("multi")) {
                                api.setMode(ItraffApi.MODE_MULTI);
                            } else {
                                api.setMode(ItraffApi.MODE_SINGLE);
                            }
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                            byte[] pictureData = stream.toByteArray();
                            api.sendPhoto(pictureData, itraffApiHandler, prefs.getBoolean("allResults", true));
                        } else {
                            Toast.makeText(getApplicationContext(), "not connected", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            }
        } else if (resultCode == RESULT_BMP_DAMAGED) {
            Log.d(LOG_TAG, "RESULT_BMP_DAMAGED");
        }
    }

}
