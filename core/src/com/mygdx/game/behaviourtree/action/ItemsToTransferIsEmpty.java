package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;

/**
 * Created by Paha on 8/18/2015.
 */
public class ItemsToTransferIsEmpty extends LeafTask{
    public ItemsToTransferIsEmpty(String name, BlackBoard blackBoard) {
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

        if(this.blackBoard.itemTransfer.itemsToTransfer.size == 0)
            this.control.finishWithSuccess();
        else
            this.control.finishWithFailure();
    }

    @Override
    public void end() {
        super.end();
    }
}
