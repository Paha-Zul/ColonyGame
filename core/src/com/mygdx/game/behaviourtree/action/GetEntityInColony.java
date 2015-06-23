package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Colonist;
import com.mygdx.game.util.BlackBoard;

/**
 * Created by Paha on 6/22/2015.
 */
public class GetEntityInColony extends LeafTask{
    public GetEntityInColony(String name, BlackBoard blackBoard) {
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

        Colonist colonist = this.blackBoard.target.getComponent(Colonist.class);
        Array<?> list = colonist.getColony().getOwnedListFromColony(this.blackBoard.clazzType);

    }

    @Override
    public void end() {
        super.end();
    }
}
