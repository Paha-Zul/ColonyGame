package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.ItemNeeded;

/**
 * Created by Paha on 9/7/2015.
 * Checks if the target or me has any of the items (at least one) in the blackBoard.itemTransfer.itemsToTransfer array.
 */
public class CheckTargetOrMeHasAnyItems extends LeafTask{
    private Inventory targetInventory;

    public CheckTargetOrMeHasAnyItems(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check() && GH.isValid(this.blackBoard.target);
    }

    @Override
    public void start() {
        super.start();

        this.targetInventory = this.blackBoard.target.getComponent(Inventory.class);
        if(this.targetInventory == null) this.control.finishWithFailure();
        if(this.blackBoard.myInventory == null) this.blackBoard.myInventory = this.blackBoard.myManager.getEntityOwner().getComponent(Inventory.class);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        //If either my inventory or the target inventory have any item in the itemsToTransfer array, finish with success!
        for(ItemNeeded item : this.blackBoard.itemTransfer.itemsToTransfer){
            if(this.blackBoard.myInventory.hasItem(item.itemName) || this.targetInventory.hasItem(item.itemName)){
                this.control.finishWithSuccess();
                return;
            }
        }

        //Otherwise, we failed :(
        this.control.finishWithFailure();
    }

    @Override
    public void end() {
        super.end();
    }
}
