package com.example.bobormapsexplorer;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.bobormapsexplorer.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.tasks.Task;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityMapsBinding binding;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locReq;
    private LocationCallback locationCallback;
    private Circle circle;
    private Polygon hidder;
    private final boolean locOn = true;
    private ArrayList<Couple> coords = new ArrayList<>();

    private final double latRef = 43.644355;
    private final double lonRef = 3.866146;
    private final double radLat = 0.0001;
    private final double radLon = 0.00015;
    private Writer dataW;

    protected void createLocationRequest() {
        locReq = LocationRequest.create();
        locReq.setInterval(10000);
        locReq.setFastestInterval(5000);
        locReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locReq);

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> {
        });

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    int REQUEST_CHECK_SETTINGS = 0;
                    resolvable.startResolutionForResult(MapsActivity.this,
                            REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignore the error.
                }
            }
        });
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locReq,
                locationCallback,
                Looper.getMainLooper());
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        dataW = new Writer(this);
        readData();

        createLocationRequest();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    System.out.println(location.toString());

                    if (circle == null) {
                        circle = mMap.addCircle(new CircleOptions()
                                .center(new LatLng(location.getLatitude(),location.getLongitude()))
                                .radius(3)
                                .strokeColor(Color.RED)
                                .fillColor(Color.BLUE)
                                .zIndex(5));
                    } else {
                        circle.setCenter(new LatLng(location.getLatitude(),location.getLongitude()));
                    }

                    int latSquare = (int)((location.getLatitude() - latRef)/radLat);
                    int lonSquare = (int)((location.getLongitude() - lonRef)/radLon);

                    for (int i=-1;i<2;i++){
                        for (int j=-1;j<2;j++){
                            Couple couple = new Couple(latSquare+i,lonSquare+j);
                            if (!coords.contains(couple)) {
                                coords.add(couple);
                                dataW.appendData(toCsv(latSquare+i,lonSquare+j));
                            }
                        }
                    }

                    Collections.sort(coords);

                    System.out.println(coords);
                    addHole();
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (locOn) {
            startLocationUpdates();
        }
    }

    /* Manipulates the map once available. */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));

        LatLng mtp = new LatLng(43.654615, 3.884689);
        mMap.addMarker(new MarkerOptions().position(mtp).title("MTP"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(mtp));
        mMap.setMinZoomPreference(13);

        System.out.println(googleMap.getCameraPosition());

        hidder = mMap.addPolygon(new PolygonOptions()
                .add(   new LatLng(0, 0),
                        new LatLng(100, 0),
                        new LatLng(100, 100),
                        new LatLng(0, 100),
                        new LatLng(0, 0))
                .fillColor(Color.rgb(241, 243, 244))
                .strokeColor(Color.rgb(241, 243, 244)));

        addHole();

    }

    private void addHole() {
        ArrayList<Double[]> pointsDown = new ArrayList<>();
        ArrayList<Double[]> pointsUp = new ArrayList<>();

        Iterator<Couple> a = coords.iterator();

        int[] previous;
        int[] next = a.next().getCoords();
        int lastLat = -1;
        int lastLatDown = -1;

        pointsUp.add(new Double[]{latRef+next[0]*radLat,lonRef+next[1]*radLon});

        while (a.hasNext()) {

            previous = next;
            next = a.next().getCoords();

            if (next[1] != previous[1]) {
                if (lastLat != previous[0]) {
                    pointsUp.add(new Double[]{latRef+(previous[0]+1)*radLat,lonRef+previous[1]*radLon});
                    lastLat = previous[0];
                }
                pointsUp.add(new Double[]{latRef+(previous[0]+1)*radLat,lonRef+(previous[1]+1)*radLon});

                if (lastLatDown != next[0]) {
                    pointsDown.add(new Double[]{latRef+next[0]*radLat,lonRef+next[1]*radLon});
                    lastLatDown = next[0];
                }
                pointsDown.add(new Double[]{latRef+next[0]*radLat,lonRef+(next[1]+1)*radLon});
            }
        }

        if (lastLat != next[0]) {
            pointsUp.add(new Double[]{latRef+(next[0]+1)*radLat,lonRef+next[1]*radLon});
        }
        pointsUp.add(new Double[]{latRef+(next[0]+1)*radLat,lonRef+(next[1]+1)*radLon});

        Collections.reverse(pointsDown);
        pointsUp.addAll(pointsDown);

        List<LatLng> points = new ArrayList<>();
        for (Double[] i : pointsUp) {
            System.out.println(i[0]+" "+i[1]);
            points.add(new LatLng(i[0],i[1]));
        }

        List<List<LatLng>> test = new ArrayList<>();
        test.add(points);

        hidder.setHoles(test);
    }

    public String toCsv(int lat, int lon) {
        return lat+";"+lon+"\n";
    }

    public void readData(){
        try {
            FileInputStream fis = this.openFileInput("data");
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                String[] split = line.split(";");
                coords.add(new Couple(Integer.parseInt(split[0]),Integer.parseInt(split[1])));
                line = reader.readLine();
            }
            Collections.sort(coords);
        } catch (FileNotFoundException e) {
            //
        } catch (IOException e) {
            // Error occurred when opening raw file for reading.
            System.out.println("PAS PU LIRE");
        }
    }
}

/*

up : 43.687817, 3.883484
left : 43.659632, 3.866146
down : 43.644355, 3.890007
right : 43.664786, 3.903482

43.687817,3.866146
43.687817, 3.903482
43.644355,3.903482
3.903482,3.866146

3.866146 - 3.903482
43.644355 - 43.687817

1° = 80404.197m

0.0001 = 8.0404197m
0.00005 = 4.0202m

 */