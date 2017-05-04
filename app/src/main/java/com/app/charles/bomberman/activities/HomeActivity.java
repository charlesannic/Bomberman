package com.app.charles.bomberman.activities;

import android.content.Intent;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.app.charles.bomberman.R;
import com.app.charles.bomberman.utils.Utils;
import com.app.charles.bomberman.views.Preferences;

public class HomeActivity extends AppCompatActivity implements View.OnClickListener,
        Preferences.PreferencesListener {

    private boolean mustRestart = false;

    private CoordinatorLayout mContent;
    private View mShadow;
    private Button mPlay;

    private LinearLayout mBottomSheet;
    private BottomSheetBehavior mBottomSheetBehavior;
    private ImageView mExpandView;
    private AnimatedVectorDrawable mExpandDrawable,
            mCollapseDrawable;
    private Preferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(Utils.getTheme(this));

        setContentView(R.layout.activity_home);

        mContent = (CoordinatorLayout) findViewById(R.id.main_content);
        mShadow = findViewById(R.id.shadow);
        mPlay = (Button) findViewById(R.id.play);

        mBottomSheet = (LinearLayout) findViewById(R.id.bottom_sheet);
        mExpandView = (ImageView) findViewById(R.id.ic_expand);
        mPreferences = (Preferences) findViewById(R.id.preferences);

        mExpandDrawable = (AnimatedVectorDrawable) getDrawable(R.drawable.ic_expand);
        mCollapseDrawable = (AnimatedVectorDrawable) getDrawable(R.drawable.ic_collapse);
        mExpandView.setImageDrawable(mCollapseDrawable);

        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN || newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    mShadow.setVisibility(View.GONE);
                    if(mustRestart) {
                        recreate();
                        mustRestart = false;
                    }
                } else {
                    mShadow.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                mShadow.setAlpha(slideOffset);

                if (slideOffset > 0.9 && mExpandView.getDrawable() != mExpandDrawable) {
                    mExpandView.setImageDrawable(mExpandDrawable);
                    mExpandDrawable.start();
                } else if (slideOffset < 0.9 && mExpandView.getDrawable() != mCollapseDrawable) {
                    mExpandView.setImageDrawable(mCollapseDrawable);
                    mCollapseDrawable.start();
                }
            }
        });

        mPlay.setOnClickListener(this);
        mShadow.setOnClickListener(this);
        mExpandView.setOnClickListener(this);
        mPreferences.setListener(this);
    }

    @Override
    public void onBackPressed() {
        if(mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED)
            super.onBackPressed();
        else
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }

    @Override
    public void onClick(View v) {
        if(v==mPlay)
            startActivity(new Intent(this, GameActivity.class));
        if (v == mShadow)
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        else if (v == mExpandView)
            mBottomSheetBehavior.setState(mBottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED ?
                    BottomSheetBehavior.STATE_COLLAPSED :
                    BottomSheetBehavior.STATE_EXPANDED);
    }

    @Override
    public void onPreferencesChanged(boolean restartHomeActivity) {
        mustRestart = restartHomeActivity;
    }
}
