package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.ExploreGame;

/**
 * Created by Bbent_000 on 12/18/2014.
 */
public class Terrain extends Component{
    Texture image;
    Sprite sprite;
    SpriteBatch batch;

    public Terrain(Texture texture) {
        this.image = texture;
    }

    @Override
    public void start() {
        super.start();

        this.batch = ExploreGame.batch;
        sprite = new Sprite(image);
        sprite.setPosition(this.owner.transform.getPosition().x, this.owner.transform.getPosition().y);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        sprite.draw(batch);
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
