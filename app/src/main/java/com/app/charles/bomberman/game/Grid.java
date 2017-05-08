package com.app.charles.bomberman.game;

import android.content.Context;
import android.graphics.drawable.TransitionDrawable;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.RelativeLayout;

import com.app.charles.bomberman.R;
import com.app.charles.bomberman.ai.AI;

import java.util.ArrayList;
import java.util.Random;

import static android.content.ContentValues.TAG;

/**
 * Created by Charles on 08-Feb-17.
 */

public class Grid {

    private static final int SIDE_NONE = 0,
            SIDE_LEFT = 1,
            SIDE_TOP = 2,
            SIDE_RIGHT = 3,
            SIDE_BOTTOM = 4;

    private static final int MOVE_LEFT_OR_TOP = -1,
            MOVE_RIGHT_OR_BOTTOM = 1,
            NO_MOVE = 0;

    public static final int CASE_EMPTY = 0,
            CASE_WALL = 1,
            CASE_BLOC = 2,
            CASE_PU_FASTER = 5,
            CASE_PU_ADD_BOMB = 6,
            CASE_PU_POWER = 7,
            CASE_PU_P_BOMB = 8;

    private final int GRID_COLUMNS_EASY = 9,
            GRID_ROWS_EASY = 7,
            GRID_COLUMNS_NORMAL = 11,
            GRID_ROWS_NORMAL = 7,
            GRID_COLUMNS_HARD = 13,
            GRID_ROWS_HARD = 9;

    private int grid[][];
            /*{{0, 0, 2, 2, 0, 2, 2, 0, 2, 0, 0},
                    {0, 1, 0, 1, 2, 1, 2, 1, 2, 1, 2},
                    {0, 2, 2, 2, 0, 0, 2, 2, 0, 0, 2},
                    {2, 1, 0, 1, 0, 1, 2, 1, 0, 1, 2},
                    {0, 0, 2, 0, 2, 2, 0, 2, 0, 2, 2},
                    {2, 1, 0, 1, 0, 1, 2, 1, 2, 1, 2},
                    {0, 2, 2, 2, 2, 2, 2, 2, 2, 0, 0},
                    {2, 1, 2, 1, 2, 1, 0, 1, 0, 1, 0},
                    {0, 0, 0, 2, 0, 2, 2, 0, 2, 0, 0}};*/

    private Context context;
    private GridLayout gridLayout;
    private RelativeLayout objectsContainer;

    private int gridColumns = GRID_COLUMNS_NORMAL,
            gridRows = GRID_ROWS_NORMAL;

    private Player player;
    private ArrayList<Bot> bots;
    private AI ai;

    public Grid(Context context, GridLayout gridL, RelativeLayout mObjectsContainer, final int difficulty) {
        this.context = context;
        this.gridLayout = gridL;
        this.objectsContainer = mObjectsContainer;

        switch (difficulty) {
            case 0:
                gridColumns = GRID_COLUMNS_EASY;
                gridRows = GRID_ROWS_EASY;
                break;
            case 1:
                gridColumns = GRID_COLUMNS_NORMAL;
                gridRows = GRID_ROWS_NORMAL;
                break;
            case 2:
                gridColumns = GRID_COLUMNS_HARD;
                gridRows = GRID_ROWS_HARD;
                break;
        }

        gridLayout.setColumnCount(gridColumns);
        gridLayout.setRowCount(gridRows);

        generateGrid();

        player = new Player(this.context, 1);

        bots = new ArrayList<>();
        bots.add(new Bot(this.context, 2));
        if (difficulty >= 1) {
            bots.add(new Bot(this.context, 3));
            if (difficulty >= 2)
                bots.add(new Bot(this.context, 4));
        }

        ai = new AI(gridColumns, gridRows);
        update();

        this.gridLayout.post(new Runnable() {
            @Override
            public void run() {
                setGridItems(gridLayout.getWidth() / gridColumns, difficulty);
            }
        });
    }

