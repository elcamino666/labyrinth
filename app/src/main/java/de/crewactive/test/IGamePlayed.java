package de.crewactive.test;

/**
 * Interface to have a connection from Class Game to GameFragment
 */
public interface IGamePlayed {
    /**
     * Updates number of Rounds that have been played
     *
     * @param gamePlayed
     */
    void update(int gamePlayed);
}
