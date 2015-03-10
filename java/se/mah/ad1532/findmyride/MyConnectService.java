package se.mah.ad1532.findmyride;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Kim on 2014-10-23.
 */
public class MyConnectService extends Service {
    public static final String SERVERIP = "195.178.232.7";
    public static final int SERVERPORT = 7117;
    private Socket socket;
    private Receive receive;
    private InputStream is;
    private DataInputStream dis;
    private OutputStream os;
    private DataOutputStream dos;
    private final IBinder mbinder = new LocalBinder();


    @Override
    public IBinder onBind(Intent intent) {
        return mbinder;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("MyConnectService", "onCreate kördes");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i("MyConnectService", "onStartCommand kördes");
        Runnable connect = new connectSocket();
        new Thread(connect).start();
        Log.i("MyConnectService", "Receive har startats ...");
        return START_STICKY;
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
                    dos.writeUTF(query);
                    dos.flush();
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


    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectSocket();
    }


    private class connectSocket implements Runnable {
        @Override
        public void run() {
            try {
                socket = new Socket(SERVERIP, SERVERPORT);
                is = socket.getInputStream();
                dis = new DataInputStream(is);
                os = socket.getOutputStream();
                dos = new DataOutputStream(os);
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


    public class LocalBinder extends Binder {
        public MyConnectService getService() {
            return MyConnectService.this;
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
                    new ServerMessage(result);
                }
            } catch (Exception e) { // IOException, ClassNotFoundException
                receive = null;
                Log.i("MyConnectService", "Verkar som receive är null ... inte bra!");
            }
        }
    }


    private class ServerMessage {

        private String theGroupImConnectedTo;

        // Send an Intent with an action named "my-event".
        private void sendMessage(String message, String[] array ) {
            Intent intent = new Intent("my-event");
            // add data
            intent.putExtra("message", message);
            intent.putExtra("array", array);
            LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);
        }

        public ServerMessage(final String result) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        Log.i("MyConnectService", "ServerMessage :" + jsonObject.getString("type"));

                        if(jsonObject.getString("type").equals("groups")){
                            JSONArray jsonArray = jsonObject.getJSONArray("groups");
                            String[] grpArray = new String[jsonArray.length()];

                            for(int i=0;i<jsonArray.length();i++){
                                grpArray[i] = jsonArray.getJSONObject(i).getString("group");
                                Log.i("MyConnectService", "Mottaget server-meddelande: Groups :" + grpArray[i]);
                            }
                            sendMessage("groups", grpArray);
                        }
                        else if(jsonObject.getString("type").equals("register")){
                            String[] array = new String[1];
                            array[0] = jsonObject.getString("id");
                            String id = array[0];
                            String[] idArray = id.split(",");
                            theGroupImConnectedTo = idArray[0];
                            sendMessage("register",array );
                            Log.i("MyConnectService", "Mottaget server-meddelande: register :" + array[0]);
                        }
                        else if(jsonObject.getString("type").equals("members")){
                            JSONArray jsonArray = jsonObject.getJSONArray("members");
                            String[] membArray = new String[jsonArray.length()];

                            for(int i=0;i<jsonArray.length();i++){
                                membArray[i] = jsonArray.getJSONObject(i).getString("member");
                                Log.i("MyConnectService", "Mottaget server-meddelande: Members :" + membArray[i]);
                            }
                            sendMessage("members", membArray);
                        }
                        else if(jsonObject.getString("type").equals("unregister")){
                            sendMessage("unregister",null);
                        }
                        else if (jsonObject.getString("type").equals("locations")){
                            JSONArray jsonArray = jsonObject.getJSONArray("location");
                            String[] allMembers = new String[jsonArray.length()];
                            for(int i=0;i<jsonArray.length();i++) {
                                String[] oneMember = new String[3];
                                allMembers[i] = jsonArray.getJSONObject(i).getString("member");
                                oneMember[0] = jsonArray.getJSONObject(i).getString("member");
                                oneMember[1] = jsonArray.getJSONObject(i).getString("longitude");
                                oneMember[2] = jsonArray.getJSONObject(i).getString("latitude");
                                Log.i("MyConnectService", "onReceive() locations for-loopen: " + oneMember[0]);
                                sendMessage("locations", oneMember);
                            }
                            sendMessage("memberArray", allMembers);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }
    }
}