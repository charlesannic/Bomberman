package com.app.charles.bomberman.java;

/**
 * Created by Charles on 05-Feb-17.
 */

public class Direction {

    public static final int STOP = 0;
    public static final int LEFT = 1;
    public static final int TOP = 2;
    public static final int RIGHT = 3;
    public static final int BOTTOM = 4;

    private float offset;
    private int direction;
    private boolean poseBomb;

    public Direction(float offset, int direction) {
        this.offset = offset;
        this.direction = direction;
        this.poseBomb = false;
    }

    public float getOffset() {
        return offset;
    }

    public void setOffset(float offset) {
        this.offset = offset;
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public boolean isPoseBomb() {
        return poseBomb;
    }

    public void setPoseBomb(boolean poseBomb) {
        this.poseBomb = poseBomb;
    }
}
