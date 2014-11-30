package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class GraphicIdentity extends Component{
	public Sprite sprite;

	SpriteBatch batch;

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

		this.sprite.setScale(this.owner.transform.getScale());
		this.sprite.setRotation(this.owner.transform.getRotation());

		this.sprite.setPosition(pos.x - (sprite.getWidth())/2, pos.y - (sprite.getHeight())/2);

		this.sprite.draw(this.batch);
	}
}