    private void setGridItems(int itemSize, int difficulty) {
        RelativeLayout.LayoutParams gridParams = (RelativeLayout.LayoutParams) gridLayout.getLayoutParams();
        gridParams.height = itemSize * gridRows;
        gridParams.width = itemSize * gridColumns;
        gridLayout.setLayoutParams(gridParams);

        RelativeLayout.LayoutParams objectsContainerParams = (RelativeLayout.LayoutParams) objectsContainer.getLayoutParams();
        objectsContainerParams.height = itemSize * gridRows;
        objectsContainerParams.width = itemSize * gridColumns;
        objectsContainer.setLayoutParams(objectsContainerParams);

        gridLayout.removeAllViews();

        for (int i = 0; i < gridRows; i++) {
            for (int j = 0; j < gridColumns; j++) {
                LayoutInflater inflater = LayoutInflater.from(context);
                View item = inflater.inflate(R.layout.item_grid_0, null, false);
                switch (grid[i][j]) {
                    case CASE_WALL:
                        item.setBackgroundResource(R.drawable.item_wall);
                        break;
                    case CASE_BLOC:
                        item.setBackgroundResource(R.drawable.item_bloc);
                        break;
                }
                gridLayout.addView(item);
                Log.i(TAG, "setGridItems: add");

                GridLayout.LayoutParams params = (GridLayout.LayoutParams) item.getLayoutParams();
                params.height = itemSize;
                params.width = itemSize;
                item.setLayoutParams(params);
            }
        }

        player.setViewPosition(gridLayout.getX(), gridLayout.getY());
        bots.get(0).setViewPosition(gridLayout.getX() + (gridColumns - 1) * itemSize, gridLayout.getY() + (gridRows - 1) * itemSize);
        if (difficulty >= 1) {
            bots.get(1).setViewPosition(gridLayout.getX() + (gridColumns - 1) * itemSize, gridLayout.getY());
            if (difficulty >= 2)
                bots.get(2).setViewPosition(gridLayout.getX(), gridLayout.getY() + (gridRows - 1) * itemSize);
        }

        objectsContainer.addView(player.getView());
        objectsContainer.addView(bots.get(0).getView());
        if (difficulty >= 1) {
            objectsContainer.addView(bots.get(1).getView());
            if (difficulty >= 2)
                objectsContainer.addView(bots.get(2).getView());
        }

        player.setView(itemSize);
        bots.get(0).setView(itemSize);
        if (difficulty >= 1) {
            bots.get(1).setView(itemSize);
            if (difficulty >= 2)
                bots.get(2).setView(itemSize);
        }

    }

    public void checkBombStatus() {
        for (Bomb bomb : getAllBombs()) {
            float playerYCenter = player.getY() + player.getSize(),
                    playerXCenter = player.getX() + player.getSize(),
                    bombYCenter = bomb.getY() + bomb.getSize(),
                    bombXCenter = bomb.getX() + bomb.getSize();
            if (bomb.getBombStatus() == Bomb.PLAYER_STILL_ON_BOMB
                    && (Math.abs(playerYCenter - bombYCenter) >= bomb.getSize()
                    || (Math.abs(playerXCenter - bombXCenter) >= bomb.getSize()))) {
                bomb.setBombStatus(Bomb.POSED);
                grid[bomb.getYGrid()][bomb.getXGrid()] = Bomb.POSED;
            }
        }
    }

    private void generateGrid() {
        grid = new int[gridRows][gridColumns];

        Random randomGenerator = new Random();

        for (int i = 0; i < gridRows; i++)
            for (int j = 0; j < gridColumns; j++) {
                if (isWall(i, j))
                    grid[i][j] = CASE_WALL;
                else if (isPlayerSpace(i, j) || randomGenerator.nextInt(100) % 10 == 0)
                    grid[i][j] = CASE_EMPTY;
                else
                    grid[i][j] = CASE_BLOC;
            }
    }

    private boolean isWall(int i, int j) {
        return i % 2 != CASE_EMPTY
                && j % 2 != CASE_EMPTY;
    }

    private boolean isPlayerSpace(int i, int j) {
        return ((j == 0 || j == gridColumns - 1)
                && (i <= 2 || i >= gridRows - 3))

                || ((i == 0 || i == gridRows - 1)
                && (j <= 2 || j >= gridColumns - 3));
    }

