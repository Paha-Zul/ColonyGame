package com.mygdx.game.helpers.managers;

import com.mygdx.game.component.Item;
import com.mygdx.game.helpers.DataBuilder;

import java.util.HashMap;

/**
 * Created by Paha on 1/18/2015.
 */
public class ItemManager {
    private static HashMap<String, DataBuilder.JsonItem> itemMap = new HashMap<>(20);

    public static Item getItemByName(String name){
        DataBuilder.JsonItem item = itemMap.get(name);
        if(item == null)
            throw new RuntimeException("Item of name '"+name+"' does not exist.");

        return new Item(item);
    }

    public static DataBuilder.JsonItem getItemReference(String name){
        DataBuilder.JsonItem item = itemMap.get(name);
        if(item == null)
            throw new RuntimeException("Item of name '"+name+"' does not exist.");

        return item;
    }

    public static boolean doesItemExist(String name){
        return itemMap.get(name) != null;
    }

    public static void addItemInstance(DataBuilder.JsonItem item){
        itemMap.put(item.getItemName(), item);
    }

}
