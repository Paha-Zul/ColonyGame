package com.mygdx.game.component;

import com.badlogic.gdx.math.Rectangle;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.collider.Collider;
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
    @JsonProperty
    private boolean addMulti = false;

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
        this.grid = ColonyGame.worldGrid;
        if(!addMulti) this.currNode = this.grid.addToGrid(this.owner);
        else{
            Rectangle bounds = new Rectangle();
            Collider collider = this.getComponent(Collider.class);
            bounds.setX(this.owner.getTransform().getPosition().x - collider.fixture.getShape().getRadius()/2);
            bounds.setY(this.owner.getTransform().getPosition().y - collider.fixture.getShape().getRadius()/2);
            bounds.setWidth(collider.fixture.getShape().getRadius());
            bounds.setHeight(collider.fixture.getShape().getRadius());
            this.currNode = this.grid.addToGrid(this.owner, true, bounds);
        }
        this.grid.addViewer(this.currNode, this.exploreRadius);
        this.load();
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {
        this.grid = ColonyGame.worldGrid;
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        this.currNode = this.grid.checkNode(this.currNode, this.owner, true, exploreRadius);
    }

    @JsonIgnore
    public GridComponent setGridType(int gridType) {
        this.gridType = gridType;
        return this;
    }

    @JsonIgnore
    public GridComponent setGrid(Grid.GridInstance grid) {
        this.grid = grid;
        return this;
    }

    @JsonIgnore
    public GridComponent setExploreRadius(int exploreRadius) {
        this.exploreRadius = exploreRadius;
        return this;
    }

    @JsonIgnore
    public GridComponent setAddMulti(boolean multi){
        this.addMulti = multi;
        return this;
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
