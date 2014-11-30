package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.entity.Entity;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public class SquareGraphic extends Component {
	public float size = 30;
	public Color color = Color.BLACK;

	private ShapeRenderer renderer;

	public SquareGraphic(ShapeRenderer renderer) {
		super();

		this.renderer = renderer;
	}

	@Override
	public void start() {
		super.start();

	}

	@Override
	public void update(float delta) {
		super.update(delta);

		Vector2 pos = this.owner.transform.getPosition();

		this.renderer.setColor(this.color);
		this.renderer.rect(pos.x - size*0.5f, pos.y - size*0.5f, size, size);
	}
}
