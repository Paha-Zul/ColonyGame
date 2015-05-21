package com.mygdx.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.component.Component;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IScalable;

public class SaveGameHelper {

    public static JsonContainer saveContainer = new JsonContainer();

    public static class JsonContainer {
        public Array<Array<Entity>> entityList;
    }

    public static class JsonWorld{
        public Array<Entity>[] lists;
        public String[] data;
    }

    //Holds minimal/necessary information about an entity.
    public static class JsonEntity{
        String name;
        int drawLevel;
        boolean active, destroyed, setToDestroy;
        Tags tags;
        double ID;
    }

    //Information about components linked to an entity.
    public static class JsonComponents{
        Array<Component> newComponentList;
        Array<Component> activeComponentList;
        Array<Component> inactiveComponentList;
        Array<IScalable> scalableComponents;
        Array<Component> destroyComponentList;
        double entityID;
    }


    public static void saveWorld() {
        JsonWorld world = new JsonWorld();
        Json json = new Json();

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
