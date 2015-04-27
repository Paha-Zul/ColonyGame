package com.mygdx.game.helpers;

import com.mygdx.game.behaviourtree.Task;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Created by Paha on 3/13/2015.
 */
public class Callbacks {
    public Consumer<Task> finishCallback, successCallback, failureCallback, startCallback;
    public Predicate successCriteria, failCriteria;
    public Predicate<Task> checkCriteria, returnCriteria;
}
