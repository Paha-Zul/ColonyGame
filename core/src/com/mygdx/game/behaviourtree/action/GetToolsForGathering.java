package com.mygdx.game.behaviourtree.action;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Equipment;
import com.mygdx.game.util.BlackBoard;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.managers.DataManager;

/**
 * Created by brad on 6/9/15.
 */
public class GetToolsForGathering extends LeafTask{
    Equipment equipment;

    public GetToolsForGathering(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return super.check();
    }

    @Override
    public void start() {
        super.start();
        this.equipment = this.blackBoard.myManager.getEntityOwner().getComponent(Equipment.class);
        if(this.equipment == null) this.control.finishWithFailure();
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        this.blackBoard.itemTransfer.itemNamesToTransfer = new Array<>();
        this.blackBoard.itemTransfer.itemAmountsToTransfer = new Array<>();
        String[] items = this.blackBoard.resourceTypeTags.getTagsAsString();

        //For each item, if we don't already have the tool in our equipment, add it to the 'wanted' list.
        for(String item : items){
            DataBuilder.JsonItem jItem = DataManager.getData(item, DataBuilder.JsonItem.class);
            for(String tool : jItem.possibleTools){
                //If we it's empty or we already have the tool, continue.
                if(tool.equals("") || this.equipment.hasTool(tool)) continue;
                //If the itemNamesToTransfer array doesn't already contain the tool, add it.
                if(!this.blackBoard.itemTransfer.itemNamesToTransfer.contains(tool, false)) {
                    this.blackBoard.itemTransfer.itemNamesToTransfer.add(tool);
                    this.blackBoard.itemTransfer.itemAmountsToTransfer.add(1);
                }
            }
        }

        if(this.blackBoard.itemTransfer.itemNamesToTransfer.size == 0) {
            this.control.finishWithFailure();
        }else {
            this.control.finishWithSuccess();
        }
    }

    @Override
    public void end() {
        super.end();
    }
}
