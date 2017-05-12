package com.app.charles.bomberman.threads;

import android.widget.TextView;

import com.app.charles.bomberman.activities.GameActivity;
import com.app.charles.bomberman.utils.Utils;

/**
 * Thread réalisant un compte à rebours lors du début de la partie ou de la reprise d'une partie.
 */

public class CountdownThread extends Thread {

    private static final int COUNTDOWN_DURATION = 3; // durée du compte à rebours en secondes.

    // attributs
    private GameActivity activity;
    private TextView tvTime; // vue affichant le compte à rebours.

    public CountdownThread(GameActivity activity, TextView tvTime) {
        this.activity = activity;
        this.tvTime = tvTime;
    }

    /**
     * Méthode appelée lorsque le thread est démaré.
     */
    @Override
    public void run() {
        // heure de début du compte à rebours.
        long countdown = System.currentTimeMillis();

        // temps restant au compte à rebours = durée - nombre de secondes écoulées depuis le début.
        int time = COUNTDOWN_DURATION - (int) Math.floor(Utils.calculateSeconds(countdown));

        // tant que la partie n'est pas en pause et que tout le temps n'est pas écoulé.
        while (!activity.gameStoped() && time >= 0) {
            // calcul des secondes et des minutes restantes dans un format mm:ss.
            final int seconds = time % 60;
            final int minutes = time / 60;

            // sur Android, la modification des vues ne peut se faire que sur le thread principal.
            // la fonction runOnUiThread permet d'exécuter le code sur ce thread principal.
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // màj de l'affichage du compte à rebours.
                    tvTime.setText(Utils.formatTime(minutes, seconds));
                }
            });

            // màj du temps restant.
            time = COUNTDOWN_DURATION - (int) Math.floor(Utils.calculateSeconds(countdown));

            try {
                Thread.sleep(100); // une actualisation du thread toutes les 100ms est suffisante, les joueurs ne jouent pas encore.
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // sur Android, la modification des vues ne peut se faire que sur le thread principal. la fonction runOnUiThread permet d'exécuter le code sur ce thread principal.
        // lancement de la partie à la fin du thread.
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.startGame();
            }
        });
    }
}
