package com.app.charles.bomberman.adapters;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.charles.bomberman.R;

import java.util.ArrayList;

/**
 * Gère la liste de sélection de thème.
 */

public class ThemesAdapter extends RecyclerView.Adapter<ThemesAdapter.ThemeCircleView> implements View.OnClickListener {

    private ArrayList<Integer> mValues = new ArrayList<>();
    private int mSelectedTheme;

    private ThemesAdapterListener listener;

    public ThemesAdapter(ThemesAdapterListener listener, Context context, int selectedTheme) {
        this.listener = listener;
        this.mSelectedTheme = selectedTheme;

        // obtention des cercles de couleur représentant les thèmes.
        TypedArray drawables = context.getResources().obtainTypedArray(R.array.colors_drawable);
        for(int i=0 ; i < 4 ; i++){
            mValues.add(drawables.getResourceId(i, -1));
        }
        drawables.recycle();
    }

    /**
     * Création d'une vue pour chaque item de la liste.
     */
    @Override
    public ThemeCircleView onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.theme_view, parent, false);
        return new ThemeCircleView(view);
    }

    /**
     * Modification spécifique de la vue de chaque item de la liste.
     * @param holder vue.
     * @param position position dans la liste.
     */
    @Override
    public void onBindViewHolder(final ThemeCircleView holder, final int position) {
        holder.item.setBackgroundResource(mValues.get(position));
        holder.item.setTag(position); // le tag nous permettra de différencier les vues dans la liste.
        holder.item.setOnClickListener(this);

        // si l'item est sélection, on anime la transparence du symbole "check" qui apparait sur le cercle de la couleur.
        if(position == mSelectedTheme && holder.isSelected.getVisibility() == View.INVISIBLE) {
            final ValueAnimator anim = ValueAnimator.ofFloat(0, 1);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    holder.isSelected.setAlpha((Float)animation.getAnimatedValue());
                }
            });
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                    holder.isSelected.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            anim.setDuration(200);
            anim.start();
        }
        // sinon si le symbole "check" était visible, on le cache en animant sa transparence.
        else if (holder.isSelected.getVisibility() == View.VISIBLE) {
            final ValueAnimator anim = ValueAnimator.ofFloat(1, 0);
            anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    holder.isSelected.setAlpha((Float)animation.getAnimatedValue());
                }
            });
            anim.addListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    holder.isSelected.setVisibility(View.INVISIBLE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }
            });
            anim.setDuration(200);
            anim.start();
        }
    }

    /**
     * Permet d'obtenir le nombre d'items dans la liste.
     * @return le nombre d'items dans la liste.
     */
    @Override
    public int getItemCount() {
        return mValues.size();
    }

    /**
     * Vue représentant le cercle de chaque thème.
     */
    class ThemeCircleView extends RecyclerView.ViewHolder {
        View item;
        ImageView isSelected;

        ThemeCircleView(View view) {
            super(view);
            item = view.findViewById(R.id.view);
            isSelected = (ImageView) view.findViewById(R.id.is_selected);
        }
    }

    /**
     * Méthode appelée lorsque l'on clique sur une vue.
     *
     * @param v vue appelant la méthode.
     */
    @Override
    public void onClick(View v) {
        mSelectedTheme = (Integer) v.getTag(); // on reconnait la vue sélectionnée selon son tag (position dans la liste).
        listener.colorChanged(mSelectedTheme);
        notifyDataSetChanged(); // indique à la liste un changement dans ses items et les redessine.
    }

    /**
     * Interface permettant de notifier le changement de thème.
     */
    public interface ThemesAdapterListener {
        void colorChanged(int index);
    }
}
