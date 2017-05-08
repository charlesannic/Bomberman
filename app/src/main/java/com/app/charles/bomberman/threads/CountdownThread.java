package com.app.charles.bomberman.threads;

import android.widget.TextView;

import com.app.charles.bomberman.activities.GameActivity;
import com.app.charles.bomberman.utils.Utils;

/**
 * Created by Charles on 05/05/2017.
 */

public class CountdownThread extends Thread {

    private GameActivity activity;
    private TextView tvTime;

    public CountdownThread(GameActivity activity, TextView tvTime) {
        this.activity = activity;
        this.tvTime = tvTime;
    }

    @Override
    public void run() {
        long countdown = System.currentTimeMillis();

        int time = 3 - (int) Math.floor(Utils.calculateSeconds(countdown));
        while (!activity.getStopTimer() && time >= 0) {
            final int seconds = time % 60;
            final int minutes = time / 60;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    tvTime.setText((minutes < 10 ? "0" : "")
                            + String.valueOf(minutes)
                            + ":"
                            + (seconds < 10 ? "0" : "")
                            + String.valueOf(seconds));
                }
            });
            time = 3 - (int) Math.floor(Utils.calculateSeconds(countdown));

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        activity.threads();
    }


}
