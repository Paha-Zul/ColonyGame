package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.util.timer.OneShotTimer;
import com.mygdx.game.util.timer.Timer;

/**
 * Created by Paha on 7/21/2015.
 */
public class Sleep extends LeafTask{
    Timer timer;

    public Sleep(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {
        super.start();

        timer = new OneShotTimer(this.blackBoard.timeToSleep, this.control::finishWithSuccess);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        timer.update(delta);
    }

    @Override
    public void end() {
        super.end();
    }
}
