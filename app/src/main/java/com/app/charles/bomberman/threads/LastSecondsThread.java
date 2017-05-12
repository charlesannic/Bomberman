package com.app.charles.bomberman.threads;

import android.graphics.Point;

import com.app.charles.bomberman.R;
import com.app.charles.bomberman.activities.GameActivity;
import com.app.charles.bomberman.game.Grid;
import com.app.charles.bomberman.java.Direction;

import java.util.ArrayList;

/**
 * Thread permettant de réduire la taille de la grille en fin de partie.
 */

public class LastSecondsThread extends Thread {

    // selon la difficulté, le temps de pause du thread n'est pas le même
    // (la grille ne se réduit pas à la même vitesse).
    private static final int SLEEP_TIME_EASY = 500,
            SLEEP_TIME_NORMAL = 333,
            SLEEP_TIME_HARD = 250;

    // attributs
    private GameActivity activity;
    private Grid grid;
    private int sleepTime, lastX, lastY, lastDirection;

    public LastSecondsThread(GameActivity activity, Grid grid, int difficulty) {
        this.activity = activity;
        this.grid = grid;

        // initialement, la grille commence à se réduire depuis le coin haut droit et la réduction se poursuit par la droite.
        this.lastX = 0;
        this.lastY = 0;
        this.lastDirection = Direction.RIGHT;

        switch (difficulty) {
            case 0:
                sleepTime = SLEEP_TIME_EASY;
                break;
            case 1:
                sleepTime = SLEEP_TIME_NORMAL;
                break;
            case 2:
                sleepTime = SLEEP_TIME_HARD;
                break;
        }
    }

    /**
     * Méthode appelée lorsque le thread est démaré.
     */
    @Override
    public void run() {
        // tant que la partie n'est pas mise en pause.
        while (!activity.gameStoped()) {
            try {
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        grid.changeCase(lastX, lastY, Grid.CASE_LAST_SECONDS_WALL, R.drawable.item_wall);

                        // selon la dernière derection on poursuit la réduction.
                        switch (lastDirection) {
                            // si la réduction de la grille allait vers la gauche on vérifie qu'elle peut encore continuer dans cette direction.
                            case Direction.LEFT:
                                // si elle ne peut pas continuer, elle se poursuit vers le haut.
                                if (lastX <= 0 || grid.getGrid()[lastY][lastX - 1] == Grid.CASE_LAST_SECONDS_WALL) {
                                    lastY--;
                                    lastDirection = Direction.TOP;
                                }
                                // sinon elle continue.
                                else
                                    lastX--;
                                break;
                            // même chose que pour gauche.
                            case Direction.TOP:
                                if (lastY <= 0 || grid.getGrid()[lastY - 1][lastX] == Grid.CASE_LAST_SECONDS_WALL) {
                                    lastX++;
                                    lastDirection = Direction.RIGHT;
                                } else
                                    lastY--;
                                break;
                            // même chose que pour gauche.
                            case Direction.RIGHT:
                                if (lastX >= grid.getGrid()[0].length - 1 || grid.getGrid()[lastY][lastX + 1] == Grid.CASE_LAST_SECONDS_WALL) {
                                    lastY++;
                                    lastDirection = Direction.BOTTOM;
                                } else
                                    lastX++;
                                break;
                            // même chose que pour gauche.
                            case Direction.BOTTOM:
                                if (lastY >= grid.getGrid().length - 1 || grid.getGrid()[lastY + 1][lastX] == Grid.CASE_LAST_SECONDS_WALL) {
                                    lastX--;
                                    lastDirection = Direction.LEFT;
                                } else
                                    lastY++;
                                break;
                        }
                    }
                });

                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Mise à jour des cases dangereuses selon l'avancement de la réduction.
     *
     * @return lise des cases dangereuses.
     */
    public ArrayList<Point> getUnsafeCases() {
        ArrayList<Point> unsafeCases = new ArrayList<>();

        int x = lastX,
                y = lastY,
                direction = lastDirection;

        // l'algorithme de recherche des cases dangereuses fonctionne de la même manière que celui de recherche de la case suivante précédent.
        // on recherche pour les 5 prochaines cases dangereuses.
        for (int i = 0; i < 5; i++) {
            switch (direction) {
                case Direction.LEFT:
                    if (x <= 0 || grid.getGrid()[y][x - 1] == Grid.CASE_LAST_SECONDS_WALL) {
                        y--;
                        direction = Direction.TOP;
                    } else
                        x--;
                    break;
                case Direction.TOP:
                    if (y <= 0 || grid.getGrid()[y - 1][x] == Grid.CASE_LAST_SECONDS_WALL) {
                        x++;
                        direction = Direction.RIGHT;
                    } else
                        y--;
                    break;
                case Direction.RIGHT:
                    if (x >= grid.getGrid()[0].length - 1 || grid.getGrid()[y][x + 1] == Grid.CASE_LAST_SECONDS_WALL) {
                        y++;
                        direction = Direction.BOTTOM;
                    } else
                        x++;
                    break;
                case Direction.BOTTOM:
                    if (y >= grid.getGrid().length - 1 || grid.getGrid()[y + 1][x] == Grid.CASE_LAST_SECONDS_WALL) {
                        x--;
                        direction = Direction.LEFT;
                    } else
                        y++;
                    break;
            }
            unsafeCases.add(new Point(x, y));
        }

        return unsafeCases;
    }
}
