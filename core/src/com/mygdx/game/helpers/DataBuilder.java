package com.mygdx.game.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.mygdx.game.component.Item;
import com.mygdx.game.component.Resource;
import com.mygdx.game.helpers.managers.ItemManager;
import com.mygdx.game.helpers.managers.ResourceManager;

import java.io.File;

/**
 * Created by Paha on 2/19/2015.
 */
public class DataBuilder {
    String filePath = "files/";
    String itemPath = "items.json";
    String resourcePath = "resources.json";
    String imgPath = "img/";
    String musicPath = "music/";
    String fontsPath = "fonts/";

    private EasyAssetManager assetManager;

    public DataBuilder(EasyAssetManager assetManager){

        this.assetManager = assetManager;
        buildImages(Gdx.files.internal(this.imgPath));
        buildItems();
        buildResources();
    }

    public boolean update(float delta){
        return assetManager.update();
    }

    private void buildImages(FileHandle dirHandle){
        for (FileHandle entry: dirHandle.list()) {
            if(entry.isDirectory()) //For every directory, call this function again to load the images.
                buildImages(new FileHandle(entry.path()+"/")); //A bit of recursion.

            loadImage(entry);
        }
    }

    private void loadImage(FileHandle entry){
        String extension = "";
        String commonName = "";

        int i = entry.name().lastIndexOf('.');
        if (i > 0) {
            extension = entry.name().substring(i + 1);
            commonName = entry.name().substring(0, i);
        }

        if(!extension.equals("png"))
            return;

        assetManager.load(entry.path(), commonName, Texture.class);
    }

    private void buildItems() {
        Json json = new Json();
        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);

        JsonItems items = json.fromJson(JsonItems.class, Gdx.files.internal(filePath+itemPath));

        for(JsonItem jsonItem : items.items){
            Item item = new Item(jsonItem.itemName, jsonItem.itemType, true, 10000, 1);
            item.setDisplayName(jsonItem.displayName);
            item.setDescription(jsonItem.description);
            ItemManager.addItemInstance(item);
        }
    }

    private void buildResources(){
        Json json = new Json();
        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);

        JsonResources resources = json.fromJson(JsonResources.class, Gdx.files.internal(filePath+resourcePath));

        for(JsonResource jRes : resources.resources){
            Resource resource = new Resource();
            resource.setResourceName(jRes.resourceName);
            resource.setDisplayName(jRes.displayName);
            resource.setResourceType(jRes.resourceType);

            //Build the item array
            String[] items = new String[jRes.items.size];
            for(int i=0;i<jRes.items.size;i++)
                items[i] = jRes.items.get(i);

            //Build the amounts array
            int[][] amounts = new int[jRes.amounts.length][2];
            for(int i=0;i<jRes.amounts.length;i++) {
                amounts[i][0] = jRes.amounts[i][0];
                amounts[i][1] = jRes.amounts[i][1];
            }

            resource.setItemNames(items);
            resource.setItemAmounts(amounts);
            resource.setTextureName(jRes.img);
            ResourceManager.addResourceInstance(resource);
        }
    }

    private static class JsonItems{
        public Array<JsonItem> items;
    }

    private static class JsonItem{
        public String itemName, displayName, itemType, description, img;
    }

    private static class JsonResources{
        public Array<JsonResource> resources;
    }

    private static class JsonResource{
        public String resourceName, displayName, resourceType, description, img;
        public Array<String> items;
        public int[][] amounts;
    }
}
