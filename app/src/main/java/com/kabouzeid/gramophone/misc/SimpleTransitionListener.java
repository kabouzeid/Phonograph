package com.kabouzeid.gramophone.misc;

import android.annotation.TargetApi;
import android.os.Build;
import android.transition.Transition;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public abstract class SimpleTransitionListener implements Transition.TransitionListener {
    @Override
    public void onTransitionStart(Transition transition) {
    }

    @Override
    public void onTransitionEnd(Transition transition) {
    }

    @Override
    public void onTransitionCancel(Transition transition) {
    }

    @Override
    public void onTransitionPause(Transition transition) {
    }

    @Override
    public void onTransitionResume(Transition transition) {
    }
}
