package com.futureprocessing.hereandhounds.augmentedreality;

import com.futureprocessing.hereandhounds.augmentedreality.appunta.android.point.Point;

import java.util.List;

public class AppuntaUtils {

    public static void unselectAllPoints(List<Point> eyeViewPoints) {
        for (Point point : eyeViewPoints) {
            point.setSelected(false);
        }
    }
}
