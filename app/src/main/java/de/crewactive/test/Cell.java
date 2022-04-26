package de.crewactive.test;

/**
 * Class that represent one Cell from Labyrinth board
 */
public class Cell {
    boolean
            topWall = true,
            leftWall = true,
            bottomWall = true,
            rightWall = true,
            visited = false;

    int col, row;

    /**
     * Cell Constructor
     *
     * @param col column number of Cell
     * @param row row number of Cell
     */
    public Cell(int col, int row) {
        this.row = row;
        this.col = col;
    }
}
