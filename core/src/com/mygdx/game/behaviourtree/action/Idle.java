package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.helpers.timer.OneShotTimer;
import com.mygdx.game.helpers.timer.Timer;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class Idle extends LeafTask {
    Timer timer;

    public Idle(String name, BlackBoard blackBoard) {
        super(name, blackBoard);

    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public void start() {
        super.start();

        timer = new OneShotTimer(1f, null);
    }

    @Override
    public void update(float delta) {
        timer.update(delta);

        if(timer.isFinished())
            this.control.finishWithSuccess();
    }
}
