package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.helpers.timer.OneShotTimer;
import com.mygdx.game.helpers.timer.Timer;

/**
 * Created by Paha on 4/11/2015.
 */
public class GatherWater extends LeafTask {
    private Timer timer;

    public GatherWater(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {
        super.start();

        timer = new OneShotTimer(5f, () -> {
            this.blackBoard.fromInventory.addItem("water");
            this.control.finishWithSuccess();
        });
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