package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.graphic.GraphicIdentity;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.BlackBoard;

/**
 * Created by Paha on 7/21/2015.
 */
public class Leave extends LeafTask{
    public Leave(String name, BlackBoard blackBoard) {
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

        Entity owner = this.blackBoard.myManager.getEntityOwner();

        //TODO setActive() is expensive...
        GraphicIdentity identity = owner.getGraphicIdentity();
        identity.setActive(true);
        owner.getTags().addTag("selectable");

        this.control.finishWithSuccess();
    }

    @Override
    public void end() {
        super.end();
    }
}
