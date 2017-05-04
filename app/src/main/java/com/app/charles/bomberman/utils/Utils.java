package com.app.charles.bomberman.utils;

import android.content.Context;
import android.preference.PreferenceManager;

import com.app.charles.bomberman.R;

/**
 * Created by Charles on 09-Feb-17.
 */

public class Utils {

    public static int getTheme(Context context) {
        int theme = PreferenceManager.getDefaultSharedPreferences(context).
                getInt(context.getString(R.string.theme),
                        0);

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

    public static double calculateSeconds(long begin) {
        return (System.currentTimeMillis() - begin) / 1000.0;
    }

}
