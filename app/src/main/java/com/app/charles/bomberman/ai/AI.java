package com.app.charles.bomberman.ai;

import android.util.Log;

import com.app.charles.bomberman.game.Bomb;
import com.app.charles.bomberman.game.Bot;
import com.app.charles.bomberman.game.Grid;
import com.app.charles.bomberman.game.Player;
import com.app.charles.bomberman.java.Direction;

import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by Charles on 20-Feb-17.
 */

public class AI {

    private int columns,
            rows;

    private static final int NONE = 10,
            LEFT = 11,
            TOP = 12,
            RIGHT = 13,
            BOTTOM = 14;

    private static final int MOVE_LEFT_OR_TOP = -1,
            MOVE_RIGHT_OR_BOTTOM = 1,
            NO_MOVE = 0;

    private int freeCases[][],
            unsafeCases[][],
            playersPosition[][];

    public AI(int columns, int rows) {
        this.columns = columns;
        this.rows = rows;

        freeCases = new int[rows][columns];
        unsafeCases = new int[rows][columns];
        playersPosition = new int[rows][columns];
    }

    public synchronized void whereIGo(Bot bot) {
        int x = bot.getRoundX(),
                y = bot.getRoundY();

        if (unsafeCases[y][x] == 1) {
            //if (bot.getId() == 2) Log.i(TAG, "whereIGo: 1");
            bot.getDirection().setDirection(flee(x, y));
        } else if (freeCases[y][x] == 3 || someoneNear(x, y)) {
            //if (bot.getId() == 2) Log.i(TAG, "whereIGo: 2");
            bot.getDirection().setPoseBomb(true);
        } else {
            //if (bot.getId() == 2) Log.i(TAG, "whereIGo: 3");
            bot.getDirection().setDirection(attack(x, y));
        }
    }

    private boolean someoneNear(int x, int y) {
        int[][] t = updatePlayersPositionForSpecificPlayer(x, y);
        return //(y > 0 && t[y - 1][x] == 9) ||
                //(x > 0 && t[y][x - 1] == 9) ||
                (t[y][x] == 9);// ||
                //(y < playersPosition.length - 1 && t[y + 1][x] == 9) ||
                //(x < playersPosition[0].length - 1 && t[y][x + 1] == 9);
    }

    private boolean blocNearby(int x, int y) {
        boolean blocNearby = false;

        if (x > 0)
            blocNearby = freeCases[y][x - 1] == 2;
        if (!blocNearby && x < columns - 1)
            blocNearby = freeCases[y][x + 1] == 2;
        if (!blocNearby && y > 0)
            blocNearby = freeCases[y - 1][x] == 2;
        if (!blocNearby && y < rows - 1)
            blocNearby = freeCases[y + 1][x] == 2;

        return blocNearby;
    }

    private boolean safePlaceNear(int x, int y) {
        int unsafeCasesCopy[][] = unsafeCases.clone();

        return false;
    }

    private int attack(int x, int y) {
        int direction = directionToClosestValue(x, y, updatePlayersPositionForSpecificPlayer(x, y), 9, NONE, 1, 6);

        if (direction == Direction.STOP) {
            direction = directionToClosestValue(x, y, copy(freeCases), 3, NONE, 1, 6);

            if (direction == Direction.STOP) {
                direction = directionToClosestEnnemy(x, y);

                switch (direction) {
                    case Direction.LEFT:
                        if (x > 0 && unsafeCases[y][x - 1] == 1)
                            direction = Direction.STOP;
                        break;
                    case Direction.TOP:
                        if (y > 0 && unsafeCases[y - 1][x] == 1)
                            direction = Direction.STOP;
                        break;
                    case Direction.RIGHT:
                        if (x < unsafeCases[0].length - 1 && unsafeCases[y][x + 1] == 1)
                            direction = Direction.STOP;
                        break;
                    case Direction.BOTTOM:
                        if (y < unsafeCases.length - 1 && unsafeCases[y + 1][x] == 1)
                            direction = Direction.STOP;
                        break;
                }
            }
        }

        return direction;
    }

    private int directionToClosestValue(int x, int y, int tab[][], int value, int excludedSide, int length, int maxDepth) {
        //Log.i(TAG, "directionToClosestValue: " + length);
        if (((value == 3 || value == 9) && unsafeCases[y][x] == 1) || length > maxDepth)
            return Integer.MAX_VALUE;
        else if (tab[y][x] != value) {
            int left = Integer.MAX_VALUE,
                    top = Integer.MAX_VALUE,
                    right = Integer.MAX_VALUE,
                    bottom = Integer.MAX_VALUE;

            if (excludedSide != LEFT && x < columns - 1 && (freeCases[y][x + 1] == 0 || freeCases[y][x + 1] == 3))
                right = directionToClosestValue(x + 1,
                        y,
                        tab,
                        value,
                        RIGHT,
                        length + 1,
                        maxDepth);
            if (excludedSide != TOP && y < rows - 1 && (freeCases[y + 1][x] == 0 || freeCases[y + 1][x] == 3))
                bottom = directionToClosestValue(x,
                        y + 1,
                        tab,
                        value,
                        BOTTOM,
                        length + 1,
                        maxDepth);
            if (excludedSide != RIGHT && x > 0 && (freeCases[y][x - 1] == 0 || freeCases[y][x - 1] == 3))
                left = directionToClosestValue(x - 1,
                        y,
                        tab,
                        value,
                        LEFT,
                        length + 1,
                        maxDepth);
            if (excludedSide != BOTTOM && y > 0 && (freeCases[y - 1][x] == 0 || freeCases[y - 1][x] == 3))
                top = directionToClosestValue(x,
                        y - 1,
                        tab,
                        value,
                        TOP,
                        length + 1,
                        maxDepth);


            int min = Math.min(left
                    , Math.min(top
                            , Math.min(right, bottom)));
            if (length == 1) {
                if (min == Integer.MAX_VALUE)
                    return Direction.STOP;
                else if (min == left)
                    return Direction.LEFT;
                else if (min == top)
                    return Direction.TOP;
                else if (min == right)
                    return Direction.RIGHT;
                else
                    return Direction.BOTTOM;
            } else
                return min;
        } else
            return length;
    }

