package com.mygdx.game.behaviourtree;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.action.*;
import com.mygdx.game.behaviourtree.composite.Parallel;
import com.mygdx.game.behaviourtree.composite.Sequence;
import com.mygdx.game.behaviourtree.control.ParentTaskController;
import com.mygdx.game.behaviourtree.decorator.AlwaysTrue;
import com.mygdx.game.behaviourtree.decorator.RepeatUntilCondition;
import com.mygdx.game.component.*;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.*;

import java.util.function.Consumer;

/**
 * Created by Paha on 4/11/2015.
 */
public class PrebuiltTasks {
    public static Task moveTo(BlackBoard blackBoard, BehaviourManagerComp behComp){
        //Get the target node/Entity.
        //Find the path.
        //Move to the target.

        Sequence sequence = new Sequence("MoveTo", blackBoard);
        FindPath findPath = new FindPath("FindPath",  blackBoard);
        MoveTo followPath = new MoveTo("FollowPath",  blackBoard);

        ((ParentTaskController)(sequence.getControl())).addTask(findPath);
        ((ParentTaskController)(sequence.getControl())).addTask(followPath);

        return sequence;
    }

    public static Task gatherResource(BlackBoard blackBoard, BehaviourManagerComp behComp){
        /*
         * Sequence:
         *      Sequence
         *          figure out tools for gathering
         *          find the tool shed
         *          check the shed and reserve
         *          find path to shed
         *          move to shed
         *          transfer tools
         *
         *      Repeat (until we are full on the items toggled):
         *          find resource
         *          find path to resource
         *          move to resource
         *          gather resource
         *
         *      find storage
         *      find path to storage
         *      move to storage
         *      transfer items.
         */

        //If we fail to find a resource, we need to explore until we find one...
        Consumer<Task> fail = tsk -> {
            Vector2 pos = tsk.blackBoard.myManager.getEntityOwner().getTransform().getPosition();
            new FloatingText("Couldn't find a nearby resource!", new Vector2(pos.x, pos.y + 1), new Vector2(pos.x, pos.y + 10), 1.5f, 0.8f);

            //When we finish moving to the newly explored area, try to gather a resource again.
            Task task = exploreUnexplored(blackBoard, behComp);
            task.getControl().getCallbacks().successCallback = tsk2 -> behComp.changeTaskImmediate("gather"); //When we finish, try to gather.
            behComp.changeTaskImmediate(task);
        };

        Sequence sequence = new Sequence("Gathering Resource", blackBoard);

        //All this should be under a repeat.
        Sequence innerGatherSeq = new Sequence("Gathering", blackBoard);
        RepeatUntilCondition repeatGather = new RepeatUntilCondition("Repeat", blackBoard, innerGatherSeq);
        FindClosestEntity fr = new FindClosestEntity("Finding Closest Resource", blackBoard);
        FindPath fpResource = new FindPath("Finding Path to Resource", blackBoard);
        MoveTo mtResource = new MoveTo("Moving to Resource", blackBoard);
        Gather gather = new Gather("Gathering Resource", blackBoard);

        FindClosestEntity findStorage = new FindClosestEntity("Finding storage.", blackBoard);
        FindPath findPathToStorage = new FindPath("Finding Path to Storage", blackBoard);
        MoveTo moveToStorage = new MoveTo("Moving to Storage", blackBoard);
        TransferItem transferItems = new TransferItem("Transferring Resources", blackBoard);

        sequence.control.addTask(getTools(blackBoard, behComp));

        //Add the repeat gather task to the main sequence, and then the rest to the inner sequence under repeat.
        ((ParentTaskController)sequence.getControl()).addTask(repeatGather);
        ((ParentTaskController)innerGatherSeq.getControl()).addTask(fr);
        ((ParentTaskController)innerGatherSeq.getControl()).addTask(fpResource);
        ((ParentTaskController)innerGatherSeq.getControl()).addTask(mtResource);
        ((ParentTaskController)innerGatherSeq.getControl()).addTask(gather);

        //Add these to the main sequence.
        ((ParentTaskController)sequence.getControl()).addTask(findStorage);
        ((ParentTaskController)sequence.getControl()).addTask(findPathToStorage);
        ((ParentTaskController)sequence.getControl()).addTask(moveToStorage);
        ((ParentTaskController)sequence.getControl()).addTask(transferItems);

        //Reset some values.
        sequence.control.callbacks.startCallback = task -> {
            //Reset blackboard values...
            task.blackBoard.itemTransfer.fromInventory = task.blackBoard.myManager.getEntityOwner().getComponent(Colonist.class).getInventory();
            task.blackBoard.itemTransfer.transferAmount = false;
            task.blackBoard.itemTransfer.transferMany = false;
            task.blackBoard.itemTransfer.transferAll = true;
            task.blackBoard.itemTransfer.itemAmountToTransfer = 0;
            task.blackBoard.itemTransfer.itemNameToTransfer = null;

            task.blackBoard.targetResource = null;
            task.blackBoard.target = null;
            task.blackBoard.targetNode = null;
        };

        //When we finish, set the target back to not taken IF it is still a valid target (if we ended early).
        sequence.getControl().callbacks.finishCallback = task -> {
            if (blackBoard.targetResource != null && blackBoard.targetResource.isValid() && sequence.getBlackboard().targetResource.getTaken() == sequence.getBlackboard().myManager.getEntityOwner()) {
                sequence.getBlackboard().targetResource.setTaken(null);
            }
        };

        //Check if we can still add more items that we are looking for. Return true if we can't (to end the repeat)
        //and false to keep repeating.
        repeatGather.getControl().callbacks.successCriteria = tsk -> {
            Task task = (Task)tsk;
            Inventory inv = task.blackBoard.myInventory;
            //Get a list of the items we are searching for. If we can hold more, keep searching for what we need.
            String[] itemNames = task.blackBoard.resourceTypeTags.getTagsAsString();
            for(String itemName : itemNames) if(inv.canAddItem(itemName)) return false;
            return true;
        };

        //If we fail, call the fail callback.
        fr.getControl().callbacks.failureCallback = fail;

        //Check if our resourceTypeTags are empty. If so, no use trying to find an entity.
        fr.control.callbacks.checkCriteria = task -> !task.blackBoard.resourceTypeTags.isEmpty();

        //Check to make sure the resource isn't taken. Also make sure the resource has an item we want.
        fr.getControl().callbacks.successCriteria = (e) -> {
            Entity ent = (Entity)e;
            Resource resource = ent.getComponent(Resource.class);
            if(resource == null || blackBoard.resourceTypeTags.isEmpty()) return false;

            String[] blackTags = blackBoard.resourceTypeTags.getTagsAsString();
            boolean notTaken = !resource.isTaken();
            boolean hasTag = resource.resourceTypeTags.hasAnyTag(blackTags);
            boolean hasAvailableItem = resource.peekAvailableOnlyWanted(blackBoard.myInventory, blackTags);

            return notTaken && hasTag && hasAvailableItem;
        };

        //On success, set the resource as taken if not already taken.
        fr.getControl().callbacks.successCallback = task ->  {
            task.blackBoard.targetResource = task.blackBoard.target.getComponent(Resource.class);
            if(!task.blackBoard.targetResource.isTaken() || task.blackBoard.targetResource.getTaken() == task.blackBoard.myManager.getEntityOwner()) {
                task.blackBoard.targetResource.setTaken(task.blackBoard.myManager.getEntityOwner());
            }
        };

        //If we don't have a target resource, try to get one from our target. Then check if it's valid and owned by us.
        //If not, cancel the job. This secondary check is done because the job can be threaded
        fpResource.control.callbacks.checkCriteria = task -> {
            if(task.blackBoard.targetResource == null) task.blackBoard.targetResource = task.blackBoard.target.getComponent(Resource.class);
            return task.blackBoard.targetResource != null && (task.blackBoard.targetResource.getTaken() == null || task.blackBoard.targetResource.getTaken() == task.blackBoard.myManager.getEntityOwner());
        };

        //Make sure we are getting a building...
        findStorage.control.callbacks.successCriteria = ent -> ((Entity)ent).getTags().hasTag("building");

        //If we find a valid building, get the inventory from it and assign it to the 'toInventory' field.
        findStorage.control.callbacks.successCallback = task -> task.blackBoard.itemTransfer.toInventory = task.blackBoard.target.getComponent(Inventory.class);

        //TODO Not sure what this does exactly...
        //When finding a path to the resource, make sure it's actually a resource and we are the ones that have claimed it!
        findPathToStorage.getControl().callbacks.checkCriteria = task -> {
            Resource res = task.blackBoard.targetResource; //Get the target resource from the blackboard.
            if(res == null) task.blackBoard.targetResource = res = task.blackBoard.target.getComponent(Resource.class); //If null, try to get it from the target.
            return res != null && task.getBlackboard().targetResource.getTaken() == task.getBlackboard().myManager.getEntityOwner(); //Return true if not null and we are the ones that took it. False otherwise.
        };

        //When we finish gathering from a source, try to untake it in case it's infinite (like a water source)
        gather.getControl().callbacks.finishCallback = task -> {
            if(blackBoard.targetResource != null && blackBoard.targetResource.isValid())
                if(blackBoard.targetResource.getTaken() == blackBoard.myManager.getEntityOwner())
                    blackBoard.targetResource.setTaken(null);
        };

        return sequence;
    }

