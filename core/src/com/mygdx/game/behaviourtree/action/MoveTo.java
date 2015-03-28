package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.component.Transform;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.helpers.Callbacks;
import com.mygdx.game.helpers.Constants;

import java.util.LinkedList;

/**
 * Created by Paha on 1/28/2015.
 */
public class MoveTo extends LeafTask{
    private Transform transform;
    private LinkedList<Vector2> path;
    private Collider collider;
    private String name;

    private final float completeDst = 1f/ Constants.SCALE;

    public MoveTo(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return (blackBoard.path != null && blackBoard.path.size() > 0);
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

        if(this.getControl().getCallbacks().criteria != null && !this.getControl().getCallbacks().criteria.criteria(this)){
            this.collider.body.setLinearVelocity(0, 0);
            this.control.finishWithFailure();
            return;
        }

        if(this.path == null || this.path.size() < 1){
            this.collider.body.setLinearVelocity(0,0);
            this.control.finishWithSuccess();
            this.path.clear();
            this.path = null;
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
        super.end();
    }
}
