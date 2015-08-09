package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.util.BlackBoard;

/**
 * Created by Paha on 1/30/2015.
 * <p>Transfers items from the {@link com.mygdx.game.util.BlackBoard.ItemTransfer#fromInventory blackBoard.fromInventory} to
 * the {@link com.mygdx.game.util.BlackBoard.ItemTransfer#toInventory blackBoard.toInventory}. If
 */
public class TransferItem extends LeafTask{
    public TransferItem(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return this.blackBoard.itemTransfer.toInventory != null;
    }

    @Override
    public void start() {
        super.start();

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

        this.control.finishWithSuccess();
    }

    @Override
    public void end() {
        super.end();

        if(this.blackBoard.itemTransfer.fromInventory != null) {
            //Attempt to unreserve the list of items...
            for (Inventory.InventoryItem item : this.blackBoard.itemTransfer.fromInventory.getItemList()) {
                this.blackBoard.itemTransfer.fromInventory.unReserveItem(item.itemRef.getItemName(), this.blackBoard.myManager.getEntityOwner().getID());
            }
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }
}
