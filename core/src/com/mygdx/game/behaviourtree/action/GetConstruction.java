package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Building;
import com.mygdx.game.component.Colonist;
import com.mygdx.game.component.Constructable;

/**
 * Created by Paha on 6/22/2015.
 */
public class GetConstruction extends LeafTask{
    public GetConstruction(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {
        super.start();

        Colonist colonist = this.blackBoard.myManager.getEntityOwner().getComponent(Colonist.class);
        Building building = colonist.getOwningColony().getOwnedFromColony(Building.class, b -> b.getEntityOwner().getTags().hasTag("constructing"));
        if(building != null) {
            this.blackBoard.target = building.getEntityOwner();
            this.blackBoard.constructable = building.getComponent(Constructable.class);
            if (this.blackBoard.target == null) this.control.finishWithFailure();
            else this.control.finishWithSuccess();
        }else
            this.control.finishWithFailure();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void end() {
        super.end();
    }
}
