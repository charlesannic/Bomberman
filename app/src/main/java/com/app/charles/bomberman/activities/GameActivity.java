package com.app.charles.bomberman.activities;

import android.content.pm.ActivityInfo;
import android.graphics.Point;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.charles.bomberman.R;
import com.app.charles.bomberman.game.Bot;
import com.app.charles.bomberman.game.Grid;
import com.app.charles.bomberman.game.Player;
import com.app.charles.bomberman.threads.CountdownThread;
import com.app.charles.bomberman.threads.LastSecondsThread;
import com.app.charles.bomberman.threads.PlayerThread;
import com.app.charles.bomberman.threads.TimeThread;
import com.app.charles.bomberman.utils.Utils;
import com.app.charles.bomberman.views.JoyStick;

import java.util.ArrayList;
import java.util.Random;

/**
 * Activité gérant une partie.
 */

@SuppressWarnings("FieldCanBeLocal")
public class GameActivity extends AppCompatActivity implements View.OnClickListener {

    // threads
    private Thread thdCountdown, thdTime, thdPlayer, thdLastSeconds;
    private ArrayList<Thread> thdBots = new ArrayList<>();

    // variables
    private boolean stopGame = true, gridShown = false;
    private long previousBackPressed;
    private static Integer scorePlayer1, scorePlayer2, scorePlayer3, scorePlayer4;
    private int difficulty;

    // grille
    private Grid grid;

    // vues
    private RelativeLayout rlContent, rlGridLayout;
    private LinearLayout llPlayer3, llPlayer4;
    private TextView tvTime, tvScorePlayer1, tvScorePlayer2, tvScorePlayer3, tvScorePlayer4;
    private FloatingActionButton fabPoseBomb;
    private JoyStick joyStick;
    private Snackbar skbExit;
    private ImageView ivPause;
    private AnimatedVectorDrawable avdPauseToPlay, avdPlayToPause;

    /**
     * Méthode appelée lors de la création de l'activité.
     * Initialisation des composants (variables, vues ...) de l'activité.
     *
     * @param savedInstanceState .
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // définition du thème selon les préférences.
        setTheme(Utils.getTheme(this));

        // choix du layout selon les préférences sur l'inversement des contrôles.
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getResources().getString(R.string.inverted_controls), false))
            setContentView(R.layout.activity_game_inverted_controls);
        else
            setContentView(R.layout.activity_game);

        // définition de l'orientation de l'écran selon les préférences de l'utilisateur.
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getResources().getString(R.string.orientation), false))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        else
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // initialisation des vues.
        avdPauseToPlay = (AnimatedVectorDrawable) getDrawable(R.drawable.ic_pause_to_play);
        avdPlayToPause = (AnimatedVectorDrawable) getDrawable(R.drawable.ic_play_to_pause);
        ivPause = (ImageView) findViewById(R.id.ic_pause);
        rlContent = (RelativeLayout) findViewById(R.id.main_content);
        rlGridLayout = (RelativeLayout) findViewById(R.id.grid_container);
        llPlayer3 = (LinearLayout) findViewById(R.id.player_3);
        llPlayer4 = (LinearLayout) findViewById(R.id.player_4);
        tvTime = (TextView) findViewById(R.id.time);
        tvScorePlayer1 = (TextView) findViewById(R.id.score_player_1);
        tvScorePlayer2 = (TextView) findViewById(R.id.score_player_2);
        tvScorePlayer3 = (TextView) findViewById(R.id.score_player_3);
        tvScorePlayer4 = (TextView) findViewById(R.id.score_player_4);
        fabPoseBomb = (FloatingActionButton) findViewById(R.id.bomb);
        joyStick = (JoyStick) findViewById(R.id.joystick);

        // modification du bouton de mise en pause.
        ivPause.setImageDrawable(avdPlayToPause);
        avdPlayToPause.start();
        ivPause.setOnClickListener(this);

        // initialisation du score si nécessaire.
        if (scorePlayer1 == null)
            scorePlayer1 = 0;
        if (scorePlayer2 == null)
            scorePlayer2 = 0;
        if (scorePlayer3 == null)
            scorePlayer3 = 0;
        if (scorePlayer4 == null)
            scorePlayer4 = 0;

        // récupération de la difficulté sélectionnée lors du lancement de la partie.
        difficulty = getIntent().getIntExtra("difficulty", 1);

        // création de la grille de jeu
        grid = new Grid(this,
                (GridLayout) findViewById(R.id.grid),
                (RelativeLayout) findViewById(R.id.objects_container),
                difficulty);

        // affichage sur le tableau des scores des joueurs présents dans la partie uniquement.
        if (difficulty <= 1) {
            llPlayer4.setVisibility(View.INVISIBLE);
            if (difficulty <= 0)
                llPlayer3.setVisibility(View.INVISIBLE);
        }

        // mise à jour du tableau des scores.
        updateScoreBoard();

        // définition de la SnackBar permettant de quitter une partie.
        // la valeur du clic précédent est égal à MIN_VALUE pour ne pas pouvoir quitter d'un simple clic lors du lancement de la partie.
        previousBackPressed = Integer.MIN_VALUE;
        skbExit = Snackbar.make(rlContent, "Quitter la partie ?", Snackbar.LENGTH_LONG)
                .setAction(getResources().getString(android.R.string.yes), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        overridePendingTransition(R.anim.anim_activity_back_in, R.anim.anim_activity_back_out);
                    }
                });

        // animation de transition du haut vers le bas pour la grille du jeu.
        // la méthode addOnGlobalLayoutListener est appelée lorsque la vue est déssinée et permet d'assurer que la hauteur ne ce sera pas encore nulle.
        rlGridLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // si la grille n'a pas déjà été dessinée et que sa taille est supérieure à 0, on lance l'animation.
                if (!gridShown && rlGridLayout.getMeasuredHeight() > 0) {
                    TranslateAnimation anim = new TranslateAnimation(0, 0, -rlGridLayout.getMeasuredHeight(), 0);
                    anim.setDuration(1000); // durée de 1s.
                    anim.setInterpolator(new AccelerateDecelerateInterpolator()); // rend l'animation plus agréable en accélérant et décélérant.
                    rlGridLayout.startAnimation(anim);

                    gridShown = true;
                }
            }
        });
    }

    /**
     * Méthode appelée lorsque l'activité est reprise (après le onCreate() ou lorsque l'utilisateur a quitté l'activité sans la fermer puis est revenu dessus).
     */
    @Override
    protected void onResume() {
        super.onResume();

        resumeGame();
    }

