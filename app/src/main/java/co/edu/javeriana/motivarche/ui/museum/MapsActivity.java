package co.edu.javeriana.motivarche.ui.museum;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import co.edu.javeriana.motivarche.R;
import co.edu.javeriana.motivarche.Utils;
import co.edu.javeriana.motivarche.common.googleMaps.FetchURL;
import co.edu.javeriana.motivarche.common.googleMaps.TaskLoadedCallback;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, TaskLoadedCallback {

    private GoogleMap mMap;
    private EditText search;
    private Marker user;
    private Marker searchMarker;
    private ImageButton walkingButton;
    private ImageButton cyclingButton;
    private ImageButton drivingButton;
    Geocoder mGeocoder;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;
    boolean gpsEnabled = false;
    private final int RADIUS_OF_EARTH_KM = 6371;
    Polyline currentPolyline;
    private List<Marker> markers = new ArrayList<Marker>();

    SensorManager sensorManager;
    Sensor lightSensor;
    SensorEventListener lightSensorListener;


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(mMap != null) {
            if (user != null) {
                markers.remove(user);
                user.remove();
            }
            LatLng userLatLng = new LatLng(4.6486259, -74.2478973);
            user = mMap.addMarker(new MarkerOptions().position(userLatLng).title("Punto Bogotá").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            markers.add(user);
            if(markers.size()>1) {
                zoomMarkers();
            }else{
                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
                mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
            }
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        search = findViewById(R.id.searchText);
        walkingButton = findViewById(R.id.walking);
        cyclingButton = findViewById(R.id.cycle);
        drivingButton = findViewById(R.id.car);

        mGeocoder = new Geocoder(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        lightSensorListener = createSensorEventListener();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mLocationRequest = createLocationRequest();
        mLocationCallback = createLocationCallback();


        walkingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateRoute("walking");
            }
        });

        cyclingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateRoute("bicycling");
            }
        });

        drivingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generateRoute("driving");
            }
        });

        Utils.requestPermission(this, Utils.gpsPermission, "Es necesario el gps para ubicar al usuario", "Solicitud permiso gps", Utils.GPS_REQUEST_CODE);
        checkSettingsLocation();
    }


    //metodo para detectar cambios en el sensor de luminosidad
    private SensorEventListener createSensorEventListener(){
        SensorEventListener lightSensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent sensorEvent) {
                if(mMap != null){
                    if(sensorEvent.values[0]<5000){
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MapsActivity.this,R.raw.googledarkmap));
                    }else{
                        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(MapsActivity.this,R.raw.googlelightmap));
                    }
                }
            }
            @Override
            public void onAccuracyChanged(Sensor sensor, int i) {
            }
        };
        return lightSensorListener;
    }

    private void generateRoute (String transport){
            String address = search.getText().toString().trim();
            if (!address.equals("")) {
                LatLng position = searchByName(address);
                if (position != null && mMap != null) {
                    if (searchMarker != null) {
                        markers.remove(searchMarker);
                        searchMarker.remove();
                    }
                    searchMarker = mMap.addMarker(new MarkerOptions().position(position).title(address).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    markers.add(searchMarker);
                    zoomMarkers();
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(position));

                    String url = getUrl(position.latitude,position.longitude,user.getPosition().latitude,user.getPosition().longitude,transport);
                    new FetchURL(this).execute(url,transport);

                    double distancia = distance(position.latitude,position.longitude,user.getPosition().latitude,user.getPosition().longitude);
                    Toast.makeText(this, "Distancia hasta el punto buscado: "+distancia, Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(this, "Dirección no puede ser vacía", Toast.LENGTH_SHORT).show();
            }


    }

    //metodo para buscar por el nombre ingresado en el editText
    private LatLng searchByName(String name){
        LatLng position = null;
        try {
            List<Address> addresses = mGeocoder.getFromLocationName(name,2);
            if (addresses != null && !addresses.isEmpty()) {
                Address addressResult = addresses.get(0);
                position = new LatLng(addressResult.getLatitude(), addressResult.getLongitude());
            } else {
                Toast.makeText(this, "Dirección no encontrada", Toast.LENGTH_SHORT).show();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return position;
    }

    //metodo para calcular la distancia entre dos puntos
    public double distance(double lat1, double long1, double lat2, double long2) {
        double latDistance = Math.toRadians(lat1 - lat2);
        double lngDistance = Math.toRadians(long1 - long2);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)); double result = RADIUS_OF_EARTH_KM * c;
        return Math.round(result*100.0)/100.0;
    }

    //Metodo para obtener la ruta desde el api de google maps
    private String getUrl(double destinationLatitude, double destinationLongitude, double originLatitude, double originLongitude, String transport) {
        String str_origin = "origin=" + originLatitude + "," + originLongitude;
        String str_destination = "destination=" + destinationLatitude + "," + destinationLongitude;
        String mode = "mode=" + transport;
        String parameters = str_origin + "&" + str_destination + "&" + mode;
        String output = "json";
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters + "&key=" + getString(R.string.google_maps_key);
        return url;
    }

    //metodo que dibuja la ruta obtenida entre los dos puntos
    @Override
    public void onTaskDone(Object... values) {
        if( currentPolyline != null){
            currentPolyline.remove();
        }
        currentPolyline = mMap.addPolyline((PolylineOptions)values[0]);
    }

    //metodo que genera la creacion de la solicitud de refresco
    private LocationRequest createLocationRequest(){
        LocationRequest locationRequest = LocationRequest.create().setInterval(10000).setFastestInterval(5000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;
    }

    //metodo que permite actualizar cierto tiempo la ubicacion del usuario
    private LocationCallback createLocationCallback(){
        LocationCallback location = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                Location location = locationResult.getLastLocation();
                Log.i("LOCATION", "Location update in the callback: " + location);
                if (location != null) {
                    if(user != null){
                        markers.remove(user);
                        user.remove();
                    }
                    LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    user = mMap.addMarker(new MarkerOptions().position(userLatLng).title("Usuario").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                    markers.add(user);
                    if(markers.size()>1) {
                        zoomMarkers();
                    }else{
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                    }
                }
            }
        };

        return location;
    }

    //metodo que permite revisar que la aplicacion tenga acceso al gps
    public void checkSettingsLocation(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());
        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                gpsEnabled = true;
                startLocationUpdates();
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                gpsEnabled = false;
                switch (statusCode) {
                    case CommonStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            ResolvableApiException resolvable = (ResolvableApiException) e ;
                            resolvable.startResolutionForResult(MapsActivity.this, Utils.CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sendEx) {
                            // Ignore the error.
                        } break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        // Location settings are not satisfied. No way to fix the settings so we won't show the dialog.
                        break;
                }
            }
        });


    }

    //metodo que permite iniciar la actualizacion de la ubicacion
    public void startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(this, Utils.gpsPermission) == PackageManager.PERMISSION_GRANTED) {
            if(gpsEnabled) {
                mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
                mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Log.i("location","location success");
                        if (location != null) {
                            if(user != null){
                                markers.remove(user);
                                user.remove();
                            }
                            LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            user = mMap.addMarker(new MarkerOptions().position(userLatLng).title("Usuario").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            markers.add(user);
                            if(markers.size()>1) {
                                zoomMarkers();
                            }else{
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
                                mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
                            }

                        }
                    }
                });
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        startLocationUpdates();
        sensorManager.registerListener(lightSensorListener,lightSensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
        sensorManager.unregisterListener(lightSensorListener);
    }

    //para el callback de ubicacion
    private void stopLocationUpdates(){
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    //metodo que permite revisar los permisos
    @Override
    public void onRequestPermissionsResult(int requestCode, String [] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case Utils.GPS_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    //checkSettingsLocation();
                }else{
                    Log.i("posicion","no se tiene acceso al gps");
                    Toast.makeText(this,"No se tiene acceso al gps del dispositivo",Toast.LENGTH_SHORT).show();
                }
                break;
        }

    }

    //metodo que revisa si tiene el gps activo en el dispositivo
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case Utils.CHECK_SETTINGS:
                if (resultCode == RESULT_OK) {
                    startLocationUpdates();

                } else {
                    Toast.makeText(this, "Sin acceso a localización, hardware deshabilitado!", Toast.LENGTH_LONG).show();
                }
                break;


        }
    }

    private void zoomMarkers() {
        if (markers.size() > 0 && markers != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Marker marker : markers) {
                builder.include(marker.getPosition());
            }
            LatLngBounds bounds = builder.build();
            int padding = 200; // offset from edges of the map in pixels
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
            mMap.animateCamera(cu);
        }
    }


}