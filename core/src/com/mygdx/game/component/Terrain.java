package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.entity.Entity;

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

        this.batch = ColonyGame.batch;
        sprite = new Sprite(image);
        sprite.setPosition(this.ownerID.transform.getPosition().x, this.ownerID.transform.getPosition().y);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        sprite.draw(batch);
    }

    @Override
    public void destroy(Entity destroyer) {
        super.destroy(destroyer);
    }
}
