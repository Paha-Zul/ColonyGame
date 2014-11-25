package com.mygdx.game.component;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.entity.Entity;

/**
 * Created by Bbent_000 on 11/24/2014.
 */
public class SquareGraphic extends Component {
	private ShapeRenderer renderer;

	public SquareGraphic(String name, int type, boolean active, ShapeRenderer renderer) {
		super(name, type, active);

		this.renderer = renderer;
	}

	@Override
	public void start(Entity owner) {
		super.start(owner);
	}

	@Override
	public void update(float delta) {
		super.update(delta);

		Vector2 pos = this.owner.transform.getWorldPosition();

		this.renderer.rect(pos.x - 5, pos.y - 5, 10, 10);
	}
}
