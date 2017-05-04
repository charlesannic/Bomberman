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
 * Created by Charles on 07-Feb-17.
 */

public class Bomb {

    public static final int PLAYER_STILL_ON_BOMB = 3;
    public static final int POSED = 4;

    private Context mContext;

    private int bombStatus = PLAYER_STILL_ON_BOMB,
            power;
    private long begin;
    private double secondsCount;
    private boolean isStopped;

    private ImageView v;
    private int xPosition;
    private int yPosition;
    private int size;

    public Bomb(Context context, int xPosition, int yPosition, int size, int power) {
        this.mContext = context;
        this.xPosition = xPosition;
        this.yPosition = yPosition;
        this.size = size;
        this.power = power;

        isStopped = false;
        begin = System.currentTimeMillis();
        secondsCount = 0;

        LayoutInflater inflater = LayoutInflater.from(mContext);
        v = (ImageView) inflater.inflate(R.layout.bomb, null, false);


        final AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) mContext.getDrawable(R.drawable.ic_bomb_animated);
        v.setImageDrawable(animatedVectorDrawable);
        animatedVectorDrawable.start();
        v.setX(this.xPosition);
        v.setY(this.yPosition);
    }

    public void setView() {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();
        params.height = size;
        params.width = size;
        v.setLayoutParams(params);
    }

    public double getSeconds() {
        return Utils.calculateSeconds(begin) + secondsCount;
    }

    public void stopTimer() {
        if (!isStopped) {
            secondsCount += Utils.calculateSeconds(begin);
            isStopped = true;
        }
    }

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
}
