package se.mah.ad1532.findmyride;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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
    MyConnectService connect;
    Switch switch_onoff;
    LatLng malmo = new LatLng(55.59362448, 13.09414008);

    public Controller(final MainActivity mainActivity, Bundle savedInstanceState) {
        Log.i("Debugg", "Controller skapades");
        this.mainActivity = mainActivity;
        map = (MapFragment) mainActivity.getFragmentManager().findFragmentById(R.id.mapFrag);
        switch_onoff = (Switch) mainActivity.findViewById(R.id.switch_onoff);
        switch_onoff.setOnCheckedChangeListener(new onoffListener());
        initializeMap(map);
    }

    private void initializeMap(MapFragment map) {
        myMap = map.getMap();
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.setMyLocationEnabled(true);
        Log.i("Debugg", "kartan initierades");
    }


    public void changeBikePos(LatLng pos){
        myMap.clear();
        myMap.addMarker(new MarkerOptions().position(pos).title("Your Ride!").snippet("Your ride is here!").icon(BitmapDescriptorFactory.fromResource(R.drawable.bmx)));
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, 8));
        Log.i("Debugg", "changeBikePos() kördes");
    }


    private class onoffListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                Toast.makeText(mainActivity,"Connecting!",Toast.LENGTH_SHORT).show();
                new supersyncconnectclassen().execute();
                Log.i("Debugg", "Switchen är på");
            }else{
                Log.i("Debugg", "Switchen är av");
                Toast.makeText(mainActivity,"Disconnecting!",Toast.LENGTH_SHORT).show();
                if(connect!=null){
                    connect.disconnectSocket();
                    myMap.clear();
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(malmo, 0));
                }
            }
        }
    }

    private class supersyncconnectclassen extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {
            Log.i("Debugg", "doinBackground()");
            connect = new MyConnectService(mainActivity);
            return null;
        }
    }
}