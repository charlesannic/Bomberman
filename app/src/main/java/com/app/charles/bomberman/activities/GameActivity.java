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
import com.app.charles.bomberman.threads.CountdownThread;
import com.app.charles.bomberman.threads.PlayerThread;
import com.app.charles.bomberman.threads.TimeThread;
import com.app.charles.bomberman.utils.Utils;
import com.app.charles.bomberman.views.JoyStick;
import com.app.charles.bomberman.views.Preferences;

import java.util.ArrayList;

import static android.view.View.VISIBLE;

public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "GameActivity";

    private Thread countdownThread,
            timeThread,
            playerThread;
    private ArrayList<Thread> botsThread = new ArrayList<>();

    private Boolean stopTimer = true;
    private long previousBackPressed;
    private static Integer scorePlayer1,
            scorePlayer2,
            scorePlayer3,
            scorePlayer4;
    private int difficulty;

    private Grid mGrid;

    private RelativeLayout mContent;
    private TextView mTime,
            mScorePlayer1,
            mScorePlayer2,
            mScorePlayer3,
            mScorePlayer4;
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

        if (scorePlayer1 == null)
            scorePlayer1 = 0;
        if (scorePlayer2 == null)
            scorePlayer2 = 0;
        if (scorePlayer3 == null)
            scorePlayer3 = 0;
        if (scorePlayer4 == null)
            scorePlayer4 = 0;

        Toast.makeText(this, String.valueOf(getIntent().getIntExtra("difficulty", 4)), Toast.LENGTH_LONG).show();
        difficulty = getIntent().getIntExtra("difficulty", 1);

        mGrid = new Grid(this,
                (GridLayout) findViewById(R.id.grid),
                (RelativeLayout) findViewById(R.id.objects_container),
                difficulty);

        //Log.i(TAG, "onCreate: " + scorePlayer1 + " " + scorePlayer2 + " " + scorePlayer3 + " " + scorePlayer4);

        mContent = (RelativeLayout) findViewById(R.id.main_content);
        mTime = (TextView) findViewById(R.id.time);
        mScorePlayer1 = (TextView) findViewById(R.id.score_player_1);
        mScorePlayer2 = (TextView) findViewById(R.id.score_player_2);
        mScorePlayer3 = (TextView) findViewById(R.id.score_player_3);
        mScorePlayer4 = (TextView) findViewById(R.id.score_player_4);
        updateScoreBoard();
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
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopTimer = false;
        mGrid.resumeBombs();

        countdownThread = new CountdownThread(this, mTime);
        countdownThread.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer = true;
        mGrid.stopBombs();
    }

    public void threads() {
        mPoseBomb.setOnClickListener(this);

        timeThread = new TimeThread(this, mTime, mGrid, difficulty);
        timeThread.start();

        playerThread = new PlayerThread(this, mGrid.getPlayer(), mGrid, mJoyStick);
        playerThread.start();

        for (final Bot bot : mGrid.getBots()) {
            Thread botThread = new PlayerThread(this, bot, mGrid, mJoyStick);
            botThread.start();
            botsThread.add(botThread);
        }
    }

    public boolean isGameOver() {
        return nbPlayersAlive() < 2;
    }

    private int nbPlayersAlive() {
        int nbAlives = 0;

        String s = "";
        for (Player p : mGrid.getAllPlayers())
            if (p.isPlayerAlive()) {
                s += String.valueOf(p.getId()) + " ";
                nbAlives++;
            }
        Log.i(TAG, "nbPlayersAlive: " + s);

        return nbAlives;
    }

    public void changeScoreBoard() {
        for (Player player : mGrid.getAllPlayers()) {
            if (player.isPlayerAlive())
                switch (player.getId()) {
                    case 1:
                        Toast.makeText(this, "Le vainceur est 1", Toast.LENGTH_LONG).show();
                        scorePlayer1++;
                        break;
                    case 2:
                        Toast.makeText(this, "Le vainceur est 2", Toast.LENGTH_LONG).show();
                        scorePlayer2++;
                        break;
                    case 3:
                        Toast.makeText(this, "Le vainceur est 3", Toast.LENGTH_LONG).show();
                        scorePlayer3++;
                        break;
                    case 4:
                        Toast.makeText(this, "Le vainceur est 4", Toast.LENGTH_LONG).show();
                        scorePlayer4++;
                        break;
                }
        }

        updateScoreBoard();
    }

    public void updateScoreBoard() {
        //Log.i(TAG, "updateScoreBoard: " + scorePlayer1 + " " + scorePlayer2 + " " + scorePlayer3 + " " + scorePlayer4);
        mScorePlayer1.setText(String.valueOf(scorePlayer1));
        mScorePlayer2.setText(String.valueOf(scorePlayer2));
        mScorePlayer3.setText(String.valueOf(scorePlayer3));
        mScorePlayer4.setText(String.valueOf(scorePlayer4));
    }

    @Override
    public void onBackPressed() {
        if (Utils.calculateSeconds(previousBackPressed) > 2.5) {
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

    public Boolean getStopTimer() {
        return stopTimer;
    }
}
