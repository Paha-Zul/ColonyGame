package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.collider.Collider;

/**
 * Created by Paha on 7/21/2015.
 * 'Leaves' an Enterable entity.
 */
public class Exit extends LeafTask{
    public Exit(String name, BlackBoard blackBoard) {
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

        //We leave this blank because our main code is in the end() function. This is so that even if the job ends early and the unit
        //leaves a building, it still is performed.

        this.blackBoard.enterable.leave(this.blackBoard.myManager.getEntityOwner());
        this.control.finishWithSuccess();
    }

    @Override
    public void end() {
        super.end();

        if(this.blackBoard.enterable != null) this.blackBoard.enterable.leave(this.blackBoard.myManager.getEntityOwner());
        this.blackBoard.myManager.getEntityOwner().getGraphicIdentity().setActive(true);
        this.blackBoard.myManager.getEntityOwner().getTags().addTag("selectable");
        Collider collider = this.blackBoard.myManager.getEntityOwner().getComponent(Collider.class);
        if(collider != null) collider.getBody().setActive(true);
    }
}
