package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.ItemNeeded;

/**
 * Created by Paha on 3/15/2015.
 * Simply checks if an inventory (fromInventory) has an item with the effect (blackboard.itemEffect) that we want. If so, reserves 1 of the item.
 */
public class CheckInventoryHasItemWithEffectAndReserve extends LeafTask {

    public CheckInventoryHasItemWithEffectAndReserve(String name, BlackBoard blackBoard) {
        super(name, blackBoard);

    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public void start() {
        super.start();

        //Loop over each item in the "fromInventory" and check if any of the items has the effect we want.
        for(Inventory.InventoryItem item : this.blackBoard.itemTransfer.fromInventory.getItemList()){
            DataBuilder.JsonItem itemRef = item.itemRef;
            //If the item (in the inventory) has effects, let's check them!
            if(itemRef.getEffects() != null && itemRef.getEffects().length > 0) {
                //If the effect matches the effect we want, finish with success and reserve this item.
                if (itemRef.hasEffect(this.blackBoard.itemEffect) && item.getAmount(false) >= this.blackBoard.itemEffectAmount) {
                    this.blackBoard.itemTransfer.itemsToTransfer = new Array<>();
                    this.blackBoard.itemTransfer.itemsToTransfer.add(new ItemNeeded(itemRef.getItemName(), 1));
                    this.blackBoard.itemTransfer.fromInventory.reserveItem(itemRef.getItemName(), 1, this.blackBoard.myManager.getEntityOwner().getID()); //Reserve the item.
                    this.control.finishWithSuccess();
                    return;
                }
            }
        }

        this.control.finishWithFailure();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void end() {
        super.end();

        //Unreserve all the items.
        for(Inventory.InventoryItem item : this.blackBoard.itemTransfer.fromInventory.getItemList()){
            this.blackBoard.itemTransfer.fromInventory.unReserveItem(item.itemRef.getItemName(), this.blackBoard.myManager.getEntityOwner().getID());
        }
    }
}
