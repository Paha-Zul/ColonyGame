package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.util.ItemNeeded;

/**
 * Created by Paha on 9/6/2015.
 * Gets a list of tools in the inventory.
 */
public class GetToolsInInventory extends LeafTask {
    public GetToolsInInventory(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {
        super.start();

        this.blackBoard.itemTransfer.itemsToTransfer = new Array<>();
        if(this.blackBoard.myInventory == null) this.blackBoard.myInventory = this.blackBoard.myManager.getEntityOwner().getComponent(Inventory.class);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        //TODO Should probably handle this better. How about getting the tools from Equipment component instead of brute force checking all items?
        for(Inventory.InventoryItem item : this.blackBoard.myInventory.getItemList()){
            if(item.itemRef.getItemCategory().equals("tool"))
                this.blackBoard.itemTransfer.itemsToTransfer.add(new ItemNeeded(item.itemRef.getItemName(), item.getAmount()));
        }

        this.control.finishWithSuccess();
    }

    @Override
    public void end() {
        super.end();
    }
}
