package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ExploreGame;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.entity.ProjectileEnt;
import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.interfaces.Functional;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 11/29/2014.
 */
public class Turret extends Component {
	public float turnSpeed = 10;
	public float range = 300;

	private static ArrayList<Entity> enemyList = new ArrayList<>();
	private Entity target = null;
	private RepeatingTimer getTargetTimer;
	private RepeatingTimer shootTimer;
	private Entity bulletSpawn;

	@Override
	public void start() {
		super.start();

		bulletSpawn = new Entity(new Vector2(100,50), 0, new Texture("img/bar.png"), ExploreGame.batch, 20);
		this.owner.transform.addChild(bulletSpawn, new Vector2(0,50));
		bulletSpawn.name = "spawn";

		this.owner.transform.setLocalScale(0.5f);

		Functional.Callback callback = () -> {
			if(this.target != null && !this.target.isDestroyed()) {
				this.getTargetTimer.cancel();
				return;
			}

			ArrayList<Entity> list = Turret.getEnemyList();
			if(list.size() > 0){
				this.target = list.get(0);
				this.getTargetTimer.cancel();
			}
		};

		this.getTargetTimer = new RepeatingTimer(0.3f, callback);

		this.shootTimer = new RepeatingTimer(0.3f, ()->{
			Vector2 spawn = bulletSpawn.transform.getPosition();
			Entity proj = new ProjectileEnt(new Vector2(spawn.x, spawn.y), this.owner.transform.getRotation(), new Texture("img/projectile.png"), ExploreGame.batch, 11);
			proj.transform.setScale(0.1f);
			proj.getComponent(Projectile.class).speed = 200;
		});

	}

	@Override
	public void update(float delta) {
		super.update(delta);

		this.getTargetTimer.update(delta);

		//If the target is not null and not destroyed...
		if(this.target != null && !this.target.isDestroyed()){
			this.shootTimer.update(delta);

			Vector2 myPos = this.owner.transform.getPosition();
			Vector2 otherPos = this.target.transform.getPosition();

			double dir = Math.atan2(otherPos.y - myPos.y, otherPos.x - myPos.x)* MathUtils.radiansToDegrees;
			this.owner.transform.setRotation((float)dir);

		//If it is null or destroyed, find a new one!
		}else{
			if(this.getTargetTimer.isCanceled())
				this.getTargetTimer.restart();
		}
	}

	@Override
	public void lateUpdate(float delta) {
		super.lateUpdate(delta);
	}


	public static void addEnemy(Entity enemy){
		enemyList.add(enemy);
	}

	public static void removeEnemy(Entity enemy){
		enemyList.remove(enemy);
	}

	public static ArrayList<Entity> getEnemyList(){
		return enemyList;
	}
}
