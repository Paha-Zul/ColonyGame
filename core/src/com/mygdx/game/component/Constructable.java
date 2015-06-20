package com.mygdx.game.component;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.entity.Entity;

/**
 * Created by Paha on 6/20/2015.
 */
public class Constructable extends Component{
    private Array<String> items = new Array<>();
    private Array<Integer> amounts = new Array<>();

    public Constructable(){
        this.setActive(false);
    }

    @Override
    public void init(Entity owner) {
        super.init(owner);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void save() {
        super.save();
    }

    @Override
    public void initLoad() {
        super.initLoad();
    }

    @Override
    public void load() {
        super.load();
    }

    public Constructable addItem(String itemName){
        this.items.add(itemName);
        return this;
    }

    public Constructable addItems(String... items){
        for(String item : items) this.addItem(item);
        return this;
    }

    public Constructable addItemAmount(int itemAmount){
        this.amounts.add(itemAmount);
        return this;
    }

    public Constructable addItems(Integer... itemAmounts){
        this.amounts.addAll(itemAmounts);
        return this;
    }
}
