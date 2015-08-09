package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class Talk extends LeafTask {

    public Talk(String name, BlackBoard blackBoard) {
        super(name, blackBoard);

    }

    @Override
    public void start() {
        super.start();

    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public void update(float delta) {
        System.out.println("HAI!");
        this.control.finishWithSuccess();
    }
}