    /**
     * Reprise de la partie en relançant les threads.
     */
    private void resumeGame() {
        stopGame = false;
        thdCountdown = new CountdownThread(this, tvTime);
        thdCountdown.start();
    }

    /**
     * Méthode appelée lorsque l'activité est mise en pause (avant que l'activité soit terminée ou si l'utilisateur quitte l'application sans la fermer).
     */
    @Override
    protected void onPause() {
        super.onPause();

        stopGame();
    }

    /**
     * Méthode terminant l'activité en cours et la recréer (onCreate() sera à nouveau appeler).
     */
    @Override
    public void recreate() {
        // avant que l'activité soit recréée pour une nouvelle partie, on anime la grille dans le sens inverse dans lequel elle est apparue.
        TranslateAnimation anim = new TranslateAnimation(0, 0, 0, -rlGridLayout.getMeasuredHeight());
        anim.setDuration(1000);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            // à la fin de l'animation, on recréer l'activité.
            @Override
            public void onAnimationEnd(Animation animation) {
                GameActivity.super.recreate();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        rlGridLayout.startAnimation(anim);
    }

    /**
     * Lancement de la partie.
     */
    public void startGame() {
        // l'appui sur le bouton de pose de bombe n'est rendu possible que lorsque la partie a commencé (pour des raisons évidentes).
        fabPoseBomb.setOnClickListener(this);

        // on créer et lance les threads de la partie.
        createThreads();
        startThreads();
    }

    /**
     * Création des threads.
     */
    public void createThreads() {
        // cette méthode peut-être appelée alors que les threads sont déjà créés, il faut donc s'assurer de ne pas en créer d'autres si la création a déjà eu lieu.
        if (thdTime == null) {
            thdTime = new TimeThread(this, tvTime, grid, difficulty);
            thdPlayer = new PlayerThread(this, grid.getPlayer(), grid, joyStick, grid.getPlayerView().getWidth() / 13);
            for (final Bot bot : grid.getBots()) {
                Thread botThread = new PlayerThread(this, bot, grid, joyStick, grid.getPlayerView().getWidth() / 13);
                thdBots.add(botThread);
            }
            thdLastSeconds = new LastSecondsThread(this, grid, difficulty);
        }
    }

    /**
     * Démarrage des threads.
     */
    public void startThreads() {
        thdTime.start();
        thdPlayer.start();
        for (final Thread thread : thdBots) {
            thread.start();
        }

        // on relance le compte à rebours des bombes sur le plateau
        grid.resumeBombs();
    }

    /**
     * Mise en pause de la partie.
     */
    public void stopGame() {
        // l'appui sur le bouton de pose de bombe n'est rendu possible que lorsque la partie a commencé (pour des raisons toujours évidentes).
        fabPoseBomb.setOnClickListener(null);

        stopGame = true;

        // on stoppe le compte à rebours des bombes sur le plateau
        grid.stopBombs();
    }

    /**
     * Lancement du threads réduisant la taille du plateau lors des dernières secondes.
     */
    public void startLastSecondsThread() {
        // cette méthode peut-être appelée alors que le threads est déjà lancé alors on vérifie qu'il n'est pas déjà actif.
        if (!thdLastSeconds.isAlive())
            thdLastSeconds.start();
    }

    /**
     * Détermine si une partie est terminée.
     *
     * @return vrai si le nombre de joueurs restants est inférieur à 2 ou si l'utilisateur est mort.
     */
    public boolean isGameOver() {
        boolean watchUntilEnd = false;
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getResources().getString(R.string.watch_until_end), false))
            watchUntilEnd = true;

