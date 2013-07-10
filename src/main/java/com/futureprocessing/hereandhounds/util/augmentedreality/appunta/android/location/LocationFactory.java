/*


   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */

package com.futureprocessing.hereandhounds.util.augmentedreality.appunta.android.location;

import android.location.Location;

/**
 * A helper intended to build a new location from simple values
 */
public class LocationFactory {
    /**
     * Creates a new location with the data provided
     *
     * @param latitude  Latitude of the point
     * @param longitude Longitude of the point
     * @param altitude  Altitude in Km of the point
     * @return A location object with the data provided
     */
    public static Location createLocation(double latitude, double longitude, double altitude) {
        Location location = new Location("");
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(altitude);
        location.setAccuracy(0);
        return location;

    }

    /**
     * Creates a new location with the data provided
     *
     * @param latitude  Latitude of the point
     * @param longitude Longitude of the point
     * @return A location object with the data provided
     */
    public static Location createLocation(double latitude, double longitude) {
        return createLocation(latitude, longitude, 0);

    }
}
