package com.mygdx.game.behaviourtree.decorator;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.Task;

/**
 * Created by Paha on 8/18/2015.
 */
public class ReturnOpposite extends TaskDecorator{
    public ReturnOpposite(String name, BlackBoard bb, Task task) {
        super(name, bb, task);
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
        //Flip the success/fail
        if(this.task.getControl().hasFinished()){
            if(this.task.getControl().hasFailed()) this.control.finishWithSuccess();
            else this.control.finishWithFailure();
        }

        this.task.update(delta);
    }

    @Override
    public void end() {
        super.end();
    }
}
