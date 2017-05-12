package com.app.charles.bomberman.java;

/**
 * Classe représentant une direction.
 */

public class Direction {

    // direction possibles.
    public static final int STOP = 0;
    public static final int LEFT = 1;
    public static final int TOP = 2;
    public static final int RIGHT = 3;
    public static final int BOTTOM = 4;

    // attribut.
    private float offset; // vitesse de déplacement.
    private int direction;
    private boolean poseBomb; // pose d'une bombe souhaitée.

    public Direction(float offset, int direction) {
        this.offset = offset;
        this.direction = direction;
        this.poseBomb = false;
    }

    public float getOffset() {
        return offset;
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
