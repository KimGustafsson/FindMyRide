package se.mah.ad1532.findmyride;

import android.os.Handler;
import android.util.Log;

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
// ip Ussis server : 213.188.152.138
// ip lokalt : 192.168.43.250
public class Connect {
    public static final String SERVERIP = "213.188.152.138";
    public static final int SERVERPORT = 25002;

    // Koder till servern
    final String start_nbr = "125,1337";
    final String rideID = "100";
    final String stop_nbr = "175";
    final String confirmReceive = "150";

    Socket socket;
    private Receive receive;
    private InputStream is;
    private OutputStream os;
    InputStreamReader isr;
    OutputStreamWriter osw;
    BufferedWriter bw;
    BufferedReader br;
    Handler mainHandler;
    MainActivity activity;

    public Connect(MainActivity activity){
        this.activity = activity;
        mainHandler = new Handler(activity.getMainLooper());
        Log.i("Debugg","Skapade en ny connect / socket");
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
                        Log.i("Debugg", "sendMessage() :" + message);
                    } catch (Exception e) {
                        Log.i("Debugg", "sendMessage fail");
                    }
                }
            }
        }).start();
    }


    public void disconnectSocket(){
        Log.i("Debugg", "disconnectSocket()");
        try {
            if(socket!=null) {
                socket.close();
                Log.i("Debugg","socket.isClosed(): " + socket.isClosed());
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("Debugg","socket.close() exception");
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
                    Log.i("Debugg", "socket.isConnected är: " + socket.isConnected());
                    sendMessage(start_nbr);
                    Log.i("Debugg","sendMessage() start_nbr");
                }
            } catch (Exception e) {
                Log.e("Debugg", "connectSocket fail", e);
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            activity.controller.serverOffline();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.i("Debugg", "fel i try catch i importantStuff()");
                        }
                    }
                });
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

                    if(message.equals("101")){
                        Log.i("Debugg","Tog emot 101");
                        sendMessage(rideID);
                        Log.i("Debugg","Skickade rideID");
                    }else if (message.startsWith("Z")){
                        String coordinates = message.substring(1,message.length());
                        Log.i("Debugg","tog emot koordinater:" + coordinates);
                        importantStuff(coordinates);
                    }else if(message.equals("299")){
                        Log.i("Debugg","error code 299: Servern har inga koordinater");
                    }else if (message.equals("199")){
                        disconnectSocket();
                    }
                }
            } catch (Exception e) { // IOException, ClassNotFoundException
                e.printStackTrace();
                receive = null;
                Log.i("Debugg", "Receive är null");
            }
        }

        private void importantStuff(String message) {
            String[]info = message.split(",");
            pos = new LatLng(Double.valueOf(info[0]), Double.valueOf(info[1]));

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
            sendMessage(confirmReceive);
        }
    }
}