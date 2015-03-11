package se.mah.ad1532.findmyride;

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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class Controller {
    MainActivity mainActivity;
    private MapFragment map;
    private GoogleMap myMap;
    MyConnectService connect;
    Button btn_poke;
    Switch switch_onoff;
    LatLng malmo = new LatLng(55.59362448, 13.09414008);

    public Controller(final MainActivity mainActivity, Bundle savedInstanceState) {
        this.mainActivity = mainActivity;
        map = (MapFragment) mainActivity.getFragmentManager().findFragmentById(R.id.mapFrag);
        switch_onoff = (Switch) mainActivity.findViewById(R.id.switch_onoff);
        switch_onoff.setOnCheckedChangeListener(new onoffListener());
        initializeMap(map);
        btn_poke = (Button) mainActivity.findViewById(R.id.btn_poke);
        btn_poke.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connect!=null)
                connect.sendMessage("Tja ussi!");
            }
        });
    }

    private void initializeMap(MapFragment map) {
        myMap = map.getMap();
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.setMyLocationEnabled(true);
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(malmo, 8));
        myMap.addMarker(new MarkerOptions().position(malmo).title("Malmö!"));
    }


    public void changeBikePos(LatLng pos){
        myMap.clear();
        Log.i("changeBikePos()","kom hit iaf");
        myMap.addMarker(new MarkerOptions().position(pos).title("Your Ride!"));
    }


    private class onoffListener implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                Toast.makeText(mainActivity,"Checked!",Toast.LENGTH_SHORT).show();
                new supersyncconnectclassen().execute();

            }else{
                Toast.makeText(mainActivity,"Not Checked!",Toast.LENGTH_SHORT).show();
                if(connect!=null)
                connect.disconnectSocket();
            }
        }
    }

    private class supersyncconnectclassen extends AsyncTask{

        @Override
        protected Object doInBackground(Object[] params) {
            connect = new MyConnectService(mainActivity);
            return null;
        }
    }
}