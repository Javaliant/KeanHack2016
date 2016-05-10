package com.luigivincent.hackkean2016;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {
    private static PrintWriter out;
    private UUID watchAppID = UUID.fromString("10a78713-d92e-4e39-9e4a-78db6cf4b612");
    private PebbleKit.PebbleDataReceiver receiver;
    private TextView text;
    private final int MAX_TEMPERATURE = 84;
    private final int MIN_TEMPERATURE = 58;
    private final int SECOND = 1000;
    private final int TEN_SECONDS = SECOND * 10;
    private int temperature;
    private AlarmManager alarmManager;
    private PendingIntent pendingIntent;
    private BroadcastReceiver broadcastReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setup();

        String ip = "10.250.208.60";
        int port = 6102;
        new Thread(new ClientThread(port, ip)).start();

        text = (TextView) findViewById(R.id.temperatureText);

        ImageButton powerButton = (ImageButton) findViewById(R.id.powerButton);
        powerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                out.println("power");
            }
        });

        ImageButton upButton = (ImageButton) findViewById(R.id.upButton);
        upButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                out.println("up");
                temperature = Math.min(temperature + 1, MAX_TEMPERATURE);
                text.setText(Integer.toString(temperature));
            }
        });

        ImageButton downButton = (ImageButton) findViewById(R.id.downButton);
        downButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                out.println("down");
                temperature = Math.max(temperature - 1, MIN_TEMPERATURE);
                text.setText(Integer.toString(temperature));
            }
        });

        Button schedulerButton = (Button) findViewById(R.id.schedulerButton);
        schedulerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alarmManager.set(
                        AlarmManager.ELAPSED_REALTIME_WAKEUP,
                        SystemClock.elapsedRealtime() + TEN_SECONDS,
                        pendingIntent
                );
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (receiver == null) {
            receiver = new PebbleKit.PebbleDataReceiver(watchAppID) {
                @Override
                public void receiveData(Context context, int transactionId, PebbleDictionary data) {
                    PebbleKit.sendAckToPebble(context, transactionId);

                    if (data.getInteger(PebbleButton.UP.ordinal()) != null) {
                        out.println("up");
                        temperature = Math.min(temperature + 1, MAX_TEMPERATURE);
                        text.setText(Integer.toString(temperature));
                    } else if (data.getInteger(PebbleButton.SELECT.ordinal()) != null) {
                        out.println("power");
                    } else if (data.getInteger(PebbleButton.DOWN.ordinal()) != null) {
                        out.println("down");
                        temperature = Math.max(temperature - 1, MIN_TEMPERATURE);
                        text.setText(Integer.toString(temperature));
                    }
                }
            };
        }
        PebbleKit.registerReceivedDataHandler(getApplicationContext(), receiver);
    }

    private void setup() {
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent i) {
                out.println("set" + (temperature + 5));
                temperature = Math.min(MAX_TEMPERATURE, temperature + 5);
                text.setText(Integer.toString(temperature));
            }
        };

        registerReceiver(broadcastReceiver, new IntentFilter("com.luigivincent.hackkean2016") );
        pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.luigivincent.hackkean2016"), 0);
        alarmManager = (AlarmManager)(this.getSystemService(Context.ALARM_SERVICE));
    }

    private class ClientThread implements Runnable {
        private final int PORT;
        private final String ADDRESS;

        ClientThread(int PORT, String ADDRESS) {
            this.PORT = PORT;
            this.ADDRESS = ADDRESS;
            Log.d("ClientActivity", "starting thread...");
        }
        @Override
        public void run() {
            try {
                Log.d("ClientActivity", "C: Connecting...");
                Socket socket = new Socket(ADDRESS, PORT);

                try {
                    //Log.d("ClientActivity", "C: Sending command.");
                    out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                            socket.getOutputStream())), true);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                    temperature = Integer.parseInt(in.readLine());
                    text.setText(Integer.toString(temperature));

                    //Log.d("ClientActivity", "C: Sent.");
                } catch (Exception e) {
                    // Log.e("ClientActivity", "S: Error", e);
                }
                // socket.close();
                Log.d("ClientActivity", "C: Closed.");
            } catch (Exception e) {
                Log.e("ClientActivity", "C: Error", e);
                //new Thread(new ClientThread(PORT, ADDRESS)).start();
            }
        }
    }
}