    public void moveLeft(Player p, float distance) {
        float x = p.getX() + distance;
        float y = p.getY();

        if (x < 0)
            x = 0;
        else {
            float indexX = x / p.getSize();
            float indexY = y / p.getSize();

            int roundX = (int) Math.floor(indexX);
            int roundY = Math.round(indexY);

            View caseOnLeft = gridLayout.getChildAt(roundX + gridColumns * roundY);

            if (grid[roundY][roundX] != CASE_EMPTY
                    && grid[roundY][roundX] != CASE_PU_FASTER
                    && grid[roundY][roundX] != CASE_PU_ADD_BOMB
                    && grid[roundY][roundX] != CASE_PU_POWER
                    && grid[roundY][roundX] != CASE_PU_P_BOMB
                    && grid[roundY][roundX] != Bomb.PLAYER_STILL_ON_BOMB)
                x = caseOnLeft.getX() + caseOnLeft.getWidth();
            else {
                float diffY = caseOnLeft.getY() - y;
                if (diffY != 0) {
                    if (Math.abs(diffY) > Math.abs(distance)) {
                        if (diffY > 0) {
                            y -= distance;
                            x -= distance;
                        } else {
                            y += distance;
                            x -= distance;
                        }
                    } else {
                        y = caseOnLeft.getY();
                        x -= (distance + Math.abs(diffY));
                    }
                }
            }

            if (grid[roundY][roundX] == CASE_PU_FASTER
                    || grid[roundY][roundX] == CASE_PU_ADD_BOMB
                    || grid[roundY][roundX] == CASE_PU_POWER
                    || grid[roundY][roundX] == CASE_PU_P_BOMB)
                takePowerUp(p, roundX, roundY);
        }

        animateView(p.getView(), x, y, 0);
    }

    public synchronized void update() {
        ai.updateBlocksCases(grid.clone(), getAllBombs(), getAllPlayers());
    }

    public void moveRight(Player p, float distance) {
        float x = p.getX() + distance;
        float y = p.getY();

        if (x >= gridLayout.getWidth() - p.getSize())
            x = gridLayout.getWidth() - p.getSize();
        else {
            float indexX = x / p.getSize();
            float indexY = y / p.getSize();

            int roundX = (int) Math.floor(indexX);
            int roundY = Math.round(indexY);

            View caseOnRight = gridLayout.getChildAt(roundX + 1 + gridColumns * roundY);

            Log.i(TAG, "moveRight: " + indexX + "=" + x + "/" + p.getSize() + "\t" + roundX);
            if (grid[roundY][roundX + 1] != CASE_EMPTY
                    && grid[roundY][roundX + 1] != CASE_PU_FASTER
                    && grid[roundY][roundX + 1] != CASE_PU_ADD_BOMB
                    && grid[roundY][roundX + 1] != CASE_PU_POWER
                    && grid[roundY][roundX + 1] != CASE_PU_P_BOMB
                    && grid[roundY][roundX + 1] != Bomb.PLAYER_STILL_ON_BOMB)
                x = caseOnRight.getX() - caseOnRight.getWidth();
            else {
                float diffY = caseOnRight.getY() - y;
                if (diffY != 0) {
                    if (Math.abs(diffY) > Math.abs(distance)) {
                        if (diffY > 0) {
                            y += distance;
                            x -= distance;
                        } else {
                            y -= distance;
                            x -= distance;
                        }
                    } else {
                        y = caseOnRight.getY();
                        x -= (distance - Math.abs(diffY));
                    }
                }
            }

            if (grid[roundY][roundX + 1] == CASE_PU_FASTER
                    || grid[roundY][roundX + 1] == CASE_PU_ADD_BOMB
                    || grid[roundY][roundX + 1] == CASE_PU_POWER
                    || grid[roundY][roundX + 1] == CASE_PU_P_BOMB)
                takePowerUp(p, roundX + 1, roundY);
        }

        animateView(p.getView(), x, y, 0);
    }

