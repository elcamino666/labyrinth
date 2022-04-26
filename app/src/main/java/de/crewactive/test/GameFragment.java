package de.crewactive.test;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Game Fragment
 */
public class GameFragment extends Fragment implements SensorEventListener, IGamePlayed {

    private Singleton singleton;                        // Singleton data
    private SensorManager sensorManager;                // Sensor Manager
    private NavController navController;                // Navigation Controller
    private MediaPlayer mpPassRound, mWin, mLose;       // Different Sounds for Game
    private Sensor sensor;                              // Sensor Object
    private Game game;                                  // Object from Game Class

    private TextView timerText;                         // Text View for timer
    private TextView roundText;                         // Text View for Rounds
    private Button withdraw;                            // Button to withdraw

    private Timer timer;                                // Timer object
    private TimerTask timerTask;                        // Timer task
    private Double time = 0.0;                          // Initialize time

    private Handler mHandler = new Handler(Looper.getMainLooper());         // Handler for updates that comes from Game Object
    private Handler timeHandler = new Handler(Looper.getMainLooper());      // Handler for time

    private static final String sub_topic = "sensor/data";                  // Endpoint to get data from RaspBerry
    private static final String pub_topic = "sensehat/message";             // Endpoint to send data to RaspBerry
    private int qos = 0;
    private String clientId;
    private MemoryPersistence persistence = new MemoryPersistence();
    private MqttClient client;
    private final String TAG = "*GameFragment*";

    /**
     * Required empty public constructor
     */
    public GameFragment() {

    }

    /**
     * Initializing sensor manager
     *
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sensorManager = (SensorManager) requireActivity().getSystemService(Context.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor != null)
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    /**
     * When View is Created
     *
     * @param inflater           inflater
     * @param container          container
     * @param savedInstanceState savedInstanceState
     * @return infiltrated View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    /**
     * After View is Created, initialize all variables, set on click listeners
     *
     * @param view               view
     * @param savedInstanceState savedInstanceState
     */
    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        withdraw = view.findViewById(R.id.finishGameButton);
        timerText = view.findViewById(R.id.txtTimer);
        roundText = view.findViewById(R.id.txtRound);

        singleton = Singleton.getInstance();

        navController = Navigation.findNavController(view);

        game = view.findViewById(R.id.gameView);
        game.setCallBack(this);

        mpPassRound = MediaPlayer.create(getActivity(), R.raw.round);
        mWin = MediaPlayer.create(getActivity(), R.raw.win);
        mLose = MediaPlayer.create(getActivity(), R.raw.lose);

        mWin.setVolume(Singleton.VOLUME, Singleton.VOLUME);
        mLose.setVolume(Singleton.VOLUME, Singleton.VOLUME);

        roundText.setText(new StringBuilder().append("Round ").append(singleton.getGamePlayed() + 1).toString());

        withdraw.setOnClickListener(v -> {
            game = new Game(getActivity(), null);
            singleton.setStopTimer(true);
            singleton.setGamePlayed(0);

            if (!singleton.getAndroidSensor())
                publish(pub_topic, "withdraw_game");

            requireActivity().runOnUiThread(() -> {
                if (singleton.getSoundSettings())
                    mLose.start();
                navController.navigate(R.id.action_gameFragment_to_endgameFragment);
            });
        });

