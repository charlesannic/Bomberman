package com.app.charles.bomberman.activities;

import android.graphics.drawable.AnimatedVectorDrawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.app.charles.bomberman.R;
import com.app.charles.bomberman.game.Bot;
import com.app.charles.bomberman.game.Grid;
import com.app.charles.bomberman.game.Player;
import com.app.charles.bomberman.java.Direction;
import com.app.charles.bomberman.utils.Utils;
import com.app.charles.bomberman.views.JoyStick;
import com.app.charles.bomberman.views.Preferences;

import static android.view.View.VISIBLE;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "GameActivity";
    private static final int SPEED = 20;

    private boolean stopTimer = true;
    private long previousBackPressed;
    private long countdown;

    private static Grid mGrid;

    private CoordinatorLayout mContent;
    private TextView mTime;
    private FloatingActionButton mPoseBomb;
    private JoyStick mJoyStick;

    private Snackbar mSnackbarExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(Utils.getTheme(this));

        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getResources().getString(R.string.inverted_controls), false))
            setContentView(R.layout.activity_game_inverted_controls);
        else
            setContentView(R.layout.activity_game);

        mGrid = new Grid(this,
                (GridLayout) findViewById(R.id.grid),
                (RelativeLayout) findViewById(R.id.objects_container));

        mContent = (CoordinatorLayout) findViewById(R.id.main_content);
        mTime = (TextView) findViewById(R.id.time);
        mPoseBomb = (FloatingActionButton) findViewById(R.id.bomb);
        mJoyStick = (JoyStick) findViewById(R.id.joystick);

        previousBackPressed = Integer.MIN_VALUE;
        mSnackbarExit = Snackbar.make(mContent, "Leave the game?", Snackbar.LENGTH_LONG)
                .setAction(getResources().getString(android.R.string.yes), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                    }
                });

        mPoseBomb.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopTimer = false;
        mGrid.resumeBombs();

        new Thread() {
            public void run() {
                countdown = System.currentTimeMillis();

                int time = 3 - (int) Math.floor(Utils.calculateSeconds(countdown));
                while(time >= 0) {
                    final int seconds = time%60;
                    final int minutes = time/60;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTime.setText((minutes < 10 ? "0" : "")
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

                timer();
            }
        }.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer = true;
        mGrid.stopBombs();
    }

    private void timer() {
        new Thread() {
            public void run() {
                countdown = System.currentTimeMillis();

                int time = 120 - (int) Math.floor(Utils.calculateSeconds(countdown));
                while(time >= 0) {
                    final int seconds = time%60;
                    final int minutes = time/60;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTime.setText((minutes < 10 ? "0" : "")
                                    + String.valueOf(minutes)
                                    + ":"
                                    + (seconds < 10 ? "0" : "")
                                    + String.valueOf(seconds));
                        }
                    });
                    time = 120 - (int) Math.floor(Utils.calculateSeconds(countdown));

                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();

        new Thread() {
            public void run() {
                while (!stopTimer && mGrid.getPlayer().isPlayerAlive()) {
                    //Log.i(TAG, "run: 1");
                    try {
                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                if (isGameOver()) {
                                    finish();
                                }

                                Direction direction = mJoyStick.getDirection();
                                //Log.i(TAG, "run: " + direction.getDirection() + " " + direction.getOffset());
                                if (direction.getDirection() != Direction.STOP && mGrid.getPlayerView() != null) {
                                    if (direction.getDirection() == Direction.LEFT)
                                        mGrid.moveLeft(mGrid.getPlayer(), SPEED * (-direction.getOffset()));
                                    else if (direction.getDirection() == Direction.RIGHT)
                                        mGrid.moveRight(mGrid.getPlayer(), SPEED * direction.getOffset());
                                    else if (direction.getDirection() == Direction.TOP)
                                        mGrid.moveTop(mGrid.getPlayer(), SPEED * (-direction.getOffset()));
                                    else if (direction.getDirection() == Direction.B0TT0M)
                                        mGrid.moveBottom(mGrid.getPlayer(), SPEED * direction.getOffset());
                                }

                                mGrid.checkBombs();
                            }
                        });
                        Thread.sleep(30);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                mPoseBomb.setOnClickListener(null);
            }
        }.start();

        for(final Bot bot : mGrid.getBots()) {
            new Thread() {
                public void run() {
                    while (!stopTimer/* && bot.isPlayerAlive()*/) {

                        runOnUiThread(new Runnable() {

                            @Override
                            public void run() {
                                mGrid.getAi().whereIGo(bot);

                                Direction direction = bot.getDirection();

                                if (direction.getDirection() != Direction.STOP && bot.getSize() > 0) {
                                    if(direction.isPoseBomb())
                                        mGrid.poseBomb(bot);
                                    else if (direction.getDirection() == Direction.LEFT)
                                        mGrid.moveLeft(bot, SPEED * (-direction.getOffset()));
                                    else if (direction.getDirection() == Direction.RIGHT)
                                        mGrid.moveRight(bot, SPEED * direction.getOffset());
                                    else if (direction.getDirection() == Direction.TOP)
                                        mGrid.moveTop(bot, SPEED * (-direction.getOffset()));
                                    else if (direction.getDirection() == Direction.B0TT0M)
                                        mGrid.moveBottom(bot, SPEED * direction.getOffset());
                                }
                            }
                        });

                        try {

                            Thread.sleep(30);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }.start();
        }
    }

    private boolean isGameOver() {
        int nbAlives = 0;

        for(Player p : mGrid.getAllPlayers())
            if(p.isPlayerAlive())
                nbAlives++;

        return nbAlives < 2;
    }

    @Override
    public void onBackPressed() {
            if(Utils.calculateSeconds(previousBackPressed) > 2.5) {
                previousBackPressed = System.currentTimeMillis();
                mSnackbarExit.show();
                //Toast.makeText(this, "Press back again", Toast.LENGTH_SHORT).show();
            } else
                super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if (v == mPoseBomb)
            mGrid.poseBomb(mGrid.getPlayer());
    }
}
