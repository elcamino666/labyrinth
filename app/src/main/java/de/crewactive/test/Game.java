package de.crewactive.test;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Random;
import java.util.Stack;

/**
 * Game Class to create bord for labyrinth and handle calculations to move player
 * @link https://www.youtube.com/watch?v=I9lTBTAk5MU&t=158s
 */
public class Game extends View {

    private IGamePlayed listener;                       // Listener to send data to GameFragment

    /**
     * Give the player directions to move to
     */
    public enum Direction {                             // Directions to move player
        UP, DOWN, LEFT, RIGHT
    }

    private Singleton singleton;                        // Singleton variables
    private Cell[][] cells;                             // Cells of Board
    private int COLS, ROWS;                             // Rows, Columns of board
    private static final float WALL_THICKNESS = 4;      // Wall thickness

    private Cell player, exit;                          // Player Cell, Exit Cell

    private float cellSize, hMargin, vMargin;           // Cell size, horizontal Margin, Vertical Margin
    private Paint wallPaint, playerPaint, exitPaint;    // Paint Objects

    private Random random;

    /**
     * Callback to set listener, that makes able that we send data from this class to GameFragment
     *
     * @param callBack
     */
    public void setCallBack(IGamePlayed callBack) {
        listener = callBack;
    }

    /**
     * Constructor von class Game
     *
     * @param context Context obj
     * @param attrs   attributes
     */
    public Game(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        singleton = Singleton.getInstance();
        wallPaint = new Paint();
        wallPaint.setColor(Color.BLACK);
        wallPaint.setStrokeWidth(WALL_THICKNESS);

        playerPaint = new Paint();
        playerPaint.setColor(Color.RED);
        playerPaint.setStrokeWidth(1);
        playerPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        exitPaint = new Paint();
        exitPaint.setColor(Color.BLACK);

        random = new Random();

        createMaze();
    }

    /**
     * Calculate Neighbours for one cell object
     *
     * @param cell cell object
     */
    private Cell getNeighbour(Cell cell) {
        ArrayList<Cell> neighbours = new ArrayList<>();

        // check left neighbour
        if (cell.col > 0) {
            if (!cells[cell.col - 1][cell.row].visited) {
                neighbours.add(cells[cell.col - 1][cell.row]);
            }
        }

        // check right neighbour
        if (cell.col < COLS - 1) {
            if (!cells[cell.col + 1][cell.row].visited) {
                neighbours.add(cells[cell.col + 1][cell.row]);
            }
        }

        // check top neighbour
        if (cell.row > 0) {
            if (!cells[cell.col][cell.row - 1].visited) {
                neighbours.add(cells[cell.col][cell.row - 1]);
            }
        }

        // check bottom neighbour
        if (cell.row < ROWS - 1) {
            if (!cells[cell.col][cell.row + 1].visited) {
                neighbours.add(cells[cell.col][cell.row + 1]);
            }
        }

        if (neighbours.size() > 0) {
            // Calculate Random Index from current neighbors
            int index = random.nextInt(neighbours.size());
            return neighbours.get(index);
        }
        return null;
    }

    /**
     * Remove Walls between two cells
     *
     * @param current current cell
     * @param next    next cell
     */
    private void removeWall(Cell current, Cell next) {

        // check if is under
        if (current.col == next.col && current.row == next.row + 1) {
            current.topWall = false;
            next.bottomWall = false;
        }

        // check if is top
        if (current.col == next.col && current.row == next.row - 1) {
            current.bottomWall = false;
            next.topWall = false;
        }

        // check if is left
        if (current.row == next.row && current.col == next.col - 1) {
            current.rightWall = false;
            next.leftWall = false;
        }

        // check if is right
        if (current.row == next.row && current.col == next.col + 1) {
            current.leftWall = false;
            next.rightWall = false;
        }
    }

    /**
     * Create the Table of Labyrinth
     */
    private void createMaze() {
        ROWS = COLS = 5 * (singleton.getGamePlayed() + 1);

        Stack<Cell> stack = new Stack<>();
        Cell current, next;

        cells = new Cell[COLS][ROWS];
        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {
                cells[x][y] = new Cell(x, y);
            }
        }

