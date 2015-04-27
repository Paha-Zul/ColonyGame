package com.mygdx.game.behaviourtree.control;

import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.helpers.Callbacks;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class TaskController {
    protected boolean ready=true, failed=false, finished=false, running = false, started = false;
    protected Task task;
    public Callbacks callbacks;

    public TaskController(Task task){
        this.task = task;
        this.callbacks = new Callbacks();
    }

    /**
     * Finishes this behaviour with failure.
     */
    public void finishWithFailure(){
        this.finished = true;
        this.failed = true;
        this.running = false;
    }

    /**
     * Finished this behaviour with success.
     */
    public void finishWithSuccess(){
        this.finished = true;
        this.running = false;
        this.failed = false;
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

        if(this.callbacks != null && this.callbacks.startCallback != null)
            this.callbacks.startCallback.accept(this.task);
    }

    /**
     * Ends the task. Also calls any callbacks if valid.
     */
    public void safeEnd(){

        if(callbacks != null) {
            if (this.hasFailed() && this.callbacks.failureCallback != null)
                this.callbacks.failureCallback.accept(this.task); //Failure callback
            else if (!this.hasFailed() && this.callbacks.successCallback != null)
                this.callbacks.successCallback.accept(this.task); //Success callback

            if(this.callbacks.finishCallback != null) //General finish callback.
                this.callbacks.finishCallback.accept(this.task);
        }

        this.task.end();
    }

    public void setTask(Task task){
        this.task = task;
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

    public Callbacks getCallbacks(){
        return this.callbacks;
    }
}
