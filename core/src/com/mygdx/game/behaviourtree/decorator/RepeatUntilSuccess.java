package com.mygdx.game.behaviourtree.decorator;

import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.util.BlackBoard;

/**
 * Created by Paha on 3/27/2015.
 */
public class RepeatUntilSuccess extends TaskDecorator{
    public RepeatUntilSuccess(String name, BlackBoard bb, Task task) {
        super(name, bb, task);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void update(float delta) {

        //If this task has finished with failure, restart!
        if(this.task.getControl().hasFinished() && this.task.getControl().hasFailed()){
            this.task.getControl().reset();
        }else if(this.task.getControl().hasFinished())
            this.control.finishWithSuccess();
        else
            this.task.update(delta);
    }

    @Override
    public void end() {
        super.end();
    }
}
