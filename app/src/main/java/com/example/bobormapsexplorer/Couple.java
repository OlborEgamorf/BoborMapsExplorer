package com.example.bobormapsexplorer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Couple implements Comparable {

    private final int lat, lon;
    private final int superInt;

    Couple(int lat,int lon) {
        this.lat = lat;
        this.lon = lon;
        this.superInt = Integer.parseInt(Integer.toString(lon)+Integer.toString(lat));
    }

    @Override
    public int compareTo(Object o) {
        Couple comp = (Couple)o;
        return Integer.compare(superInt, comp.superInt);
    }

    public int[] getCoords(){
        return new int[]{lat,lon};
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Couple comp = (Couple)obj;
        return superInt == comp.superInt;
    }

    @NonNull
    @Override
    public String toString() {
        return Integer.toString(superInt);
    }
}
