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

    @Override
    public boolean check() {
        return this.blackBoard.transferToInventory != null;
    }

    @Override
    public void start() {
        super.start();

        for (Inventory.InventoryItem item : this.blackBoard.myInventory.getItemList()){
            this.blackBoard.transferToInventory.addItem(item.item);
        }

        this.blackBoard.myInventory.clearInventory();
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
