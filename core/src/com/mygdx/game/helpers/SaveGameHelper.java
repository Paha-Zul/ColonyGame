package com.mygdx.game.helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.entity.Entity;

public class SaveGameHelper {

    public static JsonContainer saveContainer = new JsonContainer();

    public static class JsonContainer {
        public Array<Array<Entity>> entityList;
    }

    public static class JsonWorld{
        public Array<Entity>[] lists;
        public String[] data;
    }


    public static void saveWorld() {
        JsonWorld world = new JsonWorld();
        Json json = new Json();

        Array<String> data = new Array<>();

        int counter = 0;
        StringBuilder builder = new StringBuilder();
        for(Array<Entity> list : saveContainer.entityList){
            for(Entity ent : list){
                builder.append(json.toJson(ent));
                counter++;
                if(counter > 1){
                    data.add(builder.toString());
                    builder.setLength(0);
                    counter=0;
                }
            }
        }

        world.data = data.toArray(String[].class);

        writeFile("game.sav", world.data);
    }

    public static JsonContainer loadWorld() {
        String save = readFile("game.sav");
        if (!save.isEmpty()) {

            Json json = new Json();
            JsonContainer jWorld = json.fromJson(JsonContainer.class, save);

            return saveContainer;
        }
        return null;
    }

    public static void writeFile(String fileName, String[] s) {
        FileHandle file = Gdx.files.local(fileName);
        //file.writeString(com.badlogic.gdx.utils.Base64Coder.encodeString(s), false);
        for(String data : s) {
            System.out.println("Length of string: " + data.length());
            file.writeString(data, true);
        }
    }

    public static String readFile(String fileName) {
        FileHandle file = Gdx.files.local(fileName);
        if (file != null && file.exists()) {
            String s = file.readString();
            if (!s.isEmpty()) {
                return s;
            }
        }
        return "";
    }
}
