package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.util.timer.RepeatingTimer;
import com.mygdx.game.util.timer.Timer;

/**
 * Created by Paha on 4/7/2015.
 */
public class Fish extends LeafTask{
    Timer fishTimer;
    Inventory inv;
    int numFishCaught = 0;

    public Fish(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {
        super.start();
        this.inv = this.blackBoard.myManager.getEntityOwner().getComponent(Inventory.class);
        this.numFishCaught = 0;

        fishTimer = new RepeatingTimer(5f, () -> {
            this.inv.addItem("fish", 1);
            numFishCaught++;
        });
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        fishTimer.update(delta);
        if(numFishCaught >= 5)
            this.control.finishWithSuccess();
    }

    @Override
    public void end() {
        super.end();
    }
}
