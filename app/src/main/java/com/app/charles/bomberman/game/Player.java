package com.app.charles.bomberman.game;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.app.charles.bomberman.R;

import java.util.ArrayList;

/**
 * Created by Charles on 10-Feb-17.
 */

public class Player {

    public static final int ALIVE = 0;
    public static final int DEAD = 1;

    private Context context;

    private int playerStatus = ALIVE;

    private ArrayList<Bomb> bombs;
    private int bombsCapacity;
    private int bombsPower;

    private View view;
    private int size;

    public Player(Context context, int player) {
        this.context = context;

        bombs = new ArrayList<>();
        bombsCapacity = 1;
        bombsPower = 3;

        LayoutInflater inflater = LayoutInflater.from(this.context);
        switch (player) {
            case 1:
                view = inflater.inflate(R.layout.player1, null, false);
                break;
            case 2:
                view = inflater.inflate(R.layout.player2, null, false);
                break;
            case 3:
                view = inflater.inflate(R.layout.player3, null, false);
                break;
            default:
                view = inflater.inflate(R.layout.player4, null, false);
                break;
        }
    }

    public void setView(int size) {
        this.size = size;

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.height = size;
        params.width = size;
        view.setLayoutParams(params);
    }

    public View getView() {
        return view;
    }

    public ArrayList<Bomb> getBombs() {
        return bombs;
    }

    public void setViewPosition(float x, float y) {
        setX(x);
        setY(y);
    }

    public void setX(float x) {
        view.setX(x);
    }

    public void setY(float y) {
        view.setY(y);
    }

    public float getX() {
        return view.getX();
    }

    public float getY() {
        return view.getY();
    }

    public int getRoundX() {
        return Math.round(getX() / getSize());
    }

    public int getRoundY() {
        return Math.round(getY() / getSize());
    }

    public int getSize() {
        return size;
    }

    public int getBombsCapacity() {
        return bombsCapacity;
    }

    public void incrementBombsCapacity() {
        bombsCapacity++;
    }

    public int getBombsPower() {
        return bombsPower;
    }

    public void incrementBombsPower() {
        bombsPower++;
    }

    public boolean isPlayerAlive() {
        return playerStatus == ALIVE;
    }

    public void killPlayer() {
        this.playerStatus = DEAD;
    }
}
