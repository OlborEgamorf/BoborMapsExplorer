package com.example.bobormapsexplorer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

public class Laby {

    public static void main(String[] args) {
        ArrayList<Double[]> carres = new ArrayList<>(null);
        ArrayList<Double[]> pointsDown = new ArrayList<>(null);
        ArrayList<Double[]> pointsUp = new ArrayList<>(null);
        // doit être trié
        
        Iterator<Double[]> a = carres.iterator();

        double lat = 43.687817;
        double lon = 3.866146;
        double radLat = 0.0001;
        double radLon = 0.00015;

        Double[] previous;
        Double[] next = a.next();

        pointsUp.add(new Double[]{lat+next[0]*radLat,lon+next[1]*radLon});

        while (a.hasNext()) {

            previous = next;
            next = a.next();

            if (next[1] != previous[1]) {
                pointsUp.add(new Double[]{lat+(previous[0]+1)*radLat,lon+previous[1]*radLon});
                pointsUp.add(new Double[]{lat+(previous[0]+1)*radLat,lon+(previous[1]+1)*radLon});

                pointsDown.add(new Double[]{lat+next[0]*radLat,lon+next[1]*radLon});
                pointsDown.add(new Double[]{lat+next[0]*radLat,lon+(next[1]+1)*radLon});
            }
        }

        pointsUp.add(new Double[]{lat+(next[0]+1)*radLat,lon+next[1]*radLon});
        pointsUp.add(new Double[]{lat+(next[0]+1)*radLat,lon+(next[1]+1)*radLon});

        Collections.reverse(pointsDown);
        pointsUp.addAll(pointsDown);
    }
}
