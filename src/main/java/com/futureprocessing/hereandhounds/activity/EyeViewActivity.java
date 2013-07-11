package com.futureprocessing.hereandhounds.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.futureprocessing.hereandhounds.R;
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
import com.futureprocessing.hereandhounds.imagerecognition.recognizeim.RecognizeImHelper;
import com.futureprocessing.hereandhounds.location.LocationHelper;
import com.futureprocessing.hereandhounds.model.PointsModel;
import com.futureprocessing.hereandhounds.ui.DialogHelper;
import com.futureprocessing.hereandhounds.ui.viewholders.EyeViewActivityViewHolder;

import java.util.List;

public class EyeViewActivity extends Activity implements OnOrientationChangedListener, OnPointPressedListener, GestureDetector.OnGestureListener {

    public static final String LOG_TAG = "EyeViewActivity";
    private static final String EMPTY_STRING = "";
    private boolean pointIsNearEnough = false;

    private EyeViewActivityViewHolder viewHolder;

    private List<Point> eyeViewPoints;
    private List<Point> radarPoints;

    private OrientationManager compass;
    private LocationManager locationManager;
    private Location currentLocation;
    private LocationHelper locationHelper;

    private GestureDetector gestureDetector;

    private RecognizeImHelper recognizeImHelper;
    private DialogHelper dialogHelper;
    private ProgressDialog waitDialog;

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
        initializeRecognizeImHelper();
        initializeCurrentLocation();
        initializeLocationManager();
        setLocationListenerMock();
    }

    private void initializeViews() {
        viewHolder = new EyeViewActivityViewHolder();
        viewHolder.eyeView = (EyeView) findViewById(R.id.augmentedView);
        viewHolder.radarView = (RadarView) findViewById(R.id.radarView);
        viewHolder.tvLocationOutput = (TextView) findViewById(R.id.tv_location_output);
        viewHolder.cameraFrame = (FrameLayout) findViewById(R.id.cameraFrame);
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
        appuntaBuilder.activity(this).compass(compass).eyeView(viewHolder.eyeView).radarView(viewHolder.radarView).eyeViewPoints(eyeViewPoints).radarPoints(radarPoints).build();
    }

    private void initializeCamera() {
        viewHolder.cameraView = new CameraView(this);
        viewHolder.cameraFrame.addView(viewHolder.cameraView);
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

    private void initializeRecognizeImHelper() {
        recognizeImHelper = new RecognizeImHelper(this);
    }

    public void setCurrentLocation(Location currentLocation) {
        this.currentLocation = currentLocation;
    }

    public EyeView getEyeView() {
        return viewHolder.eyeView;
    }

    public RadarView getRadarView() {
        return viewHolder.radarView;
    }

    public CameraView getCameraView() {
        return viewHolder.cameraView;
    }

    public DialogHelper getDialogHelper() {
        return dialogHelper;
    }

    private void setLocationListener() {
        locationHelper.setLocationListener(currentLocation, locationManager, this);
    }

    private void setLocationListenerMock() {
        this.pointIsNearEnough = locationHelper.setLocationListenerMock(currentLocation, eyeViewPoints, viewHolder.tvLocationOutput, this);
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
        viewHolder.eyeView.setOrientation(orientation);
        viewHolder.eyeView.setPhoneRotation(OrientationManager.getPhoneRotation(this));
        viewHolder.radarView.setOrientation(orientation);
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
            recognizeImHelper.takePicture();
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

    @Override
    public boolean onKeyDown(int keycode, KeyEvent event) {
        if (keycode == KeyEvent.KEYCODE_MENU && pointIsNearEnough) {
            recognizeImHelper.takePicture();
            return true;
        }
        return super.onKeyDown(keycode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        recognizeImHelper.onActivityResultCalled(requestCode, resultCode, data);
    }

}
