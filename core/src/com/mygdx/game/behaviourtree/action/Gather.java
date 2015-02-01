package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.*;
import com.mygdx.game.helpers.timer.OneShotTimer;
import com.mygdx.game.helpers.timer.Timer;

/**
 * Created by Paha on 1/29/2015.
 */
public class Gather extends LeafTask{
    private Resource resource;
    private Timer gatherTimer;

    public Gather(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return this.blackBoard.target != null;
    }

    @Override
    public void start() {
        super.start();

        if(this.blackBoard.myInventory == null)
            this.blackBoard.myInventory = this.blackBoard.getEntityOwner().getComponent(Inventory.class);

        this.resource = this.blackBoard.target.getComponent(Resource.class);
        if(this.resource == null){
            this.control.finishWithFailure();
            return;
        }

        this.gatherTimer = new OneShotTimer(this.resource.getGatherTime(), ()->{
            if(this.resource.isDestroyed()){
                this.control.finishWithFailure();
                return;
            }

            Colony targetColony = this.blackBoard.getEntityOwner().getComponent(Colonist.class).getColony();
            this.blackBoard.targetNode = null;
            this.blackBoard.target = targetColony.getEntityOwner();
            this.blackBoard.transferToInventory = targetColony.getInventory();
            this.blackBoard.myInventory.addItem(this.resource.getItem());
            this.resource.getEntityOwner().destroy();
            this.control.finishWithSuccess();
        });
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        this.gatherTimer.update(delta);
    }

    @Override
    public void end() {
        super.end();
    }
}
