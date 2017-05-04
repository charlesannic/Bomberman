package com.app.charles.bomberman.game;

import android.content.Context;

import com.app.charles.bomberman.java.Direction;

/**
 * Created by Charles on 24-Feb-17.
 */

public class Bot extends Player {

    private Direction direction;
    private boolean poseBomb;

    public Bot(Context context, int player) {
        super(context, player);
        direction = new Direction(1, Direction.STOP);
        poseBomb = false;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isPoseBomb() {
        return poseBomb;
    }
}
