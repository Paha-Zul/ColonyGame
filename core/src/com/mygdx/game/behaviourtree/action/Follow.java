package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Transform;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.Grid;
import com.mygdx.game.util.Pathfinder;

import java.util.LinkedList;

/**
 * Created by Paha on 4/13/2015.
 * Follows an Entity, recalculating the path when necessary. When far out, the path does not recalculate as often, but increases the closer the Entity gets to the Entity target.
 */
public class Follow extends LeafTask{
    private Transform transform;
    private LinkedList<Vector2> path;
    private Collider collider;

    private final float completeDst = GH.toMeters(1);

    public Follow(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check() && this.blackBoard.target.isValid() && this.blackBoard.target.getTags().hasTag("alive");
    }

    @Override
    public void start() {
        super.start();

        if(transform == null) this.transform = this.blackBoard.myManager.getEntityOwner().getTransform();
        if(collider == null) this.collider = this.transform.getComponent(Collider.class);
        this.name = this.transform.getEntityOwner().name;
        this.blackBoard.targetNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.target);
        this.blackBoard.path = this.path = Pathfinder.GetInstance().findPath(this.blackBoard.myManager.getEntityOwner().getTransform().getPosition(), this.blackBoard.target.getTransform().getPosition());
    }

    @Override
    public void update(float delta) {
        super.update(delta);


        //If our fail criteria passes, fail this task.
        if(this.getControl().getCallbacks().failCriteria != null && this.getControl().getCallbacks().failCriteria.test(this)){
            this.collider.getBody().setLinearVelocity(0, 0);
            this.control.finishWithFailure();
            return;
        }

        //If our success criteria passes, succeed this task.
        if(this.getControl().getCallbacks().successCriteria != null && this.getControl().getCallbacks().successCriteria.test(this)){
            this.collider.getBody().setLinearVelocity(0, 0);
            this.control.finishWithSuccess();
            return;
        }

        Vector2 currPoint = path.peekLast();
        float nodeX, nodeY, x, y;
        double rot;
        Grid.Node myNode = this.blackBoard.colonyGrid.getNode(blackBoard.myManager.getEntityOwner());
        Grid.Node targetNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.target);
        int disToTarget = Math.abs(myNode.getX() - targetNode.getX()) + Math.abs(myNode.getY() - targetNode.getY());
        int disFromTargetToTargetNode = Math.abs(targetNode.getX() - this.blackBoard.targetNode.getX()) + Math.abs(targetNode.getY() - this.blackBoard.targetNode.getY());

        //TODO Some magic numbers here.
        //We want to repath when the target away from it's original path targetNode by about half of our distance to the target. Example:
        // us to target: 10, target to original path dest: 6, 10/2 = 5 which is <= 6, so repath!
        if(disToTarget/2 <= disFromTargetToTargetNode) {
            this.path = this.blackBoard.path = Pathfinder.GetInstance().findPath(this.blackBoard.myManager.getEntityOwner().getTransform().getPosition(), this.blackBoard.target.getTransform().getPosition());
            this.blackBoard.targetNode = this.blackBoard.colonyGrid.getNode(this.blackBoard.target);
        }

        //If we are still outside our range to move towards the target, move!
        if(blackBoard.target.getTransform().getPosition().dst(blackBoard.myManager.getEntityOwner().getTransform().getPosition()) >= GH.toMeters(this.blackBoard.followDis)){

            //If we are within a certain distance of our target, direct move to him!
            if(disToTarget < 3){
                nodeX = blackBoard.target.getTransform().getPosition().x;
                nodeY = blackBoard.target.getTransform().getPosition().y;

            //Otherwise, let's move towards our next path destination.
            }else{
                nodeX = currPoint.x;
                nodeY = currPoint.y;

                if((Math.abs(transform.getPosition().x - nodeX) + Math.abs(transform.getPosition().y - nodeY) < completeDst*(delta*100))) {
                    this.path.removeLast();
                }
            }

            //Move!
            rot = (Math.atan2(nodeY - transform.getPosition().y, nodeX - transform.getPosition().x));
            x = (float)Math.cos(rot)*this.blackBoard.moveSpeed*delta;
            y = (float)Math.sin(rot)*this.blackBoard.moveSpeed*delta;
            this.collider.getBody().setLinearVelocity(x, y);

        //If we are within the stop range, then don't move!
        }else {
            collider.getBody().setLinearVelocity(0, 0);
            this.control.finishWithSuccess();
        }
    }

    @Override
    public void end() {
        if(this.collider != null) this.collider.getBody().setLinearVelocity(0, 0);
        this.transform = null;
        this.collider = null;
        this.name = null;
        this.path = null;
        super.end();
    }
}
