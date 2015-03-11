package se.mah.ad1532.findmyride;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    public static final String SERVERIP = "192.168.43.250";
    public static final int SERVERPORT = 25002;
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
                if(!socket.isConnected()) {
                    Runnable runnable = new connectSocket();
                    new Thread(runnable).start();
                }
                try {
                    String message = query + "\n";
                    bw.write(message);
                    bw.flush();
                    Log.i("MyConnectService", "sendMessage kördes");
                    String state = receive.getState().toString();
                    Log.i("MyConnectService", "sendMessage() receive state: " + state);
                } catch (Exception e) {
                    Log.i("MyConnectService", "sendMessage fail");
                    if(receive == null) {
                        receive = new Receive();
                        receive.start();
                    }
                }
            }
        }).start();
    }


    public void disconnectSocket(){
        try {
            socket.close();
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
                    Log.i("MyConnectService", "connectSocket kördes och socket.isConnected är true");
                }
            } catch (Exception e) {
                Log.e("MyConnectService", "connectSocket fail", e);
            }
        }
    }


    private class Receive extends Thread {
        LatLng pos;
        public void run() {
            try {
                Log.i("MyConnectService", "Klassen Receive har körts..");
                while (receive != null) {
                    String message = br.readLine();
                    Log.i("Recive","Svar från servern: " + message);
                    importantStuff(message);
                }
            } catch (Exception e) { // IOException, ClassNotFoundException
                receive = null;
                Log.i("MyConnectService", "Verkar som receive är null ... inte bra!");
            }
        }

        private void importantStuff(String message) {
            String[]info = message.split(",");
            pos = new LatLng(Double.valueOf(info[0]), Double.valueOf(info[1]));
            Log.i("Receive","innan changeBikePos()");
            new Thread(new Runnable() {
                @Override
                public void run() {
                    activity.controller.changeBikePos(pos);
                }
            }).start();
            Log.i("Receive", "efter changeBikePos()");
            sendMessage("25");
            Log.i("Receive","efter sendMessage(25)");
        }
    }
}