        timer = new Timer();
        startTimer();
    }

    /**
     * When Accelerometer values Change, fires this function
     *
     * @param event event with Accelerometer values
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!singleton.getStopTimer() && singleton.getAndroidSensor()) {
            float x = event.values[0];
            float y = event.values[1];
            movePlayer(x, y);
        }
    }

    /**
     * Give direction to game object where to move the player
     *
     * @param x value from Accelerometer
     * @param y value from Accelerometer
     */
    private void movePlayer(float x, float y) {

        float absDx = Math.abs(x);
        float absDy = Math.abs(y);

        if (absDx > absDy) {
            if (x > 0) game.movePlayer(Game.Direction.LEFT);
            else game.movePlayer(Game.Direction.RIGHT);
        } else {
            if (y > 0) game.movePlayer(Game.Direction.DOWN);
            else game.movePlayer(Game.Direction.UP);
        }
    }

    /**
     * Starts the timer on Game View
     */
    private void startTimer() {
        timerTask = new TimerTask() {
            @Override
            public void run() {
                if (singleton.getStopTimer()) {
                    timer.cancel();
                    timer.purge();
                } else {
                    timeHandler.post(() -> {
                        time++;
                        timerText.setText(getTimerText());
                    });
                }
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    /**
     * Extract the Time, Calculate the hours, minutes, seconds,
     * and transform it into String
     */
    private String getTimerText() {
        int rounded = (int) Math.round(time);
        int seconds = ((rounded % 86400) % 3600) % 60;
        int minutes = ((rounded % 86400) % 3600) / 60;
        int hours = ((rounded % 86400) / 3600);
        return formatTime(seconds, minutes, hours);
    }

    /**
     * Transform Time in String Format with two 0
     *
     * @param seconds played the Game
     * @param minutes played the Game
     * @param hours   played the Game
     */
    private String formatTime(int seconds, int minutes, int hours) {
        return new StringBuilder().append(String.format("%02d", hours)).append(" : ").append(String.format("%02d", minutes)).append(" : ").append(String.format("%02d", seconds)).toString();
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * Update Game rounds from Game Class and resets some variables
     *
     * @param gamePlayed number of game that are current played from user
     */
    @Override
    public void update(int gamePlayed) {
        mHandler.post(() -> {
            if (gamePlayed == singleton.getMaxGamePlay()) {
                if (!singleton.getAndroidSensor())
                    publish(pub_topic, "finish_game");
                if (singleton.getSoundSettings()) mWin.start();
                singleton.setStopTimer(true);
                singleton.setTimePlayed(getTimerText());
                singleton.setGamePlayed(1);
                sensorManager.unregisterListener(this);
                game = new Game(getActivity(), null);
                navController.navigate(R.id.action_gameFragment_to_endgameFragment);

            } else {
                String nextRound = new StringBuilder().append("Round ").append(gamePlayed + 1).toString();
                roundText.setText(nextRound);
                if (singleton.getSoundSettings()) mpPassRound.start();
            }
        });
    }

    /**
     * On Resume from Fragment connects to Broker, subscribes to given topic and send data start_game to raspBerry
     */
    @Override
    public void onResume() {
        super.onResume();
        if (!singleton.getAndroidSensor()) {
            connect(singleton.getBroker());
            subscribe(sub_topic);
            publish(pub_topic, "start_game");
        }
    }

    /**
     * On Pause disconnects from Broker if it was connected
     */
    @Override
    public void onPause() {
        super.onPause();
        if (!singleton.getAndroidSensor()) {
            disconnect();
        }
    }

    /**
     * Connects to broker
     *
     * @param broker Broker to connect to
     */
    private void connect(String broker) {
        try {
            clientId = MqttClient.generateClientId();
            client = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);
        } catch (MqttException me) {
            Log.e(TAG, "Reason: " + me.getReasonCode());
            Log.e(TAG, "Message: " + me.getMessage());
            Log.e(TAG, "localizedMsg: " + me);
            Log.e(TAG, "cause: " + me.getCause());
            Log.e(TAG, "exception: " + me);
        }
    }

    /**
     * Subscribes to a given topic
     *
     * @param topic Topic to subscribe to
     */
    private void subscribe(String topic) {
        try {
            client.subscribe(topic, qos, (topic1, msg) -> {
                if (!singleton.getStopTimer() && !singleton.getAndroidSensor()) {
                    String message = new String(msg.getPayload());
                    float x = -Float.parseFloat(message.split(",")[0].trim());
                    float y = Float.parseFloat(message.split(",")[1].trim());
                    movePlayer(x, y);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Publishes a message via MQTT (with fixed topic)
     *
     * @param topic topic to publish with
     * @param msg   message to publish with publish topic
     */
    private void publish(String topic, String msg) {
        MqttMessage message = new MqttMessage(msg.getBytes());
        message.setQos(qos);
        try {
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unsubscribe from default topic (please unsubscribe from further
     * topics prior to calling this function)
     */
    private void disconnect() {
        try {
            client.unsubscribe(sub_topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        try {
            client.disconnect();
        } catch (MqttException me) {
            Log.e(TAG, me.getMessage());
        }
    }
}