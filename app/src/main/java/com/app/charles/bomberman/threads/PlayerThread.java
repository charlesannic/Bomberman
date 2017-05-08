package com.app.charles.bomberman.threads;

import com.app.charles.bomberman.activities.GameActivity;
import com.app.charles.bomberman.game.Bot;
import com.app.charles.bomberman.game.Grid;
import com.app.charles.bomberman.game.Player;
import com.app.charles.bomberman.java.Direction;
import com.app.charles.bomberman.views.JoyStick;

/**
 * Created by Charles on 05/05/2017.
 */

public class PlayerThread extends Thread {

    private static final int SPEED = 10;

    private GameActivity activity;
    private Player player;
    private Grid grid;
    private JoyStick joyStick;

    public PlayerThread(GameActivity activity, Player player, Grid grid, JoyStick joyStick) {
        this.activity = activity;
        this.player = player;
        this.grid = grid;
        this.joyStick = joyStick;
    }

    @Override
    public void run() {
        while (!activity.getStopTimer() && player.isPlayerAlive()) {
            Direction d;
            if(player instanceof Bot) {
                grid.getAi().whereIGo((Bot)player);
                d = ((Bot)player).getDirection();
            } else
                d = joyStick.getDirection();

            final Direction direction = d;
            try {
                activity.runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        if (direction.getDirection() != Direction.STOP && grid.getPlayerView() != null) {
                            if (player instanceof Bot && direction.isPoseBomb())
                                grid.poseBomb(player);
                            else if (direction.getDirection() == Direction.LEFT)
                                grid.moveLeft(player, (SPEED + player.getSpeed()) * (-direction.getOffset()));
                            else if (direction.getDirection() == Direction.RIGHT)
                                grid.moveRight(player, (SPEED + player.getSpeed()) * direction.getOffset());
                            else if (direction.getDirection() == Direction.TOP)
                                grid.moveTop(player, (SPEED + player.getSpeed()) * (-direction.getOffset()));
                            else if (direction.getDirection() == Direction.BOTTOM)
                                grid.moveBottom(player, (SPEED + player.getSpeed()) * direction.getOffset());
                        }
                    }
                });
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
