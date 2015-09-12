package se.mah.ad1532.findmyride;

import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Klassen som har hand om komunikationen mellan MainActivity och Connect.
 * Denhar också hand om de saker som bör göras på UI-tråden.
 * Kartan och alla komponenter initieras i denna klassen.
 */
public class Controller {
    MainActivity mainActivity;
    private MapFragment map;
    private GoogleMap myMap;
    Connect connect;
    Switch switch_track;
    Switch switch_zoom;
    Button btn_normal, btn_satelite, btn_hybrid;
    LatLng malmo = new LatLng(55.59362448, 13.09414008);
    LatLng latestPos;
    boolean autozoom = false;

    /**
     * Konstruktor som tar emot en aktivitet och den aktivitetens Bundle.
     * Här instansieras kartan, alla knappar/switchar och dess lyssnare.
     * @param mainActivity = aktiviteten
     * @param savedInstanceState = Bundle
     */
    public Controller(final MainActivity mainActivity, Bundle savedInstanceState) {
        Log.i("Debugg", "Controller skapades");
        this.mainActivity = mainActivity;
        map = (MapFragment) mainActivity.getFragmentManager().findFragmentById(R.id.mapFrag);
        switch_track = (Switch) mainActivity.findViewById(R.id.switch_onoff);
        switch_zoom = (Switch) mainActivity.findViewById(R.id.switch_zoom);
        switch_track.setOnCheckedChangeListener(new trackListener());
        switch_zoom.setOnCheckedChangeListener(new zoomListener());
        btn_normal = (Button) mainActivity.findViewById(R.id.btn_normal);
        btn_hybrid = (Button) mainActivity.findViewById(R.id.btn_hybrid);
        btn_satelite = (Button) mainActivity.findViewById(R.id.btn_satelite);
        btn_normal.setOnClickListener(new normalMapClicked());
        btn_satelite.setOnClickListener(new sateliteMapClicked());
        btn_hybrid.setOnClickListener(new hybridMapClicked());
        initializeMap(map);
}

    /**
     * Här initieras kartan.
     * @param map = den karta som ska initieras.
     */
    private void initializeMap(MapFragment map) {
        myMap = map.getMap();
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.setMyLocationEnabled(true);

        Log.i("Debugg", "kartan initierades");
    }


    /**
     * Metod som först tar bor allt på kartan och sedan sätter ut
     * en markör på de koordinaterna som skickas med.
     * Sedan zoomas kartan in vid markören och ett ljud spelas upp.
     * @param pos
     */
    public void changeBikePos(LatLng pos){
        latestPos = pos;
        myMap.clear();
        myMap.addMarker(new MarkerOptions().position(pos).title("Your Ride!").snippet("Your ride is here!").icon(BitmapDescriptorFactory.fromResource(R.drawable.bmx)));
        if(autozoom)
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 20));
        Log.i("Debugg", "changeBikePos(): " + pos.toString());
        playSound();
    }


    /**
     * Metod som spelar upp ett ljud.
     */
    public void playSound(){
        final MediaPlayer mp = MediaPlayer.create(mainActivity, R.raw.magic);
        mp.start();
    }


    /**
     * Metod som skriver ut en Toast på skärmen.
     */
    public void serverOffline(){
        Toast.makeText(mainActivity,"Server is offline, our server gnomes are trying to fix it!",Toast.LENGTH_LONG).show();
    }


    /**
     * Lyssnaren på switchen. Om switchen är på så startas en AsyncTask som kopplar upp applikationen mot servern.
     * Om switchen är off så skickas ett meddelande till servern som säger att man ska koppla ifrån socketen.
     */
    private class trackListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                Toast.makeText(mainActivity,"Fetching position!",Toast.LENGTH_SHORT).show();
                new startConnection().execute();
                Log.i("Debugg", "Switchen är på");
            }else{
                Log.i("Debugg", "Switchen är av");
                Toast.makeText(mainActivity,"Disconnecting!",Toast.LENGTH_SHORT).show();
                if(connect!=null){
                    connect.sendMessage(connect.stop_nbr);
                }
            }
        }
    }

    /**
     * En AsyncTask som startar uppkopplingen mot servern. Är en asynctask för att inte frysa GUI vid uppkoppling.
     */
    private class startConnection extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {
            Log.i("Debugg", "doinBackground()");
            connect = new Connect(mainActivity);
            return null;
        }
    }

    /**
     * Sätter kartans typ till Normal.
     */
    private class normalMapClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    /**
     * Sätter kartans typ till Sattelit.
     */
    private class sateliteMapClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
    }

    /**
     * Sätter kartans typ till Hybrid.
     */
    private class hybridMapClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
    }

    /**
     * Lyssnare till auto zoom switchen.
     */
    private class zoomListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked){
                autozoom = true;
                Toast.makeText(mainActivity,"Auto zoom activated!",Toast.LENGTH_SHORT).show();
            }
            else{
                autozoom = false;
                Toast.makeText(mainActivity,"Auto zoom deactivated!",Toast.LENGTH_SHORT).show();
            }
        }
    }
}