    private static Task gatherTarget(BlackBoard blackBoard, BehaviourManagerComp behComo){
        /**
         * Sequence:
         *  find path to resource
         *  move to resource
         *  gather resource
         *  find path to storage
         *  move to storage
         *  transfer itemNames to storage
         */

        Sequence sequence = new Sequence("gatherTarget", blackBoard);

        //Create all the job objects we need...
        FindPath findPath = new FindPath("Finding Path to Resource", blackBoard);
        MoveTo move = new MoveTo("Moving to Resource", blackBoard);
        Gather gather = new Gather("Gathering Resource", blackBoard);
        FindClosestEntity findStorage = new FindClosestEntity("Finding storage.", blackBoard);
        FindPath findPathToStorage = new FindPath("Finding Path to Storage", blackBoard);
        MoveTo moveToStorage = new MoveTo("Moving to Storage", blackBoard);
        TransferItem transferItems = new TransferItem("Transferring Resources", blackBoard);

        ((ParentTaskController)sequence.getControl()).addTask(findPath);
        ((ParentTaskController)sequence.getControl()).addTask(move);
        ((ParentTaskController)sequence.getControl()).addTask(gather);
        ((ParentTaskController)sequence.getControl()).addTask(findStorage);
        ((ParentTaskController)sequence.getControl()).addTask(findPathToStorage);
        ((ParentTaskController)sequence.getControl()).addTask(moveToStorage);
        ((ParentTaskController)sequence.getControl()).addTask(transferItems);

        //When we finish, set the target back to not taken IF it is still a valid target (if we ended early).
        sequence.getControl().callbacks.finishCallback = task -> {
            if (blackBoard.targetResource != null && blackBoard.targetResource.isValid() && sequence.getBlackboard().targetResource.getTaken() == sequence.getBlackboard().myManager.getEntityOwner()) {
                sequence.getBlackboard().targetResource.setTaken(null);
            }
        };

        //Make sure we have a target resource and
        sequence.control.callbacks.checkCriteria = task -> {
            if(task.blackBoard.targetResource == null) task.blackBoard.targetResource = task.blackBoard.target.getComponent(Resource.class);
            return task.blackBoard.targetResource != null && (task.blackBoard.targetResource.getTaken() == null || task.blackBoard.targetResource.getTaken() == task.blackBoard.myManager.getEntityOwner());
        };

        //Set the 'fromInventory' field and set the resource as taken by us!
        sequence.getControl().callbacks.startCallback = task -> {
            task.blackBoard.itemTransfer.fromInventory = task.blackBoard.myManager.getEntityOwner().getComponent(Inventory.class);
            task.blackBoard.targetResource.setTaken(blackBoard.myManager.getEntityOwner());
        };

        //Make sure we are getting a building...
        findStorage.control.callbacks.successCriteria = ent -> ((Entity)ent).getTags().hasTag("building");

        //When finding a path to the resource, make sure it's actually a resource and we are the ones that have claimed it!
        findPath.getControl().callbacks.checkCriteria = task -> {
            Resource res = task.blackBoard.targetResource; //Get the target resource from the blackboard.
            if(res == null) task.blackBoard.targetResource = res = task.blackBoard.target.getComponent(Resource.class); //If null, try to get it from the target.
            return res != null && task.getBlackboard().targetResource.getTaken() == task.getBlackboard().myManager.getEntityOwner(); //Return true if not null and we are the ones that took it. False otherwise.
        };

        //When we finish gathering from a source, try to untake it in case it's infinite (like a water source)
        gather.getControl().callbacks.finishCallback = task -> {
            if(blackBoard.targetResource != null && blackBoard.targetResource.isValid())
                if(blackBoard.targetResource.getTaken() == blackBoard.myManager.getEntityOwner())
                    blackBoard.targetResource.setTaken(null);
        };

        return sequence;
    }

