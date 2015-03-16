package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.component.Item;
import com.mygdx.game.helpers.managers.ItemManager;

/**
 * Created by Paha on 1/30/2015.
 */
public class TransferResource extends LeafTask{
    public TransferResource(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return this.blackBoard.toInventory != null;
    }

    @Override
    public void start() {
        super.start();

        //If we are transferring the whole inventory, go through each item and add each one to the 'toInventory'. Clear the 'fromInventory'.
        if(this.blackBoard.transferAll) {
            for (Inventory.InventoryItem item : this.blackBoard.fromInventory.getItemList())
                this.blackBoard.toInventory.addItem(item.item);
            this.blackBoard.fromInventory.clearInventory();

        //Otherwise, take the number of items specified.
        }else{
            int amount = this.blackBoard.fromInventory.removeItemAmount(this.blackBoard.itemNameToTake, this.blackBoard.takeAmount);
            Item item = new Item(ItemManager.getItemReference(this.blackBoard.itemNameToTake));
            item.setCurrStack(amount);
            this.blackBoard.toInventory.addItem(item);
        }

        this.control.finishWithSuccess();
    }

    @Override
    public void end() {
        super.end();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }
}
