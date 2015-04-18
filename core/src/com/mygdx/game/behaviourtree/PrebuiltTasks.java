package com.mygdx.game.behaviourtree;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.action.*;
import com.mygdx.game.behaviourtree.composite.Parallel;
import com.mygdx.game.behaviourtree.composite.Sequence;
import com.mygdx.game.behaviourtree.control.ParentTaskController;
import com.mygdx.game.behaviourtree.decorator.RepeatUntilCondition;
import com.mygdx.game.component.*;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.FloatingText;
import com.mygdx.game.helpers.GH;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.interfaces.Functional;

/**
 * Created by Paha on 4/11/2015.
 */
public class PrebuiltTasks {
    
    public static Task moveTo(BlackBoard blackBoard){
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
         *  Find the closest valid resource and valid storage closest to the resource.
         *  Find a path to the resource.
         *  Move to the resource.
         *  Gather the resource.
         *  Find a path back to the storage.
         *  Store the resource.
         */

        //If we fail to find a resource, we need to explore until we find one...
        Functional.Callback fail = () -> {
            Vector2 pos = blackBoard.getEntityOwner().transform.getPosition();
            new FloatingText("Couldn't find a nearby resource!", new Vector2(pos.x, pos.y + 1), new Vector2(pos.x, pos.y + 10), 1.5f, 0.8f);

            //When we finish moving to the newly explored area, try to gather a resource again.
            Task task = exploreUnexplored(blackBoard, behComp);
            task.getControl().getCallbacks().finishCallback = () -> behComp.changeTask(gatherResource(blackBoard, behComp));
            behComp.changeTask(task);
        };

        Sequence sequence = new Sequence("Gathering Resource", blackBoard);
        FindClosestEntity fr = new FindClosestEntity("Finding Closest Resource", blackBoard, Constants.ENTITY_RESOURCE);

        ((ParentTaskController)sequence.getControl()).addTask(fr);
        ((ParentTaskController)sequence.getControl()).addTask(gatherTarget(blackBoard, behComp));

        //If we fail, call the fail callback.
        fr.getControl().callbacks.failureCallback = fail;

        fr.control.callbacks.checkCriteria = task -> !task.blackBoard.resourceTypeTags.isEmpty();

        //Check to make sure the resource isn't taken.
        fr.getControl().callbacks.successCriteria = (e) -> {
            Entity ent = (Entity)e;
            Resource resource = ent.getComponent(Resource.class);
            if(resource == null || blackBoard.resourceTypeTags.isEmpty()) return false;

            boolean taken = !resource.isTaken();
            int[] blackTags = blackBoard.resourceTypeTags.getTags();
            boolean hasTag = resource.resourceTypeTags.hasAnyTag(blackTags);

            return taken && hasTag;
        };

        //On success, set the resource as taken if not already taken.
        fr.getControl().callbacks.successCallback = () ->  {
            blackBoard.targetResource = blackBoard.target.getComponent(Resource.class);
            if(!blackBoard.targetResource.isTaken())
                blackBoard.targetResource.setTaken(blackBoard.getEntityOwner());
        };

        sequence.getControl().callbacks.startCallback = ()->{
            //Reset blackboard values...
            blackBoard.fromInventory = blackBoard.getEntityOwner().getComponent(Colonist.class).getInventory();
            blackBoard.toInventory = blackBoard.getEntityOwner().getComponent(Colonist.class).getColony().getInventory();
            blackBoard.transferAll = true;
            blackBoard.takeAmount = 0;
            blackBoard.itemNameToTake = null;
            blackBoard.targetResource = null;
            blackBoard.target = null;
            blackBoard.targetNode = null;
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
         *  transfer items to storage
         */

        Sequence sequence = new Sequence("Gathering", blackBoard);

        //Create all the job objects we need...
        FindPath findPath = new FindPath("Finding Path to Resource", blackBoard);
        MoveTo move = new MoveTo("Moving to Resource", blackBoard);
        Gather gather = new Gather("Gathering Resource", blackBoard);
        FindPath findPathToStorage = new FindPath("Finding Path to Storage", blackBoard);
        MoveTo moveToStorage = new MoveTo("Moving to Storage", blackBoard);
        TransferResource transferItems = new TransferResource("Transferring Resources", blackBoard);

        ((ParentTaskController)sequence.getControl()).addTask(findPath);
        ((ParentTaskController)sequence.getControl()).addTask(move);
        ((ParentTaskController)sequence.getControl()).addTask(gather);
        ((ParentTaskController)sequence.getControl()).addTask(findPathToStorage);
        ((ParentTaskController)sequence.getControl()).addTask(moveToStorage);
        ((ParentTaskController)sequence.getControl()).addTask(transferItems);

        //When finding a path to the resource, make sure it's actually a resource and we are the ones that have claimed it!
        findPath.getControl().callbacks.checkCriteria = task -> {
            Resource res = task.blackBoard.targetResource; //Get the target resource from the blackboard.
            if(res == null) task.blackBoard.targetResource = res = task.blackBoard.target.getComponent(Resource.class); //If null, try to get it from the target.
            return res != null && task.getBlackboard().targetResource.getTaken() == task.getBlackboard().getEntityOwner(); //Return true if not null and we are the ones that took it. False otherwise.
        };

        //When we finish, set the target back to not taken IF it is still a valid target (if we ended early).
        sequence.getControl().callbacks.finishCallback = () -> {
            if (sequence.getBlackboard().target != null && sequence.getBlackboard().target.isValid() && sequence.getBlackboard().target.hasTag(Constants.ENTITY_RESOURCE)) {
                if(sequence.getBlackboard().targetResource.getTaken() == sequence.getBlackboard().getEntityOwner())
                    sequence.getBlackboard().targetResource.setTaken(null);
            }
        };

        return sequence;
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

        Sequence sequence = new Sequence("Exploring", blackBoard);

        FindClosestUnexplored findClosestUnexplored = new FindClosestUnexplored("Finding Closest Unexplored Location", blackBoard);
        FindPath findPathToUnexplored = new FindPath("Finding Path to Unexplored", blackBoard);
        MoveTo moveToLocation = new MoveTo("Moving to Explore", blackBoard);

        ((ParentTaskController) sequence.getControl()).addTask(findClosestUnexplored);
        ((ParentTaskController) sequence.getControl()).addTask(findPathToUnexplored);
        ((ParentTaskController) sequence.getControl()).addTask(moveToLocation);

        return sequence;
    }

    public static Task idleTask(float baseIdleTime, float randomIdleTime, int radius, BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         * Sequence:
         *  find random nearby location
         *  find path to location
         *  move to location
         *  idle for random amount of time
         */

        Sequence sequence = new Sequence("Idling", blackBoard);

        FindRandomNearbyLocation findNearbyLocation = new FindRandomNearbyLocation("Finding Nearby Location", blackBoard, radius);
        FindPath findPath = new FindPath("Finding Path to Nearby Location", blackBoard);
        MoveTo moveTo = new MoveTo("Moving to Nearby Location", blackBoard);
        Idle idle = new Idle("Standing Still", blackBoard, baseIdleTime, randomIdleTime);

        ((ParentTaskController) sequence.getControl()).addTask(findNearbyLocation);
        ((ParentTaskController) sequence.getControl()).addTask(findPath);
        ((ParentTaskController) sequence.getControl()).addTask(moveTo);
        ((ParentTaskController) sequence.getControl()).addTask(idle);

        return sequence;
    }

    public static Task consumeTask(String effect, BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         * Sequence:
         *  check that the inventory of the storage has the item effect we want/need
         *  find path to storage
         *  move to storage
         *  transfer needed item to me (colonist)
         *  consume item
         */

        blackBoard.target = blackBoard.getEntityOwner().getComponent(Colonist.class).getColony().getEntityOwner();
        blackBoard.targetNode = null;
        blackBoard.fromInventory = blackBoard.getEntityOwner().getComponent(Colonist.class).getColony().getInventory();
        blackBoard.toInventory = blackBoard.getEntityOwner().getComponent(Inventory.class);
        blackBoard.transferAll = false;
        blackBoard.takeAmount = 1;
        blackBoard.itemNameToTake = null;

        Sequence sequence = new Sequence("Consuming Item", blackBoard);

        CheckInventoryHas check = new CheckInventoryHas("Checking Inventory", blackBoard, effect, 1);
        FindPath fp = new FindPath("Finding Path to consume item", blackBoard);
        MoveTo moveTo = new MoveTo("Moving to consume item", blackBoard);
        TransferResource tr = new TransferResource("Transferring Consumable", blackBoard);
        Consume consume = new Consume("Consuming Item", blackBoard, effect);

        ((ParentTaskController) sequence.getControl()).addTask(check);
        ((ParentTaskController) sequence.getControl()).addTask(fp);
        ((ParentTaskController) sequence.getControl()).addTask(moveTo);
        ((ParentTaskController) sequence.getControl()).addTask(tr);
        ((ParentTaskController) sequence.getControl()).addTask(consume);

        sequence.getControl().callbacks.startCallback = ()->{
            //Reset blackboard values.
            blackBoard.target = blackBoard.getEntityOwner().getComponent(Colonist.class).getColony().getEntityOwner();
            blackBoard.targetNode = null;
            blackBoard.fromInventory = blackBoard.getEntityOwner().getComponent(Colonist.class).getColony().getInventory();
            blackBoard.toInventory = blackBoard.getEntityOwner().getComponent(Inventory.class);
            blackBoard.transferAll = false;
            blackBoard.takeAmount = 1;
            blackBoard.itemNameToTake = null;
        };

        return sequence;
    }

    public static Task huntTarget(BlackBoard blackBoard, BehaviourManagerComp behComp){
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
         *      transfer items to storage
         */

        blackBoard.fromInventory = blackBoard.getEntityOwner().getComponent(Inventory.class);
        blackBoard.toInventory = blackBoard.getEntityOwner().getComponent(Colonist.class).getColony().getInventory();
        blackBoard.transferAll = true;

        Sequence mainSeq = new Sequence("Following", blackBoard);

        FindClosestEntity fc = new FindClosestEntity("Finding Closest Animal", blackBoard, Constants.ENTITY_ANIMAL);

        ((ParentTaskController) mainSeq.getControl()).addTask(fc); //Add the find closest entity job.
        ((ParentTaskController) mainSeq.getControl()).addTask(attackTarget(blackBoard, behComp)); //Add the attack target task to this sequence.
        ((ParentTaskController) mainSeq.getControl()).addTask(gatherTarget(blackBoard, behComp)); //Add the gather target task to this sequence.

        //Creates a floating text object when trying to find an animal fails.
        fc.getControl().callbacks.failureCallback = () -> {
            Vector2 pos = blackBoard.getEntityOwner().transform.getPosition();
            new FloatingText("Couldn't find a nearby animal to hunt!", new Vector2(pos.x, pos.y + 1), new Vector2(pos.x, pos.y + 10), 1.5f, 0.8f);
        };

        return mainSeq;
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

        blackBoard.transferAll = true;
        blackBoard.itemNameToTake = null;
        blackBoard.fromInventory = blackBoard.getEntityOwner().getComponent(Inventory.class);

        Sequence seq = new Sequence("Fishing", blackBoard);

        FindClosestTile fct = new FindClosestTile("Finding fishing spot", blackBoard);
        FindPath fp = new FindPath("Finding path to fishing spot", blackBoard);
        MoveTo mt = new MoveTo("Moving to fishing spot", blackBoard);
        Fish fish = new Fish("Fishing", blackBoard);
        FindClosestEntity fc = new FindClosestEntity("Finding base", blackBoard, Constants.ENTITY_BUILDING);
        FindPath fpBase = new FindPath("Finding path to base", blackBoard);
        MoveTo mtBase = new MoveTo("Moving to base", blackBoard);
        TransferResource tr = new TransferResource("Transfering resources", blackBoard);

        //We need to tell this fct what can pass as a valid tile.
        fct.getControl().callbacks.successCriteria = nd -> {
            Grid.Node node = (Grid.Node)nd;
            Grid.TerrainTile tile = blackBoard.colonyGrid.getNode(node.getX(), node.getY()).getTerrainTile();
            int visibility = blackBoard.colonyGrid.getVisibilityMap()[node.getX()][node.getY()].getVisibility();

            return tile.category.equals("LightWater") && visibility != Constants.VISIBILITY_UNEXPLORED;
        };

        //We want to remove the last step in our destination (first in the list) since it will be on the shore line.
        fp.getControl().callbacks.successCallback = () -> blackBoard.path.removeFirst();

        fc.getControl().callbacks.successCallback = () -> blackBoard.toInventory = blackBoard.target.getComponent(Inventory.class);

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

    public static Task attackTarget(BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         * repeatUntilCondition:
         *  Parallel:
         *      find path to target
         *      move to target
         *      attack target
         */

        Parallel parallel = new Parallel("Attacking", blackBoard);
        RepeatUntilCondition repeat = new RepeatUntilCondition("Repeating", blackBoard, parallel);

        FindPath fp = new FindPath("Finding path", blackBoard);
        MoveTo mt = new MoveTo("Moving", blackBoard);
        Attack attack = new Attack("Attacking Target", blackBoard);

        ((ParentTaskController)parallel.getControl()).addTask(fp);
        ((ParentTaskController)parallel.getControl()).addTask(mt);
        ((ParentTaskController)parallel.getControl()).addTask(attack);

        //Make sure the target is not null.
        repeat.getControl().callbacks.checkCriteria = task -> task.getBlackboard().target != null;

        //To succeed this repeat job, the target must be null, not valid, or not alive.
        repeat.getControl().callbacks.successCriteria = task -> {
            Entity target = ((Task)task).getBlackboard().target;
            return target == null || !target.isValid() || !target.hasTag(Constants.ENTITY_ALIVE);
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
            float dis = task.getBlackboard().target.transform.getPosition().dst(task.getBlackboard().getEntityOwner().transform.getPosition());
            return dis <= GH.toMeters(task.getBlackboard().attackRange);
        };

        return repeat;
    }
    
    public static Task gatherWaterTask(BlackBoard blackBoard, BehaviourManagerComp behComp){
        /**
         * Sequence:
         *  find closest tile with water on it
         *  find path to tile
         *  move to tile
         *  gather water from tile
         *  find storage
         *  move to storage
         *  transfer to storage
         */

        blackBoard.transferAll = true;
        blackBoard.fromInventory = blackBoard.getEntityOwner().getComponent(Inventory.class);

        Sequence seq = new Sequence("Gathering Water", blackBoard);

        FindClosestTile findWater = new FindClosestTile("Finding water", blackBoard);
        FindPath fp = new FindPath("Finding path to water", blackBoard);
        MoveTo mt = new MoveTo("Moving to water", blackBoard);
        GatherWater gatherWater = new GatherWater("Gathering water", blackBoard);
        FindClosestEntity fb = new FindClosestEntity("Finding base", blackBoard, Constants.ENTITY_BUILDING);
        FindPath fpBase = new FindPath("Finding path to base", blackBoard);
        MoveTo mtBase = new MoveTo("Moving to base", blackBoard);
        TransferResource transfer = new TransferResource("Transferring to base", blackBoard);

        seq.control.addTask(findWater);
        seq.control.addTask(fp);
        seq.control.addTask(mt);
        seq.control.addTask(gatherWater);
        seq.control.addTask(fb);
        seq.control.addTask(fpBase);
        seq.control.addTask(mtBase);
        seq.control.addTask(transfer);

        findWater.control.callbacks.successCriteria = node -> {
            Grid.Node n = (Grid.Node)node;
            boolean cat = n.getTerrainTile().category.equals("LightWater");
            boolean vis = ColonyGame.worldGrid.getVisibilityMap()[n.getX()][n.getY()].getVisibility() != Constants.VISIBILITY_UNEXPLORED;
            return cat && vis;
        };
        fp.control.callbacks.successCallback = () -> blackBoard.path.poll();
        fpBase.control.callbacks.successCallback = () -> blackBoard.toInventory = blackBoard.target.getComponent(Inventory.class);
        seq.control.callbacks.failureCallback = () -> behComp.idle();

        return seq;
    }


}
