package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.helpers.Profiler;
import com.mygdx.game.helpers.worldgeneration.WorldGen;

public class GraphicIdentity extends Component{
	public Sprite sprite;
    public int alignment = 0; //center

	private SpriteBatch batch;
    private int currVisibility=0;

	public GraphicIdentity(Texture image, SpriteBatch batch){
		super();

		this.sprite = new Sprite(image);
		this.batch = batch;
	}

	@Override
	public void start() {
		Vector2 pos = this.owner.transform.getPosition(); //Cache the owner's position.
		this.sprite.setPosition(pos.x - this.sprite.getWidth()/2, pos.y - this.sprite.getHeight()/2);
	}

	@Override
	public void update(float delta) {
		Vector2 pos = this.owner.transform.getPosition(); //Cache the owner's position.

		if(!ColonyGame.camera.frustum.boundsInFrustum(pos.x, pos.y, 0, sprite.getWidth()*0.5f, sprite.getHeight()*0.5f, 0)) {
			return;
		}

        Grid.Node node = ColonyGame.worldGrid.getNode(this.owner);
        int visibility = WorldGen.getInstance().getVisibilityMap()[node.getCol()][node.getRow()].getVisibility();
        if(visibility == Constants.VISIBILITY_UNEXPLORED) {
            return;
        }

        this.changeVisibility(visibility);

		this.sprite.setScale(this.owner.transform.getScale());
		this.sprite.setRotation(this.owner.transform.getRotation());

        if(alignment == 0)
		    this.sprite.setPosition(pos.x - (sprite.getWidth())/2, pos.y - (sprite.getHeight()/2));
        if(alignment == 1)
            this.sprite.setPosition(pos.x - (sprite.getWidth())/2, pos.y);

        this.sprite.draw(this.batch);
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
