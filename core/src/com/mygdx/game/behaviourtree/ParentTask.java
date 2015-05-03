package com.mygdx.game.behaviourtree;

import com.mygdx.game.behaviourtree.control.ParentTaskController;
import com.mygdx.game.behaviourtree.control.TaskController;
import com.mygdx.game.component.BlackBoard;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public abstract class ParentTask extends Task{
    protected ParentTaskController control;

    public ParentTask(String name, BlackBoard blackBoard) {
        super(name, blackBoard);

        this.control = new ParentTaskController(this);
    }

    @Override
    public boolean check(){
        return this.control.callbacks.checkCriteria == null || this.control.callbacks.checkCriteria.test(this);
    }

    @Override
    public abstract void start();

    @Override
    public void update(float delta) {
        Task currTask = this.control.getCurrTask();

        //If we have finished, return.
        if(this.control.hasFinished()) {
            return;

        //If we have a null task, something went wrong, return.
        }else if(this.control.getCurrTask() == null) {
            return;

        //If the task hasn't been started, start it!
        }else if(!this.control.getCurrTask().getControl().hasStarted()) {
            if(!currTask.check())
                this.control.finishWithFailure();
            else
                currTask.getControl().safeStart();

        //If the task has finished, end it!
        }else if(currTask.getControl().hasFinished()){
            currTask.getControl().safeEnd(); //End the task.
            //If failed, call childFailed.
            if(currTask.getControl().hasFailed())
                this.childFailed();
            //If succeeded, call childSucceeded.
            else
                this.childSucceeded();

        //We're ready! Update the task!
        }else {
            this.control.getCurrTask().update(delta);
        }
    }

    @Override
    public abstract void end();

    @Override
    public TaskController getControl() {
        return this.control;
    }

    public abstract void childSucceeded();

    public abstract void childFailed();

    @Override
    public void setBlackBoard(BlackBoard blackBoard) {
        for(Task task : control.getSubTasks()){
            task.setBlackBoard(blackBoard);
        }
    }

    @Override
    public String getName() {
        if(this.control.currTask != null)
            return this.name;

        return "Nothing";
    }

    @Override
    public BlackBoard getBlackboard() {
        return this.blackBoard;
    }
}
