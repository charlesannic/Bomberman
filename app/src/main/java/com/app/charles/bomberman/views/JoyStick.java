package com.app.charles.bomberman.views;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.app.charles.bomberman.R;
import com.app.charles.bomberman.java.Direction;

import static android.content.ContentValues.TAG;

/**
 * Created by Charles on 08-Feb-17.
 */

public class JoyStick extends FrameLayout implements View.OnTouchListener {

    private float dX, dY;

    private FloatingActionButton mMove;
    private View mMoveContainer;

    public JoyStick(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context, attrs);
    }

    private void initViews(Context context, AttributeSet attrs) {
        LayoutInflater.from(context).inflate(R.layout.view_joystick, this);

        mMove = (FloatingActionButton) findViewById(R.id.move);
        mMoveContainer = findViewById(R.id.move_container);

        mMove.setOnTouchListener(this);
    }

    public Direction getDirection() {
        float centerX = mMove.getX() + mMove.getWidth() / 2,
                centerY = mMove.getY() + mMove.getHeight() / 2,
                centerXContainer = mMoveContainer.getX() + mMoveContainer.getWidth() / 2,
                centerYContainer = mMoveContainer.getY() + mMoveContainer.getHeight() / 2;

        float diffX = centerX - centerXContainer;
        float diffY = centerY - centerYContainer;

        float offsetX = diffX / (mMoveContainer.getWidth() / 2),
                offsetY = diffY / (mMoveContainer.getHeight() / 2);

        if (offsetX < 0.2 && offsetX > -0.2)
            offsetX = 0;
        if (offsetY < 0.2 && offsetY > -0.2)
            offsetY = 0;
        float offset = offsetX * offsetX + offsetY * offsetY;

        if (offset == 0)
            return new Direction(offset, Direction.STOP);
        else if (Math.abs(offsetX) > Math.abs(offsetY)) {
            if (offsetX > 0)
                return new Direction(offset, Direction.RIGHT);
            else
                return new Direction(offset, Direction.LEFT);

        } else {
            if (offsetY > 0)
                return new Direction(offset, Direction.B0TT0M);
            else
                return new Direction(offset, Direction.TOP);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (v == mMove)
            switch (event.getAction()) {

                case MotionEvent.ACTION_DOWN:
                    dX = v.getX() - event.getRawX();
                    dY = v.getY() - event.getRawY();
                    break;

                case MotionEvent.ACTION_MOVE:
                    float mPosX = event.getRawX() + dX,// + dX,
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
