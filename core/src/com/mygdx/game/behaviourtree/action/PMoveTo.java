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
public class PMoveTo extends LeafTask{
    private Transform transform;
    private LinkedList<Vector2> path;
    private Collider collider;

    private final float completeDst = GH.toMeters(1);

    public PMoveTo(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {
        super.start();

        this.transform = this.blackBoard.getEntityOwner().transform;
        this.name = this.transform.getEntityOwner().name;
        this.path = this.blackBoard.path;
        this.collider = this.transform.getComponent(Collider.class);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        //If the path is null or empty, simply set velocity to 0 and return.
        if(this.path == null || this.path.size() < 1 || (control.callbacks.returnCriteria != null && control.callbacks.returnCriteria.criteria(this))){
            this.collider.body.setLinearVelocity(0, 0);
            return;
        }

        if(this.getControl().getCallbacks().failCriteria != null && this.getControl().getCallbacks().failCriteria.criteria(this)){
            this.collider.body.setLinearVelocity(0, 0);
            this.control.finishWithFailure();
            return;
        }

        if(this.getControl().getCallbacks().successCriteria != null && this.getControl().getCallbacks().successCriteria.criteria(this)){
            this.collider.body.setLinearVelocity(0, 0);
            this.control.finishWithSuccess();
            return;
        }

        Vector2 currPoint = path.peekLast();
        float nodeX = currPoint.x;
        float nodeY = currPoint.y;

        double rot = (Math.atan2(nodeY - transform.getPosition().y, nodeX - transform.getPosition().x));

        float x = (float)Math.cos(rot)*this.blackBoard.moveSpeed*delta;
        float y = (float)Math.sin(rot)*this.blackBoard.moveSpeed*delta;

        this.collider.body.setLinearVelocity(x, y);

        if((Math.abs(transform.getPosition().x - nodeX) + Math.abs(transform.getPosition().y - nodeY) < completeDst*(delta*100))) {
            this.path.removeLast();
        }
    }

    @Override
    public void end() {
        if(this.collider != null) this.collider.body.setLinearVelocity(0, 0);
        super.end();
    }
}
