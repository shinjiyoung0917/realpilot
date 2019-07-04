package com.example.realpilot.utilAndConfig;

public class ComputeLatLngDistance {
    public double distance(double lat1, double lng1, double lat2, double lng2, String unit) {
        double theta = lng1 - lng2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));

        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;

        if (unit.equals("kilometer")) {
            dist = dist * 1.609344;
        } else if(unit.equals("meter")){
            dist = dist * 1609.344;
        }

        return (dist);
    }

    /* decimal degree를 radians로 */
    public double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    /* radians를 decimal degree로 */
    public double rad2deg(double rad) {
        return (rad * 180 / Math.PI);
    }

}
