package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.graphic.GraphicIdentity;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.ui.PlayerInterface;
import com.mygdx.game.util.BlackBoard;

/**
 * Created by Paha on 7/21/2015.
 */
public class Enter extends LeafTask{
    public Enter(String name, BlackBoard blackBoard) {
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

        //TODO setActive() is kind of expensive. Maybe we can do some kind of temp inactive where we skip the update loop instead of moving to a different list?
        GraphicIdentity identity = owner.getComponents().getIdentity();
        identity.setActive(false);
        owner.getTags().removeTag("selectable");
        PlayerInterface.getInstance().deselectEntity(owner);

        this.control.finishWithSuccess();
    }

    @Override
    public void end() {
        super.end();
    }
}
