package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.util.Constants;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.Grid;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

public class GraphicIdentity extends Component{
    @JsonIgnore
	public Sprite sprite;
    @JsonProperty
    public int alignment = 0; //center
    @JsonProperty
    private int currVisibility=0;

    public GraphicIdentity(){

    }


	@Override
	public void start() {
        //This is initially needed for getting the sprite to be the right size. If we simply scaled it using this method, then
        //the image would draw the right size, but the offset from the width and height being unaffected causes real problems whenever the image
        //is not centered.
        this.sprite.setSize(GH.toMeters(sprite.getRegionWidth()), GH.toMeters(sprite.getRegionHeight()));
        this.sprite.setOrigin(this.sprite.getWidth() / 2, this.sprite.getHeight() / 2);
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {

    }

    @Override
    public void render(float delta, SpriteBatch batch) {
        super.render(delta, batch);
        Grid.GridInstance grid = ColonyGame.worldGrid;

        Vector2 pos = this.owner.getTransform().getPosition(); //Cache the owner's position.

        if(!ColonyGame.camera.frustum.boundsInFrustum(pos.x, pos.y, 0, sprite.getWidth(), sprite.getHeight(), 0))
            return;

        Grid.Node node = grid.getNode(this.owner);
        int visibility = grid.getVisibilityMap()[node.getX()][node.getY()].getVisibility();
        if(visibility == Constants.VISIBILITY_UNEXPLORED)
            return;

        this.changeVisibility(visibility);

        this.sprite.setRotation(this.owner.getTransform().getRotation());
        this.sprite.setScale(this.owner.getTransform().getScale());

        if(alignment == 0)
            this.sprite.setPosition(pos.x - (sprite.getWidth()/2), pos.y - (sprite.getHeight()/2));
        if(alignment == 1)
            this.sprite.setPosition(pos.x - (sprite.getWidth() / 2), pos.y);

        this.sprite.draw(batch);
    }

    public void setSprite(Sprite sprite){
        if(this.sprite == null) this.sprite = new Sprite(sprite);
        else this.sprite.set(sprite);
    }

    public void setTexture(Texture texture){
        this.sprite.setTexture(texture);
    }

    private void changeVisibility(int visibility){
        if(this.currVisibility == visibility)
            return;

        this.currVisibility = visibility;
        if(this.currVisibility == Constants.VISIBILITY_EXPLORED)
            this.sprite.setColor(Constants.COLOR_EXPLORED);
        else if(this.currVisibility == Constants.VISIBILITY_VISIBLE)
            this.sprite.setColor(Constants.COLOR_VISIBILE);
    }
}
