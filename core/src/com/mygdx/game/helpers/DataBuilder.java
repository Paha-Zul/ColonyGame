package com.mygdx.game.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonWriter;

/**
 * Created by Paha on 2/19/2015.
 */
public class DataBuilder {
    String filePath = "files/";
    String itemPath = "items.json";
    String resourcePath = "resources.json";

    public DataBuilder(){

        buildItems();
    }

    private void buildItems() {
        Json json = new Json();
        json.setTypeName(null);
        json.setUsePrototypes(false);
        json.setIgnoreUnknownFields(true);
        json.setOutputType(JsonWriter.OutputType.json);

        JsonItems items = json.fromJson(JsonItems.class, Gdx.files.internal(filePath+itemPath));

    }

    private static class JsonItems{
        public Array<JsonItem> items;
    }

    private static class JsonItem{
        public String itemName, displayName, itemType, description;
    }
}
