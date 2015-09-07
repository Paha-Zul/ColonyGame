package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.util.GH;

/**
 * Created by Paha on 9/6/2015.
 * Gets the Entity owner of the Constructable and makes it the target.
 */
public class GetTargetFromConstructable extends LeafTask{
    public GetTargetFromConstructable(String name, BlackBoard blackBoard) {
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
        this.blackBoard.target = this.blackBoard.constructable.getEntityOwner();
        this.blackBoard.targetNode = null;
        this.control.finishWithSuccess();
    }

    @Override
    public void end() {
        super.end();
    }
}
