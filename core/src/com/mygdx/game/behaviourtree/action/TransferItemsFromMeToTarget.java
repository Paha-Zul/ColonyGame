package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.component.Inventory;

/**
 * Created by Paha on 8/18/2015.
 * Extends the TransferItems task. Simply sets the fromInventory to me, and the toInventory to the target's inventory.
 */
public class TransferItemsFromMeToTarget extends TransferItems{
    public TransferItemsFromMeToTarget(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check() && this.blackBoard.target != null;
    }

    @Override
    public void start() {
        super.start();

        this.blackBoard.itemTransfer.fromInventory = this.blackBoard.myInventory;
        this.blackBoard.itemTransfer.toInventory = this.blackBoard.target.getComponent(Inventory.class);
    }

    @Override
    public void end() {
        super.end();
    }
}
