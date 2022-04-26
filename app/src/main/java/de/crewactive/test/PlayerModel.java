package de.crewactive.test;

/**
 * Player Class Model
 */
public class PlayerModel implements Comparable<PlayerModel> {

    private int id;                 // Id of player
    private String name, time;      // name, time of Player
    private final int duration;     // duration in seconds of Player

    /**
     * Constructor of Player
     *
     * @param id   Id of player that will be used in db
     * @param name name of player
     * @param time time played
     */
    public PlayerModel(int id, String name, String time) {
        this.id = id;
        this.name = name;
        this.time = time;
        this.duration = calculateDuration(time);
    }

    /**
     * prints all props of player Object
     *
     * @return string
     */
    @Override
    public String toString() {
        return "PlayerModel{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", time='" + time + '\'' +
                ", duration=" + duration +
                '}';
    }

    /**
     * getter of duration
     *
     * @return duration of the player
     */
    public int getDuration() {
        return duration;
    }

    /**
     * getter of Id
     *
     * @return id of player
     */
    public int getId() {
        return id;
    }

    /**
     * setter for id
     *
     * @param id id of player
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * getter of name
     *
     * @return name of player
     */
    public String getName() {
        return name;
    }

    /**
     * setter of name
     *
     * @param name name of player
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter of time
     *
     * @return time played
     */
    public String getTime() {
        return time;
    }

    /**
     * setter for time
     *
     * @param time time played
     */
    public void setTime(String time) {
        this.time = time;
    }

    /**
     * Extracts duration in seconds from string
     *
     * @param time played time
     */
    private int calculateDuration(String time) {
        String[] split = time.split(":");
        int hours = Integer.parseInt(split[0].trim());
        int minutes = Integer.parseInt(split[1].trim());
        int seconds = Integer.parseInt(split[2].trim());
        return hours * 60 * 60 + minutes * 60 + seconds;
    }

    /**
     * Compares two players on value duration
     *
     * @param o other player to be compared
     */
    @Override
    public int compareTo(PlayerModel o) {
        return this.getDuration() - o.getDuration();
    }
}
