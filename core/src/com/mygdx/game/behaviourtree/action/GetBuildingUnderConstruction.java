package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Building;
import com.mygdx.game.component.Colonist;
import com.mygdx.game.util.BlackBoard;

/**
 * Created by Paha on 6/22/2015.
 */
public class GetBuildingUnderConstruction extends LeafTask{
    public GetBuildingUnderConstruction(String name, BlackBoard blackBoard) {
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
        Building building = colonist.getColony().getOwnedFromColony(Building.class, b -> b.getEntityOwner().getTags().hasTag("construction"));
        this.blackBoard.target = building.getEntityOwner();
        if(this.blackBoard.target == null) this.control.finishWithFailure();
        else this.control.finishWithSuccess();
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
