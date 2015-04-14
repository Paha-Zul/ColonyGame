package com.mygdx.game.helpers;

import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.interfaces.Functional;

/**
 * Created by Paha on 3/13/2015.
 */
public class Callbacks {
    public Functional.Callback finishCallback, successCallback, failureCallback, startCallback;
    public Functional.Criteria successCriteria, failCriteria;
    public Functional.Criteria<Task> checkCriteria, returnCriteria;
}
