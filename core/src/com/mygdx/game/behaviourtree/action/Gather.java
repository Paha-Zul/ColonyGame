package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.*;
import com.mygdx.game.helpers.managers.ItemManager;
import com.mygdx.game.helpers.managers.SoundManager;
import com.mygdx.game.helpers.timer.OneShotTimer;
import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.helpers.timer.Timer;

/**
 * Created by Paha on 1/29/2015.
 */
public class Gather extends LeafTask{
    private Resource resource;
    private Timer gatherTimer;
    private Timer soundTimer;

    private static Sound[] chopTreeSounds = new Sound[]{ColonyGame.assetManager.get("axechop1", Sound.class), ColonyGame.assetManager.get("axechop2", Sound.class), ColonyGame.assetManager.get("axechop3", Sound.class),
            ColonyGame.assetManager.get("axechop4", Sound.class)  , ColonyGame.assetManager.get("axechop5", Sound.class), ColonyGame.assetManager.get("axechop6", Sound.class)};

    public Gather(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return this.blackBoard.target != null;
    }

    @Override
    public void start() {
        super.start();

        if(this.blackBoard.myInventory == null)
            this.blackBoard.myInventory = this.blackBoard.getEntityOwner().getComponent(Inventory.class);

        this.resource = this.blackBoard.target.getComponent(Resource.class);
        if(this.resource == null){
            this.control.finishWithFailure();
            return;
        }

        this.soundTimer = new RepeatingTimer(0.5f, ()->{
            SoundManager.play(chopTreeSounds[MathUtils.random(chopTreeSounds.length - 1)], this.blackBoard.getEntityOwner().transform.getPosition(),
                    new Vector2(ColonyGame.camera.position.x, ColonyGame.camera.position.y), 200, 1000);
        });

        this.gatherTimer = new OneShotTimer(5f, ()->{
            if(this.resource.isDestroyed()){
                this.control.finishWithFailure();
                return;
            }

            Colony targetColony = this.blackBoard.getEntityOwner().getComponent(Colonist.class).getColony();
            this.blackBoard.targetNode = null;
            this.blackBoard.target = targetColony.getEntityOwner();
            this.blackBoard.transferToInventory = targetColony.getInventory();
            for(int i=0;i<this.resource.getItemNames().length;i++){
                Item item = ItemManager.getItemByName(this.resource.getItemNames()[i]);
                int diff = this.resource.getItemAmounts()[i][1] - this.resource.getItemAmounts()[i][0];
                int base = this.resource.getItemAmounts()[i][0];
                item.setCurrStack(MathUtils.random(diff) + base);
                this.blackBoard.myInventory.addItem(item);
            }

            this.resource.getEntityOwner().destroy();
            this.control.finishWithSuccess();
        });
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        //this.soundTimer.update(delta);
        this.gatherTimer.update(delta);
    }

    @Override
    public void end() {

        super.end();
    }
}
