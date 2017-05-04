package com.app.charles.bomberman.views;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.animation.Animation;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.app.charles.bomberman.R;
import com.app.charles.bomberman.adapters.ThemesAdapter;

import static android.content.ContentValues.TAG;

/**
 * Created by Charles on 09-Feb-17.
 */

public class Preferences extends LinearLayout implements CompoundButton.OnCheckedChangeListener,
        ThemesAdapter.ThemesAdapterListener {

    private PreferencesListener mListener;
    private ThemesAdapter.ThemesAdapterListener mThemesListener;

    public void setListener(PreferencesListener listener) {
        this.mListener = listener;
    }

    private Context mContext;
    private Switch mInverseControls,
            mVibrate;
    private RecyclerView mRecyclerView;

    public Preferences(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context, attrs);
    }

    private void initViews(Context context, AttributeSet attrs) {
        this.mContext = context;

        LayoutInflater.from(context).inflate(R.layout.preferences, this);

        mInverseControls = (Switch) findViewById(R.id.inverse_controls);
        mVibrate = (Switch) findViewById(R.id.vibrate);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerView.setAdapter(new ThemesAdapter(this
                , context
                , PreferenceManager.getDefaultSharedPreferences(context).getInt(getResources().getString(R.string.theme), 0)));

        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(getResources().getString(R.string.inverted_controls), false))
            mInverseControls.setChecked(true);
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(getResources().getString(R.string.vibrate), true))
            mVibrate.setChecked(true);

        mInverseControls.setOnCheckedChangeListener(this);
        mVibrate.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView == mInverseControls) {
            PreferenceManager.getDefaultSharedPreferences(mContext)
                    .edit()
                    .putBoolean(getResources().getString(R.string.inverted_controls), isChecked)
                    .apply();
            mListener.onPreferencesChanged(false);
        } else if (buttonView == mVibrate) {
            PreferenceManager.getDefaultSharedPreferences(mContext)
                    .edit()
                    .putBoolean(getResources().getString(R.string.vibrate), isChecked)
                    .apply();
        }
    }

    public interface PreferencesListener {
        void onPreferencesChanged(boolean restartHomeActivity);
    }

    @Override
    public void colorChanged(int index) {
        PreferenceManager.getDefaultSharedPreferences(mContext)
                .edit()
                .putInt(getResources().getString(R.string.theme), index)
                .apply();
        mListener.onPreferencesChanged(true);
    }
}
