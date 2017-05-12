package com.app.charles.bomberman.threads;

import android.widget.TextView;

import com.app.charles.bomberman.activities.GameActivity;
import com.app.charles.bomberman.game.Grid;
import com.app.charles.bomberman.utils.Utils;

/**
 * Thread utilisé pour gérer une partie.
 */

public class TimeThread extends Thread {

    // durée de la partie selon la difficulté du jeu.
    private static final int DURATION_EASY = 60, DURATION_NORMAL = 90, DURATION_HARD = 120;

    // activité depuis laquelle est appelé le thread.
    private GameActivity activity;

    // vue pour l'affichage du temps.
    private TextView tvTime;

    // grille.
    private Grid grid;

    // attributs.
    private boolean isGameOver = false;
    private int difficulty;
    private int gameDuration;

    public TimeThread(GameActivity activity, TextView tvTime, Grid grid, int difficulty) {
        this.activity = activity;
        this.tvTime = tvTime;
        this.grid = grid;
        this.difficulty = difficulty;

        switch (difficulty) {
            case 0:
                gameDuration = DURATION_EASY;
                break;
            case 1:
                gameDuration = DURATION_NORMAL;
                break;
            default:
                gameDuration = DURATION_HARD;
                break;
        }
    }

    /**
     * Méthode appelée lorsque le thread est démaré.
     */
    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        double time = gameDuration;

        while (!activity.gameStoped() && time >= 0 && !isGameOver) {
            final int seconds = (int)Math.floor(time % 60);
            final int minutes = (int)Math.floor(time / 60);
            final int tmpTime = (int)Math.floor(time); // nécessaire car la variable doit être finale pour être utilisée dans le thread principal.

            // sur Android, la modification des vues ne peut se faire que sur le thread principal. la fonction runOnUiThread permet d'exécuter le code sur ce thread principal.
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // màj du temps restant.
                    tvTime.setText(Utils.formatTime(minutes, seconds));

                    // vérifie si des bombes sur la grille doivent exploser.
                    grid.checkBombs();

                    // lancement du thread de réduction de taille de la grille selon le temps restant.
                    switch (difficulty) {
                        case 0:
                            if(tmpTime <= Math.round((float)(grid.getGridColumns() * grid.getGridRows()) / 2f)) // 2 car 2 nouveaux blocs apparaissent par seconde.
                                activity.startLastSecondsThread();
                            break;
                        case 1:
                            if(tmpTime <= Math.round((float)(grid.getGridColumns() * grid.getGridRows()) / 3f))
                                activity.startLastSecondsThread();
                            break;
                        case 2:
                            if(tmpTime <= Math.round((float)(grid.getGridColumns() * grid.getGridRows()) / 4f))
                                activity.startLastSecondsThread();
                            break;
                    }

                    // vérifie si la partie est terminée.
                    if (activity.isGameOver()) {
                        isGameOver = true;
                        activity.changeScoreBoard();
                        activity.recreate();
                    }
                }
            });
            // màj du temps calculer.
            time = gameDuration - (int) Math.floor(Utils.calculateSeconds(startTime));

            // màj des informations dont dispose l'IA
            grid.updateAIInformations();

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // màj de la durée de la partie. utile si la partie est mise en pause, pour ne pas recommencer depuis le début
        gameDuration = (int) Math.round(time);
    }
}