    private static Task getTools(BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         *  Sequence
         *      figure out tools
         *      find tool shed
         *      check and reserve tools
         *      get path to tool shed
         *      move to tool shed
         *      transfer tools
         */

        AlwaysTrue alwaysTrue = new AlwaysTrue("Always true", blackBoard);
        Sequence seq = new Sequence("Getting tools", blackBoard);
        GetToolsForGathering get = new GetToolsForGathering("Figuring tools", blackBoard);
        FindClosestEntity findShed = new FindClosestEntity("Finding tool shed", blackBoard);
        CheckAndReserve checkShed = new CheckAndReserve("CheckingReserving", blackBoard);
        FindPath fpToShed = new FindPath("PathToShed", blackBoard);
        MoveTo mtShed = new MoveTo("Moving", blackBoard);
        TransferItem transferTools = new TransferItem("Transferring", blackBoard);

        seq.control.callbacks.startCallback = task -> {
            task.blackBoard.itemTransfer.reset();

            System.out.println("tool seq is starting");
        };

        //When we finish, if we still have plans to take items (itemNamesToTransfer and itemAmountsToTransfer is not null), try to unreserve it.
        seq.control.callbacks.finishCallback = task -> {
            if(task.blackBoard.itemTransfer.itemNamesToTransfer != null && task.blackBoard.itemTransfer.itemAmountsToTransfer != null && task.blackBoard.itemTransfer.fromInventory != null){
                for(int i=0;i<task.blackBoard.itemTransfer.itemNamesToTransfer.size;i++){
                    String itemName = task.blackBoard.itemTransfer.itemNamesToTransfer.get(i);
                    task.blackBoard.itemTransfer.fromInventory.unReserveItem(itemName, task.blackBoard.itemTransfer.itemAmountsToTransfer.get(i));
                }
            }
        };

        //We need to find a building with that tag 'equipment'.
        findShed.control.callbacks.successCriteria = entity -> {
            Entity ent = (Entity)entity;
            if(ent.getTags().hasTag("building"))
                if(ent.getComponent(Building.class).buildingTags.hasTag("equipment"))
                    return true;
            return false;
        };

        //When we find the shed, assign the to and from inventory.
        findShed.control.callbacks.successCallback = task -> {
            task.blackBoard.itemTransfer.toInventory = task.blackBoard.myManager.getEntityOwner().getComponent(Inventory.class);
            task.blackBoard.itemTransfer.fromInventory = task.blackBoard.target.getComponent(Inventory.class);
        };

        seq.control.addTask(get);
        seq.control.addTask(findShed);
        seq.control.addTask(checkShed);
        seq.control.addTask(fpToShed);
        seq.control.addTask(mtShed);
        seq.control.addTask(transferTools);

        alwaysTrue.setTask(seq);

        return alwaysTrue;
    }

