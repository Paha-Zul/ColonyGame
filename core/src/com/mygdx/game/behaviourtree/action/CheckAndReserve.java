package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.util.ItemNeeded;

/**
 * Created by brad on 6/10/15.
 * <p>Attempts to reserve each item in the blackboard.itemTransfer.itemsToTransfer from the blackboard.itemTransfer.fromInventory.</p>
 * <p>The fromInventory and toInventory must already be set, and itemsToTransfer must not be null or empty.</p>
 * <p>Finishes with success if at least one item was able to be reserved.</p>
 */
public class CheckAndReserve extends LeafTask{
    public CheckAndReserve(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check() && this.blackBoard.itemTransfer.itemsToTransfer != null && this.blackBoard.itemTransfer.itemsToTransfer.size != 0 &&
                this.blackBoard.itemTransfer.fromInventory != null && this.blackBoard.itemTransfer.toInventory != null;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        boolean atLeastOne = false;
        this.blackBoard.itemTransfer.takingReserved = true;

        //For each item name in the array, try to reserve.
        for (ItemNeeded item : this.blackBoard.itemTransfer.itemsToTransfer) {
            int amountCanAdd = this.blackBoard.itemTransfer.toInventory.getItemCanAddAmount(item.itemName); //Get the amount we can reserve.
            int amountToReserve = Math.min(amountCanAdd, item.amountNeeded); //Get the smaller of the two values.
            int reserve = this.blackBoard.itemTransfer.fromInventory.reserveItem(item.itemName, amountToReserve, this.blackBoard.myManager.getEntityOwner().getID()); //Attempt to reserve
            item.amountNeeded = reserve; //Set the amount to the amount we were able to reserve.
            if(reserve > 0) atLeastOne = true;
        }

        if(atLeastOne) {
            this.control.finishWithSuccess();
        } else {
            this.control.finishWithFailure();
        }
    }

    @Override
    public void end() {
        super.end();
    }
}
