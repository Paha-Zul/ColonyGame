package com.mygdx.game.helpers.managers;

import com.mygdx.game.component.Item;

import java.util.HashMap;

/**
 * Created by Paha on 1/18/2015.
 */
public class ItemManager {
    private static HashMap<String, Item> itemMap = new HashMap<>(20);

    static{

    }

    public static Item getItemByName(String name){
        Item item = itemMap.get(name);
        if(item == null)
            throw new RuntimeException("Item of name '"+name+"' does not exist.");

        return new Item(item);
    }

    public static boolean doesItemExist(String name){
        if(itemMap.get(name) == null)
            return false;

        return true;
    }

    public static void addItemInstance(Item item){
        itemMap.put(item.getItemName(), item);
    }

}
