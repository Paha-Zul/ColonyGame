package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.component.Stats;
import com.mygdx.game.util.BlackBoard;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.managers.DataManager;

/**
 * Created by Paha on 3/14/2015.
 */
public class Consume extends LeafTask{

    public Consume(String name, BlackBoard blackBoard) {
        super(name, blackBoard);
    }

    @Override
    public boolean check() {
        return this.blackBoard.myManager.getEntityOwner().getComponent(Inventory.class).getItemList().size() > 0;
    }

    @Override
    public void start() {
        super.start();
        Inventory inv = this.blackBoard.myManager.getEntityOwner().getComponent(Inventory.class);
        //Search for an itemRef that has the effect we want. If we find one, consume one!
        for(Inventory.InventoryItem item : inv.getItemList()){
            DataBuilder.JsonItem ref = DataManager.getData(item.itemRef.getItemName(), DataBuilder.JsonItem.class);
            if(ref.hasEffect(this.blackBoard.itemEffect)) { //If this itemRef has the effect we want, get some and consume it!
                int itemAmount = inv.removeItem(ref.getItemName(), 1);
                this.addEffect(ref, itemAmount);
                this.control.finishWithSuccess();
                break;
            }
        }
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    public void addEffect(DataBuilder.JsonItem item, int amount){
        Stats stats = this.getBlackboard().myManager.getEntityOwner().getComponent(Stats.class);
        if(stats == null) return;

        //For each effect in the itemRef we are consuming...
        for(int i=0;i<item.getEffects().length;i++) {
            String effect = item.getEffects()[i];
            int strength = item.getStrengths()[i];
            Stats.Stat stat = stats.getStatWithEffect(effect);
            if(stat != null) stat.addToCurrent(strength);
        }
    }

    @Override
    public void end() {
        super.end();
    }
}
