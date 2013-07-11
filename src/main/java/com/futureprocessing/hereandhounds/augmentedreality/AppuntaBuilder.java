package com.futureprocessing.hereandhounds.augmentedreality;

import android.app.Activity;

import com.futureprocessing.hereandhounds.activity.EyeViewActivity;
import com.futureprocessing.hereandhounds.R;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.location.LocationFactory;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.orientation.OrientationManager;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.point.Point;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.ui.AppuntaView;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.ui.EyeView;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.ui.RadarView;

import java.util.List;

public class AppuntaBuilder {

    private Activity activity;
    private OrientationManager compass;
    private EyeView eyeView;
    private RadarView radarView;
    private List<Point> eyeViewPoints;
    private List<Point> radarPoints;

    public AppuntaBuilder activity(EyeViewActivity activity) {
        this.activity = activity;
        return this;
    }

    public AppuntaBuilder compass(OrientationManager compass) {
        this.compass = compass;
        return this;
    }

    public AppuntaBuilder eyeView(EyeView eyeView) {
        this.eyeView = eyeView;
        return this;
    }

    public AppuntaBuilder radarView(RadarView radarView) {
        this.radarView = radarView;
        return this;
    }

    public AppuntaBuilder eyeViewPoints(List<Point> eyeViewPoints) {
        this.eyeViewPoints = eyeViewPoints;
        return this;
    }

    public AppuntaBuilder radarPoints(List<Point> radarPoints) {
        this.radarPoints = radarPoints;
        return this;
    }

    public void build() {
        compass.setAxisMode(OrientationManager.MODE_AR);
        compass.setOnOrientationChangeListener((OrientationManager.OnOrientationChangedListener) activity);
        compass.startSensor(activity);

        eyeView.setMaxDistance(AppuntaConstants.MAX_DISTANCE);
        radarView.setMaxDistance(AppuntaConstants.MAX_DISTANCE);
        eyeView.setOnPointPressedListener((AppuntaView.OnPointPressedListener) activity);
        radarView.setOnPointPressedListener((AppuntaView.OnPointPressedListener) activity);

        eyeView.setPoints(eyeViewPoints);
        eyeView.setPosition(LocationFactory.createLocation(AppuntaConstants.DEFAULT_LATITUDE, AppuntaConstants.DEFAULT_LONGITUDE, AppuntaConstants.DEFAULT_ALTITUDE));
        eyeView.setOnPointPressedListener((AppuntaView.OnPointPressedListener) activity);
        radarView.setPoints(radarPoints);
        radarView.setPosition(LocationFactory.createLocation(AppuntaConstants.DEFAULT_LATITUDE, AppuntaConstants.DEFAULT_LONGITUDE, AppuntaConstants.DEFAULT_ALTITUDE));
        radarView.setRotableBackground(R.drawable.arrow);
    }


}
