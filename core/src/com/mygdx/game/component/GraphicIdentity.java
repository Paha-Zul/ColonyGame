package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.GH;
import com.mygdx.game.helpers.Grid;

public class GraphicIdentity extends Component{
	public Sprite sprite;
    public int alignment = 0; //center

    private int currVisibility=0;

	public GraphicIdentity(TextureRegion image){
		super();

		this.sprite = new Sprite(image);
	}

    public GraphicIdentity(Sprite sprite){
        super();

        this.sprite = new Sprite(sprite);
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
    public void render(float delta, SpriteBatch batch) {
        super.render(delta, batch);
        Grid.GridInstance grid = ColonyGame.worldGrid;

        Vector2 pos = this.ownerID.transform.getPosition(); //Cache the ownerID's position.

        if(!ColonyGame.camera.frustum.boundsInFrustum(pos.x, pos.y, 0, sprite.getWidth(), sprite.getHeight(), 0))
            return;

        Grid.Node node = grid.getNode(this.ownerID);
        int visibility = grid.getVisibilityMap()[node.getX()][node.getY()].getVisibility();
        if(visibility == Constants.VISIBILITY_UNEXPLORED)
            return;

        this.changeVisibility(visibility);

        this.sprite.setRotation(this.ownerID.transform.getRotation());
        this.sprite.setScale(this.ownerID.transform.getScale());

        if(alignment == 0)
            this.sprite.setPosition(pos.x - (sprite.getWidth()/2), pos.y - (sprite.getHeight()/2));
        if(alignment == 1)
            this.sprite.setPosition(pos.x - (sprite.getWidth() / 2), pos.y);

        this.sprite.draw(batch);
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
