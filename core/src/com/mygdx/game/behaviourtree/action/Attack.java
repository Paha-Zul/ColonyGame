package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Projectile;
import com.mygdx.game.component.Transform;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.entity.ProjectileEnt;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.managers.EventSystem;
import com.mygdx.game.util.managers.SoundManager;
import com.mygdx.game.util.timer.RepeatingTimer;

/**
 * Created by Paha on 3/27/2015.
 */
public class Attack extends LeafTask{
    static Sound gunSound = ColonyGame.instance.assetManager.get("Walther-P22", Sound.class);
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

        if(blackBoard.attackTimer == null) {
            //Attack!
            blackBoard.attackTimer = new RepeatingTimer(1f, true, () -> {
                float dis = this.blackBoard.target.getTransform().getPosition().dst(this.blackBoard.myManager.getEntityOwner().getTransform().getPosition());
                if (dis >= GH.toMeters(20)) {
                    Transform trans = this.blackBoard.myManager.getEntityOwner().getTransform();               //Cache the transform.
                    Entity target = this.blackBoard.target;                                     //Cache the target
                    float rot = (float) Math.atan2(target.getTransform().getPosition().y - trans.getPosition().y, target.getTransform().getPosition().x - trans.getPosition().x) * MathUtils.radDeg; //Get the rotation to the target
                    rot += MathUtils.random(20) - 10;                                           //Randomize rotation a little bit
                    Entity bullet = new ProjectileEnt(trans.getPosition(), rot, new String[]{"ball", ""}, 11);          //Spawn the projectile.
                    ColonyGame.instance.listHolder.addEntity(bullet);
                    Projectile projectile = bullet.getComponent(Projectile.class);                 //Get the projectile component.
                    SoundManager.play(gunSound, bullet.getTransform().getPosition(), new Vector2(ColonyGame.instance.camera.position.x, ColonyGame.instance.camera.position.y), 200, 2000);

                    if (projectile == null)
                        GH.writeErrorMessage("Somehow making a projectile didn't make a projectile...");
                    else {
                        projectile.projOwner = this.blackBoard.myManager.getEntityOwner();
//                    projectile.projOwner = this.blackBoard.getEntityOwner();
//                    projectile.lifetime = dis / GH.toMeters(projectile.speed);
//                    projectile.lifetime += projectile.lifetime*0.5f;
//                    projectile.lifetime += MathUtils.random(projectile.lifetime*0.2f) - projectile.lifetime*0.1f;
                    }
                } else
                    EventSystem.notifyEntityEvent(this.blackBoard.target, "damage", this.blackBoard.myManager.getEntityOwner(), -this.blackBoard.attackDamage);
            });
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
        //If the target is null or dead, return with success (it died... I or someone else killed it!)
        if(this.blackBoard.target == null || this.blackBoard.target.isDestroyed() || this.blackBoard.target.isSetToBeDestroyed() || !this.blackBoard.target.getTags().hasTag("alive")) {
            this.control.finishWithSuccess();
            this.blackBoard.target = null;
            return;
        }

        //If we are out of range, finish with failure.
        if(GH.toMeters(this.blackBoard.attackRange + this.blackBoard.attackRange*0.2f) < this.blackBoard.myManager.getEntityOwner().getTransform().getPosition().dst(this.blackBoard.target.getTransform().getPosition())) {
            this.control.finishWithFailure();
            return;
        }


        blackBoard.attackTimer.update(delta);
    }

    @Override
    public void end() {
        super.end();
    }
}
