package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.component.Transform;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.helpers.GH;

import java.util.LinkedList;

/**
 * Created by Paha on 4/13/2015.
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
        return super.check();
    }

    @Override
    public void start() {
        super.start();

        if(transform == null) this.transform = this.blackBoard.getEntityOwner().transform;
        if(collider == null) this.collider = this.transform.getComponent(Collider.class);
        this.name = this.transform.getEntityOwner().name;
        this.path = this.blackBoard.path;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        //If the path is null or empty, simply set velocity to 0 and return.
        if(this.path == null || this.path.size() < 1 || (control.callbacks.returnCriteria != null && control.callbacks.returnCriteria.test(this))){
            this.collider.body.setLinearVelocity(0, 0);
            return;
        }

        if(this.getControl().getCallbacks().failCriteria != null && this.getControl().getCallbacks().failCriteria.test(this)){
            this.collider.body.setLinearVelocity(0, 0);
            this.control.finishWithFailure();
            return;
        }

        if(this.getControl().getCallbacks().successCriteria != null && this.getControl().getCallbacks().successCriteria.test(this)){
            this.collider.body.setLinearVelocity(0, 0);
            this.control.finishWithSuccess();
            return;
        }

        Vector2 currPoint = path.peekLast();
        float nodeX, nodeY, x, y;
        double rot;

        //If we are still outside our range to move towards the target, move!
        if(blackBoard.target.transform.getPosition().dst(blackBoard.getEntityOwner().transform.getPosition()) >= GH.toMeters(blackBoard.followDis)){

            //If the node of our target that we are following matches ours (we're on the same tile!), direct move to it!
            if(blackBoard.colonyGrid.getNode(blackBoard.target) == blackBoard.colonyGrid.getNode(blackBoard.getEntityOwner())){
                nodeX = blackBoard.target.transform.getPosition().x;
                nodeY = blackBoard.target.transform.getPosition().y;

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
            this.collider.body.setLinearVelocity(x, y);

        //If we are within the stop range, then don't move!
        }else
            collider.body.setLinearVelocity(0, 0);
    }

    @Override
    public void end() {
        if(this.collider != null) this.collider.body.setLinearVelocity(0, 0);
        this.transform = null;
        this.collider = null;
        this.name = null;
        this.path = null;
        super.end();
    }
}
