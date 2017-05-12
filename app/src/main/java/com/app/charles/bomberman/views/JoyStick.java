package com.app.charles.bomberman.views;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.app.charles.bomberman.R;
import com.app.charles.bomberman.java.Direction;

/**
 * Création d'une vue joystick pour les déplacements d'un joueur.
 */

public class JoyStick extends FrameLayout implements View.OnTouchListener {

    // le joystick doit être déplacé d'au moins 20% pour renvoyer une valeur.
    private static final double MINIMAL_OFFSET = 0.2;

    // positions en x et y du bouton du joystick.
    private float dX, dY;

    // vues
    private FloatingActionButton mMove; // bouton du joystick.
    private View mMoveContainer; // arrière plan du joystick.

    public JoyStick(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    /**
     * Initialisation des vues du joystick
     * @param context context dans lequel est créé le joystick.
     */
    private void initViews(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_joystick, this);

        mMove = (FloatingActionButton) findViewById(R.id.move);
        mMoveContainer = findViewById(R.id.move_container);

        // le joystick est sensible au touché
        mMove.setOnTouchListener(this);
    }

    /**
     * Méthode calculant la direction selon la position du joystick.
     * @return retourne la direction.
     */
    public Direction getDirection() {
        float centerX = mMove.getX() + mMove.getWidth() / 2,
                centerY = mMove.getY() + mMove.getHeight() / 2,
                centerXContainer = mMoveContainer.getX() + mMoveContainer.getWidth() / 2,
                centerYContainer = mMoveContainer.getY() + mMoveContainer.getHeight() / 2;

        // position du bouton du joystick selon son conteneur.
        float diffX = centerX - centerXContainer;
        float diffY = centerY - centerYContainer;

        float offsetX = diffX / (mMoveContainer.getWidth() / 2),
                offsetY = diffY / (mMoveContainer.getHeight() / 2);

        // si le joystick n'est pas assez bougé, alors il est considéré comme nul.
        if (offsetX < MINIMAL_OFFSET && offsetX > -MINIMAL_OFFSET)
            offsetX = 0;
        if (offsetY < MINIMAL_OFFSET && offsetY > -MINIMAL_OFFSET)
            offsetY = 0;
        float offset = offsetX * offsetX + offsetY * offsetY;

        // renvoie de la direction correspondante.
        if (offset == 0)
            return new Direction(offset, Direction.STOP);
        else if (Math.abs(offsetX) > Math.abs(offsetY)) {
            if (offsetX > 0)
                return new Direction(offset, Direction.RIGHT);
            else
                return new Direction(offset, Direction.LEFT);

        } else {
            if (offsetY > 0)
                return new Direction(offset, Direction.BOTTOM);
            else
                return new Direction(offset, Direction.TOP);
        }
    }

    /**
     * Méthode appelée lorsqu'une vue est touchée.
     * @param v vue touchée.
     * @param event évènement effectué sur la vue.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == mMove)
            switch (event.getAction()) {

                // si le bouton est appuyé initialisation du position du doigt sur le bouton.
                case MotionEvent.ACTION_DOWN:
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    break;

                // si le bouton est déplacé on déplace le joystick selon la position du doigt.
                case MotionEvent.ACTION_MOVE:
                    float mPosX = event.getRawX() + dX,
                            mPosY = event.getRawY() + dY,
                            mBorderRadius = mMoveContainer.getWidth() / 2,
                            mCenterX = mMoveContainer.getX() + mBorderRadius - mMove.getWidth() / 2,
                            mCenterY = mMoveContainer.getY() + mBorderRadius - mMove.getWidth() / 2;// + dY;


                    double abs = Math.sqrt((mPosX - mCenterX) * (mPosX - mCenterX)
                            + (mPosY - mCenterY) * (mPosY - mCenterY));

                    if (abs > mBorderRadius) {
                        mPosX = (int) ((mPosX - mCenterX) * mBorderRadius / abs + mCenterX);
                        mPosY = (int) ((mPosY - mCenterY) * mBorderRadius / abs + mCenterY);
                    }

                    v.animate()
                            .x(mPosX)
                            .y(mPosY)
                            .setDuration(0)
                            .start();

                    break;

                // si le bouton est relaché on l'anime jusqu'au centre.
                case MotionEvent.ACTION_UP:
                    mMove.animate()
                            .x(mMoveContainer.getX() + mMoveContainer.getWidth() / 2 - mMove.getWidth() / 2)
                            .y(mMoveContainer.getY() + mMoveContainer.getHeight() / 2 - mMove.getHeight() / 2)
                            .setDuration(100)
                            .start();
                    break;

                default:
                    return false;
            }

        return true;
    }
}
