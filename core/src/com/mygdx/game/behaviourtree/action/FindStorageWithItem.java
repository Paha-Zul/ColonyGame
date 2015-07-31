package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Building;
import com.mygdx.game.component.Colonist;
import com.mygdx.game.component.Constructable;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.util.BlackBoard;
import com.mygdx.game.util.ItemNeeded;

/**
 * Created by Paha on 7/30/2015.
 *
 * <p>
 * Finds a storage building that contains any item in the {@link com.mygdx.game.util.BlackBoard.ItemTransfer#itemsToTransfer blackboard.itemsToTransfer} array.
 * If a valid storage is found, the entity is stored in {@link com.mygdx.game.util.BlackBoard#target blackboard.target} for further use.
 * </p>
 * <p>
 * In short:
 * </p>
 * uses:
 *  <ul>
 *      <li>{@link com.mygdx.game.util.BlackBoard.ItemTransfer#itemsToTransfer blackboard.itemsToTransfer} for checking the storage</li>
 *      <li>{@link com.mygdx.game.util.BlackBoard#target blackboard.target} to store the result in.</li>
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
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        Colonist colonist = this.blackBoard.myManager.getEntityOwner().getComponent(Colonist.class);
        Building storageWithItem = colonist.getColony().getOwnedFromColony(Building.class, building -> {
            Inventory inv = building.getComponent(Inventory.class);
            Constructable constructable = building.getComponent(Constructable.class);
            if(inv == null || constructable != null) //If there is no inventory or the building is being constructed, give up on this one.
                return false;

            for(ItemNeeded item : this.blackBoard.itemTransfer.itemsToTransfer)
                if(inv.hasItem(item.itemName)) {
                    this.blackBoard.target = building.getEntityOwner();
                    this.blackBoard.itemTransfer.fromInventory = inv; //Might as well cache this...
                    this.blackBoard.itemTransfer.toInventory = this.blackBoard.myManager.getComponent(Inventory.class); //Might as well cache this...
                    this.control.finishWithSuccess();
                    return true;
                }

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
