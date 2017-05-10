package com.innovagenesis.aplicaciones.android.examenonce;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.Manifest;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class MainActivity extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback, SensorEventListener {

    private static final String[] PERMISOS = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    public static final int PLACE_PICKER_REQUEST = 1;
    private static int REQUEST_CODE = 1;
    private GoogleApiClient googleApiClient;

    private double latitud;
    private double longitud;

    private LatLng ultimasCoordenadas;
    private Place place;

    private GoogleMap googleMaps;

    private SensorManager sensorManager;

    String valor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        int leer = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (leer == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, PERMISOS, REQUEST_CODE);
        }
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /** Encargado de gestionar las coordenadas del GPS*/
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                int PLACE_PICKER_REQUEST = 1;
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(builder.build(MainActivity.this), PLACE_PICKER_REQUEST);
                } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorManager.registerListener(this, sensorManager
                        .getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
                SensorManager.SENSOR_DELAY_NORMAL);
    }


    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onStart() {
        googleApiClient.connect();
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {


            SensorManager mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            Sensor temperatureSensor = mySensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);


            sensorManager.registerListener(this, sensorManager
                            .getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
                    SensorManager.SENSOR_DELAY_NORMAL);

            /** Valida existencia de sensor temperatura*/
            if (temperatureSensor != null){

                AlertDialog.Builder dialogo = new AlertDialog.Builder(this);
                dialogo.setTitle("La temperatura acual es: ");
                dialogo.setMessage(valor);
                dialogo.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                dialogo.create().show();

            }else
                Toast.makeText(this, "No tiene sensor el dispositivo ", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Tiene que estar definido aca porque siempre los tiene que pedir
        int leer = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (leer == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, PERMISOS, REQUEST_CODE);
        }

        Location location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {

            latitud = location.getLatitude();
            longitud = location.getLongitude();

            ultimasCoordenadas = new LatLng(latitud, longitud);


            Toast.makeText(this, "Latitud: " + latitud + "Logitud: " + longitud, Toast.LENGTH_SHORT).show();

            /** Instancia el fragment despues del connect para asignar los marcadores */
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /**
     * Gestiona todos los eventos de incio de mapa
     * */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        final LatLng CIUDAD = new LatLng(latitud, longitud);

        googleMap.addMarker(new MarkerOptions()
                .title("Esta es su posicion")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .snippet("Latitud: " + latitud + " Longitud: " + longitud)
                .position(CIUDAD));

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(CIUDAD, 16));

        //Inicializa googleMaps para utilizarlo en el onActivityResult
        googleMaps = googleMap;

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {

            /**
             * Encargado de crear los marcadores si se hace una presion larga
             * */
            @Override
            public void onMapLongClick(LatLng latLng) {
                mCrearMarcadores(place, latLng); // metodo que  crean los marcadores
                ultimasCoordenadas = latLng;
            }
        });


        Toast.makeText(this, "Latitud: " + latitud + "Logitud: " + longitud, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PLACE_PICKER_REQUEST)
            if (resultCode == RESULT_OK) {
                place = PlacePicker.getPlace(this, data);

                LatLng coordenadas = (place.getLatLng());
                mCrearMarcadores(place, coordenadas); //se crean los marcadores
                ultimasCoordenadas = coordenadas;

                Toast.makeText(this, "la ubicacion es: " + place.getLatLng(), Toast.LENGTH_LONG).show();
            }
    }

    /**
     * Método encargado de asignar los marcadores y las polineas en el map
     */
    private void mCrearMarcadores(Place place, LatLng coordenadas) {
        /*
         *  Creacion de marcadores
         *  valida para poner nombre en los marcadores
         *  */
        if (place == null)
            mCrearMarca(place, coordenadas, true);
        else
            mCrearMarca(place, coordenadas, false);
       /*
        *  uso de polilineas
        *  */
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(ultimasCoordenadas)
                .add(coordenadas)
                .color(Color.RED);

        googleMaps.addPolyline(polylineOptions);
    }

    /**
     * Define los atributos de los marcadores
     */
    private void mCrearMarca(Place place, LatLng coordenadas, Boolean nulo) {
        if (nulo) {
            googleMaps.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
                    .snippet("Latitud: " + latitud + " Longitud: " + longitud)
                    .position(coordenadas));

        } else {
            googleMaps.addMarker(new MarkerOptions()
                    .title(place.getName().toString())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA))
                    .snippet("Latitud: " + latitud + " Longitud: " + longitud)
                    .position(coordenadas));
        }
    }

    /**
     * Método encargado de mostrar la temperatura ambiente
     * */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        valor="";
        NumberFormat numberFormat = new DecimalFormat("#0.00");
        valor += "\n"+numberFormat.format(sensorEvent.values[0]) + " °C";
        valor += "\n"+numberFormat.format((9*(sensorEvent.values[0])/5)+32) +" °F";
        valor += "\n"+numberFormat.format((sensorEvent.values[0])+273.15) +" °K";
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
