package de.crewactive.test;

/**
 * Singleton pattern to manage all key-global variables
 */
public class Singleton {

    // Settings
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String IP_ADDRESS = "ipAddress";
    public static final String SOUND_SWITCH = "soundSwitch";
    public static final String CONSOLE_SWITCH = "consoleSwitch";

    private boolean androidSensor = true;               // true --> use Android Sensor, false --> use Raspberry Sensor
    private boolean soundSettings = true;               // true --> Sound on, false --> Sound off

    // Sound volume
    public static final float VOLUME = 0.1f;            // Volume of Sound

    // Raspberry Broker
    private String broker = "";                         // Broker Ip

    // Timer status
    private boolean stopTimer = false;                  // stopTimer = true --> stops timer in game, stopTimer = false --> don't stop timer

    // Game Settings
    private final int MAX_GAME_PLAY = 2;                // Max rounds to be played in one gameplay

    private int gamePlayed = 0;                         // rounds played
    private String timePlayed = null;                   // time played all max. rounds


    public static Singleton instance;

    /**
     * Private Constructor for Singleton
     */
    private Singleton() {

    }

    /**
     * get instance of Singleton
     *
     * @return Singleton instance
     */
    public static Singleton getInstance() {
        if (instance == null) {
            instance = new Singleton();
        }
        return instance;
    }

    /**
     * getter of sound setting
     *
     * @return boolean
     */
    public boolean getSoundSettings() {
        return soundSettings;
    }

    /**
     * setter for sound setting
     *
     * @param soundSettings value of sound setting, true or false
     */
    public void setSoundSettings(boolean soundSettings) {
        this.soundSettings = soundSettings;
    }

    /**
     * getter of Rounds played
     *
     * @return number of rounds played
     */
    public int getGamePlayed() {
        return gamePlayed;
    }

    /**
     * setter for rounds played
     *
     * @param gamePlayed number of rounds played
     */
    public void setGamePlayed(int gamePlayed) {
        this.gamePlayed = gamePlayed;
    }

    /**
     * getter for max. rounds
     *
     * @return number of max. rounds
     */
    public int getMaxGamePlay() {
        return MAX_GAME_PLAY;
    }

    /**
     * getter of android sensor variable
     *
     * @return value of android sensor variable
     */
    public boolean getAndroidSensor() {
        return androidSensor;
    }

    /**
     * setter for android sensor variable
     *
     * @param androidSensor value of android sensor
     */
    public void setAndroidSensor(boolean androidSensor) {
        this.androidSensor = androidSensor;
    }

    /**
     * getter of stopTimer
     *
     * @return value of stopTimer
     */
    public boolean getStopTimer() {
        return stopTimer;
    }

    /**
     * setter for stopTimer variable
     *
     * @param stopTimer value of stopTimer
     */
    public void setStopTimer(boolean stopTimer) {
        this.stopTimer = stopTimer;
    }

    /**
     * getter of timePlayed
     *
     * @return time that one user has played all max rounds
     */
    public String getTimePlayed() {
        return timePlayed;
    }

    /**
     * setter for time played
     *
     * @param timePlayed time that one user has played all max rounds
     */
    public void setTimePlayed(String timePlayed) {
        this.timePlayed = timePlayed;
    }

    /**
     * getter of broker
     *
     * @return broker sting url
     */
    public String getBroker() {
        return broker;
    }

    /**
     * setter for broker
     *
     * @param broker ip of broker
     */
    public void setBroker(String broker) {
        this.broker = "tcp://" + broker + ":1883";
    }
}
