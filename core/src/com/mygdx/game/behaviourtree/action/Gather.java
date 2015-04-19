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
import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.helpers.timer.Timer;

import java.util.Arrays;

/**
 * A task that gathers a resource when the right criteria is met.
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

        //If the 'myInventory' field is null of the blackboard, get it from the blackboard's owner.
        if(this.blackBoard.myInventory == null)
            this.blackBoard.myInventory = this.blackBoard.getEntityOwner().getComponent(Inventory.class);

        //If we can't get a resource component from the target, end this with failure.
        this.resource = this.blackBoard.target.getComponent(Resource.class);
        if(this.resource == null){
            this.control.finishWithFailure();
            return;
        }

        //Not used currently...
        this.soundTimer = new RepeatingTimer(0.5f, ()->{
            SoundManager.play(chopTreeSounds[MathUtils.random(chopTreeSounds.length - 1)], this.blackBoard.getEntityOwner().transform.getPosition(),
                    new Vector2(ColonyGame.camera.position.x, ColonyGame.camera.position.y), 200, 1000);
        });

        //Gather after the amount of time needed.
        this.gatherTimer = new RepeatingTimer(this.resource.getGatherTick(), ()->{
            if(this.resource.isDestroyed()){
                this.control.finishWithFailure();
                return;
            }

            //get the item names to gather. If the size is 0, we are done gathering this resource.
            String[] itemNames = this.resource.gatherFrom();
            if(itemNames.length == 0){
                endWithSuccess();
                return;
            }

            //Fill an array with 1s
            int[] amounts = new int[itemNames.length];
            Arrays.fill(amounts, 1);

            //For each item name, add it to the inventory.
            for (String itemName : itemNames)
                this.blackBoard.myInventory.addItem(itemName);

            //Create the gather message
            createGatherMessage(itemNames, amounts);

            //Peek into the resource. If the next tick is empty, end this job with success.
            if(!resource.peek())
                endWithSuccess();
        });
    }

    private void endWithSuccess(){
        //This sets up the information for moving and transfering to the colony.
        Colony targetColony = this.blackBoard.getEntityOwner().getComponent(Colonist.class).getColony(); //Get the colony.
        this.blackBoard.targetNode = null; //Set the target node to null to make sure we use the target (not the node)
        this.blackBoard.target = targetColony.getEntityOwner(); //Set the target to the entity owner of the colony.
        this.blackBoard.toInventory = targetColony.getInventory(); //Set the inventory to the colony's inventory.

        this.control.finishWithSuccess();
    }

    private void createGatherMessage(String[] itemNames, int[] amounts){
        StringBuilder text = new StringBuilder("Gathered ");
        for(int i=0;i<itemNames.length;i++){
            DataBuilder.JsonItem ref = DataManager.getData(itemNames[i], DataBuilder.JsonItem.class);
            text.append(amounts[i]).append(" ").append(ref.getDisplayName());
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