    public void moveTop(Player p, float distance) {
        float y = p.getY() + distance,
                x = p.getX();

        if (y < 0)
            y = 0;
        else {
            float indexX = x / p.getSize();
            float indexY = y / p.getSize();

            int roundX = Math.round(indexX);
            int roundY = (int) Math.floor(indexY);

            View caseOnTop = gridLayout.getChildAt(roundX + gridColumns * roundY);

            if (grid[roundY][roundX] != CASE_EMPTY
                    && grid[roundY][roundX] != CASE_PU_FASTER
                    && grid[roundY][roundX] != CASE_PU_ADD_BOMB
                    && grid[roundY][roundX] != CASE_PU_POWER
                    && grid[roundY][roundX] != CASE_PU_P_BOMB
                    && grid[roundY][roundX] != Bomb.PLAYER_STILL_ON_BOMB)
                y = caseOnTop.getY() + caseOnTop.getHeight();
            else {
                Log.i(TAG, "moveTop: " + roundX + " " + gridColumns + " " + roundY);
                float diffX = caseOnTop.getX() - x;
                if (diffX != 0) {
                    if (Math.abs(diffX) > Math.abs(distance)) {
                        if (diffX > 0) {
                            y -= distance;
                            x -= distance;
                        } else {
                            y -= distance;
                            x += distance;
                        }
                    } else {
                        x = caseOnTop.getX();
                        y -= (distance + Math.abs(diffX));
                    }
                }
            }

            if (grid[roundY][roundX] == CASE_PU_FASTER
                    || grid[roundY][roundX] == CASE_PU_ADD_BOMB
                    || grid[roundY][roundX] == CASE_PU_POWER
                    || grid[roundY][roundX] == CASE_PU_P_BOMB)
                takePowerUp(p, roundX, roundY);
        }

        animateView(p.getView(), x, y, 0);
    }

    public void moveBottom(Player p, float distance) {
        float y = p.getY() + distance,
                x = p.getX();

        if (y >= gridLayout.getHeight() - p.getSize())
            y = gridLayout.getHeight() - p.getSize();
        else {
            float indexX = x / p.getSize();
            float indexY = y / p.getSize();

            int roundX = Math.round(indexX);
            int roundY = (int) Math.floor(indexY);

            View caseOnBottom = gridLayout.getChildAt(roundX + gridColumns * (roundY + 1));

            if (grid[roundY + 1][roundX] != CASE_EMPTY
                    && grid[roundY + 1][roundX] != CASE_PU_FASTER
                    && grid[roundY + 1][roundX] != CASE_PU_ADD_BOMB
                    && grid[roundY + 1][roundX] != CASE_PU_POWER
                    && grid[roundY + 1][roundX] != CASE_PU_P_BOMB
                    && grid[roundY + 1][roundX] != Bomb.PLAYER_STILL_ON_BOMB)
                y = caseOnBottom.getY() - caseOnBottom.getHeight();
            else {
                float diffX = caseOnBottom.getX() - x;
                if (diffX != 0) {
                    if (Math.abs(diffX) > Math.abs(distance)) {
                        if (diffX > 0) {
                            y -= distance;
                            x += distance;
                        } else {
                            y -= distance;
                            x -= distance;
                        }
                    } else {
                        x = caseOnBottom.getX();
                        y -= (distance - Math.abs(diffX));
                    }
                }
            }

            if (grid[roundY + 1][roundX] == CASE_PU_FASTER
                    || grid[roundY + 1][roundX] == CASE_PU_ADD_BOMB
                    || grid[roundY + 1][roundX] == CASE_PU_POWER
                    || grid[roundY + 1][roundX] == CASE_PU_P_BOMB)
                takePowerUp(p, roundX, roundY + 1);
        }

        animateView(p.getView(), x, y, 0);
    }

    private void animateView(View view, float x, float y, int duration) {
        view.animate()
                .x(x)
                .y(y)
                .setDuration(duration)
                .start();
    }

    private void takePowerUp(Player p, int x, int y) {
        switch (grid[y][x]) {
            case CASE_PU_FASTER:
                p.increaseSpeed();
                break;
            case CASE_PU_ADD_BOMB:
                p.incrementBombsCapacity();
                break;
            case CASE_PU_POWER:
                p.incrementBombsPower();
                break;
            case CASE_PU_P_BOMB:
                p.setHasPBomb();
                break;
        }
        removePowerUp(x, y);
    }

