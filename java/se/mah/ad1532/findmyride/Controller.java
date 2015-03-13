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

public class Controller {
    MainActivity mainActivity;
    private MapFragment map;
    private GoogleMap myMap;
    Connect connect;
    Switch switch_track;
    Button btn_normal, btn_satelite, btn_hybrid;
    LatLng malmo = new LatLng(55.59362448, 13.09414008);
    LatLng latestPos;

    public Controller(final MainActivity mainActivity, Bundle savedInstanceState) {
        Log.i("Debugg", "Controller skapades");
        this.mainActivity = mainActivity;
        map = (MapFragment) mainActivity.getFragmentManager().findFragmentById(R.id.mapFrag);
        switch_track = (Switch) mainActivity.findViewById(R.id.switch_onoff);
        switch_track.setOnCheckedChangeListener(new trackListener());
        btn_normal = (Button) mainActivity.findViewById(R.id.btn_normal);
        btn_hybrid = (Button) mainActivity.findViewById(R.id.btn_hybrid);
        btn_satelite = (Button) mainActivity.findViewById(R.id.btn_satelite);
        btn_normal.setOnClickListener(new normalMapClicked());
        btn_satelite.setOnClickListener(new sateliteMapClicked());
        btn_hybrid.setOnClickListener(new hybridMapClicked());
        initializeMap(map);
}

    private void initializeMap(MapFragment map) {
        myMap = map.getMap();
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.setMyLocationEnabled(true);

        Log.i("Debugg", "kartan initierades");
    }


    public void changeBikePos(LatLng pos){
        latestPos = pos;
        myMap.clear();
        myMap.addMarker(new MarkerOptions().position(pos).title("Your Ride!").snippet("Your ride is here!").icon(BitmapDescriptorFactory.fromResource(R.drawable.bmx)));
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 15));
        Log.i("Debugg", "changeBikePos(): " + pos.toString());
        playSound();
    }

    public void playSound(){
        final MediaPlayer mp = MediaPlayer.create(mainActivity, R.raw.magic);
        mp.start();
    }


    public void serverOffline(){
        Toast.makeText(mainActivity,"Server is offline, our server gnomes are trying to fix it!",Toast.LENGTH_LONG).show();
    }


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
                    myMap.clear();
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(malmo, 0));
                }
            }
        }
    }

    private class startConnection extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {
            Log.i("Debugg", "doinBackground()");
            connect = new Connect(mainActivity);
            return null;
        }
    }

    private class normalMapClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }
    }

    private class sateliteMapClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            myMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        }
    }

    private class hybridMapClicked implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            myMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        }
    }
}