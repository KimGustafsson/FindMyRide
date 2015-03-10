package se.mah.ad1532.findmyride;

import android.util.Log;

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
    private DataInputStream dis;
    private OutputStream os;
    OutputStreamWriter osw;
    BufferedWriter bw;

    public MyConnectService(){
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
                    bw.write(query);
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
                dis = new DataInputStream(is);
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
        public void run() {
            String result;
            try {
                Log.i("MyConnectService", "Klassen Receive har körts..");
                while (receive != null) {
                    result = dis.readUTF();
                    Log.i("MyConnectService", "Inne i while i Receive :" + result);
                }
            } catch (Exception e) { // IOException, ClassNotFoundException
                receive = null;
                Log.i("MyConnectService", "Verkar som receive är null ... inte bra!");
            }
        }
    }
}