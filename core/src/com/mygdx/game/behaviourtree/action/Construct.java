package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Constructable;
import com.mygdx.game.util.BlackBoard;
import com.mygdx.game.util.timer.RepeatingTimer;
import com.mygdx.game.util.timer.Timer;

/**
 * Created by Paha on 6/22/2015.
 */
public class Construct extends LeafTask{
    private Constructable constructable;
    private Timer buildTimer;

    public Construct(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {
        super.start();

        this.constructable = this.blackBoard.target.getComponent(Constructable.class);
        this.buildTimer = new RepeatingTimer(1, false, () -> {
            this.constructable.build();
            if(this.constructable.isComplete())
                this.control.finishWithSuccess();
            else if(this.constructable.getInventory().isEmpty())
                this.control.finishWithFailure();
        });
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        this.buildTimer.update(delta);
    }
}
