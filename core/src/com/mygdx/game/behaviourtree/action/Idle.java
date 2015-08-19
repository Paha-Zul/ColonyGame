package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.util.timer.OneShotTimer;
import com.mygdx.game.util.timer.Timer;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class Idle extends LeafTask {
    private Timer timer;

    public Idle(String name, BlackBoard blackBoard) {
        super(name, blackBoard);

        //Random a timer.
        timer = new OneShotTimer(this.blackBoard.baseIdleTime + MathUtils.random()*this.blackBoard.randomIdleTime, null);
    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public void start() {
        super.start();

        this.timer.restart();
    }

    @Override
    public void update(float delta) {
        timer.update(delta);

        if(timer.isFinished())
            this.control.finishWithSuccess();
    }

    @Override
    public void end() {
        super.end();
    }
}
