package se.mah.ad1532.findmyride;

import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;

public class Controller {
    MainActivity mainActivity;
    private MapFragment map;
    private GoogleMap myMap;
    private LatLng malmo;
    Switch switch_onoff;

    public Controller(final MainActivity mainActivity, Bundle savedInstanceState) {
        this.mainActivity = mainActivity;
        map = (MapFragment) mainActivity.getFragmentManager().findFragmentById(R.id.mapFrag);
        switch_onoff = (Switch) mainActivity.findViewById(R.id.switch_onoff);
        switch_onoff.setOnCheckedChangeListener(new onoffListener());
        initializeMap(map);
    }

    private void initializeMap(MapFragment map) {

        myMap = map.getMap();
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        malmo = new LatLng(55.59362448, 13.09414008);
        myMap.setMyLocationEnabled(true);
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(malmo, 8));
    }

    private class onoffListener implements CompoundButton.OnCheckedChangeListener {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked){
                Toast.makeText(mainActivity,"Checked!",Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(mainActivity,"Not Checked!",Toast.LENGTH_SHORT).show();
            }
        }
    }
}