    public static Task returnTools(BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         *  Sequence:
         *      get list of tools that we have.
         *      *not implemented and optional* check if inventory has space
         *      find closest tool shed
         *      find path to tool shed
         *      move to tool shed
         *      transfer tools
         */

        Sequence seq = new Sequence("Returning tools", blackBoard);
        FindClosestEntity findShed = new FindClosestEntity("Finding tool shed", blackBoard);
        FindPath fpToShed = new FindPath("PathToShed", blackBoard);
        MoveTo mtShed = new MoveTo("Moving", blackBoard);
        TransferItem transferTools = new TransferItem("Transferring", blackBoard);

        seq.control.addTask(findShed);
        seq.control.addTask(fpToShed);
        seq.control.addTask(mtShed);
        seq.control.addTask(transferTools);

        //Set some flags and get a list of item names to be removed.
        seq.control.callbacks.startCallback = task -> {
            task.blackBoard.itemTransfer.reset();

            task.blackBoard.itemTransfer.transferMany = true;

            //TODO Something here... check!
            task.blackBoard.itemTransfer.itemNamesToTransfer = new Array<>(task.blackBoard.myManager.getComponent(Equipment.class).getToolNames());
            task.blackBoard.itemTransfer.itemAmountsToTransfer = new Array<>(task.blackBoard.itemTransfer.itemNamesToTransfer.size);
            for(int i=0;i<task.blackBoard.itemTransfer.itemNamesToTransfer.size;i++)
                task.blackBoard.itemTransfer.itemAmountsToTransfer.add(1);
        };

        //We need a building with the tag "equipment".
        findShed.control.callbacks.successCriteria = entity -> {
            Entity ent = (Entity)entity;
            if(ent.getTags().hasTag("building"))
                if(ent.getComponent(Building.class).buildingTags.hasTag("equipment"))
                    return true;

            return false;
        };

        //When we are successful in finding the shed, set the inventories.
        findShed.control.callbacks.successCallback = task -> {
            task.blackBoard.itemTransfer.toInventory = task.blackBoard.target.getComponent(Inventory.class);
            task.blackBoard.itemTransfer.fromInventory = task.blackBoard.myManager.getEntityOwner().getComponent(Inventory.class);
        };

        return seq;

    }

