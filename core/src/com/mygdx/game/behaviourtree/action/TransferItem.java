package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.util.BlackBoard;

/**
 * Created by Paha on 1/30/2015.
 * <p>Transfers items from the {@link com.mygdx.game.util.BlackBoard.ItemTransfer#fromInventory blackBoard.fromInventory} to
 * the {@link com.mygdx.game.util.BlackBoard.ItemTransfer#toInventory blackBoard.toInventory}. If
 * {@link com.mygdx.game.util.BlackBoard.ItemTransfer#transferAll blackBoard.transferAll} is set to true, the entire inventory in the blackBoard.fromInventory will be transferred. Otherwise,
 * only the item denoted by {@link com.mygdx.game.util.BlackBoard.ItemTransfer#itemNameToTake blackBoard.itemNameToTake} will be transferred.</p>
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
        if(this.blackBoard.itemTransfer.transferAll) {
            for (Inventory.InventoryItem item : this.blackBoard.itemTransfer.fromInventory.getItemList())
                this.blackBoard.itemTransfer.toInventory.addItem(item.itemRef.getItemName(), item.getAmount());
            this.blackBoard.itemTransfer.fromInventory.clearInventory();

        //If we are taking many items but not all of them.
        }else if(this.blackBoard.itemTransfer.transferMany){
            for(int i=0;i<this.blackBoard.itemTransfer.itemNamesToTake.size;i++){
                String itemName = this.blackBoard.itemTransfer.itemNamesToTake.get(i);
                int amount = this.blackBoard.itemTransfer.itemAmountsToTake.get(i);
                this.blackBoard.itemTransfer.fromInventory.removeItem(itemName, amount, this.blackBoard.itemTransfer.takingReserved);
                this.blackBoard.itemTransfer.toInventory.addItem(itemName, amount);
            }

        //If we are only taking one item.
        }else{
            this.blackBoard.itemTransfer.toInventory.addItem(this.blackBoard.itemTransfer.itemNameToTake, this.blackBoard.itemTransfer.itemAmountToTake);
        }

        this.blackBoard.itemTransfer.itemNamesToTake = null;
        this.blackBoard.itemTransfer.itemAmountsToTake = null;
        this.blackBoard.itemTransfer.itemNameToTake = null;
        this.blackBoard.itemTransfer.itemAmountToTake = 0;

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
