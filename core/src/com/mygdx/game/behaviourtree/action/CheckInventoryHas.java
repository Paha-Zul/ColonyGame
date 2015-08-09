package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.ItemNeeded;
import com.mygdx.game.util.managers.DataManager;

/**
 * Created by Paha on 3/15/2015.
 */
public class CheckInventoryHas extends LeafTask {

    public CheckInventoryHas(String name, BlackBoard blackBoard) {
        super(name, blackBoard);

    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public void start() {
        super.start();

        //TODO This probably needs to be fixed. Maybe it worked out alright though.

        //Loop over each item in the "fromInventory" and check if any of the items has the effect we want.
        for(Inventory.InventoryItem item : this.blackBoard.itemTransfer.fromInventory.getItemList()){
            DataBuilder.JsonItem itemRef = DataManager.getData(item.itemRef.getItemName(), DataBuilder.JsonItem.class);
            if(itemRef.getEffects() != null && itemRef.getEffects().length > 0) {
                if (itemRef.hasEffect(this.blackBoard.itemEffect) && item.getAmount(false) >= this.blackBoard.itemEffectAmount) {
                    this.control.finishWithSuccess();
                    this.blackBoard.itemTransfer.itemsToTransfer = new Array<>();
                    this.blackBoard.itemTransfer.itemsToTransfer.add(new ItemNeeded(itemRef.getItemName(), 1));
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
    }
}
