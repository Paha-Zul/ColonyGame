package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.component.Projectile;
import com.mygdx.game.component.Transform;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.entity.ProjectileEnt;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.EventSystem;
import com.mygdx.game.helpers.GH;
import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.helpers.timer.Timer;

/**
 * Created by Paha on 3/27/2015.
 */
public class Attack extends LeafTask{
    private Timer fireTimer;

    public Attack(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {
        super.start();

        //Spawn a projectile
        fireTimer = new RepeatingTimer(1f, () -> {
            float dis =  this.blackBoard.target.transform.getPosition().dst(this.blackBoard.getEntityOwner().transform.getPosition());
            if(dis >= GH.toMeters(20)) {
                TextureRegion tex = new TextureRegion(ColonyGame.assetManager.get("ball", Texture.class)); //Get a texture for the bullet.
                Transform trans = this.blackBoard.getEntityOwner().transform;               //Cache the transform.
                Entity target = this.blackBoard.target;                                      //Cache the target
                float rot = (float) Math.atan2(target.transform.getPosition().y - trans.getPosition().y, target.transform.getPosition().x - trans.getPosition().x) * MathUtils.radDeg; //Get the rotation to the target
                rot += MathUtils.random(20) - 10;
                Entity ent = new ProjectileEnt(trans.getPosition(), rot, tex, 11);           //Spawn the projectile.
                Projectile projectile = ent.getComponent(Projectile.class);
                if(projectile == null) GH.writeErrorMessage("Somehow making a projectile didn't make a projectile...");
                else {
                    projectile.projOwner = this.blackBoard.getEntityOwner();
                    projectile.lifetime = dis / GH.toMeters(projectile.speed);
                    projectile.lifetime += projectile.lifetime*0.5f;
                    projectile.lifetime += MathUtils.random(projectile.lifetime*0.2f) - projectile.lifetime*0.1f;
                    System.out.println("projectile lifetime: "+projectile.lifetime);
                }
            }else
                EventSystem.notifyEntityEvent(this.blackBoard.target, "damage", this.blackBoard.getEntityOwner(), -this.blackBoard.attackDamage);
        });

        this.blackBoard.path = null;
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        fireTimer.update(delta);

        //If the target is null or dead, return with success (it died... I or someone else killed it!)
        if(this.blackBoard.target == null || this.blackBoard.target.isDestroyed() || this.blackBoard.target.isSetToBeDestroyed() || !this.blackBoard.target.hasTag(Constants.ENTITY_ALIVE)) {
            this.control.finishWithSuccess();
            this.blackBoard.target = null;
            return;
        }

        //If we are out of range, finish with failure.
        if(GH.toMeters(this.blackBoard.attackRange + this.blackBoard.attackRange*0.2f) < this.blackBoard.getEntityOwner().transform.getPosition().dst(this.blackBoard.target.transform.getPosition())) {
            this.control.finishWithFailure();
        }
    }

    @Override
    public void end() {
        super.end();
    }
}
