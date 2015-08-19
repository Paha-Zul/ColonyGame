package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.CraftingStation;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.util.ItemNeeded;

/**
 * Created by Paha on 8/18/2015.
 * <p>Crafts an item.</p>
 */
public class CraftItem extends LeafTask{
    private CraftingStation.CraftingJob craftingJob;
    private float craftTickAmount;


    public CraftItem(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check() && this.blackBoard.targetCraftingStation != null && (this.blackBoard.targetCraftingStation.hasAvailableJob() || this.blackBoard.targetCraftingStation.hasStalledJob());
    }

    @Override
    public void start() {
        super.start();

        Array<ItemNeeded> itemsNeeded = this.blackBoard.targetCraftingStation.getItemsNeededForJob(this.blackBoard.targetCraftingStation.getFirstAvailableJob().id);
        if(itemsNeeded == null || itemsNeeded.size > 0)
            this.control.finishWithFailure(); //If the list is null (the job doesn't exist) or we still need items, fail!

        //Otherwise, no items are needed. Proceed with working the job!
        else{
            this.craftingJob = this.blackBoard.targetCraftingStation.setFirstAvailableToInProgress();
            if(this.craftingJob == null) this.control.finishWithFailure(); //If the job we tried to transfer was null, fail! (it didn't exist)
            else {
                this.blackBoard.itemTransfer.toInventory = this.blackBoard.targetCraftingStation.getComponent(Inventory.class); //Set the toInventory...
                for(int i=0;i<this.craftingJob.itemRecipe.items.length;i++) //Remove all of the items that are needed for the item from the inventory.
                    this.blackBoard.itemTransfer.toInventory.removeItem(this.craftingJob.itemRecipe.items[i], this.craftingJob.itemRecipe.itemAmounts[i]);

                this.craftTickAmount = 1f/this.craftingJob.itemRecipe.time; // 100%/time in seconds
            }
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        this.craftingJob.percentageDone += this.craftTickAmount*delta;
        if(this.craftingJob.percentageDone >= 1) {
            this.blackBoard.targetCraftingStation.finishJobInProgress(this.craftingJob.id); //Finish the crafting job!
            this.blackBoard.itemTransfer.toInventory.addItem(this.craftingJob.itemRef.getItemName(), 1); //Add the item to the inventory!
            this.craftingJob = null; //Set it to null (used for something in end() method)
            this.control.finishWithSuccess(); //Finish with success!
        }
    }

    @Override
    public void end() {
        super.end();

        //If the craftingJob is not null (meaning we ended while crafting it), send it to the stalled list!
        if(this.craftingJob != null)
            this.blackBoard.targetCraftingStation.setInProgressToStalled(this.craftingJob.id);
    }
}
