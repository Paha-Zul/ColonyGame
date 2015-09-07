package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.component.Inventory;

/**
 * Created by Paha on 8/18/2015.
 * <p>Transfers items from the blackboard.target to me! Sets the toInventory as my inventory, and the fromInventory from the target.</p>
 * <p>It then transfers all the items in blackBoard.itemTransfer.itemsToTransfer from fromInventory to toInventory, unreserving (hopefully) what was taken.</p>
 */
public class TransferItemsFromTargetToMe extends TransferItems{
    public TransferItemsFromTargetToMe(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check() && this.blackBoard.target != null;
    }

    @Override
    public void start() {
        super.start();

        this.blackBoard.itemTransfer.fromInventory = this.blackBoard.target.getComponent(Inventory.class);
        this.blackBoard.itemTransfer.toInventory = this.blackBoard.myInventory;
    }


    @Override
    public void end() {
        super.end();
    }
}
