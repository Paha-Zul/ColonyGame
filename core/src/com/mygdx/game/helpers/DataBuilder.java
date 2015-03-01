package com.mygdx.game.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.TextureLoader;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Item;
import com.mygdx.game.component.Resource;
import com.mygdx.game.helpers.managers.ItemManager;
import com.mygdx.game.helpers.managers.ResourceManager;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.interfaces.IDestroyable;

import java.io.File;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Paha on 2/19/2015.
 */
public class DataBuilder implements IDestroyable{
    String filePath = "files/";
    String itemPath = "items.json";
    String resourcePath = "resources.json";
    String tilePath = "tiles.json";
    String imgPath = "img/";

    private EasyAssetManager assetManager;

    public static JsonTile[] tileList;

    public DataBuilder(EasyAssetManager assetManager){
        TextureLoader.TextureParameter param = new TextureLoader.TextureParameter();
        param.minFilter = Texture.TextureFilter.MipMapLinearLinear;
        param.magFilter = Texture.TextureFilter.MipMapLinearLinear;
        param.genMipMaps = true;

        this.assetManager = assetManager;
        buildImages(Gdx.files.internal(this.imgPath), param);
    }

    public boolean update(){
        return assetManager.update();
    }

    public void loadFiles(){
        buildItems();
        buildResources();
        buildTiles();
    }

    private void buildImages(FileHandle dirHandle, TextureLoader.TextureParameter param){
        for (FileHandle entry: dirHandle.list()) {
            if(entry.isDirectory()) //For every directory, call this function again to load the images.
                buildImages(new FileHandle(entry.path()+"/"), param); //A bit of recursion.

            loadImage(entry, param);
        }
    }

    private void loadImage(FileHandle entry, TextureLoader.TextureParameter param){
        String extension = "";
        String commonName = "";

        int i = entry.name().lastIndexOf('.');
        if (i > 0) {
            extension = entry.name().substring(i + 1);
            commonName = entry.name().substring(0, i);
        }

        if(!extension.equals("png"))
            return;

        assetManager.load(entry.path(), commonName, Texture.class, param);
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

    private void buildTiles(){
        Json json = new Json();
        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);

        JsonTiles tiles = json.fromJson(JsonTiles.class, Gdx.files.internal(filePath+tilePath));
        tileList = tiles.tiles;
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

    private static class JsonTiles{
        public JsonTile[] tiles;
    }

    public static class JsonTile{
        public String[] tileNames, img, resources;
        public String category;
        public float[] height;
        public float[][] resourcesChance;
        public boolean avoid;
    }

    @Override
    public void destroy() {

    }

    @Override
    public boolean isDestroyed() {
        return false;
    }
}
