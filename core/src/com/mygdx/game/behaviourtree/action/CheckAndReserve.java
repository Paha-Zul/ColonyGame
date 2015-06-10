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
        return super.check();
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        this.blackBoard.itemTransfer.takingReserved = true;

        if(this.blackBoard.itemTransfer.transferMany) {
            //For each item name in the array, try to reserve.
            for (int i = 0; i < this.blackBoard.itemTransfer.itemNamesToTake.size; i++) {
                String itemName = this.blackBoard.itemTransfer.itemNamesToTake.get(i); //Get the name
                int amount = this.blackBoard.itemTransfer.itemAmountsToTake.get(i); //Get the amount
                int reserve = this.blackBoard.itemTransfer.fromInventory.reserveItem(itemName, amount); //Attempt to reserve
                this.blackBoard.itemTransfer.itemAmountsToTake.set(i, reserve); //Set the amount to the amount we were able to reserve.
            }
        }

        this.control.finishWithSuccess();
    }

    @Override
    public void end() {
        super.end();
    }
}
