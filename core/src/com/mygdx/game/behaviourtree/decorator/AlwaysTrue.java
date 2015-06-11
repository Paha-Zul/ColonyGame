package com.mygdx.game.behaviourtree.decorator;

import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.util.BlackBoard;

/**
 * Created by brad on 6/10/15.
 */
public class AlwaysTrue extends TaskDecorator{
    public AlwaysTrue(String name, BlackBoard bb, Task task) {
        super(name, bb, task);
    }

    public AlwaysTrue(String name, BlackBoard bb) {
        super(name, bb, null);
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
    public void end() {
        super.end();
    }

    @Override
    public void update(float delta) {
        this.task.update(delta);
        if(this.task.getControl().hasFinished()) //If the task finished, always return success.
            this.control.finishWithSuccess();

    }
}
