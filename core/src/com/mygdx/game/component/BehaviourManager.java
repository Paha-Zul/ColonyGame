package com.mygdx.game.component;

import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.behaviourtree.action.Idle;
import com.mygdx.game.behaviourtree.action.Talk;
import com.mygdx.game.behaviourtree.composite.Sequence;
import com.mygdx.game.behaviourtree.control.ParentTaskController;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class BehaviourManager extends Component{
    BlackBoard blackBoard;
    Task behaviourTree;

    public BehaviourManager() {

    }

    @Override
    public void start() {
        super.start();

        this.blackBoard = this.owner.addComponent(new BlackBoard());

        Sequence sequence = new Sequence("Sequence", blackBoard);
        ((ParentTaskController)sequence.getControl()).addTask(new Idle("Idle", blackBoard));
        ((ParentTaskController)sequence.getControl()).addTask(new Talk("Talk", blackBoard));

        behaviourTree = sequence;
        sequence.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        behaviourTree.update(delta);
    }
}
