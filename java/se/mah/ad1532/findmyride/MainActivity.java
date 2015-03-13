package se.mah.ad1532.findmyride;

import android.content.SharedPreferences;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends ActionBarActivity {
    Controller controller;
    SharedPreferences prefs;
    SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        prefs = this.getSharedPreferences("MyPrefs", 0);
        controller = new Controller(this, savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("Debugg","onResume()");
        String position = prefs.getString("lastPos","fail");
        if(!position.equals("fail")){
            String[]info = position.split(",");
            LatLng pos = new LatLng(Double.valueOf(info[0]), Double.valueOf(info[1]));
            controller.changeBikePos(pos);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i("Debugg","onPause()");
        if(controller!=null&&controller.connect!=null)
        if (controller.connect.socket!=null){
            if(controller.connect.socket.isConnected())
            controller.connect.sendMessage(controller.connect.stop_nbr);
        }
        if(controller.latestPos!=null){
            Double lat, lng;
            lat = controller.latestPos.latitude;
            lng = controller.latestPos.longitude;
            String posToSave = lat.toString() + "," + lng.toString();
            editor = prefs.edit();
            editor.putString("lastPos", posToSave);
            editor.apply();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("Debugg","onStop()");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("Debugg", "onDestroy()");
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
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
