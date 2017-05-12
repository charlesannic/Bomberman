package com.app.charles.bomberman.game;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.app.charles.bomberman.R;
import com.app.charles.bomberman.utils.Utils;

/**
 * Classe gérant les bombes placées sur la grille.
 */

@SuppressWarnings("WeakerAccess")
public class Bomb {

    // statuts d'une bombe.
    public static final int PLAYER_STILL_ON_BOMB = 3; // si un joueur est encore dessus alors il peut se déplacer comme sur une case vide.
    public static final int POSED = 4; // sinon la bombe agit comme un mur.

    private Context mContext;

    // attributs.
    private int bombStatus = PLAYER_STILL_ON_BOMB, power;
    private long begin;
    private double secondsCount;
    private boolean isStopped;
    private boolean isPBomb;

    // vues et attributs de la vue de la bombe.
    private ImageView v;
    private int xPosition;
    private int yPosition;
    private int size;

    @SuppressWarnings({"ConstantConditions","InflateParams"})
    public Bomb(Context context, int xPosition, int yPosition, int size, int power, boolean isPBomb) {
        this.mContext = context;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.size = size;
        this.isPBomb = isPBomb;

        // s'il s'agit d'un P Bomb, sa puissance est maximale et l'explosion parcourt toute la grille.
        if(isPBomb)
            this.power = Integer.MAX_VALUE;
        else
            this.power = power;

        isStopped = false;
        begin = System.currentTimeMillis();
        secondsCount = 0;

        // inflation de la vue de la bombe et lancement de l'animation de celle-ci.
        LayoutInflater inflater = LayoutInflater.from(mContext);
        v = (ImageView) inflater.inflate(R.layout.bomb, null, false);
        AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) mContext.getDrawable(R.drawable.ic_bomb_animated);
        v.setImageDrawable(animatedVectorDrawable);
        animatedVectorDrawable.start();

        // modification de la position de la bombe sur la grille.
        v.setX(this.xPosition);
        v.setY(this.yPosition);
    }

    /**
     * Modification de la taille de la vue d'une bombe.
     */
    public void resizeView() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.height = size;
        params.width = size;
        v.setLayoutParams(params);
    }

    public double getSeconds() {
        return Utils.calculateSeconds(begin) + secondsCount;
    }

    /**
     * Mise en pause du compte à rebours de la bombe.
     */
    public void stopTimer() {
        if (!isStopped) {
            secondsCount += Utils.calculateSeconds(begin);
            isStopped = true;
        }
    }

    /**
     * Reprise du compte à rebours de la bombe.
     */
    public void resumeTimer() {
        if (isStopped) {
            begin = System.currentTimeMillis();
            isStopped = false;
        }
    }

    public View getV() {
        return v;
    }

    public int getPower() {
        return power;
    }

    public int getX() {
        return xPosition;
    }

    public int getXGrid() {
        return xPosition / size;
    }

    public int getY() {
        return yPosition;
    }

    public int getYGrid() {
        return yPosition / size;
    }

    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public int getSize() {
        return size;
    }

    public int getBombStatus() {
        return bombStatus;
    }

    public void setBombStatus(int bombStatus) {
        this.bombStatus = bombStatus;
    }

    public boolean isPBomb() {
        return isPBomb;
    }
}
