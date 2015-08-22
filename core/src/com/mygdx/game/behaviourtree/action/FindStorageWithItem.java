package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Building;
import com.mygdx.game.component.Colonist;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.util.ItemNeeded;

/**
 * Created by Paha on 7/30/2015.
 *
 * <p>
 * Finds a storage building that contains any item in the {@link BlackBoard.ItemTransfer#itemsToTransfer blackboard.itemsToTransfer} array.
 * If a valid storage is found, the entity is stored in {@link BlackBoard#target blackboard.target} for further use.
 * </p>
 * <p>
 * In short:
 * </p>
 * uses:
 *  <ul>
 *      <li>{@link BlackBoard.ItemTransfer#itemsToTransfer blackboard.itemsToTransfer} for checking the storage</li>
 *      <li>{@link BlackBoard#target blackboard.target} to store the result in.</li>
 *  </ul>
 *  </p>
 */
public class FindStorageWithItem extends LeafTask{
    public FindStorageWithItem(String name, BlackBoard blackBoard) {
        super(name, blackBoard);

    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {
        super.start();

        this.blackBoard.targetNode = null;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        //Get the colonist Component.
        Colonist colonist = this.blackBoard.myManager.getEntityOwner().getComponent(Colonist.class);
        //Search for a building that satisfies the function defined...
        Building storageWithItem = colonist.getColony().getOwnedFromColony(Building.class, building -> {
            //Let's not steal items from buildings being constructed.
            if(building.getEntityOwner().getTags().hasTag("constructing")) return false;

            Inventory inv = building.getComponent(Inventory.class); //Get the inventory
            //Search for at least one item from our list in the inventory of the building.
            for(ItemNeeded item : this.blackBoard.itemTransfer.itemsToTransfer)
                //If the inventory does have an item, set our target as the building, the to and from inventories, and finish with success.
                if(inv.hasItem(item.itemName)) {
                    this.blackBoard.target = building.getEntityOwner();
                    this.blackBoard.itemTransfer.fromInventory = inv; //Might as well cache this...
                    this.blackBoard.itemTransfer.toInventory = this.blackBoard.myManager.getComponent(Inventory.class); //Might as well cache this...
                    this.control.finishWithSuccess();
                    return true;
                }

            //Otherwise, we didn't find anything. Return false.
            return false;
        });

        if(storageWithItem == null)
            this.control.finishWithFailure();
    }

    @Override
    public void end() {
        super.end();
    }
}