    public static Task exploreUnexplored(BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         * Sequence:
         *  find closest unexplored tile
         *  get path to tile
         *  move to tile
         */

        //Reset these. Left over assignments from other jobs will cause the explore behaviour to simply move to the wrong area.
        blackBoard.target = null;
        blackBoard.targetNode = null;

        Sequence sequence = new Sequence("exploreUnexplored", blackBoard);

        FindClosestUnexplored findClosestUnexplored = new FindClosestUnexplored("Finding Closest Unexplored Location", blackBoard);
        FindPath findPathToUnexplored = new FindPath("Finding Path to Unexplored", blackBoard);
        MoveTo moveToLocation = new MoveTo("Moving to Explore", blackBoard);

        findClosestUnexplored.control.callbacks.startCallback = task -> {
            Colonist col = task.blackBoard.myManager.getEntityOwner().getComponent(Colonist.class);
            task.blackBoard.target = col.getColony().getOwnedFromColony(Building.class, building -> building.buildingTags.hasTag("main")).getEntityOwner();
        };

        ((ParentTaskController) sequence.getControl()).addTask(findClosestUnexplored);
        ((ParentTaskController) sequence.getControl()).addTask(findPathToUnexplored);
        ((ParentTaskController) sequence.getControl()).addTask(moveToLocation);

        return sequence;
    }

    public static Task idleTask(BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         * Sequence:
         *  find random nearby location
         *  find path to location
         *  move to location
         *  idle for random amount of time
         */

        Sequence sequence = new Sequence("idle", blackBoard);

        FindRandomNearbyLocation findNearbyLocation = new FindRandomNearbyLocation("Finding Nearby Location", blackBoard, blackBoard.idleDistance);
        FindPath findPath = new FindPath("Finding Path to Nearby Location", blackBoard);
        MoveTo moveTo = new MoveTo("Moving to Nearby Location", blackBoard);
        Idle idle = new Idle("Standing Still", blackBoard, blackBoard.baseIdleTime, blackBoard.randomIdleTime);

        ((ParentTaskController) sequence.getControl()).addTask(findNearbyLocation);
        ((ParentTaskController) sequence.getControl()).addTask(findPath);
        ((ParentTaskController) sequence.getControl()).addTask(moveTo);
        ((ParentTaskController) sequence.getControl()).addTask(idle);

        return sequence;
    }

