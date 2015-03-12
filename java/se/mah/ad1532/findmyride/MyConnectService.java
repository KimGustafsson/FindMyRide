package se.mah.ad1532.findmyride;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by Kim on 2014-10-23.
 */
public class MyConnectService  {
    public static final String SERVERIP = "213.188.152.138";
    public static final int SERVERPORT = 25002;

    // Koder till servern
    final String start_nbr = "125";
    final String rideID = "100,1337";
    final String stop_nbr = "175";
    final String confirmRecive = "150";

    private Socket socket;
    private Receive receive;
    private InputStream is;
    private OutputStream os;
    InputStreamReader isr;
    OutputStreamWriter osw;
    BufferedWriter bw;
    BufferedReader br;
    MainActivity activity;

    public MyConnectService(MainActivity activity){
        this.activity = activity;
        Runnable connect = new connectSocket();
        new Thread(connect).start();
    }

    public void sendMessage(final String query) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                if(socket!=null){
                    if(!socket.isConnected()) {
                        Runnable runnable = new connectSocket();
                        new Thread(runnable).start();
                    }
                    try {
                        String message = query + "\n";
                        bw.write(message);
                        bw.flush();
                        Log.i("Debugg", "sendMessage kördes");
                        String state = receive.getState().toString();
                        Log.i("Debugg", "sendMessage() receive state: " + state);
                    } catch (Exception e) {
                        Log.i("Debugg", "sendMessage fail");
                        if(receive == null) {
                            receive = new Receive();
                            receive.start();
                        }
                    }
                }else{
                    Looper.prepare();
                    Toast.makeText(activity,"Server is offline",Toast.LENGTH_SHORT).show();
                }
            }
        }).start();
    }


    public void disconnectSocket(){
        Log.i("Debugg", "disconnectSocket()");
        sendMessage(stop_nbr);
        try {
            if(socket!=null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        socket = null;
    }


    private class connectSocket implements Runnable {
        @Override
        public void run() {
            try {
                socket = new Socket(SERVERIP, SERVERPORT);
                is = socket.getInputStream();
                isr = new InputStreamReader(is);
                br = new BufferedReader(isr);

                os = socket.getOutputStream();
                osw = new OutputStreamWriter(os);
                bw = new BufferedWriter(osw);
                receive = new Receive();
                receive.start();
                //create a socket to make the connection with the server
                if (socket.isConnected()) {
                    Log.i("Debugg", "connectSocket kördes och socket.isConnected är true");
                    sendMessage(start_nbr);
                    sendMessage(rideID);
                    Log.i("Debugg","sendMessage() start_nbr och rideId!");
                }
            } catch (Exception e) {
                Log.e("Debugg", "connectSocket fail", e);
            }
        }
    }


    private class Receive extends Thread {
        LatLng pos;
        public void run() {
            try {
                Log.i("Debugg", "Klassen Receive har körts..");
                while (receive != null) {
                    String message = br.readLine();
                    Log.i("Debugg","Svar från servern: " + message);
                    importantStuff(message);
                }
            } catch (Exception e) { // IOException, ClassNotFoundException
                receive = null;
                Log.i("Debugg", "Verkar som receive är null ... inte bra!");
            }
        }

        private void importantStuff(String message) {
            String[]info = message.split(",");
            pos = new LatLng(Double.valueOf(info[0]), Double.valueOf(info[1]));
            Log.i("Debugg","innan changeBikePos()");
            Handler mainHandler = new Handler(activity.getMainLooper());
            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        activity.controller.changeBikePos(pos);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("Debugg", "fel i try catch i importantStuff()");
                    }
                }
            });
            Log.i("Debugg", "efter changeBikePos()");
            sendMessage(confirmRecive);
            Log.i("Debugg", "efter sendMessage(25)");
        }
    }
}