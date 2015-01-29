package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.graphics.Texture;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.component.Transform;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.helpers.Grid;

/**
 * Created by Paha on 1/28/2015.
 */
public class MoveTo extends LeafTask{
    private int currIndex = 0;
    private Transform transform;
    private Grid.Node[] path;
    private int squareSize;
    private Texture square = new Texture("img/blueSquare.png");
    private Collider collider;

    public MoveTo(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return (blackBoard.path != null && blackBoard.path.length > 0);
    }

    @Override
    public void start() {
        super.start();

        this.transform = this.blackBoard.getEntityOwner().transform;
        this.path = this.blackBoard.path;
        this.squareSize = this.blackBoard.colonyGrid.getSquareSize();
        this.collider = this.transform.getComponent(Collider.class);
        this.currIndex = this.path.length - 1;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        if(this.currIndex < 0){
            this.control.finishWithSuccess();
            return;
        }

        Grid.Node currNode = path[currIndex];
        float nodeX = currNode.getCol()*squareSize + squareSize/2;
        float nodeY = currNode.getRow()*squareSize + squareSize/2;

        double rot = (Math.atan2(nodeY - transform.getPosition().y, nodeX - transform.getPosition().x));

        float x = (float)Math.cos(rot)*2000*delta;
        float y = (float)Math.sin(rot)*2000*delta;

        this.collider.body.setLinearVelocity(x, y);

        if((Math.abs(transform.getPosition().x - nodeX) + Math.abs(transform.getPosition().y - nodeY) < 10)) {
            currIndex--;
        }
    }

    @Override
    public void end() {
        super.end();
    }
}
