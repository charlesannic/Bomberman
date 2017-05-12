package com.app.charles.bomberman.game;

import android.content.Context;

import com.app.charles.bomberman.java.Direction;

/**
 * Classe réprésentant un robot dans une partie.
 * La classe étant la classe Joueur pour lui rajouter un attribut correspondant à sa direction sur la grille.
 */

@SuppressWarnings("WeakerAccess")
public class Bot extends Player {

    private Direction direction;

    public Bot(Context context, int player) {
        super(context, player);

        direction = new Direction(1, // la vitesse de déplacement d'un robot est toujours maximale.
                Direction.STOP); // de base, le robot ne se déplace pas.
    }

    public Direction getDirection() {
        return direction;
    }

}
