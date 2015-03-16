package com.mygdx.game.behaviourtree.action;

import com.mygdx.game.behaviourtree.LeafTask;
import com.mygdx.game.component.BlackBoard;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.component.Stats;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.helpers.managers.ItemManager;

/**
 * Created by Paha on 3/14/2015.
 */
public class Consume extends LeafTask{
    private String effectWanted;

    public Consume(String name, BlackBoard blackBoard, String effectWanted) {
        super(name, blackBoard);
        this.effectWanted = effectWanted;
    }

    @Override
    public boolean check() {
        return this.blackBoard.getEntityOwner().getComponent(Inventory.class).getItemList().size() > 0;
    }

    @Override
    public void start() {
        super.start();


        Inventory inv = this.blackBoard.getEntityOwner().getComponent(Inventory.class);
        //Search for an item that has the effect we want. If we find one, consume one!
        for(Inventory.InventoryItem item : inv.getItemList()){
            DataBuilder.JsonItem ref = ItemManager.getItemReference(item.item.getItemName()); //Get the item list.
            if(ref.hasEffect(effectWanted)) { //If this item has the effect we want, get some and consume it!
                int itemAmount = inv.removeItemAmount(ref.getItemName(), 1);
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
        Stats stats = this.getBlackboard().getEntityOwner().getComponent(Stats.class);

        //For each effect in the item we are consuming...
        for(int i=0;i<item.getEffects().length;i++) {
            String effect = item.getEffects()[i];
            int strength = item.getStrengths()[i];

            switch (effect) {
                case "heal":
                    stats.addHealth(strength*amount);
                    break;
                case "feed":
                    stats.addFood(strength*amount);
                    break;
                case "hydrate":
                    stats.addWater(strength*amount);
                    break;
            }
        }
    }

    @Override
    public void end() {
        super.end();
    }
}
