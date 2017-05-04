package com.app.charles.bomberman.adapters;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.app.charles.bomberman.R;

import java.util.ArrayList;

/**
 * Created by Charles on 09-Feb-17.
 */

public class ThemesAdapter extends RecyclerView.Adapter<ThemesAdapter.ViewHolder> implements View.OnClickListener {

    private static final String TAG = "CourseAdapter";

    private ArrayList<Integer> mValues = new ArrayList<>();
    private int mSelectedTheme;

    private Context mContext;
    private ThemesAdapterListener listener;

    public ThemesAdapter(ThemesAdapterListener listener, Context context, int selectedTheme) {
        this.listener = listener;
        this.mContext = context;
        this.mSelectedTheme = selectedTheme;

        TypedArray drawables = context.getResources().obtainTypedArray(R.array.colors_drawable);

        for(int i=0 ; i < 4 ; i++){
            mValues.add(drawables.getResourceId(i, -1));
        }

        drawables.recycle();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.theme_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.item.setBackgroundResource(mValues.get(position));
        holder.item.setTag(position);
        holder.item.setOnClickListener(this);

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

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        View item;
        ImageView isSelected;

        ViewHolder(View view) {
            super(view);
            item = view.findViewById(R.id.view);
            isSelected = (ImageView) view.findViewById(R.id.is_selected);
        }
    }

    @Override
    public void onClick(View v) {
        mSelectedTheme = (Integer) v.getTag();
        listener.colorChanged(mSelectedTheme);
        //notifyItemRangeChanged(0, getItemCount());
        notifyDataSetChanged();
    }

    public interface ThemesAdapterListener {
        void colorChanged(int index);
    }
}
