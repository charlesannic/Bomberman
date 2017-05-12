package com.app.charles.bomberman.views;

import android.content.Context;
import android.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;

import com.app.charles.bomberman.R;
import com.app.charles.bomberman.adapters.ThemesAdapter;

/**
 * Création d'un vue faisant apparaître les préférences de l'application.
 */

@SuppressWarnings("FieldCanBeLocal")
public class Preferences extends LinearLayout implements CompoundButton.OnCheckedChangeListener,
        ThemesAdapter.ThemesAdapterListener {

    // écouteur sur les préférences.
    private PreferencesListener mListener;

    // context de l'application ayant créé ce tableau des préférences.
    private Context context;

    // vues
    private Switch swtOrientation, swtInverseControls, swtVibrate, swtWatchUntilEnd; // switchs
    private RecyclerView rcvTheme; // vue pour afficher les thèmes sous forme de liste.

    public Preferences(Context context, AttributeSet attrs) {
        super(context, attrs);
        initViews(context);
    }

    private void initViews(Context context) {
        this.context = context;

        LayoutInflater.from(context).inflate(R.layout.preferences, this);

        // initialisation des vues.
        swtOrientation = (Switch) findViewById(R.id.orientation);
        swtInverseControls = (Switch) findViewById(R.id.inverse_controls);
        swtVibrate = (Switch) findViewById(R.id.vibrate);
        swtWatchUntilEnd = (Switch) findViewById(R.id.watch_until_end);
        rcvTheme = (RecyclerView) findViewById(R.id.recycler_view);

        // création de la liste et de son contenu.
        rcvTheme.setLayoutManager(new LinearLayoutManager(this.context, LinearLayoutManager.HORIZONTAL, false));
        rcvTheme.setAdapter(new ThemesAdapter(this
                , context
                , PreferenceManager.getDefaultSharedPreferences(context).getInt(getResources().getString(R.string.theme), 0)));

        // Initialisation des switch selon les préférences déjà sauvegardées.
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(getResources().getString(R.string.inverted_controls), false))
            swtOrientation.setChecked(true);
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(getResources().getString(R.string.inverted_controls), false))
            swtInverseControls.setChecked(true);
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(getResources().getString(R.string.vibrate), true))
            swtVibrate.setChecked(true);
        if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(getResources().getString(R.string.watch_until_end), true))
            swtWatchUntilEnd.setChecked(true);

        // listeners.
        swtOrientation.setOnCheckedChangeListener(this);
        swtInverseControls.setOnCheckedChangeListener(this);
        swtVibrate.setOnCheckedChangeListener(this);
        swtWatchUntilEnd.setOnCheckedChangeListener(this);
    }

    /**
     * Méthode appelée si un bouton change de position.
     * @param buttonView bouton changé.
     * @param isChecked est sélectionné.
     */
    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // changement des préférences selon la position du switch actionnée
        if (buttonView == swtOrientation) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean(getResources().getString(R.string.orientation), isChecked)
                    .apply();
        } else if (buttonView == swtInverseControls) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean(getResources().getString(R.string.inverted_controls), isChecked)
                    .apply();
        } else if (buttonView == swtVibrate) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean(getResources().getString(R.string.vibrate), isChecked)
                    .apply();
        } else if (buttonView == swtWatchUntilEnd) {
            PreferenceManager.getDefaultSharedPreferences(context)
                    .edit()
                    .putBoolean(getResources().getString(R.string.watch_until_end), isChecked)
                    .apply();
        }
    }

    public void setListener(PreferencesListener listener) {
        this.mListener = listener;
    }

    /**
     * Interface utilisée pour notifier que les préférences ont changées et que l'activité doit redémarrer.
     */
    public interface PreferencesListener {
        void onPreferencesChanged(boolean restartHomeActivity);
    }

    /**
     * Changement du thème dans les préférences.
     * @param index index du thème dans la liste.
     */
    @Override
    public void colorChanged(int index) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(getResources().getString(R.string.theme), index)
                .apply();
        mListener.onPreferencesChanged(true);
    }
}
