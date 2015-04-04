package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.*;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.helpers.FloatingText;
import com.mygdx.game.helpers.GH;
import com.mygdx.game.helpers.managers.DataManager;
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

        //Gather after the amount of time needed.
        this.gatherTimer = new OneShotTimer(this.resource.getGatherTime(), ()->{
            if(this.resource.isDestroyed()){
                this.control.finishWithFailure();
                return;
            }

            String[] itemNames = new String[this.resource.getItemNames().length];
            int[] amounts = new int[this.resource.getItemNames().length];
            //For each resource, random an amount to add to my (the colonists') inventory.
            for(int i=0;i<this.resource.getItemNames().length;i++){
                DataBuilder.JsonItem item = DataManager.getData(this.resource.getItemNames()[i], DataBuilder.JsonItem.class); //Get the reference.
                this.blackBoard.myInventory.addItem(item.getItemName(), this.resource.getItemAmount(i)); //Random a number!
                itemNames[i] = item.getDisplayName();
                amounts[i] = this.resource.getItemAmount(i);
            }

            createGatherMessage(itemNames, amounts);

            //This sets up the information for moving and transfering to the colony.
            Colony targetColony = this.blackBoard.getEntityOwner().getComponent(Colonist.class).getColony(); //Get the colony.
            this.blackBoard.targetNode = null; //Set the target node to null to make sure we use the target (not the node)
            this.blackBoard.target = targetColony.getEntityOwner(); //Set the target to the entity owner of the colony.
            this.blackBoard.toInventory = targetColony.getInventory(); //Set the inventory to the colony's inventory.

            //Destroy the resource and finish with success..
            this.resource.getEntityOwner().setToDestroy();
            this.control.finishWithSuccess();
        });
    }

    private void createGatherMessage(String[] itemNames, int[] amounts){
        StringBuilder text = new StringBuilder("Gathered ");
        for(int i=0;i<itemNames.length;i++){
            text.append(amounts[i]).append(" ").append(itemNames[i]);
            if(i != itemNames.length-1) text.append(", ");
        }

        if(itemNames.length == 0)
            text.append("nothing...");

        Vector2 start = new Vector2(this.blackBoard.getEntityOwner().transform.getPosition());
        start.y += GH.toMeters(40);

        Vector2 end = new Vector2(this.blackBoard.getEntityOwner().transform.getPosition());
        end.y += GH.toMeters(90);

        new FloatingText(text.toString(), start, end, 2f, 0.8f);
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
