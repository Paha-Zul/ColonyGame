package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.helpers.managers.ItemManager;

/**
 * Created by Paha on 3/15/2015.
 */
public class CheckInventoryHas extends LeafTask {
    private String itemName, effect;
    private int quantity;

    public CheckInventoryHas(String name, BlackBoard blackBoard, String effect, int quantity) {
        super(name, blackBoard);

        this.effect = effect;
        this.quantity = quantity;
    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public void start() {
        super.start();

        for(Inventory.InventoryItem item : this.blackBoard.fromInventory.getItemList()){
            DataBuilder.JsonItem itemRef = ItemManager.getItemReference(item.itemRef.getItemName());
            if(itemRef.getEffects() != null && itemRef.getEffects().length > 0) {
                if (itemRef.hasEffect(effect) && item.getAmount() >= quantity) {
                    this.control.finishWithSuccess();
                    this.blackBoard.itemNameToTake = itemRef.getItemName();
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
