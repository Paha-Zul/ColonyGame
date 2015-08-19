package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.util.ItemNeeded;

/**
 * Created by Paha on 8/18/2015.
 */
public class TransferItemsFromMeToTarget extends LeafTask{
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
    public void update(float delta) {
        super.update(delta);

        //Loop over the itemsToTransfer and try to take from the fromInventory and add to the toInventory.
        for(ItemNeeded itemNeeded : this.blackBoard.itemTransfer.itemsToTransfer){
            int amountInInv = this.blackBoard.itemTransfer.fromInventory.getItemAmount(itemNeeded.itemName);
            int amountICanAdd = this.blackBoard.itemTransfer.toInventory.getItemCanAddAmount(itemNeeded.itemName);
            //Get the lowest of the amount in the fromInventory or the amount we need, then the lowest of that or how much we can add.
            int amountToAdd = Integer.min(amountICanAdd, Integer.min(amountInInv, itemNeeded.amountNeeded));
            this.blackBoard.itemTransfer.fromInventory.unReserveItem(itemNeeded.itemName, this.blackBoard.myManager.getEntityOwner().getID()); //Unreserve the item completely.
            this.blackBoard.itemTransfer.fromInventory.removeItem(itemNeeded.itemName, amountToAdd);
            this.blackBoard.itemTransfer.toInventory.addItem(itemNeeded.itemName, amountToAdd);
            itemNeeded.amountNeeded = amountToAdd; //Set the itemNeeded.amountNeeded to the amountToAdd for use later. Adjust!!
        }

        this.control.finishWithSuccess();
    }

    @Override
    public void end() {
        super.end();
    }
}
