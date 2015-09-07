package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.util.ItemNeeded;

/**
 * Created by Paha on 8/18/2015.
 */
public abstract class TransferItems extends LeafTask{
    public TransferItems(String name, BlackBoard blackBoard) {
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

        //We want to transfer the whole inventory
        if(this.blackBoard.itemTransfer.transferAll){
            //If we are transferring the whole inventory, go through each itemRef and add each one to the 'toInventory'. Clear the 'fromInventory'.
            for (Inventory.InventoryItem item : this.blackBoard.itemTransfer.fromInventory.getItemList()) {
                //If the types to ignore is 0 (no types to ignore) or the item's type is not in the list, transfer.
                if(this.blackBoard.itemTransfer.itemTypesToIgnore.size == 0 || !this.blackBoard.itemTransfer.itemTypesToIgnore.contains(item.itemRef.getItemType(), false)) {
                    int canAddAmount = this.blackBoard.itemTransfer.toInventory.getItemCanAddAmount(item.itemRef.getItemName()); //Get the amount we can add to the inventory.
                    int amountToRemove = canAddAmount < item.getAmount() ? canAddAmount : item.getAmount(); //Choose the lower of the two.
                    long id = this.blackBoard.itemTransfer.takingReserved ? this.blackBoard.myManager.getEntityOwner().getID() : 0;
                    int amountRemoved = this.blackBoard.itemTransfer.fromInventory.removeItem(item.itemRef.getItemName(), amountToRemove, id); //Remove what we can.
                    this.blackBoard.itemTransfer.toInventory.addItem(item.itemRef.getItemName(), amountRemoved); //Add the item to the inventory.
                }
            }

        //Otherwise, we want to transfer only what is in the ItemNeeded list.
        }else {
            //Loop over the itemsToTransfer and try to take from the fromInventory and add to the toInventory.
            for (ItemNeeded itemNeeded : this.blackBoard.itemTransfer.itemsToTransfer) {
                int amountInInv = this.blackBoard.itemTransfer.fromInventory.getItemAmount(itemNeeded.itemName);
                int amountICanAdd = this.blackBoard.itemTransfer.toInventory.getItemCanAddAmount(itemNeeded.itemName);
                //Get the lowest of the amount in the fromInventory or the amount we need, then the lowest of that or how much we can add.
                int amountToAdd = Integer.min(amountICanAdd, Integer.min(amountInInv, itemNeeded.amountNeeded));
                this.blackBoard.itemTransfer.fromInventory.unReserveItem(itemNeeded.itemName, this.blackBoard.myManager.getEntityOwner().getID()); //Unreserve the item completely.
                this.blackBoard.itemTransfer.fromInventory.removeItem(itemNeeded.itemName, amountToAdd);
                this.blackBoard.itemTransfer.toInventory.addItem(itemNeeded.itemName, amountToAdd);
                itemNeeded.amountNeeded = amountToAdd; //Set the itemNeeded.amountNeeded to the amountToAdd for use later. Adjust!!
            }
        }

        this.control.finishWithSuccess();
    }

    @Override
    public void end() {
        super.end();
    }
}
