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
 * Denna klass har hand om all komunikation mellan applikationen och servern.
 * Den skapar sockets och input/output streams så att vi kan komunicera med servern.
 */
// ip Ussis server : 213.188.152.138
// ip lokalt : 192.168.43.250

public class Connect {
    public static final String SERVERIP = "213.188.152.138";    // IP till servern
    public static final int SERVERPORT = 25002;                 // Serverns port

    /*
     * Koder som betyder olika saker för servern. Servern förväntar sig dessa koder
     * vid olika händelser. Applikationen förväntar sig ochså olika koder vid vissa event.
     */
    final String start_nbr = "125,1337";    // Skicka först vid uppkoppling.
    final String rideID = "100";            // Id på den enhet vi vill spåra. (Är just nu hårdkodad men kan ändras vid implementering av fler enheter)
    final String stop_nbr = "175";          // Skickas när vi vill koppla från servern.
    final String confirmReceive = "150";    // Skickas varje gång vi tar emot ett meddelande från servern.

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

    /**
     * Klassens konstruktor som startar en ny tråd och kör en runnable som heter connect
     * för att koppla upp mot servern.
     * @param activity
     */
    public Connect(MainActivity activity){
        this.activity = activity;
        mainHandler = new Handler(activity.getMainLooper());
        Log.i("Debugg","Skapade en ny connect / socket");
        Runnable connect = new connectSocket();
        new Thread(connect).start();
    }


    /**
     * Metod som kör en tråd för att skriva ett meddelande till servern.
     * @param query = Meddelandet man vill skicka (En String)
     */
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


    /**
     * Metod som stänger socketen.
     */
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


    /**
     * ---->| Where all the magic shit happens |<----
     * En klass som implementerar Runnable interfacet för att kunna köras i en tråd.
     * Här skapas input/output streams för att kunna komunicera med servern.
     * Man startar äver en tråd som lyssnar på meddelanden från servern.
     */
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


    /**
     * Lyssnaren som lyssnar på servern. Är en tråd.
     * Tråden lyssnar efter meddelanden från servern och kommer att
     * göra olika saker beroende på vad meddelandet från servern är.
     */
    private class Receive extends Thread {
        LatLng pos;
        public void run() {
            try {
                Log.i("Debugg", "Klassen Receive har körts..");
                while (receive != null) {
                    String message = br.readLine();                         // Läser meddelandet
                    Log.i("Debugg","Svar från servern: " + message);

                    if(message.equals("101")){                              // Servern säger att vi är uppkopplade och förväntar sig då ett id från oss.
                        Log.i("Debugg","Tog emot 101");
                        sendMessage(rideID);
                        Log.i("Debugg","Skickade rideID");
                    }else if (message.startsWith("Z")){                     // Servern skickar koordinater. Den förväntar sig ett svar att vi har tegit emot meddelandet.
                        String coordinates = message.substring(1,message.length()); // Skalar bort "Z" eftersom det inte är en del av koordinaterna utan bara ett sätter för oss att veta att det faktist är koordinater som skickas.
                        Log.i("Debugg","tog emot koordinater:" + coordinates);
                        handleCoordinates(coordinates);                        // Här skickas koordinaterna till en metod som tar hand om dem.
                        sendMessage(confirmReceive);                        // Skickar ett meddelande till servern att vi har tagit emot koordinaterna.
                    }else if(message.equals("299")){                        // Servern säger att den inte har några koordinater att skicka men att den fortfarande är uppkopplad.
                        Log.i("Debugg","error code 299: Servern har inga koordinater");
                    }else if (message.equals("199")){                       // Servern säger att det är okej att koppla ifrån nu. (Den kommer också att koppla ifrån sin socket)
                        disconnectSocket();
                    }
                }
            } catch (Exception e) { // IOException, ClassNotFoundException
                e.printStackTrace();
                receive = null;
                Log.i("Debugg", "Receive är null");
            }
        }

        /**
         * Tar emot koordinaterna i en String och splittar den vid "," för att kunna
         * sära på latitude och longitude så vi kan skapa en LatLng och skicka den vidare till
         * kontrollern som då kan byta positionen på markören så den visar den nya positionen.
         * @param coordinates = Koordinaterna i String-form
         */
        private void handleCoordinates(String coordinates) {
            String[]info = coordinates.split(",");
            pos = new LatLng(Double.valueOf(info[0]), Double.valueOf(info[1]));

            mainHandler.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        activity.controller.changeBikePos(pos);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Log.i("Debugg", "fel i try catch i handleCoordinates()");
                    }
                }
            });
        }
    }
}