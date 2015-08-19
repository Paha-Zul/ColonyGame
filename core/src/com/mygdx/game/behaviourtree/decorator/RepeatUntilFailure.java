package com.mygdx.game.behaviourtree.decorator;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.Task;

/**
 * Created by Paha on 8/18/2015.
 * Repeats until its task that it decorates finishes with failure.
 */
public class RepeatUntilFailure extends TaskDecorator{
    public RepeatUntilFailure(String name, BlackBoard bb, Task task) {
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
        this.task.update(delta);

        //If the task finished...
        if(this.task.getControl().hasFinished()){
            //If failed, succeed this task decorator.
            if(this.task.getControl().hasFailed())
                this.control.finishWithSuccess();

            //Otherwise, if succeeded, try to restart the task.
            else{
                this.task.getControl().reset(); //Reset the task
                if(!this.task.check())          //If it doesn't pass the check, finish with success.
                    this.control.finishWithSuccess();
                else                            //Otherwise, start the task.
                    this.task.getControl().safeStart();
            }
        }
    }

    @Override
    public void end() {
        super.end();
    }
}
