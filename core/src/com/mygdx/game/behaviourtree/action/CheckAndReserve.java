package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.util.BlackBoard;

/**
 * Created by brad on 6/10/15.
 */
public class CheckAndReserve extends LeafTask{
    public CheckAndReserve(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check() && this.blackBoard.itemTransfer.itemNamesToTransfer != null && this.blackBoard.itemTransfer.itemNamesToTransfer.size != 0;
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

        if(this.blackBoard.itemTransfer.transferMany) {
            //For each item name in the array, try to reserve.
            for (int i = 0; i < this.blackBoard.itemTransfer.itemNamesToTransfer.size; i++) {
                String itemName = this.blackBoard.itemTransfer.itemNamesToTransfer.get(i); //Get the name
                int amount = this.blackBoard.itemTransfer.itemAmountsToTransfer.get(i); //Get the amount
                int reserve = this.blackBoard.itemTransfer.fromInventory.reserveItem(itemName, amount); //Attempt to reserve
                this.blackBoard.itemTransfer.itemAmountsToTransfer.set(i, reserve); //Set the amount to the amount we were able to reserve.
                if(reserve > 0) atLeastOne = true;
            }
        }

        if(atLeastOne)
            this.control.finishWithSuccess();
        else
            this.control.finishWithFailure();
    }

    @Override
    public void end() {
        super.end();
    }
}
