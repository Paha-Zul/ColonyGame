package com.mygdx.game.behaviourtree.control;

import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.interfaces.Functional;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class TaskController {
    protected boolean ready=true, failed=false, finished=false, running = false, started = false;
    protected Task task;
    protected Functional.Callback successCallback, failureCallback;

    public TaskController(Task task){
        this.task = task;
    }

    public TaskController(Task task, Functional.Callback successCallback, Functional.Callback failureCallback){
        this.task = task;
        this.successCallback = successCallback;
        this.failureCallback = failureCallback;
    }

    /**
     * Finishes this behaviour with failure.
     */
    public void finishWithFailure(){
        this.finished = true;
        this.failed = true;
        this.running = false;

        this.task.getControl().safeEnd();
    }

    /**
     * Finished this behaviour with success.
     */
    public void finishWithSuccess(){
        this.finished = true;
        this.running = false;
        this.failed = false;

        this.safeEnd();
    }

    /**
     * Resets this controller.
     */
    public void reset(){
        this.ready = true;
        this.running = false;
        this.failed = false;
        this.started = false;
        this.finished = false;
    }

    /**
     * Starts the task.
     */
    public void safeStart(){
        this.started = true;
        this.task.start();
    }

    /**
     * Ends the task. Also calls any callbacks if valid.
     */
    public void safeEnd(){
        if(hasFailed() && this.failureCallback != null)
            this.failureCallback.callback();
        else if(this.successCallback != null)
            this.successCallback.callback();

        this.task.end();
    }

    /**
     * Sets the TaskController as ready or not ready.
     * @param ready A boolean to set the TaskController as ready or not ready.
     */
    public void setReady(boolean ready){this.ready = ready;}

    /**
     * Sets this TaskController to running/not running.
     * @param running A Boolean to set the TaskController as running or not running.
     */
    public void setRunning(boolean running){this.running = running;}

    /**
     * @return True if running, false otherwise.
     */
    public boolean isRunning(){
        return running;
    }

    /**
     * @return True if ready, false otherwise.
     */
    public boolean isReady(){
        return ready;
    }

    /**
     * @return True if failed previously, false otherwise.
     */
    public boolean hasFailed(){
        return failed;
    }

    /**
     * @return True if finished, false otherwise.
     */
    public boolean hasFinished(){
        return finished;
    }

    public boolean hasStarted(){
        return this.started;
    }
}
