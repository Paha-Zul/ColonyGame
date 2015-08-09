package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Constructable;
import com.mygdx.game.util.timer.RepeatingTimer;
import com.mygdx.game.util.timer.Timer;

/**
 * Created by Paha on 6/22/2015.
 */
public class Construct extends LeafTask{
    private Constructable constructable;
    private Timer buildTimer;
    private Sound hammerSound;

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
            int rand = MathUtils.random(2) + 1;
            int result = this.constructable.build();
            if(result == 0) ColonyGame.assetManager.get("hammer_"+rand, Sound.class).play();
            if(this.constructable.isComplete() || result == -1)
                this.control.finishWithSuccess();
        });
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        this.buildTimer.update(delta);
    }
}
