package com.mygdx.game.component;

import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.Grid;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by Paha on 1/17/2015.
 */
public class GridComponent extends Component{
    @JsonProperty
    private int gridType;
    @JsonIgnore
    private Grid.Node currNode;
    @JsonIgnore
    private Grid.GridInstance grid;
    @JsonProperty
    public int exploreRadius = 3;

    public GridComponent() {
        super();
    }

    @Override
    public void init(Entity owner) {
        super.init(owner);
    }

    @Override
    public void start() {
        super.start();
        //Gets a node to start.
        this.currNode = this.grid.addToGrid(this.owner);
        this.grid.addViewer(this.currNode, this.exploreRadius);
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {

    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.currNode = this.grid.checkNode(this.currNode, this.owner, true, exploreRadius);
    }

    public void setGridType(int gridType) {
        this.gridType = gridType;
    }

    public void setGrid(Grid.GridInstance grid) {
        this.grid = grid;
    }

    public void setExploreRadius(int exploreRadius) {
        this.exploreRadius = exploreRadius;
    }

    @JsonIgnore
    public Grid.Node getCurrNode(){
        return this.currNode;
    }

    @Override
    public void destroy(Entity destroyer) {
        this.currNode.removeEntity(this.owner);

        super.destroy(destroyer);
    }
}
