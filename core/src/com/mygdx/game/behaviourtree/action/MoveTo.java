package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.component.Transform;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.helpers.GH;

import java.util.LinkedList;

/**
 * Created by Paha on 1/28/2015.
 *
 * <p>This task will move the {@link com.mygdx.game.entity.Entity Entity} that owns this Task towards a target {@link com.mygdx.game.helpers.Grid.Node Node }
 * following the blackBoard.path that was generated beforehand.</p>
 *
 * <p>During the update, {@link com.mygdx.game.helpers.Callbacks#successCriteria control.callbacks.successCriteria } and {@link com.mygdx.game.helpers.Callbacks#failCriteria control.callbacks.failCriteria }
 * are tested to succeed or fail the task, respectively. To move the Entity of this Task,
 * the Entity must have a {@link Collider collider} to apply velocity to. The collider will be applied velocity that is dependant upon the {@link BlackBoard#moveSpeed moveSpeed} blackBoard.moveSpeed field.
 * When the path is completed, the Entity's collider velocity will be set to 0 and the task will succeed.</p>
 */
public class MoveTo extends LeafTask{
    private Transform transform;
    private LinkedList<Vector2> path;
    private Collider collider;

    private final float completeDst = GH.toMeters(1);

    public MoveTo(String name, BlackBoard blackBoard) {
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

        //If the target is null, path is null, or path is empty, end this job with failure!
        if(this.path == null || this.path.size() < 1){
            this.collider.body.setLinearVelocity(0, 0);
            this.control.finishWithSuccess();
            if(this.path != null) this.path.clear();
            this.path = null;
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
        float nodeX = currPoint.x;
        float nodeY = currPoint.y;

        double rot = (Math.atan2(nodeY - transform.getPosition().y, nodeX - transform.getPosition().x));

        float x = (float)Math.cos(rot)*this.blackBoard.moveSpeed*delta;
        float y = (float)Math.sin(rot)*this.blackBoard.moveSpeed*delta;

        this.collider.body.setLinearVelocity(x, y);

        if((Math.abs(transform.getPosition().x - nodeX) + Math.abs(transform.getPosition().y - nodeY) <= completeDst*(delta*blackBoard.moveSpeed))) {
            this.path.removeLast();
        }
    }

    @Override
    public void end() {
        if(this.collider != null) this.collider.body.setLinearVelocity(0, 0);
        super.end();
    }
}