    public static Task consumeTask(BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         * Sequence:
         *  check that the inventory of the storage has the item effect we want/need
         *  find path to storage
         *  move to storage
         *  transfer needed item to me (colonist)
         *  consume item
         */

        Sequence sequence = new Sequence("consume", blackBoard);

        CheckInventoryHas check = new CheckInventoryHas("Checking Inventory", blackBoard);
        FindPath fp = new FindPath("Finding Path to consume item", blackBoard);
        MoveTo moveTo = new MoveTo("Moving to consume item", blackBoard);
        TransferItem tr = new TransferItem("Transferring Consumable", blackBoard);
        Consume consume = new Consume("Consuming Item", blackBoard);

        ((ParentTaskController) sequence.getControl()).addTask(check);
        ((ParentTaskController) sequence.getControl()).addTask(fp);
        ((ParentTaskController) sequence.getControl()).addTask(moveTo);
        ((ParentTaskController) sequence.getControl()).addTask(tr);
        ((ParentTaskController) sequence.getControl()).addTask(consume);

        sequence.getControl().callbacks.startCallback = task->{
            //Reset blackboard values.
            blackBoard.targetNode = null;
            blackBoard.itemTransfer.transferAll = false;
            blackBoard.itemTransfer.itemAmountToTransfer = 1;
            blackBoard.itemTransfer.itemNameToTransfer = null;

            blackBoard.target = blackBoard.myManager.getEntityOwner().getComponent(Colonist.class).getColony().getEntityOwner();
            blackBoard.itemTransfer.fromInventory = blackBoard.myManager.getEntityOwner().getComponent(Colonist.class).getColony().getOwnedFromColony(Building.class, building -> building.buildingTags.hasTag("main")).getComponent(Inventory.class);
            blackBoard.itemTransfer.toInventory = blackBoard.myManager.getEntityOwner().getComponent(Inventory.class);
        };

        return sequence;
    }

    /**
     * Searches for a target, hunts the target, and gathers the resources from it. This Task is composed of the attackTarget and gatherResource Tasks.
     * @param blackBoard The blackboard of this Task.
     * @param behComp THe BehaviourManager that owns this Task.
     * @return The created Task.
     */
    public static Task searchAndHunt(BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         * Sequence:
         *  find closest entity (animal to hunt)
         *  repeatUntilCondition: (Attack target)
         *      Parallel:
         *          find path to target
         *          move to target
         *          attack target
         *  Sequence:   (Gather resource)
         *      get path to resource
         *      move to resource
         *      gather resource
         *      find storage building
         *      find path to building
         *      move to building
         *      transfer itemNames to storage
         */

        blackBoard.itemTransfer.transferAll = true;

        Sequence mainSeq = new Sequence("hunt", blackBoard);

        FindClosestEntity fc = new FindClosestEntity("Finding Closest Animal", blackBoard);

        ((ParentTaskController) mainSeq.getControl()).addTask(fc); //Add the find closest entity job.
        ((ParentTaskController) mainSeq.getControl()).addTask(attackTarget(blackBoard, behComp)); //Add the attack target task to this sequence.
        ((ParentTaskController) mainSeq.getControl()).addTask(gatherTarget(blackBoard, behComp)); //Add the gather target task to this sequence.

        fc.control.callbacks.successCriteria = ent -> {
            Entity entity = (Entity)ent;
            return entity.getTags().hasTag("animal") && entity.getTags().hasTag("alive");
        };

        //Creates a floating text object when trying to find an animal fails.
        fc.getControl().callbacks.failureCallback = task -> {
            Vector2 pos = blackBoard.myManager.getEntityOwner().getTransform().getPosition();
            new FloatingText("Couldn't find a nearby animal to hunt!", new Vector2(pos.x, pos.y + 1), new Vector2(pos.x, pos.y + 10), 1.5f, 0.8f);
            behComp.changeTaskImmediate(behComp.getBehaviourStates().getDefaultState().getUserData().apply(behComp.getBlackBoard(), behComp));
        };

        return mainSeq;
    }

