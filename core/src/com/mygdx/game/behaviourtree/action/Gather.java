package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Colonist;
import com.mygdx.game.component.Colony;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.component.Resource;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.FloatingText;
import com.mygdx.game.util.GH;
import com.mygdx.game.util.managers.DataManager;
import com.mygdx.game.util.managers.SoundManager;
import com.mygdx.game.util.timer.RepeatingTimer;
import com.mygdx.game.util.timer.Timer;

/**
 * <p>A task that gathers a resource when the right criteria is met.</p>
 *
 * <p>If the 'blackBoard.myInventory' field is null, the inventory from the Entity owner of the blackboard will be used. The 'blackBoard.targetResource' field will
 * be used to gather from. If this field is null, the task will end with failure.</p>
 */
public class Gather extends LeafTask{
    private Resource resource;
    private Timer gatherTimer;
    private Timer soundTimer;
    private DataBuilder.JsonSoundGroup sounds;

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
            this.blackBoard.myInventory = this.blackBoard.myManager.getEntityOwner().getComponent(Inventory.class);

        //If we can't get a resource component from the target, end this with failure.
        this.resource = this.blackBoard.targetResource;
        if(this.resource == null || !this.resource.isTakenBy(this.blackBoard.myManager.getEntityOwner())){
            this.control.finishWithFailure();
            return;
        }

        this.sounds = DataManager.getData(this.resource.getResRef().soundGroup, DataBuilder.JsonSoundGroup.class);

        //If the resource has gather sounds, make the timer!
        if(this.sounds != null) {
            this.soundTimer = new RepeatingTimer(1f, () -> {
                SoundManager.play(this.sounds.getRandomSound(), this.blackBoard.myManager.getEntityOwner().getTransform().getPosition(),
                        new Vector2(ColonyGame.camera.position.x, ColonyGame.camera.position.y), 200, 1000);
            });
        }

        //Gather after the amount of time needed.
        this.gatherTimer = new RepeatingTimer(resource.getGatherTick(), ()->{
            if(!this.resource.isValid()){
                this.control.finishWithFailure();
                return;
            }

            //get the item names to gather. If the size is 0, we are done gathering this resource.
            if(!this.resource.gatherFrom(this.blackBoard.myInventory))
                endWithSuccess();

            //Create the gather message
            createGatherMessage(this.blackBoard.myInventory.lasAddedItem, 1);
        });
    }

    private void endWithSuccess(){
        //This sets up the information for moving and transferring to the colony.
        Colony targetColony = this.blackBoard.myManager.getEntityOwner().getComponent(Colonist.class).getColony(); //Get the colony.
        this.blackBoard.targetNode = null; //Set the target node to null to make sure we use the target (not the node)
        this.blackBoard.target = targetColony.getEntityOwner(); //Set the target to the entity owner of the colony.
        this.blackBoard.itemTransfer.toInventory = targetColony.getInventory(); //Set the inventory to the colony's inventory.

        this.control.finishWithSuccess();
    }

    private void createGatherMessage(String[] itemNames, int[] amounts){
        StringBuilder text = new StringBuilder("+");
        for(int i=0;i<itemNames.length;i++){
            DataBuilder.JsonItem ref = DataManager.getData(itemNames[i], DataBuilder.JsonItem.class);
            text.append(amounts[i]).append(" ").append(ref.getDisplayName());
            if(i != itemNames.length-1) text.append(", ");
        }

        if(itemNames.length == 0)
            text.append("nothing...");

        Vector2 start = new Vector2(this.blackBoard.myManager.getEntityOwner().getTransform().getPosition());
        start.y += GH.toMeters(40);

        Vector2 end = new Vector2(this.blackBoard.myManager.getEntityOwner().getTransform().getPosition());
        end.y += GH.toMeters(90);

        new FloatingText(text.toString(), start, end, 2f, 0.8f);
    }

    private void createGatherMessage(String itemName, int amount){
        //Get the message we want to display.
        if(itemName == null || itemName.isEmpty()) itemName = "nothing...";
        else itemName = "+"+amount+" "+itemName;

        //Starting vector
        Vector2 start = new Vector2(this.blackBoard.myManager.getEntityOwner().getTransform().getPosition());
        start.y += GH.toMeters(40);

        //Ending vector.
        Vector2 end = new Vector2(this.blackBoard.myManager.getEntityOwner().getTransform().getPosition());
        end.y += GH.toMeters(90);

        //Make the floating text!
        new FloatingText(itemName, start, end, 2f, 0.8f);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        //this.soundTimer.update(delta);
        this.gatherTimer.update(delta);
        if(this.soundTimer != null) this.soundTimer.update(delta);
    }

    @Override
    public void end() {
        super.end();
        if(this.blackBoard.targetResource != null && this.blackBoard.targetResource.isTakenBy(this.blackBoard.myManager.getEntityOwner()))
            this.blackBoard.targetResource.setTaken(null);
    }
}
