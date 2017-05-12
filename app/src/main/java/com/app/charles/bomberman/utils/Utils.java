package com.app.charles.bomberman.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import com.app.charles.bomberman.R;

/**
 * Classe comprenant des méthodes utiles à tout moment dans l'application.
 */

public class Utils {

    /**
     * Retourne le thème sélectionné dans les préférences.
     * @param context context de l'activité appelant la méthode.
     * @return retourne le thème.
     */
    public static int getTheme(Context context) {
        // récupération de l'id du thème sélectionné dans les préférences. 0 de base.
        int theme = PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.theme), 0);

        switch (theme) {
            case 1:
                return R.style.AppTheme2;
            case 2:
                return R.style.AppTheme3;
            case 3:
                return R.style.AppTheme4;
            default:
                return R.style.AppTheme;

        }
    }

    /**
     * Retourne l'image du Bomberman à utiliser selon le thème.
     * @param context context de l'activité appelant la méthode.
     * @return l'image du Bomberman correspondant au thème.
     */
    public static int getBombermanImage(Context context) {
        int theme = PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.theme), 0);

        switch (theme) {
            case 1:
                return R.drawable.bomberman_2;
            case 2:
                return R.drawable.bomberman_3;
            case 3:
                return R.drawable.bomberman_4;
            default:
                return R.drawable.bomberman_1;

        }
    }

    /**
     * Calcule le nombre de secondes écoulées depuis un temps initial donné.
     * @param begin heure de début.
     * @return nombre de secondes écoulées.
     */
    public static double calculateSeconds(long begin) {
        return (System.currentTimeMillis() - begin) / 1000.0;
    }

    /**
     * Renvoie le temps au format mm:ss.
     * @param minutes minutes.
     * @param seconds secondes.
     * @return temps formaté.
     */
    public static String formatTime(int minutes, int seconds) {
        // si les secondes ou les minutes ne comporte qu'un seul digit, rajout d'un 0 devant.
        return (minutes < 10 ? "0" : "")
                + String.valueOf(minutes)
                + ":"
                + (seconds < 10 ? "0" : "")
                + String.valueOf(seconds);
    }
}
