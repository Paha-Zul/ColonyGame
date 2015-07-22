package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.graphic.GraphicIdentity;
import com.mygdx.game.util.BlackBoard;

/**
 * Created by Paha on 7/21/2015.
 */
public class Enter extends LeafTask{
    public Enter(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        //TODO setActive() is kind of expensive. Maybe we can do some kind of temp inactive where we skip the update loop instead of moving to a different list?
        GraphicIdentity identity = this.blackBoard.myManager.getEntityOwner().getComponents().getIdentity();
        identity.setActive(false);

        this.control.finishWithSuccess();
    }

    @Override
    public void end() {
        super.end();
    }
}