    /**
     * Generates the Task for hunting a target. This Task requires that the blackboard has the 'target' parameter set or it will fail.
     * @param blackBoard The blackboard of the Task.
     * @param behComp The BehaviourComponentComp that will own this Task.
     * @return The created Task.
     */
    public static Task attackTarget(BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         * repeatUntilCondition:
         *  Parallel:
         *      find path to target
         *      move to target
         *      attack target
         */
        Parallel parallel = new Parallel("Attacking", blackBoard);
        RepeatUntilCondition mainRepeat = new RepeatUntilCondition("attackTarget", blackBoard, parallel);

        FindPath fp = new FindPath("Finding path", blackBoard);
        Follow mt = new Follow("Following", blackBoard);
        Attack attack = new Attack("Attacking Target", blackBoard);

        //Make sure the target is not null.
        mainRepeat.getControl().callbacks.checkCriteria = task -> task.getBlackboard().target != null && task.blackBoard.target.getTags().hasTag("alive");

        //To succeed this repeat job, the target must be null, not valid, or not alive.
        mainRepeat.getControl().callbacks.successCriteria = task -> {
            Entity target = ((Task)task).getBlackboard().target;
            return target == null || !target.isValid() || !target.getTags().hasTag("alive");
        };

        //If the target has moved away from it's last square AND the move job is still active (why repath if not moving?), fail the parallel job.
        parallel.getControl().callbacks.failCriteria = tsk -> {
            Task task = (Task)tsk;
            boolean moved = task.getBlackboard().targetNode != ColonyGame.worldGrid.getNode((task.getBlackboard().target));
            boolean moveJobAlive = !mt.control.hasFinished();
            boolean outOfRange = attack.control.hasFinished() && attack.control.hasFailed();

            return (moveJobAlive && moved) || (!moveJobAlive && outOfRange);
        };

        //If we are within range of the target, succeed the MoveTo task.
        mt.getControl().callbacks.successCriteria = tsk -> {
            Task task = (Task)tsk;
            float dis = task.getBlackboard().target.getTransform().getPosition().dst(task.getBlackboard().myManager.getEntityOwner().getTransform().getPosition());
            return dis <= GH.toMeters(task.getBlackboard().attackRange);
        };

        fp.control.callbacks.checkCriteria = task -> task.blackBoard.target != null && task.blackBoard.target.getTags().hasTag("alive");

        ((ParentTaskController)parallel.getControl()).addTask(fp);
        ((ParentTaskController)parallel.getControl()).addTask(mt);
        ((ParentTaskController)parallel.getControl()).addTask(attack);

        return mainRepeat;
    }

    public static Task fish(BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         * Find a fishing spot.
         * Path to it.
         * Move to it.
         * Fish.
         * Find base.
         * Path to it.
         * Move to it.
         * Transfer all resources.
         */

        blackBoard.itemTransfer.transferAll = true;
        blackBoard.itemTransfer.itemNameToTransfer = null;
        blackBoard.itemTransfer.fromInventory = blackBoard.myManager.getEntityOwner().getComponent(Inventory.class);

        Sequence seq = new Sequence("fish", blackBoard);

        FindClosestTile fct = new FindClosestTile("Finding fishing spot", blackBoard);
        FindPath fp = new FindPath("Finding path to fishing spot", blackBoard);
        MoveTo mt = new MoveTo("Moving to fishing spot", blackBoard);
        Fish fish = new Fish("Fishing", blackBoard);
        FindClosestEntity fc = new FindClosestEntity("Finding base", blackBoard);
        FindPath fpBase = new FindPath("Finding path to base", blackBoard);
        MoveTo mtBase = new MoveTo("Moving to base", blackBoard);
        TransferItem tr = new TransferItem("Transferring resources", blackBoard);

        //We need to tell this fct what can pass as a valid tile.
        fct.getControl().callbacks.successCriteria = nd -> {
            Grid.Node node = (Grid.Node)nd;
            Grid.TerrainTile tile = blackBoard.colonyGrid.getNode(node.getX(), node.getY()).getTerrainTile();
            int visibility = blackBoard.colonyGrid.getVisibilityMap()[node.getX()][node.getY()].getVisibility();

            return tile.tileRef.category.equals("LightWater") && visibility != Constants.VISIBILITY_UNEXPLORED;
        };

        //We want to remove the last step in our destination (first in the list) since it will be on the shore line.
        fp.getControl().callbacks.successCallback = task -> blackBoard.path.removeFirst();

        fc.getControl().callbacks.successCallback = task -> blackBoard.itemTransfer.toInventory = blackBoard.target.getComponent(Inventory.class);

        ((ParentTaskController)seq.getControl()).addTask(fct);
        ((ParentTaskController)seq.getControl()).addTask(fp);
        ((ParentTaskController)seq.getControl()).addTask(mt);
        ((ParentTaskController)seq.getControl()).addTask(fish);
        ((ParentTaskController)seq.getControl()).addTask(fc);
        ((ParentTaskController)seq.getControl()).addTask(fpBase);
        ((ParentTaskController)seq.getControl()).addTask(mtBase);
        ((ParentTaskController)seq.getControl()).addTask(tr);

        return seq;
    }

