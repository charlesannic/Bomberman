package com.app.charles.bomberman.threads;

import android.widget.GridLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.charles.bomberman.R;
import com.app.charles.bomberman.activities.GameActivity;
import com.app.charles.bomberman.game.Grid;
import com.app.charles.bomberman.utils.Utils;

/**
 * Created by Charles on 05/05/2017.
 */

public class TimeThread extends Thread {

    private boolean isGameOver = false;
    private GameActivity activity;
    private TextView tvTime;
    private Grid grid;
    private int difficulty;

    public TimeThread(GameActivity activity, TextView tvTime, Grid grid, int difficulty) {
        this.activity = activity;
        this.tvTime = tvTime;
        this.grid = grid;
        this.difficulty = difficulty;
    }

    @Override
    public void run() {
        long countdown = System.currentTimeMillis();

        int time = 120 - (int) Math.floor(Utils.calculateSeconds(countdown));
        while (!activity.getStopTimer() && time >= 0 && !isGameOver) {
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

                    grid.checkBombs();

                    if (activity.isGameOver()) {
                        isGameOver = true;
                        activity.changeScoreBoard();
                        grid = new Grid(activity,
                                (GridLayout) activity.findViewById(R.id.grid),
                                (RelativeLayout) activity.findViewById(R.id.objects_container),
                                difficulty);
                        activity.recreate();
                    }
                }
            });
            time = 120 - (int) Math.floor(Utils.calculateSeconds(countdown));

            grid.update();

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
