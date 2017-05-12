package com.app.charles.bomberman.activities;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.app.charles.bomberman.R;
import com.app.charles.bomberman.utils.Utils;
import com.app.charles.bomberman.views.Preferences;

/**
 * Activité d'acceuil de l'application.
 */

@SuppressWarnings("FieldCanBeLocal")
public class HomeActivity extends AppCompatActivity implements View.OnClickListener,
        Preferences.PreferencesListener {

    // attributs
    private boolean mustRestart = false;

    // vues
    private View vShadow;
    private Button btnEasy, btnNormal, btnHard;
    private ImageView ivBomberman;
    private LinearLayout llBottomSheet;
    private BottomSheetBehavior bottomSheetBehavior;
    private ImageView ivExpandView;
    private AnimatedVectorDrawable avdExpandDrawable, avdCollapseDrawable;
    private Preferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // définition du thème selon les préférences.
        setTheme(Utils.getTheme(this));

        // création de la vue de l'activité.
        setContentView(R.layout.activity_home);

        // initialisation des vues.
        ivBomberman = (ImageView) findViewById(R.id.iv_bomberman);
        ivBomberman.setImageResource(Utils.getBombermanImage(this));
        vShadow = findViewById(R.id.shadow);
        btnEasy = (Button) findViewById(R.id.easy);
        btnNormal = (Button) findViewById(R.id.normal);
        btnHard = (Button) findViewById(R.id.hard);
        llBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        ivExpandView = (ImageView) findViewById(R.id.ic_expand);
        preferences = (Preferences) findViewById(R.id.preferences);
        avdExpandDrawable = (AnimatedVectorDrawable) getDrawable(R.drawable.ic_expand);
        avdCollapseDrawable = (AnimatedVectorDrawable) getDrawable(R.drawable.ic_collapse);
        ivExpandView.setImageDrawable(avdCollapseDrawable);
        bottomSheetBehavior = BottomSheetBehavior.from(llBottomSheet);

        // initialisation du comportement du panneau des préférences.
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                // si le panneau est caché, l'ombre est cachée et l'activité redémarre si nécessaire.
                if (newState == BottomSheetBehavior.STATE_HIDDEN || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    vShadow.setVisibility(View.GONE);
                    if (mustRestart) {
                        recreate();
                        mustRestart = false;
                    }
                }
                // sinon l'ombre est cachée.
                else {
                    vShadow.setVisibility(View.VISIBLE);
                }
            }

            /**
             * Méthode appelée lors du glissement du panneau.
             * @param bottomSheet vue glissée.
             * @param slideOffset position de la vue (0 cachée, 1 développée).
             */
            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // modification de la transparence de l'ombre.
                vShadow.setAlpha(slideOffset);

                // si le panneau est déplacé à 90%, on anime la flèche.
                if (slideOffset > 0.9 && ivExpandView.getDrawable() != avdExpandDrawable) {
                    ivExpandView.setImageDrawable(avdExpandDrawable);
                    avdExpandDrawable.start();
                } else if (slideOffset < 0.9 && ivExpandView.getDrawable() != avdCollapseDrawable) {
                    ivExpandView.setImageDrawable(avdCollapseDrawable);
                    avdCollapseDrawable.start();
                }
            }
        });

        // listeners.
        btnEasy.setOnClickListener(this);
        btnNormal.setOnClickListener(this);
        btnHard.setOnClickListener(this);
        vShadow.setOnClickListener(this);
        ivExpandView.setOnClickListener(this);
        preferences.setListener(this);
    }

    /**
     * Méthode appelée lorsque l'utilisateur souhaite retourner en arrière et quitter la partie.
     */
    @Override
    public void onBackPressed() {
        // si le panneau des préférences n'est pas développé, l'activité se termine.
        if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
            super.onBackPressed();
        // sinon on cache le panneau des préférences.
        else
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    /**
     * Méthode appelée lorsque l'on clique sur une vue.
     *
     * @param v vue appelant la méthode.
     */
    @Override
    public void onClick(View v) {
        // boutons de lancement de partie
        if (v == btnEasy) {
            // création d'un Intent pour lancer une nouvelle activité.
            Intent intent = new Intent(this, GameActivity.class);
            // ajout de la difficulté sélectionnée pour la communiquer à la prochaine activité.
            intent.putExtra("difficulty", 0);
            // lancement de la nouvelle activité.
            startActivity(intent);
            // ajout d'une animation de transition entre les deux activités.
            overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
        }
        else if (v == btnNormal) {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("difficulty", 1);
            startActivity(intent);
            overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
        }
        else if (v == btnHard) {
            Intent intent = new Intent(this, GameActivity.class);
            intent.putExtra("difficulty", 2);
            startActivity(intent);
            overridePendingTransition(R.anim.anim_activity_in, R.anim.anim_activity_out);
        }
        // si l'ombre est cliquée, on ferme le panneau des préférences.
        else if (v == vShadow)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        // si la flèche d'ouverture du panneau des préférences est cliquée, on ouvre le panneau.
        else if (v == ivExpandView)
            bottomSheetBehavior.setState(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED ?
                    BottomSheetBehavior.STATE_COLLAPSED :
                    BottomSheetBehavior.STATE_EXPANDED);
    }

    /**
     * Méthode appelée si les préférences de l'application ont changées.
     * @param restartHomeActivity vrai si l'activité doit être relancée.
     */
    @Override
    public void onPreferencesChanged(boolean restartHomeActivity) {
        mustRestart = restartHomeActivity;
    }
}