        player = cells[0][0];
        exit = cells[COLS - 1][ROWS - 1];
        current = cells[0][0];
        current.visited = true;
        do {
            next = getNeighbour(current);
            if (next != null) {
                // Neighbour found
                removeWall(current, next);
                stack.push(current);
                current = next;
                current.visited = true;
            } else {
                // Neighbour not found
                current = stack.pop();
            }
        } while (!stack.empty());
    }

    /**
     * Draws the Table of Maze, player and exit point on Canvas
     *
     * @param canvas to draw the table in
     */
    @Override
    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight();

        if (width / height < COLS / ROWS) {
            cellSize = width / (COLS + 1);
        } else {
            cellSize = height / (ROWS + 1);
        }

        hMargin = (width - COLS * cellSize) / 2;
        vMargin = (height - ROWS * cellSize) / 2;

        canvas.translate(hMargin, vMargin);

        for (int x = 0; x < COLS; x++) {
            for (int y = 0; y < ROWS; y++) {

                if (cells[x][y].topWall) {

                    canvas.drawLine(
                            x * cellSize,
                            y * cellSize,
                            (x + 1) * cellSize,
                            (y) * cellSize,
                            wallPaint
                    );
                }
                if (cells[x][y].bottomWall) {
                    canvas.drawLine(
                            x * cellSize,
                            (y + 1) * cellSize,
                            (x + 1) * cellSize,
                            (y + 1) * cellSize,
                            wallPaint
                    );
                }
                if (cells[x][y].leftWall) {
                    canvas.drawLine(
                            (x) * cellSize,
                            (y) * cellSize,
                            (x) * cellSize,
                            (y + 1) * cellSize,
                            wallPaint
                    );
                }
                if (cells[x][y].rightWall) {
                    canvas.drawLine(
                            (x + 1) * cellSize,
                            (y) * cellSize,
                            (x + 1) * cellSize,
                            (y + 1) * cellSize,
                            wallPaint
                    );
                }
            }
        }
        float margin = cellSize / ROWS;
        playerPaint.setShader(new RadialGradient(
                player.col * cellSize + cellSize / 1.6f,
                player.row * cellSize + cellSize / 3,
                cellSize / 2 - margin, Color.WHITE, Color.BLUE, Shader.TileMode.MIRROR));
        canvas.drawCircle(player.col * cellSize + cellSize / 2, player.row * cellSize + cellSize / 2, cellSize / 2 - margin, playerPaint);
        canvas.drawCircle(
                exit.col * cellSize + cellSize / 2,
                exit.row * cellSize + cellSize / 2,
                cellSize / 2 - margin / 2,
                exitPaint);
        exitPaint.setShader(new RadialGradient(
                exit.col * cellSize + cellSize / 2,
                exit.row * cellSize + cellSize / 2,
                cellSize - margin, Color.BLACK, Color.GRAY, Shader.TileMode.MIRROR));

    }

    /**
     * Move the player on given direction
     *
     * @param direction to move player
     */
    public void movePlayer(Direction direction) {
        switch (direction) {
            case UP:
                if (!player.topWall) {
                    player = cells[player.col][player.row - 1];
                }
                break;
            case DOWN:
                if (!player.bottomWall) {
                    player = cells[player.col][player.row + 1];
                }
                break;
            case LEFT:
                if (!player.leftWall) {
                    player = cells[player.col - 1][player.row];
                }
                break;
            case RIGHT:
                if (!player.rightWall) {
                    player = cells[player.col + 1][player.row];
                }
                break;
        }
        checkExit();
        invalidate();
    }

    /**
     * Checks if player is on Exit Point
     * and if it is True creates new game until Max. game allowed
     * and notify GameFragment
     */
    private void checkExit() {
        if (player == exit) {
            singleton.setGamePlayed(singleton.getGamePlayed() + 1);
            listener.update(singleton.getGamePlayed());
            createMaze();
        }
    }
}