    private int flee(int x, int y) {
        return (directionToClosestValue(x, y, copy(unsafeCases), 0, NONE, 1, Integer.MAX_VALUE));
    }

    public void updateBlocksCases(int grid[][], ArrayList<Bomb> bombs, ArrayList<Player> players) {
        for (int i = 0; i < grid.length; i++)
            for (int j = 0; j < grid[i].length; j++) {
                unsafeCases[i][j] = 0;
                playersPosition[i][j] = 0;
                if (grid[i][j] == Grid.CASE_EMPTY || grid[i][j] == Grid.CASE_PU_FASTER || grid[i][j] == Grid.CASE_PU_ADD_BOMB)
                    freeCases[i][j] = 0;
                else if (grid[i][j] == Grid.CASE_BLOC)
                    freeCases[i][j] = 2;
                else
                    freeCases[i][j] = 1;
            }
        updateUnsafeCases(bombs);
        updateBlocNearbyCases();
        updatePlayersPosition(players);
    }

    private void updateBlocNearbyCases() {
        for (int i = 0; i < freeCases.length; i++)
            for (int j = 0; j < freeCases[i].length; j++)
                if (blocNearby(j, i) && freeCases[i][j] == 0)
                    freeCases[i][j] = 3;
    }

    private void updatePlayersPosition(ArrayList<Player> players) {
        for (Player player : players) {
            int y = player.getRoundY(),
                    x = player.getRoundX();
            if (player.isPlayerAlive())
                playersPosition[y][x] = playersPosition[player.getRoundY()][player.getRoundX()] + 1;
        }
    }

    private int[][] updatePlayersPositionForSpecificPlayer(int x, int y) {
        int[][] tab = copy(playersPosition);
        tab[y][x] = tab[y][x] - 1;

        // toutes les cases où il y a au moins un joueur
        for (int i = 0; i < tab.length; i++)
            for (int j = 0; j < tab[i].length; j++)
                if (tab[i][j] >= 1)
                    tab[i][j] = 9;

        return tab;
    }

    public void updateUnsafeCases(ArrayList<Bomb> bombs) {
        for (Bomb bomb : bombs) {
            int x = bomb.getXGrid(),
                    y = bomb.getYGrid(),
                    power = bomb.getPower();

            unsafeCases[y][x] = 1;

            spreadExplosion(x + MOVE_LEFT_OR_TOP,
                    y,
                    MOVE_LEFT_OR_TOP,
                    NO_MOVE,
                    power);
            spreadExplosion(x,
                    y + MOVE_LEFT_OR_TOP,
                    NO_MOVE,
                    MOVE_LEFT_OR_TOP,
                    power);
            spreadExplosion(x + MOVE_RIGHT_OR_BOTTOM,
                    y,
                    MOVE_RIGHT_OR_BOTTOM,
                    NO_MOVE,
                    power);
            spreadExplosion(x,
                    y + MOVE_RIGHT_OR_BOTTOM,
                    NO_MOVE,
                    MOVE_RIGHT_OR_BOTTOM,
                    power);
        }
    }

    public void spreadExplosion(int xGrid, int yGrid, int moveX, int moveY, int depth) {
        if (depth > 0 && xGrid >= 0 && xGrid < columns && yGrid >= 0 && yGrid < rows)
            switch (freeCases[yGrid][xGrid]) {
                case 0:
                    unsafeCases[yGrid][xGrid] = 1;

                    spreadExplosion(xGrid + moveX, yGrid + moveY, moveX, moveY, depth - 1);
                    break;
            }
    }

    public int directionToClosestEnnemy(int x, int y) {
        int[][] tab = updatePlayersPositionForSpecificPlayer(x, y);

        int xClosest = tab[0].length - 1,
                yClosest = tab.length - 1;

        for (int i = 0; i < tab.length; i++)
            for (int j = 0; j < tab[i].length; j++) {
                if (tab[i][j] == 9) {
                    int diffY = i - y,
                            diffX = j - x;

                    if (Math.abs(diffY) + Math.abs(diffX)
                            < Math.abs(yClosest) + Math.abs(xClosest)) {
                        yClosest = diffY;
                        xClosest = diffX;
                    }
                }
            }

        if (x % 2 == 0 && y % 2 != 0)
            return yClosest < 0 ? Direction.TOP : Direction.BOTTOM;
        else if (x % 2 != 0 && y % 2 == 0)
            return xClosest < 0 ? Direction.LEFT : Direction.RIGHT;
        else
            return xClosest < 0 ? Direction.LEFT : Direction.RIGHT;
    }

    public void afficher(int[][] tab) {
        String s = "";
        for (int i = 0; i < tab.length; i++) {
            for (int j = 0; j < tab[i].length; j++)
                s += tab[i][j];
            s += "\n";
        }
        Log.i(TAG, " afficher: playersPosition \n" + s);
    }

    public int[][] copy(int[][] original) {
        int[][] copy = new int[original.length][original[0].length];

        for (int i = 0; i < playersPosition.length; i++)
            for (int j = 0; j < playersPosition[i].length; j++)
                copy[i][j] = original[i][j];

        return copy;
    }
}
