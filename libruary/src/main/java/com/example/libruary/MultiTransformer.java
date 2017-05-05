package com.example.libruary;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 *  A transformer for FriendlyViewpager ,
 *  let it can switch mode between FADE_MODE and TRANSLATION_MODE
 */
public class MultiTransformer implements ViewPager.PageTransformer {
    boolean canFade = false;

    private void setCanFade(boolean canFade) {
        this.canFade = canFade;
    }

    public void transformPage(View view, float position) {
        if (!canFade) {
            return;
        }
        view.setTranslationX(view.getWidth() * -position);
        if (position <= -1.0F || position >= 1.0F) {
            view.setAlpha(0.0F);
        } else if (position == 0.0F) {
            view.setAlpha(1.0F);
        } else {
            view.setAlpha(1.0F - Math.abs(position));
        }
    }
}