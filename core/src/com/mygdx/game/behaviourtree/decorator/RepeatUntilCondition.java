package com.mygdx.game.behaviourtree.decorator;

import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.Task;

/**
 * Created by Paha on 4/6/2015.
 *
 * <p>Repeats a task until a condition is met. The condition is tested by using {@link com.mygdx.game.util.Callbacks#successCriteria successCritera} which takes a {@link Task task} to test
 * the task for completion.</p>
 */
public class RepeatUntilCondition extends TaskDecorator{
    public RepeatUntilCondition(String name, BlackBoard bb, Task task) {
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

        if(this.task.getControl().hasFinished()){
            if(control.callbacks.successCriteria != null && control.callbacks.successCriteria.test(this.task))
                this.control.finishWithSuccess();
            else if(control.callbacks.failCriteria != null && control.callbacks.failCriteria.test(this.task))
                this.control.finishWithFailure();
            else{
                this.task.getControl().reset();
                this.task.getControl().safeStart();
            }
        }
    }

    @Override
    public void end() {
        super.end();
    }

    @Override
    public String getName() {
        return super.getName()+" "+this.task.getName();
    }
}
