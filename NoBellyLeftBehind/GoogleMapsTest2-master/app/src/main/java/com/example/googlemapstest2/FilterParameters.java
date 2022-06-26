package com.example.googlemapstest2;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FilterParameters {
    private boolean[] daysOpenPopup;
    private List<String> whoTheyServePopup;
    private double proximityPopup;

    public FilterParameters(boolean[] daysOpenChecked, List<String> whoTheyServePopup, double proximity) {
        this.daysOpenPopup = daysOpenChecked;
        this.whoTheyServePopup = whoTheyServePopup;
        this.proximityPopup = proximity;
    }

    public void update(boolean[] daysOpen, List<String> whoTheyServe, double proximity) {
        this.daysOpenPopup = daysOpen;
        this.whoTheyServePopup = whoTheyServe;
        this.proximityPopup = proximity;
    }
    public boolean[] getDaysOpenChecked() {
        return Arrays.copyOf(daysOpenPopup, daysOpenPopup.length);
    }
    public String getWhoTheyServePopup() {
        return getWhoTheyServePopup();
    }
    public double getProximityPopup() {
        return proximityPopup;
    }

    public List<FoodLocation> filter(List<FoodLocation> allLocations, LatLng currentPosition) {
        // empty list
        List<FoodLocation> res = new ArrayList<>();

        for (int i = 0; i < allLocations.size(); i++) {
            FoodLocation foodLocation = allLocations.get(i);
            if (!checkDistance(currentPosition, foodLocation.getLatLng())) continue;
            if (!passDaysCheck(foodLocation)) continue;
            if (!whoTheyServeCheck(foodLocation)) continue;
            res.add(foodLocation);
        }
        return res;
    }
    private boolean passDaysCheck(FoodLocation foodLocation) {
        boolean[] foodLocationDaysOpen = foodLocation.getDaysOpen();
        for (int i = 0; i < daysOpenPopup.length; i++) {
            if (daysOpenPopup[i] && foodLocationDaysOpen[i]) {
                return true;
            }
        }
        return false;
    }
    private boolean whoTheyServeCheck(FoodLocation foodLocation) {
        for (String servePossibility : whoTheyServePopup) {
            String target = foodLocation.getWhoTheyServe();
            if (target.contains(servePossibility)) return true;
            // random edge cases
            if ((target.contains("requested") || target.contains("Women") || target.contains("Hospital")) && servePossibility.equals("OTHER")) {
                return true;
            }
        }
        return false;
    }

    private boolean checkDistance(LatLng one, LatLng two) {
        double lat1 = one.latitude;
        double lat2 = two.latitude;
        double lon1 = one.longitude;
        double lon2 = two.longitude;

        // internet code
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;
        return kilometersToMiles(dist) < proximityPopup;

    }
    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }

    private double kilometersToMiles(double kilometers) {
        return kilometers * 0.621371;
    }
}
