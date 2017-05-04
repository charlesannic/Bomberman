package com.app.charles.bomberman.ai;

import android.util.Log;

import com.app.charles.bomberman.game.Bomb;
import com.app.charles.bomberman.game.Bot;
import com.app.charles.bomberman.game.Grid;
import com.app.charles.bomberman.game.Player;
import com.app.charles.bomberman.java.Direction;

import java.util.ArrayList;
import java.util.Random;

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

        //Log.i(TAG, "whereIGo: ");

        if (unsafeCases[y][x] == 1) {
            //Log.i(TAG, "whereIGo: flee");
            bot.getDirection().setDirection(flee(x, y));
        }
        /*else {
            if(bot.getBombsCapacity() < bot.getBombs().size()
                    && blocNearby(x, y)
                    && safePlaceNear(x, y))
                bot.getDirection().setPoseBomb(true);
            else
            bot.getDirection().setDirection(attack(x, y));

        }*/
        else if (freeCases[y][x] == 3) {
            //Log.i(TAG, "whereIGo: blockNearby");
            bot.getDirection().setPoseBomb(true);
        } else {
            int at = attack(x, y);
            //Log.i(TAG, "whereIGo: attack !!!!!" + at);
            bot.getDirection().setDirection(at);
        }

        //afficher();
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

        //Log.i(TAG, "blocNearby: " + blocNearby + " " + x + " " + y);
        return blocNearby;
    }

    private boolean safePlaceNear(int x, int y) {
        int unsafeCasesCopy[][] = unsafeCases.clone();

        return false;
    }

    private int attack(int x, int y) {
        int direction = directionToClosestValue(x, y, freeCases.clone(), 3, NONE, 1, 6);
        //Log.i(TAG, "attack: abcde " + direction);
        /*if (direction == Direction.STOP) {
            int tab[][] = playersPosition.clone();
            tab[y][x] = 0;
            direction = directionToClosestValue(x, y, playersPosition.clone(), 5, NONE, 1, 10);
            Log.i(TAG, "attack: 1 " + direction);
            if(direction == Direction.STOP) {
                Log.i(TAG, "attack: 2 ");
                Random randomGenerator = new Random();
                direction = randomGenerator.nextInt(4) + 1;
            }
        }*/

        return direction;
    }

    private int directionToClosestValue(int x, int y, int tab[][], int value, int excludedSide, int length, int maxDepth) {
        //Log.i(TAG, "directionToClosestValue: " + length);
        if ((value == 3 && unsafeCases[y][x] == 1) || length > maxDepth)
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
                //Log.i(TAG, "directionToClosestValue: " + length + " " + left + " " + top + " " + right + " " + left);
                if(min == Integer.MAX_VALUE)
                    return Direction.STOP;
                else if (min == left)
                    return Direction.LEFT;
                else if (min == top)
                    return Direction.TOP;
                else if (min == right)
                    return Direction.RIGHT;
                else
                    return Direction.B0TT0M;
            } else
                return min;
        } else
            return length;
    }

    /*private int directionToCase(int x, int y, int xDestination, int yDestination, int excludedSide, int length) {

    }*/

    private int flee(int x, int y) {
        return (directionToClosestValue(x, y, unsafeCases.clone(), 0, NONE, 1, Integer.MAX_VALUE));
    }

    public void updateBlocksCases(int grid[][], ArrayList<Bomb> bombs, ArrayList<Player> players) {
        for (int i = 0; i < grid.length; i++)
            for (int j = 0; j < grid[i].length; j++) {
                unsafeCases[i][j] = 0;
                playersPosition[i][j] = 0;
                if (grid[i][j] == Grid.CASE_EMPTY)
                    freeCases[i][j] = 0;
                else if (grid[i][j] == Grid.CASE_BLOC)
                    freeCases[i][j] = 2;
                else
                    freeCases[i][j] = 1;
            }
        updateUnsafeCases(bombs);
        updateBlocNearbyCases();
        updatePlayersPosition(players);
        //afficher("updateBlocksCases");
    }

    private void updateBlocNearbyCases() {
        for (int i = 0; i < freeCases.length; i++)
            for (int j = 0; j < freeCases[i].length; j++) {
                if (blocNearby(j, i) && freeCases[i][j] == 0)
                    freeCases[i][j] = 3;
            }
    }

    private void updatePlayersPosition(ArrayList<Player> players) {
        for (Player player : players)
            playersPosition[player.getRoundY()][player.getRoundX()] = 5;
    }

    public void afficher(String string) {
        String s = "";
        for (int i = 0; i < freeCases.length; i++) {
            for (int j = 0; j < freeCases[i].length; j++)
                s += freeCases[i][j];
            s += "\n";
        }
        Log.i(TAG, string + " afficher: freeCases\n" + s);

        /*s = "";
        for (int i = 0; i < unsafeCases.length; i++) {
            for (int j = 0; j < unsafeCases[i].length; j++)
                s += unsafeCases[i][j];
            s += "\n";
        }
        Log.i(TAG, "afficher: unsafeCases\n" + s);*/
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


}
