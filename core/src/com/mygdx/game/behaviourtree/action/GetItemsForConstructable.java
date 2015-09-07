package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.util.GH;

/**
 * Created by Paha on 9/6/2015.
 * Gets the Items needed from our Constructable and stores it in blackboard.itemTransfer.itemsToTransfer.
 */
public class GetItemsForConstructable extends LeafTask{
    public GetItemsForConstructable(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check() && GH.isValid(this.blackBoard.constructable);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        this.blackBoard.itemTransfer.itemsToTransfer = new Array<>();
        this.blackBoard.itemTransfer.itemsToTransfer = this.blackBoard.constructable.getItemsNeededCopy();

        //If the list is not null and has at least one item, finish this with success.
        if(this.blackBoard.itemTransfer.itemsToTransfer.size > 0) this.control.finishWithSuccess();
        else this.control.finishWithFailure();
    }

    @Override
    public void end() {
        super.end();
    }
}
