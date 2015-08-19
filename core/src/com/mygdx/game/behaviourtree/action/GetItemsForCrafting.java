package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;

/**
 * Created by Paha on 8/18/2015.
 * <p>Uses the blackboard.targetCraftingStation to get a list of items needed for the first available job.</p>
 * <p>The task is successful if at least one item is required.</p>
 * <p>The task fails if there are no items required or the list is null for some reason (no crafting jobs?), or the crafting station is null</p>
 */
public class GetItemsForCrafting extends LeafTask{
    public GetItemsForCrafting(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check() && this.blackBoard.targetCraftingStation != null && this.blackBoard.targetCraftingStation.hasAvailableJob();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        this.blackBoard.itemTransfer.itemsToTransfer = new Array<>();
        this.blackBoard.itemTransfer.itemsToTransfer = this.blackBoard.targetCraftingStation.getItemsNeededForJob(this.blackBoard.targetCraftingStation.getFirstAvailableJob().id);

        //If the list is not null and has at least one item, finish this with success.
        if(this.blackBoard.itemTransfer.itemsToTransfer != null && this.blackBoard.itemTransfer.itemsToTransfer.size > 0) this.control.finishWithSuccess();
        else this.control.finishWithFailure();
    }

    @Override
    public void end() {
        super.end();
    }
}