    public void poseBomb(Player p) {
        if (p.getId() == 2)
            Log.i(TAG, "poseBomb " + ((Bot) p).getDirection().isPoseBomb() + " " + p.isPlayerAlive());

        if (p.isPlayerAlive()) {
            if (p instanceof Bot)
                ((Bot) p).getDirection().setPoseBomb(false);

            if (p.getBombsCapacity() > p.getBombs().size()) {
                int roundX = p.getRoundX();
                int roundY = p.getRoundY();
                if (grid[roundY][roundX] == CASE_EMPTY) {
                    boolean isPoseBomb = p.hasPBomb() && p.isPBombAvailable();
                    Bomb bomb = new Bomb(context, roundX * p.getSize(), roundY * p.getSize(), p.getSize(), p.getBombsPower(), isPoseBomb);

                    grid[roundY][roundX] = bomb.getBombStatus();

                    objectsContainer.addView(bomb.getV(), 0);
                    bomb.setView();

                    p.getBombs().add(bomb);
                }
            }

        }
    }

    public int findBombAtXY(int x, int y) {
        Bomb bomb;
        for (int i = 0; i < getAllBombs().size(); i++) {
            bomb = getAllBombs().get(i);
            if (bomb.getXGrid() == x && bomb.getYGrid() == y)
                return i;
        }
        return -1;
    }

    public void checkBombs() {
        boolean vibrate = false;
        for (int i = 0; i < getAllBombs().size(); i++) {
            if (getAllBombs().get(i).getSeconds() > 3) {
                Bomb bomb = getAllBombs().get(i);

                int power = removeBomb(i, bomb.getXGrid(), bomb.getYGrid());

                performExplosion(bomb.getXGrid(),
                        bomb.getYGrid(), SIDE_NONE, power);

                vibrate = true;
            }
        }


        if (vibrate && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getResources().getString(R.string.vibrate), true)) {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(50);
        }

