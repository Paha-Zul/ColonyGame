package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Enterable;

/**
 * Created by Paha on 8/18/2015.
 */
public class GetEnterableFromTarget extends LeafTask{
    public GetEnterableFromTarget(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check() && this.blackBoard.target != null;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        this.blackBoard.enterable = this.blackBoard.target.getComponent(Enterable.class);
        this.control.finishWithSuccess();
    }

    @Override
    public void end() {
        super.end();
    }
}
