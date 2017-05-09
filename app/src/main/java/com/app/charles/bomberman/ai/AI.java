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

    private ArrayList<Player> players;

    private static final int NONE = 10,
            LEFT = 11,
            TOP = 12,
            RIGHT = 13,
            BOTTOM = 14;

    private static final int CASE_POSE_BOMB = 9;

    private static final int MOVE_LEFT_OR_TOP = -1,
            MOVE_RIGHT_OR_BOTTOM = 1,
            NO_MOVE = 0;

    private int freeCases[][],
            unsafeCases[][];

    public AI(int columns, int rows, ArrayList<Player> players) {
        this.columns = columns;
        this.rows = rows;
        this.players = players;

        freeCases = new int[rows][columns];
        unsafeCases = new int[rows][columns];
    }

    public synchronized void whereIGo(Bot bot) {
        int x = bot.getRoundX(),
                y = bot.getRoundY();

        if (unsafeCases[y][x] == 1 || playersPositionForSpecificPlayer(bot)[y][x] == 9) {
            if (bot.getId() == 2) Log.i(TAG, "whereIGo: 1");
            bot.getDirection().setDirection(directionToClosestValue(bot, x, y, unsafeCasesForSpecificPlayer(bot), 0, NONE, 1, Integer.MAX_VALUE, false));
            return;
        } else if (someoneNear(bot)) {
            if (bot.getId() == 2) Log.i(TAG, "whereIGo: 2");
            bot.getDirection().setPoseBomb(true);
            return;
        }

        int direction = directionToClosestValue(bot, x, y, playersPositionForSpecificPlayer(bot), CASE_POSE_BOMB, NONE, 1, 6, true);
        //Log.i(TAG, "whereIGo: ---- " + direction);
        if (direction != Direction.STOP) {
            if (bot.getId() == 2) Log.i(TAG, "whereIGo: 3");
            bot.getDirection().setDirection(direction);
            return;
        } else if (freeCases[y][x] == CASE_POSE_BOMB) {
            if (bot.getId() == 2) Log.i(TAG, "whereIGo: 4");
            bot.getDirection().setPoseBomb(true);
            return;
        }

        direction = directionToClosestValue(bot, x, y, copy(freeCases), CASE_POSE_BOMB, NONE, 1, 6, true);
        if (direction != Direction.STOP) {
            if (bot.getId() == 2) Log.i(TAG, "whereIGo: 5");
            bot.getDirection().setDirection(direction);
        } else {
            if (bot.getId() == 2) Log.i(TAG, "whereIGo: 6");
            bot.getDirection().setDirection(directionToClosestEnnemy(bot));
        }
    }

    private boolean someoneNear(Player player) {
        int x = player.getRoundX(),
                y = player.getRoundY();
        int[][] t = playersPositionForSpecificPlayer(player);
        return (y > 0 && t[y - 1][x] == CASE_POSE_BOMB) ||
                (x > 0 && t[y][x - 1] == CASE_POSE_BOMB) ||
                (t[y][x] == CASE_POSE_BOMB) ||
                (y < t.length - 1 && t[y + 1][x] == CASE_POSE_BOMB) ||
                (x < t[0].length - 1 && t[y][x + 1] == CASE_POSE_BOMB);
    }

    private boolean blocNearby(int x, int y) {
        boolean blocNearby = false;

        if (x > 0)
            blocNearby = freeCases[y][x - 1] == Grid.CASE_BLOC;
        if (!blocNearby && x < columns - 1)
            blocNearby = freeCases[y][x + 1] == Grid.CASE_BLOC;
        if (!blocNearby && y > 0)
            blocNearby = freeCases[y - 1][x] == Grid.CASE_BLOC;
        if (!blocNearby && y < rows - 1)
            blocNearby = freeCases[y + 1][x] == Grid.CASE_BLOC;

        return blocNearby;
    }

    private boolean safePlaceNear(int x, int y) {
        int unsafeCasesCopy[][] = unsafeCases.clone();

        return false;
    }

    /*private int attack(int x, int y) {
        int direction = directionToClosestValue(x, y, playersPositionForSpecificPlayer(x, y), CASE_POSE_BOMB, NONE, 1, 6);

        if (direction == Direction.STOP) {
            direction = directionToClosestValue(x, y, copy(freeCases), CASE_POSE_BOMB, NONE, 1, 6);

            if (direction == Direction.STOP) {
                direction = directionToClosestEnnemy(x, y);


            }
        }

        return direction;
    }*/

    private int directionToClosestValue(Player player, int x, int y, int tab[][], int value, int excludedSide, int length, int maxDepth, boolean avoidUnsafeCases) {
        //if(player.getId() == 2 && length > maxDepth && x == 7 && y == 2)
          //  afficher(tab);
            //Log.i(TAG, "directionToClosestValue: " + length + " " + tab[y][x] + " " + value);

        if ((avoidUnsafeCases
                && unsafeCases[y][x] == 1
                || length > maxDepth)) {
            return Integer.MAX_VALUE;
        } else if (tab[y][x] == value) {
            return length;
        } else /*if (tab[y][x] != value) */ {
            int left = Integer.MAX_VALUE,
                    top = Integer.MAX_VALUE,
                    right = Integer.MAX_VALUE,
                    bottom = Integer.MAX_VALUE;

            if (excludedSide != LEFT && x < columns - 1 && (freeCases[y][x + 1] == 0
                    || freeCases[y][x + 1] == CASE_POSE_BOMB))
                right = directionToClosestValue(player,
                        x + 1,
                        y,
                        tab,
                        value,
                        RIGHT,
                        length + 1,
                        maxDepth,
                        avoidUnsafeCases);
            if (excludedSide != TOP && y < rows - 1 && (freeCases[y + 1][x] == 0 || freeCases[y + 1][x] == CASE_POSE_BOMB))
                bottom = directionToClosestValue(player,
                        x,
                        y + 1,
                        tab,
                        value,
                        BOTTOM,
                        length + 1,
                        maxDepth,
                        avoidUnsafeCases);
            if (excludedSide != RIGHT && x > 0 && (freeCases[y][x - 1] == 0 || freeCases[y][x - 1] == CASE_POSE_BOMB))
                left = directionToClosestValue(player,
                        x - 1,
                        y,
                        tab,
                        value,
                        LEFT,
                        length + 1,
                        maxDepth,
                        avoidUnsafeCases);
            if (excludedSide != BOTTOM && y > 0 && (freeCases[y - 1][x] == 0 || freeCases[y - 1][x] == CASE_POSE_BOMB))
                top = directionToClosestValue(player,
                        x,
                        y - 1,
                        tab,
                        value,
                        TOP,
                        length + 1,
                        maxDepth,
                        avoidUnsafeCases);


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
        }
    }

    public void updateBlocksCases(int grid[][], ArrayList<Bomb> bombs) {
        for (int i = 0; i < grid.length; i++)
            for (int j = 0; j < grid[i].length; j++) {
                unsafeCases[i][j] = Grid.CASE_EMPTY;
                if (grid[i][j] == Grid.CASE_EMPTY
                        || grid[i][j] == Grid.CASE_PU_FASTER
                        || grid[i][j] == Grid.CASE_PU_ADD_BOMB
                        || grid[i][j] == Grid.CASE_PU_POWER
                        || grid[i][j] == Grid.CASE_PU_P_BOMB
                        || grid[i][j] == Bomb.PLAYER_STILL_ON_BOMB)
                    freeCases[i][j] = Grid.CASE_EMPTY;
                else if (grid[i][j] == Grid.CASE_BLOC)
                    freeCases[i][j] = Grid.CASE_BLOC;
                else
                    freeCases[i][j] = Grid.CASE_WALL;
            }
        updateUnsafeCases(bombs);
        updateBlocNearbyCases();
    }

    private void updateBlocNearbyCases() {
        for (int i = 0; i < freeCases.length; i++)
            for (int j = 0; j < freeCases[i].length; j++)
                if (blocNearby(j, i) && freeCases[i][j] == 0)
                    freeCases[i][j] = CASE_POSE_BOMB;
    }

    private int[][] playersPositionForSpecificPlayer(Player player) {
        int[][] tab = new int[rows][columns];
        for (int i = 0; i < tab.length; i++)
            for (int j = 0; j < tab[i].length; j++)
                tab[i][j] = 0;

        // toutes les cases où il y a au moins un joueur adverse
        for (Player p : players)
            if (p.getId() != player.getId() && p.isPlayerAlive()) {
                tab[p.getRoundY()][p.getRoundX()] = CASE_POSE_BOMB;
            }

        return tab;
    }

    private int[][] unsafeCasesForSpecificPlayer(Player player) {
        int[][] tab = copy(unsafeCases);
        // toutes les cases où il y a au moins un joueur adverse
        for (Player p : players)
            if (p.getId() != player.getId() && p.isPlayerAlive()) {
                tab[p.getRoundY()][p.getRoundX()] = 1;
            }

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

    public int directionToClosestEnnemy(Player player) {
        int x = player.getRoundX(),
                y = player.getRoundY();
        int[][] tab = playersPositionForSpecificPlayer(player);

        int xClosest = tab[0].length - 1,
                yClosest = tab.length - 1;

        for (int i = 0; i < tab.length; i++)
            for (int j = 0; j < tab[i].length; j++) {
                if (tab[i][j] == CASE_POSE_BOMB) {
                    int diffY = i - y,
                            diffX = j - x;

                    if (Math.abs(diffY) + Math.abs(diffX)
                            <= Math.abs(yClosest) + Math.abs(xClosest)) {
                        yClosest = diffY;
                        xClosest = diffX;
                    }
                }
            }

        int direction;

        if (x % 2 == 0 && y % 2 != 0)
            direction = yClosest < 0 ? Direction.TOP : Direction.BOTTOM;
        else if (x % 2 != 0 && y % 2 == 0)
            direction = xClosest < 0 ? Direction.LEFT : Direction.RIGHT;
        else if (xClosest == 0)
            direction = yClosest < 0 ? Direction.TOP : Direction.BOTTOM;
        else
            direction = xClosest < 0 ? Direction.LEFT : Direction.RIGHT;

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

        return direction;
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

        for (int i = 0; i < original.length; i++)
            for (int j = 0; j < original[i].length; j++)
                copy[i][j] = original[i][j];

        return copy;
    }
}