        return nbPlayersAlive() < 2 || (!watchUntilEnd && !grid.getPlayer().isPlayerAlive());
    }

    /**
     * @return le nombre de joueurs encore en vie sur le plateau.
     */
    private int nbPlayersAlive() {
        int nbAlives = 0;

        for (Player p : grid.getAllPlayers())
            if (p.isPlayerAlive())
                nbAlives++;

        return nbAlives;
    }

    /**
     * Change le score des joueurs puis met à jour le tableau d'affichage.
     */
    public void changeScoreBoard() {
        // si le nombre de joueurs est supérieur ou égal à 2 (que l'utilisateur est mort donc) on choisi un vainqueur aléatoirement.
        if (nbPlayersAlive() >= 2) {
            ArrayList<Player> playersAlive = new ArrayList<>();
            for (Player player : grid.getAllPlayers())
                if (player.isPlayerAlive())
                    playersAlive.add(player);

            Random rnd = new Random();
            int rndInt = rnd.nextInt(playersAlive.size());
            updateScore(playersAlive.get(rndInt).getId());
        }
        // sinon on recherche le joueur encore envie et met à jour son score.
        else
            for (Player player : grid.getAllPlayers()) {
                if (player.isPlayerAlive()) {
                    updateScore(player.getId());
                    break;
                }
            }

        updateScoreBoard();
    }

    /**
     * Met à jour les score.
     *
     * @param id identifiant du joueur dont le score doit être augmenté.
     */
    private void updateScore(int id) {
        switch (id) {
            case 1:
                scorePlayer1++;
                break;
            case 2:
                scorePlayer2++;
                break;
            case 3:
                scorePlayer3++;
                break;
            case 4:
                scorePlayer4++;
                break;
        }
    }

    /**
     * Mise à jour du tableau des scores.
     */
    public void updateScoreBoard() {
        tvScorePlayer1.setText(String.valueOf(scorePlayer1));
        tvScorePlayer2.setText(String.valueOf(scorePlayer2));
        tvScorePlayer3.setText(String.valueOf(scorePlayer3));
        tvScorePlayer4.setText(String.valueOf(scorePlayer4));
    }

    /**
     * Méthode appelée lorsque l'utilisateur souhaite retourner en arrière et quitter la partie.
     */
    @Override
    public void onBackPressed() {
        // si l'appui précédent est supérieur à 3 secondes, on met à jour le dernier appui et affiche la SnackBar proposant de quitter la partie.
        if (Utils.calculateSeconds(previousBackPressed) > 3) {
            previousBackPressed = System.currentTimeMillis();
            skbExit.show();
        }
        // sinon, même si l'utilisateur n'a pas choisi la SnackBar pour quitter la partie, on termine l'activité avant une animation.
        else {
            super.onBackPressed();
            overridePendingTransition(R.anim.anim_activity_back_in, R.anim.anim_activity_back_out);
        }
    }

    /**
     * Méthode appelée lorsque l'on clique sur une vue.
     *
     * @param v vue appelant la méthode.
     */
    @Override
    public void onClick(View v) {
        // s'il s'agit du bouton de pose d'une bombe, on pose une bombe sur la grille
        if (v == fabPoseBomb)
            grid.poseBomb(grid.getPlayer());
            // s'il s'agit du bouton de mise en pause de la partie ...
        else if (v == ivPause) {
            // .. on lance l'animation appropriée.
            if (ivPause.getDrawable() != avdPauseToPlay) {
                ivPause.setImageDrawable(avdPauseToPlay);
                avdPauseToPlay.start();
                stopGame();
            } else {
                ivPause.setImageDrawable(avdPlayToPause);
                avdPlayToPause.start();
                resumeGame();
            }
        }
    }

    public boolean gameStoped() {
        return stopGame;
    }

    /**
     * @return les cases considérées comme dangereuses par le threads qui réduit la taille de la grille en fin de partie.
     */
    public ArrayList<Point> getUnsafeCases() {
        if (thdLastSeconds != null)
            return ((LastSecondsThread) thdLastSeconds).getUnsafeCases();
        else
            return new ArrayList<>();
    }
}
