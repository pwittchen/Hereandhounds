package com.futureprocessing.hereandhounds.location;

import android.app.Activity;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.futureprocessing.hereandhounds.activity.EyeViewActivity;
import com.futureprocessing.hereandhounds.augmentedreality.AppuntaConstants;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.location.LocationFactory;
import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.point.Point;

import java.util.List;

public class LocationHelper {

    public void setLocationListener(final Location currentLocation, LocationManager locationManager, final EyeViewActivity activity) {
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                activity.setCurrentLocation(location);
                activity.getEyeView().setPosition(LocationFactory.createLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), currentLocation.getAltitude()));
                activity.getRadarView().setPosition(LocationFactory.createLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), currentLocation.getAltitude()));
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

    public boolean setLocationListenerMock(Location currentLocation, List<Point> eyeViewPoints, TextView tvLocationOutput, Activity activity) {
        currentLocation.setLatitude(AppuntaConstants.DEFAULT_LATITUDE);
        currentLocation.setLongitude(AppuntaConstants.DEFAULT_LONGITUDE);
        currentLocation.setAltitude(AppuntaConstants.DEFAULT_ALTITUDE);

        for (Point point : eyeViewPoints)
            if (point.getLocation().distanceTo(currentLocation) < 800) {
                tvLocationOutput.setVisibility(View.VISIBLE);
                tvLocationOutput.setText("Location: " + point.getName() + " found. Take a picture");
                return true;
            }
        tvLocationOutput.setVisibility(View.GONE);
        return false;
    }

}
