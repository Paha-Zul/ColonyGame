package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.component.Inventory;

/**
 * Created by Paha on 1/30/2015.
 */
public class TransferResource extends LeafTask{
    public TransferResource(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    public TransferResource(String name, BlackBoard blackBoard, Inventory fromInv, Inventory toInv) {
        super(name, blackBoard);
        this.blackBoard.fromInventory = fromInv;
        this.blackBoard.toInventory = toInv;
    }

    @Override
    public boolean check() {
        return this.blackBoard.toInventory != null;
    }

    @Override
    public void start() {
        super.start();

        //If we are transferring the whole inventory, go through each itemRef and add each one to the 'toInventory'. Clear the 'fromInventory'.
        if(this.blackBoard.transferAll) {
            for (Inventory.InventoryItem item : this.blackBoard.fromInventory.getItemList())
                this.blackBoard.toInventory.addItem(item.itemRef.getItemName(), item.getAmount());
            this.blackBoard.fromInventory.clearInventory();

        //Otherwise, take the number of itemNames specified.
        }else{
            int amount = this.blackBoard.fromInventory.removeItemAmount(this.blackBoard.itemNameToTake, this.blackBoard.takeAmount);
            this.blackBoard.toInventory.addItem(this.blackBoard.itemNameToTake, this.blackBoard.takeAmount);
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