        checkBombStatus();
    }

    public void stopBombs() {
        for (Bomb bomb : getAllBombs()) {
            bomb.stopTimer();
        }
    }

    public void resumeBombs() {
        for (Bomb bomb : getAllBombs()) {
            bomb.resumeTimer();
        }
    }

    public int removeBomb(int index, int x, int y) {
        Player p = player;
        Bomb bomb = getAllBombs().get(index);
        /*if(player1.getBombs().contains(getAllBombs().get(index)))
            p = player1;
        else*/
        for (Bot bot : bots)
            if (bot.getBombs().contains(bomb))
                p = bot;

        Log.i(TAG, "removeBomb: " + (p == player));

        int power = bomb.getPower();
        grid[y][x] = 0;
        objectsContainer.removeView(bomb.getV());
        p.getBombs().remove(bomb);
        return power;
    }

    public void removeBloc(int x, int y) {
        double rnd = Math.random();
        // 1/4 de chance d'avoir un powerup sur la case explosée
        if (rnd < 0.05) {
            grid[y][x] = CASE_PU_FASTER;
            gridLayout.getChildAt(x + gridColumns * y).setBackgroundResource(R.drawable.ic_pu_faster);
        } else if (rnd < 0.14) {
            grid[y][x] = CASE_PU_ADD_BOMB;
            gridLayout.getChildAt(x + gridColumns * y).setBackgroundResource(R.drawable.ic_pu_add_bomb);
        } else if (rnd < 0.15) {
            grid[y][x] = CASE_PU_POWER;
            gridLayout.getChildAt(x + gridColumns * y).setBackgroundResource(R.drawable.ic_pu_power);
        } else if (rnd < 0.4) {
            grid[y][x] = CASE_PU_P_BOMB;
            gridLayout.getChildAt(x + gridColumns * y).setBackgroundResource(R.drawable.ic_pu_p_bomb);
        } else {
            grid[y][x] = 0;
            gridLayout.getChildAt(x + gridColumns * y).setBackgroundResource(R.drawable.item_empty);
        }
    }

    public void removePowerUp(int x, int y) {
        grid[y][x] = 0;
        gridLayout.getChildAt(x + gridColumns * y).setBackgroundResource(R.drawable.item_empty);
    }

    public void showExplosion(View view) {
        view.setBackgroundResource(R.drawable.explosion_transition);
        TransitionDrawable transition = (TransitionDrawable) view.getBackground();
        transition.startTransition(500);
    }

    public void performExplosion(int xGrid, int yGrid, int excludedSide, int power) {
        if (xGrid >= 0 && xGrid < gridColumns && yGrid >= 0 && yGrid < gridRows)
            if (grid[yGrid][xGrid] == CASE_EMPTY) {

                showExplosion(gridLayout.getChildAt(xGrid + gridColumns * yGrid));

                killPlayers(xGrid, yGrid);

                if (excludedSide != SIDE_LEFT)
                    spreadExplosion(xGrid + MOVE_LEFT_OR_TOP,
                            yGrid,
                            MOVE_LEFT_OR_TOP,
                            NO_MOVE,
                            SIDE_RIGHT,
                            power);
                if (excludedSide != SIDE_TOP)
                    spreadExplosion(xGrid,
                            yGrid + MOVE_LEFT_OR_TOP,
                            NO_MOVE,
                            MOVE_LEFT_OR_TOP,
                            SIDE_BOTTOM,
                            power);
                if (excludedSide != SIDE_RIGHT)
                    spreadExplosion(xGrid + MOVE_RIGHT_OR_BOTTOM,
                            yGrid,
                            MOVE_RIGHT_OR_BOTTOM,
                            NO_MOVE,
                            SIDE_LEFT,
                            power);
                if (excludedSide != SIDE_BOTTOM)
                    spreadExplosion(xGrid,
                            yGrid + MOVE_RIGHT_OR_BOTTOM,
                            NO_MOVE,
                            MOVE_RIGHT_OR_BOTTOM,
                            SIDE_TOP,
                            power);
            }
    }

    public void spreadExplosion(int xGrid, int yGrid, int moveX, int moveY, int from, int depth) {
        if (depth > 0 && xGrid >= 0 && xGrid < gridColumns && yGrid >= 0 && yGrid < gridRows)
            switch (grid[yGrid][xGrid]) {
                case CASE_EMPTY:
                    showExplosion(gridLayout.getChildAt(xGrid + gridColumns * yGrid));

                    killPlayers(xGrid, yGrid);

                    spreadExplosion(xGrid + moveX, yGrid + moveY, moveX, moveY, from, depth - 1);
                    break;
                case CASE_PU_FASTER:
                case CASE_PU_ADD_BOMB:
                case CASE_PU_POWER:
                case CASE_PU_P_BOMB:
                    removePowerUp(xGrid, yGrid);
                    break;
                case CASE_BLOC:
                    removeBloc(xGrid, yGrid);
                    break;
                case Bomb.PLAYER_STILL_ON_BOMB:
                case Bomb.POSED:
                    int index = findBombAtXY(xGrid, yGrid);
                    if (index >= 0) {
                        int power = removeBomb(index, xGrid, yGrid);
                        performExplosion(xGrid, yGrid, from, power);
                    }
                    break;
            }
    }

    private void killPlayers(int x, int y) {
        for (Player player : getAllPlayers())
            if (player.getRoundX() == x && player.getRoundY() == y) {
                player.killPlayer();
                objectsContainer.removeView(player.getView());
            }
    }

    private ArrayList<Bomb> getAllBombs() {
        ArrayList<Bomb> bombs = new ArrayList<>();
        bombs.addAll(player.getBombs());
        for (Bot bot : bots)
            bombs.addAll(bot.getBombs());
        return bombs;
    }

    public ArrayList<Player> getAllPlayers() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(player);
        players.addAll(bots);
        return players;
    }

    public Player getPlayer() {
        return player;
    }

    public View getPlayerView() {
        return player.getView();
    }

    public ArrayList<Bot> getBots() {
        return bots;
    }

    public AI getAi() {
        return ai;
    }
}
