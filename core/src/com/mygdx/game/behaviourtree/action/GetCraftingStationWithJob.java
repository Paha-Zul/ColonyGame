package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Colonist;
import com.mygdx.game.component.Colony;
import com.mygdx.game.component.CraftingStation;

/**
 * Created by Paha on 8/18/2015.
 */
public class GetCraftingStationWithJob extends LeafTask{
    public GetCraftingStationWithJob(String name, BlackBoard blackBoard) {
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

        Colony colony = this.blackBoard.myManager.getEntityOwner().getComponent(Colonist.class).getOwningColony();
        CraftingStation craftingStation = colony.getOwnedFromColony(CraftingStation.class, CraftingStation::hasAvailableJob);

        //If we couldn't get a crafting station, fail this task.
        if(craftingStation == null)
            this.control.finishWithFailure();
        else{
            this.control.finishWithSuccess();
            this.blackBoard.targetCraftingStation = craftingStation;
        }
    }

    @Override
    public void end() {
        super.end();
    }
}
