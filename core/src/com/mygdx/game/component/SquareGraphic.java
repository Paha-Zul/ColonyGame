package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public class SquareGraphic extends Component {
	public float size = 30;
	public Color color = Color.BLACK;

	private ShapeRenderer renderer;

	public SquareGraphic() {
		super();

		this.renderer = new ShapeRenderer();
	}

	@Override
	public void start() {
		super.start();

	}

	@Override
	public void update(float delta) {
		super.update(delta);

		this.renderer.begin(ShapeRenderer.ShapeType.Filled);

		Vector2 pos = this.owner.getTransform().getPosition();

		this.renderer.setColor(this.color);
		this.renderer.rect(pos.x - size*0.5f, pos.y - size*0.5f, size, size);

		this.renderer.end();
	}
}
