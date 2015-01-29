package com.mygdx.game.component;

import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.behaviourtree.action.FindPath;
import com.mygdx.game.behaviourtree.action.Idle;
import com.mygdx.game.behaviourtree.action.MoveTo;
import com.mygdx.game.behaviourtree.action.Talk;
import com.mygdx.game.behaviourtree.composite.Sequence;
import com.mygdx.game.behaviourtree.control.ParentTaskController;
import com.mygdx.game.helpers.BehaviourManager;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class BehaviourManagerComp extends Component{
    private BlackBoard blackBoard;
    private Task behaviourTree;

    public BehaviourManagerComp() {

    }

    @Override
    public void start() {
        super.start();

        this.blackBoard = this.owner.addComponent(new BlackBoard());
        this.blackBoard.colonyGrid = ColonyGame.worldGrid;
        this.blackBoard.targetNode = this.blackBoard.colonyGrid.getNode(0,0);
        behaviourTree = moveTo();
        behaviourTree.start();
    }

    private Task moveTo(){
        //Get the target node/Entity.
        //Find the path.
        //Move to the target.

        Sequence sequence = new Sequence("MoveTo", this.blackBoard);

        FindPath findPath = new FindPath("FindPath",  this.blackBoard);
        MoveTo followPath = new MoveTo("FollowPath",  this.blackBoard);

        ((ParentTaskController)(sequence.getControl())).addTask(findPath);
        ((ParentTaskController)(sequence.getControl())).addTask(followPath);

        return sequence;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        behaviourTree.update(delta);
    }

    private void testTask(){

        Sequence sequence = new Sequence("Sequence", blackBoard);
        ((ParentTaskController)sequence.getControl()).addTask(new Idle("Idle", blackBoard));
        ((ParentTaskController)sequence.getControl()).addTask(new Talk("Talk", blackBoard));

        behaviourTree = sequence;
        sequence.start();
    }
}
