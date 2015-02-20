package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.component.Transform;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.interfaces.Functional;

import java.util.ArrayList;

/**
 * Created by Paha on 2/1/2015.
 */
public class FindRandomNearbyLocation extends LeafTask{
    private int possibleDistance;

    public FindRandomNearbyLocation(String name, BlackBoard blackBoard, int possibleDistance) {
        super(name, blackBoard);
        this.possibleDistance = possibleDistance;
    }

    public FindRandomNearbyLocation(String name, BlackBoard blackBoard) {
        this(name, blackBoard, 1);
    }

    @Override
    public boolean check() {
        return true;
    }

    @Override
    public void start() {
        super.start();

        //Cache the owner transform.
        Transform transform = this.blackBoard.getEntityOwner().transform;

        //This function will get valid nodes around the entity.
        Functional.PerformAndGet<ArrayList<Grid.Node>, Grid.Node[][]> getValidNodes = (graph) -> {
            ArrayList<Grid.Node> nodes = new ArrayList<>();
            int radius = MathUtils.random(this.possibleDistance-1) + 1; //At least one...

            while(nodes.size() < 1) {
                int startX = (int) (transform.getPosition().x / this.blackBoard.colonyGrid.getSquareSize()) - radius;
                int endX = (int) (transform.getPosition().x / this.blackBoard.colonyGrid.getSquareSize()) + radius;
                int startY = (int) (transform.getPosition().y / this.blackBoard.colonyGrid.getSquareSize()) - radius;
                int endY = (int) (transform.getPosition().y / this.blackBoard.colonyGrid.getSquareSize()) + radius;

                for (int col = startX; col <= endX; col++) {
                    for (int row = startY; row <= endY; row++) {
                        Grid.Node node = this.blackBoard.colonyGrid.getNode(col, row);
                        if (node != null && (col == startX || col == endX || row == startY || row == endY) && WorldGen.getNode(node.getCol(), node.getRow()).type != Constants.TERRAIN_WATER)
                            nodes.add(node);
                    }
                }

                radius++;
            }

            return nodes;
        };

        //Get the valid nodes.
        ArrayList<Grid.Node> nodeList = this.blackBoard.colonyGrid.performAndGet(getValidNodes);

        //Pick a random node to go to.
        this.blackBoard.target = null;
        this.blackBoard.targetNode = null;
        if(nodeList.size() > 0)
            this.blackBoard.targetNode = nodeList.get(MathUtils.random(nodeList.size()-1));

        if(this.blackBoard.targetNode != null)
            this.control.finishWithSuccess();
        else
            this.control.finishWithFailure();
    }

    @Override
    public void end() {
        super.end();
    }
}
