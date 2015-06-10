package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.util.BlackBoard;

/**
 * Created by brad on 6/10/15.
 */
public class CheckAndReserve extends LeafTask{
    public CheckAndReserve(String name, BlackBoard blackBoard) {
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

        for(int i=0;i<this.blackBoard.itemTransfer.itemNamesToTake.size;i++){
            String itemName = this.blackBoard.itemTransfer.itemNamesToTake.get(i);
            int amount = this.blackBoard.itemTransfer.itemAmountsToTake.get(i);
            int reserve = this.blackBoard.itemTransfer.fromInventory.reserveItem(itemName, amount);
        }
    }

    @Override
    public void end() {
        super.end();
    }
}
