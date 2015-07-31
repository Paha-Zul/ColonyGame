package com.mygdx.game.behaviourtree;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.action.*;
import com.mygdx.game.behaviourtree.composite.Parallel;
import com.mygdx.game.behaviourtree.composite.Selector;
import com.mygdx.game.behaviourtree.composite.Sequence;
import com.mygdx.game.behaviourtree.control.ParentTaskController;
import com.mygdx.game.behaviourtree.decorator.AlwaysTrue;
import com.mygdx.game.behaviourtree.decorator.RepeatUntilCondition;
import com.mygdx.game.component.*;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.*;

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
        /**
         *  Selector:
         *      Sequence:
         *          Sequence
         *              figure out tools for gathering
         *              find the tool shed
         *              check the shed and reserve
         *              find path to shed
         *              move to shed
         *              transfer tools
         *
         *          Repeat (until we are full on the items toggled):
         *              find resource
         *              find path to resource
         *              move to resource
         *              gather resource
         *
         *          find inventory
         *          find path to inventory
         *          move to inventory
         *          transfer items.
         *
         *     explore
         */

        Selector gatherOrExplore = new Selector("Gathering", blackBoard);
        Sequence sequence = new Sequence("Gathering Resource", blackBoard);

        //All this should be under a repeat.
        Sequence innerGatherSeq = new Sequence("Gathering", blackBoard);
        RepeatUntilCondition repeatGather = new RepeatUntilCondition("Repeat", blackBoard, innerGatherSeq);
        FindResource _fr = new FindResource("Finding resource", blackBoard);
        ReserveResource rr = new ReserveResource("Reserving", blackBoard);
        //FindClosestEntity fr = new FindClosestEntity("Finding Closest Resource", blackBoard);
        FindPath fpResource = new FindPath("Finding Path to Resource", blackBoard);
        MoveTo mtResource = new MoveTo("Moving to Resource", blackBoard);
        Gather gather = new Gather("Gathering Resource", blackBoard);

        //Selector - either gather sequence or explore.
        gatherOrExplore.control.addTask(sequence);
        gatherOrExplore.control.addTask(exploreUnexplored(blackBoard, behComp));

        sequence.control.addTask(getTools(blackBoard, behComp));

        //Add the repeat gather task to the main sequence, and then the rest to the inner sequence under repeat.
        sequence.control.addTask(repeatGather);
        innerGatherSeq.control.addTask(_fr);
        innerGatherSeq.control.addTask(rr);
        innerGatherSeq.control.addTask(fpResource);
        innerGatherSeq.control.addTask(mtResource);
        innerGatherSeq.control.addTask(gather);

        //If we are not actually set to find any resources, just fail the task!
        gatherOrExplore.control.callbacks.checkCriteria = task -> !task.blackBoard.resourceTypeTags.isEmpty();

        //Add these to the main sequence.
        ((ParentTaskController)sequence.getControl()).addTask(returnItems(blackBoard, behComp));

        //Reset some values.
        sequence.control.callbacks.startCallback = task -> {
            //Reset blackboard values...
            task.blackBoard.itemTransfer.reset();
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

        return gatherOrExplore;
    }

    private static Task gatherTarget(BlackBoard blackBoard, BehaviourManagerComp behComo){
        /**
         * Sequence:
         *  find path to resource
         *  move to resource
         *  gather resource
         *  find path to inventory
         *  move to inventory
         *  transfer itemNames to inventory
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
            task.blackBoard.itemTransfer.takingReserved = true;
        };

        //When we finish, if we still have plans to take items (itemNamesToTransfer and itemAmountsToTransfer is not null), try to unreserve it.
        seq.control.callbacks.finishCallback = task -> {
            if(task.blackBoard.itemTransfer.itemsToTransfer != null && task.blackBoard.itemTransfer.fromInventory != null){
                for(ItemNeeded item : task.blackBoard.itemTransfer.itemsToTransfer)
                    task.blackBoard.itemTransfer.fromInventory.unReserveItem(item.itemName, task.blackBoard.myManager.getEntityOwner().getID());
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
        };

        //When this finishes remove any on the way stuff
        seq.control.callbacks.finishCallback = task -> {
            for(ItemNeeded item : task.blackBoard.itemTransfer.itemsToTransfer)
                task.blackBoard.itemTransfer.toInventory.removeOnTheWay(item.itemName, task.blackBoard.myManager.getEntityOwner().getID());
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

            String[] toolNames = task.blackBoard.myManager.getComponent(Equipment.class).getToolNames();
            for (String toolName : toolNames) {
                task.blackBoard.itemTransfer.itemsToTransfer.add(new ItemNeeded(toolName, 1));
                //TODO Don't leave this as is.
                task.blackBoard.itemTransfer.toInventory.addOnTheWay(toolName, 1, task.blackBoard.myManager.getEntityOwner().getID());
            }
        };

        return seq;

    }

    public static Task returnItems(BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         *  Sequence
         *      Find inventory
         *      get path to inventory
         *      move to inventory
         *      transfer all items.  (except tools)
         */

        Sequence mainSeq = new Sequence("Dropping Off", blackBoard);
        FindPath fpStorage = new FindPath("Finding path", blackBoard);
        MoveTo mtStorage = new MoveTo("Moving to storage", blackBoard);
        TransferItem transfer = new TransferItem("Transferring", blackBoard);

        mainSeq.control.addTask(fpStorage);
        mainSeq.control.addTask(mtStorage);
        mainSeq.control.addTask(transfer);

        mainSeq.control.callbacks.startCallback = task -> {
            Colonist col = task.blackBoard.myManager.getComponent(Colonist.class);
            Building building = col.getColony().getOwnedFromColony(Building.class, b -> b.buildingTags.hasTag("storage"));
            if(building == null) task.getControl().finishWithFailure();
            else {
                task.blackBoard.target = building.getEntityOwner();
                if (task.blackBoard.target == null) task.getControl().finishWithFailure();
                else{
                    task.blackBoard.itemTransfer.reset();
                    task.blackBoard.itemTransfer.fromInventory = col.getInventory();
                    task.blackBoard.itemTransfer.toInventory = building.getInventory();
                    task.blackBoard.itemTransfer.itemTypesToIgnore.add("tool");
                }
            }
        };

        return mainSeq;
    }

    public static Task build(BlackBoard blackboard, BehaviourManagerComp behComp){
        /**
         *  Selector
         *      Sequence{
         *          find a building under construction
         *          Sequence - Always true decorator
         *              get list of items needed to build
         *              find inventory
         *              reserve items
         *              find path to inventory
         *              move to inventory
         *              transfer items.
         *         Sequence
         *              get path to building
         *              move to building
         *              transfer any materials
         *              build
         *      }
         *
         *      idle
         */

        /**
         * TODO When the building only needs a limited amount (ie: building needs 5 stone and the colonist has 10 stone), the colonists inventory
         * TODO is cleared, but not all items are transferred. Make sure we only remove from the inventory what we need to. Maybe... can't reproduce it.
        */

        Selector mainSelector = new Selector("Build", blackboard);
        Sequence constructionSeq = new Sequence("BuildSeq", blackboard);
        GetConstruction getConstruction = new GetConstruction("GettingConstruction", blackboard);

        //This is where we possible get items for the construction.
        Sequence getItemsForConstSeq = new Sequence("GetItemSeq", blackboard);
        AlwaysTrue getItemsSeqTrue = new AlwaysTrue("AlwaysTrue", blackboard, getItemsForConstSeq);
        FindStorageWithItem findItem = new FindStorageWithItem("FindStorageWithItem", blackboard);
        CheckAndReserve reserve = new CheckAndReserve("CheckAndReserve", blackboard);
        FindPath fpToStorage = new FindPath("PathToStorage", blackboard);
        MoveTo mtStorage = new MoveTo("MoveToStorage", blackboard);
        TransferItem transferItems = new TransferItem("Transferring", blackboard);

        //This is where we build
        Sequence buildSeq = new Sequence("Build", blackboard);
        FindPath fpToBuilding = new FindPath("FindPathBuilding", blackboard);
        MoveTo mtBuilding = new MoveTo("MoveToBuilding", blackboard);
        TransferItem transferToBuilding = new TransferItem("TransferToBuilding", blackboard);
        Construct construct = new Construct("Constructing", blackboard);

        //The main selector between constructing and idling
        mainSelector.control.addTask(constructionSeq);
        mainSelector.control.addTask(idleTask(blackboard, behComp));

        //The main sequence of construction.
        constructionSeq.control.addTask(getConstruction);
        constructionSeq.control.addTask(getItemsSeqTrue); //Use the always true as getting items isn't necessary
        constructionSeq.control.addTask(buildSeq);

        //Get items sequence
        //Find a storage with any of the items we need.
        getItemsForConstSeq.control.addTask(findItem);
        getItemsForConstSeq.control.addTask(reserve);
        getItemsForConstSeq.control.addTask(fpToStorage);
        getItemsForConstSeq.control.addTask(mtStorage);
        getItemsForConstSeq.control.addTask(transferItems);

        //Build sequence
        buildSeq.control.addTask(fpToBuilding);
        buildSeq.control.addTask(mtBuilding);
        buildSeq.control.addTask(transferToBuilding);
        buildSeq.control.addTask(construct);

        //Time for the crap

        constructionSeq.control.callbacks.startCallback = task -> {
            task.blackBoard.target = null;
            task.blackBoard.targetNode = null;
        };

        //First, we need to get a building under construction. Then, we need to get a list of items. Then we need to find a inventory
        //building with any of the items we need.
        getConstruction.control.callbacks.successCallback = task -> {
            //reset and set some values.
            task.blackBoard.itemTransfer.reset();
            task.blackBoard.itemTransfer.takingReserved = true;

            //Get the constructable and get the items to transfer.
            task.blackBoard.constructable = task.blackBoard.target.getComponent(Constructable.class);
            for(ItemNeeded needed : task.blackBoard.constructable.getItemsNeeded()) //Copy the result
                task.blackBoard.itemTransfer.itemsToTransfer.add(new ItemNeeded(needed.itemName, needed.amountNeeded));

        };

        getItemsForConstSeq.control.callbacks.finishCallback = task -> {
            task.blackBoard.target = task.blackBoard.constructable.getEntityOwner();
            if(task.blackBoard.target != null) {
                task.blackBoard.itemTransfer.fromInventory = task.blackBoard.myManager.getEntityOwner().getComponent(Inventory.class);
                task.blackBoard.itemTransfer.toInventory = task.blackBoard.target.getComponent(Inventory.class);
            }
        };

        buildSeq.control.callbacks.startCallback = task -> {
            task.blackBoard.itemTransfer.takingReserved = false;
            task.blackBoard.target = task.blackBoard.constructable.getEntityOwner();
            task.blackBoard.targetNode = null;
        };

        //Make sure the building either has materials or we have the materials.
        buildSeq.control.callbacks.checkCriteria = task -> {
            for(ItemNeeded item : task.blackBoard.itemTransfer.itemsToTransfer){
                if(task.blackBoard.itemTransfer.toInventory.hasItem(item.itemName) || task.blackBoard.itemTransfer.fromInventory.hasItem(item.itemName))
                    return true;
            }
            return false;
        };

        return mainSelector;
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

        //Make sure we clear our targets and target node first to get a fresh unexplored area.
        sequence.control.callbacks.startCallback = task -> {
            task.blackBoard.target = null;
            task.blackBoard.targetNode = null;
        };

        //Get the main building of the colony as our target to explore around.
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
         *  check that the inventory of the inventory has the item effect we want/need
         *  find path to inventory
         *  move to inventory
         *  transfer needed item to me (colonist)
         *  consume item
         */

        //TODO The itemTransfer stuff was refactored and probably broke this area, namely the CheckInventoryHas class.

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
            task.blackBoard.itemTransfer.reset(); //Reset item transfers
            task.blackBoard.targetNode = null; //We don't want this set.

            //Get a inventory building from my colony, store the entity as the target, and set the from/to inventory.
            Building storage = task.blackBoard.myManager.getEntityOwner().getComponent(Colonist.class).getColony().getOwnedFromColony(Building.class, b -> b.buildingTags.hasTag("storage"));
            task.blackBoard.target = storage.getEntityOwner();
            task.blackBoard.itemTransfer.fromInventory = storage.getComponent(Inventory.class);
            task.blackBoard.itemTransfer.toInventory = blackBoard.myManager.getEntityOwner().getComponent(Inventory.class);
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
         *      find inventory building
         *      find path to building
         *      move to building
         *      transfer itemNames to inventory
         */

        Sequence mainSeq = new Sequence("hunt", blackBoard);

        FindClosestEntity fc = new FindClosestEntity("Finding Closest Animal", blackBoard);

        ((ParentTaskController) mainSeq.getControl()).addTask(fc); //Add the find closest entity job.
        ((ParentTaskController) mainSeq.getControl()).addTask(attackTarget(blackBoard, behComp)); //Add the attack target task to this sequence.
        ((ParentTaskController) mainSeq.getControl()).addTask(gatherTarget(blackBoard, behComp)); //Add the gather target task to this sequence.

        //Get an alive animal.
        fc.control.callbacks.successCriteria = ent -> {
            Entity entity = (Entity)ent;
            return entity.getTags().hasTags("animal", "alive");
        };

        //Creates a floating text object when trying to find an animal fails.
        fc.getControl().callbacks.failureCallback = task -> {
            Vector2 pos = blackBoard.myManager.getEntityOwner().getTransform().getPosition();
            new FloatingText("Couldn't find a nearby animal to hunt!", new Vector2(pos.x, pos.y + 1), new Vector2(pos.x, pos.y + 10), 1.5f, 0.8f);
            //TODO What did this do? Change to default state? Redundant I think.
            //behComp.changeTaskImmediate(behComp.getBehaviourStates().getDefaultState().getUserData().apply(behComp.getBlackBoard(), behComp));
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

        //We want to end when the counter is more than 5.
        repeatFiveTimes.getControl().callbacks.successCriteria = tsk -> {
            Task task = (Task)tsk;
            return task.blackBoard.counter > 5 || (findNearbyTile.getControl().hasFinished() && findNearbyTile.getControl().hasFailed());
        };

        //Reset the counter when we start.
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

    public static Task sleep(BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         *  Sequence
         *      Sequence (always true / optional)
         *          Find building
         *          path to building
         *          enter building
         *      sleep...
         *      exit building
         */

        Sequence mainSeq = new Sequence("Sleeping", blackBoard);
        FindPath fp = new FindPath("Pathing", blackBoard);
        MoveTo mt = new MoveTo("Moving", blackBoard);
        Enter enter = new Enter("Entering", blackBoard);
        Sleep sleep = new Sleep("Sleeping", blackBoard);
        Leave leave = new Leave("Leaving", blackBoard);

        mainSeq.control.addTask(fp);
        mainSeq.control.addTask(mt);
        mainSeq.control.addTask(enter);
        mainSeq.control.addTask(sleep);
        mainSeq.control.addTask(leave);

        mainSeq.control.callbacks.startCallback = task -> {
            task.blackBoard.targetNode = null;

            Colonist col = task.blackBoard.myManager.getEntityOwner().getComponent(Colonist.class);
            Colony colony = col.getColony();
            Building building = colony.getOwnedFromColony(Building.class, b -> b.buildingTags.hasTag("main"));
            if(building == null) task.getControl().finishWithFailure();
            else task.blackBoard.target = building.getEntityOwner();
        };

        mainSeq.control.callbacks.finishCallback = task -> {
            task.blackBoard.myManager.getEntityOwner().getGraphicIdentity().setActive(true);
            task.blackBoard.myManager.getEntityOwner().getTags().addTag("selectable");
        };

        return mainSeq;
    }

    public static Task craftItem(BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         *  Sequence
         *      Repeat until
         *          Sequence
         *              gather a list of items we need
         *
         *
         */

        return null;
    }
}
