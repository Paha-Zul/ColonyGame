package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;

/**
 * Created by Paha on 7/31/2015.
 */
public class ReserveResource extends LeafTask{
    public ReserveResource(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check() && this.blackBoard.targetResource != null
                && (!this.blackBoard.targetResource.isTaken() || this.blackBoard.targetResource.getTaken() == this.blackBoard.myManager.getEntityOwner());
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        this.blackBoard.targetResource.setTaken(this.blackBoard.myManager.getEntityOwner());
        this.control.finishWithSuccess();
    }

    @Override
    public void end() {
        super.end();
    }
}