    public static Task fleeTarget(BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         * Sequence
         *  FindNearbyTile - find a tile away from the target we are fleeing
         *  MoveTo - Move directly to the tile, no need to find a path
         */

        Task sequence = new Sequence("flee", blackBoard);
        Task repeatFiveTimes = new RepeatUntilCondition("Fleeing", blackBoard, sequence);
        Task findNearbyTile = new FindNearbyTile("Finding place to flee to!", blackBoard);
        Task moveTo = new MoveTo("Moving away!", blackBoard);

        ((ParentTaskController)sequence.getControl()).addTask(findNearbyTile);
        ((ParentTaskController)sequence.getControl()).addTask(moveTo);

        repeatFiveTimes.getControl().callbacks.successCriteria = tsk -> {
            Task task = (Task)tsk;
            return task.blackBoard.counter > 5 || (findNearbyTile.getControl().hasFinished() && findNearbyTile.getControl().hasFailed());
        };

        repeatFiveTimes.getControl().callbacks.startCallback = task -> task.blackBoard.counter = 0;

        //Reset some stuff.
        sequence.getControl().callbacks.startCallback = task -> {
            task.blackBoard.target = null;
            task.blackBoard.targetNode = null;
            task.blackBoard.counter = 0;
        };

        //Calculate my distance.
        sequence.getControl().callbacks.startCallback = task -> {
            task.blackBoard.myDisToTarget = (int)(Math.abs(blackBoard.myManager.getEntityOwner().getTransform().getPosition().x - blackBoard.target.getTransform().getPosition().x)
                + Math.abs(blackBoard.myManager.getEntityOwner().getTransform().getPosition().y - blackBoard.target.getTransform().getPosition().y));
        };

        //Try to find a tile away from our target to flee to.
        findNearbyTile.getControl().callbacks.successCriteria = n -> {
            Grid.Node node = (Grid.Node)n;

            //The distance to the target (node distance).
            float nodeDisToTarget = Math.abs(node.getXCenter() - blackBoard.target.getTransform().getPosition().x)
                    + Math.abs(node.getYCenter() - blackBoard.target.getTransform().getPosition().y);

            //If the node distance is greater than my distance to the target, we'll take it!
            if(nodeDisToTarget > blackBoard.myDisToTarget){
                blackBoard.targetNode = node;
                blackBoard.path.clear();
                blackBoard.path.add(new Vector2(node.getXCenter(), node.getYCenter()));
                return true;
            }

            return false;
        };

        moveTo.getControl().callbacks.finishCallback = task -> task.blackBoard.counter++;

        return repeatFiveTimes;

    }

    public static Task returnToBase(BlackBoard blackBoard, BehaviourManagerComp behComp){
        Sequence seq = new Sequence("returnToBase", blackBoard);
        FindClosestEntity fc = new FindClosestEntity("findingBase", blackBoard);

        //Make sure it's a building we find.
        fc.control.callbacks.successCriteria = ent -> ((Entity)ent).getTags().hasTag("alive");

        seq.control.addTask(fc);
        seq.control.addTask(moveTo(blackBoard, behComp));

        return seq;
    }
}
