package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Stats;
import com.mygdx.game.util.timer.RepeatingTimer;
import com.mygdx.game.util.timer.Timer;

/**
 * Created by Paha on 7/21/2015.
 * <p>Makes a Colonist sleep, increasing the energy until full.</p>
 */
public class Sleep extends LeafTask{
    private Stats stats;
    private Stats.Stat energyStat;
    private Timer timer;


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

        this.stats = this.blackBoard.myManager.getEntityOwner().getComponent(Stats.class);
        this.stats.pauseTimers(true);
        this.energyStat = this.stats.getStat("energy");

        if(this.energyStat == null)
            this.control.finishWithFailure();

        this.timer = new RepeatingTimer(0.5f, () -> {
            if(this.energyStat.addToCurrent(1))
                this.control.finishWithSuccess();
        });
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        this.timer.update(delta);
    }

    @Override
    public void end() {
        super.end();

        //Make sure this isn't null. This will get called if the parent task ends early, which means this was never set.
        if(this.stats != null) this.stats.pauseTimers(false);
    }
}
