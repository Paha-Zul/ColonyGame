package com.mygdx.game.util.runnables;

import com.mygdx.game.ColonyGame;
import com.mygdx.game.interfaces.Functional;

/**
 * A Runnable that executes a callback function.
 */
public class CallbackRunnable implements Runnable{
    private Functional.Callback callback;

    public CallbackRunnable(Functional.Callback callback){
        this.callback = callback;
    }

    @Override
    public void run() {
        if(ColonyGame.closed) return;
        this.callback.callback();
    }
}
