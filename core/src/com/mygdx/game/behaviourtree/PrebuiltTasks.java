package com.mygdx.game.behaviourtree;

import com.mygdx.game.behaviourtree.action.*;
import com.mygdx.game.behaviourtree.composite.Sequence;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.helpers.Constants;
import com.mygdx.game.helpers.Grid;

/**
 * Created by Paha on 4/11/2015.
 */
public class PrebuiltTasks {
    public static Task gatherWaterTask(BlackBoard blackBoard){
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

        findWater.control.callbacks.successCriteria = node -> ((Grid.Node)node).getTerrainTile().category.equals("LightWater");
        fp.control.callbacks.successCallback = () -> blackBoard.path.removeFirst();
        fpBase.control.callbacks.successCallback = () -> blackBoard.toInventory = blackBoard.target.getComponent(Inventory.class);

        return seq;
    }
}
