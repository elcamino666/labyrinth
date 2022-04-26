package de.crewactive.test;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Start Fragment class, first screen
 */
public class StartFragment extends Fragment {

    private Singleton singleton;                // singleton data
    private NavController navController;        // navigator controller

    private EditText ipEditTxt;                 // input for raspberry ip
    private Button checkIp;                     // button to check the connection to raspBerry
    private SwitchMaterial consoleSwitch;       // switch for raspBerry and android sensor setting
    private SwitchMaterial soundSwitch;         // switch for sound setting
    private Button play;                        // button to start the game
    private TextView records;                   // text button to see the top records list

    private View loader;                        // loader view

    private MqttClient client;
    private final MemoryPersistence persistence = new MemoryPersistence();
    SharedPreferences sharedPreferences;

    /**
     * Required empty public constructor
     */
    public StartFragment() {

    }

    /**
     * on Create method of Fragment
     *
     * @param savedInstanceState saved Instance State
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * When Fragment view is created
     *
     * @param inflater           inflater
     * @param container          container
     * @param savedInstanceState saved Instance State
     * @return inflater view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_start, container, false);
    }

    /**
     * After fragment view is created, initialize variables and set on click listeners and load saved settings
     *
     * @param view               view
     * @param savedInstanceState saved Instance State
     */
    @Override
    public void onViewCreated(@NonNull @org.jetbrains.annotations.NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        singleton = Singleton.getInstance();
        singleton.setGamePlayed(0);
        sharedPreferences = requireActivity().getSharedPreferences(Singleton.SHARED_PREFS, Context.MODE_PRIVATE);
        navController = Navigation.findNavController(view);

        loader = view.findViewById(R.id.loader);
        play = view.findViewById(R.id.startGameButton);
        ipEditTxt = view.findViewById(R.id.editTextIpAddress);
        records = view.findViewById(R.id.records);
        consoleSwitch = view.findViewById(R.id.switch1);
        soundSwitch = view.findViewById(R.id.soundSwitchSettings);
        checkIp = view.findViewById(R.id.checkMQTT);

        // On Play button clicked
        play.setOnClickListener(v -> {
            singleton.setStopTimer(false);
            saveRaspberrySettings();
            loadSettings();
            if (singleton.getAndroidSensor()) {
                // use Android Sensor
                navController.navigate(R.id.action_startFragment_to_gameFragment);
            } else {
                // use Raspberry Sensor
                loader.setVisibility(View.VISIBLE);
                CheckConnection checkConnection = new CheckConnection("play");
                new Thread(checkConnection).start();
            }
        });

        records.setOnClickListener(v -> navController.navigate(R.id.action_startFragment_to_endgameFragment));

        consoleSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            singleton.setAndroidSensor(!isChecked);
            if (isChecked) {
                ipEditTxt.setVisibility(View.VISIBLE);
                checkIp.setVisibility(View.VISIBLE);
            } else {
                ipEditTxt.setVisibility(View.GONE);
                checkIp.setVisibility(View.GONE);
            }
        });

        soundSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            singleton.setSoundSettings(isChecked);
            saveSoundSettings();
        });

        checkIp.setOnClickListener(v -> {
            loader.setVisibility(View.VISIBLE);
            singleton.setBroker(ipEditTxt.getText().toString());
            CheckConnection checkConnection = new CheckConnection("check");
            new Thread(checkConnection).start();
        });

        loadSettings();
    }

    /**
     * Saves Ip from Raspberry in SharedPreferences
     */
    private void saveRaspberrySettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(Singleton.IP_ADDRESS, ipEditTxt.getText().toString());
        editor.putBoolean(Singleton.CONSOLE_SWITCH, !singleton.getAndroidSensor());
        editor.apply();
    }

    /**
     * Saves Sound settings in SharedPreferences
     */
    private void saveSoundSettings() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Singleton.SOUND_SWITCH, singleton.getSoundSettings());
        editor.apply();
    }

    /**
     * Loads data from SharedPreferences
     */
    private void loadSettings() {
        String broker = sharedPreferences.getString(Singleton.IP_ADDRESS, "");
        boolean soundSettings = sharedPreferences.getBoolean(Singleton.SOUND_SWITCH, true);
        boolean consoleSettings = sharedPreferences.getBoolean(Singleton.CONSOLE_SWITCH, false);
        singleton.setBroker(broker);
        singleton.setSoundSettings(soundSettings);
        singleton.setAndroidSensor(!consoleSettings);
        soundSwitch.setChecked(soundSettings);
        consoleSwitch.setChecked(consoleSettings);
        ipEditTxt.setText(broker);
    }

    /**
     * Checks connection to the broker
     *
     * @param broker broker to be tested from connection
     */
    private boolean connection(String broker) {
        try {
            String clientId = MqttClient.generateClientId();
            client = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            client.connect(connOpts);
            return true;
        } catch (MqttException me) {
            return false;
        }
    }

    /**
     * Disconnect from Broker if connection exist
     */
    private void disconnect() {
        try {
            client.disconnect();
        } catch (MqttException me) {
            Log.e("Disconnecting****", me.getMessage());
        }
    }

    /**
     * Class to check connection to Broker in separate thread from Main thread
     */
    class CheckConnection extends Thread {

        private final String where;

        /**
         * Constructor of class CheckConnection
         *
         * @param where string to know where this class is been used
         */
        public CheckConnection(String where) {
            this.where = where;
        }

        /**
         * Checks the connection to raspBerry in separated thread, than on ui thread makes the changes needed to give a feedback to the user back
         */
        @Override
        public void run() {
            boolean status = connection(singleton.getBroker());
            requireActivity().runOnUiThread(() -> {
                if (status) {
                    checkIp.setBackgroundColor(getResources().getColor(R.color.positive));
                    if (where.equals("play")) {
                        Toast.makeText(getActivity(), "Go", Toast.LENGTH_SHORT).show();
                        navController.navigate(R.id.action_startFragment_to_gameFragment);
                    } else {
                        Toast.makeText(getActivity(), "Connected", Toast.LENGTH_SHORT).show();
                        disconnect();
                    }
                } else {
                    checkIp.setBackgroundColor(getResources().getColor(R.color.negative));
                    Toast.makeText(getActivity(), "Cant find Raspberry", Toast.LENGTH_SHORT).show();
                }
                loader.setVisibility(View.GONE);
            });
        }
    }
}