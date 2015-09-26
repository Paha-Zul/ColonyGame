package com.mygdx.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Component;
import com.mygdx.game.entity.Entity;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

public class SaveGameHelper {
    public static JsonWorld world = new JsonWorld();

    private static ObjectMapper mapper;

    private static TLongObjectHashMap<Entity> giantEntityMap = new TLongObjectHashMap<>(10000, 0.75f);
    private static TLongObjectHashMap<Component> giantCompMap = new TLongObjectHashMap<>(100000, 0.75f);

    static{
        mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        mapper.addMixIn(Vector2.class, Vector2MixIn.class);
    }

    public static void saveWorld() {
        Json json = new Json();

        //mapper.getSerializationConfig().addMixInAnnotations(Array.class, MixIn.class);
        //mapper.getSerializationConfig().addMixInAnnotations(Vector2.class, MixIn.class);

        getJsonEntities();

        FileHandle file = Gdx.files.local("game.sav");
        file.writeString("", false); //Clear the file.

        Runnable task = () -> {
            try {
                writeFile(file, mapper.writerWithDefaultPrettyPrinter().writeValueAsString(world));
                world.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        ColonyGame.threadPool.submit(task);

        //writeFile(file, json.toJson(entComps, JsonComponents[].class));
        //writeFile(file, json.toJson(allComps, Component[].class));
    }

    private static void getJsonEntities(){
        world.clear();
        for(Array<Entity> list : ListHolder.getEntityList())
            for(Entity ent : list) {
                world.entities.add(new JsonEntity(ent, ent.getComponents().getComponentIDs()));
                Collections.addAll(world.allComps, ent.getComponents().getAllComponents());
            }
    }

    public static void writeFile(FileHandle file, String s) {
        //file.writeString(com.badlogic.gdx.utils.Base64Coder.encodeString(s), false);
        file.writeString(s, true);
        //file.writeBytes(s.getBytes(Charset.forName("UTF-8")), true);

        Vector2 vect;
    }

    public static void loadWorld(){
        loadWorld(Gdx.files.local("game.sav"));
    }

    private static void loadWorld(FileHandle file) {
        String save = readFile(file);
        JsonWorld world = new JsonWorld();
        ListHolder.clearEntityList();

        try {
            world = mapper.readValue(file.file(), JsonWorld.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        //Make the three giant hash maps.
        System.out.println("Done!");
        for(Component comp : world.allComps) giantCompMap.put(comp.getCompID(), comp);
        for(JsonEntity ent : world.entities) giantEntityMap.put(ent.entity.getID(), ent.entity);

        //Clear everything from the world.
        System.out.println("Done2!");
        System.out.println("Done3!");

        //Load it all back in (sync as we go!).
        for(JsonEntity ent : world.entities){
            if(ent.entity.getTags().hasTag("colonist")){
                System.out.println("colo");
            }
            //ent.getComponents().transform = (Transform)giantCompMap.get(ent.getTrasnformID());
            //if(ent.getGraphicIdentityID() != 0) ent.getComponents().identity = (GraphicIdentity)giantCompMap.get(ent.getGraphicIdentityID());
            for(long id : ent.compIDs){
                Component comp = giantCompMap.get(id);
                comp.setOwner(ent.entity);
                if(ent.entity == null)
                    System.out.println("YEA");
                ent.entity.addComponent(comp);
                comp.initLoad();
            }

            ent.entity.load(); //Load the entity.
            ent.entity.getComponents().iterateOverComponents(Component::load); //Load all the components on the Entity.
            ListHolder.addEntity(ent.entity);
        }

        System.out.println("Done4!");
    }

    public static String readFile(FileHandle file) {
        if (file != null && file.exists()) {
            String s = file.readString();
            if (!s.isEmpty()) {
                return s;
            }
        }
        return "";
    }

    /**
     * This interface is to mix in some rules for the Vector2 class. Since we don't have control of the source,
     * we use this mixin to tell Jackson to serialize all fields (because Vector2 only has an X and Y field and we want both)
     */
    @JsonAutoDetect(setterVisibility = JsonAutoDetect.Visibility.NONE,
            getterVisibility = JsonAutoDetect.Visibility.NONE,
            isGetterVisibility = JsonAutoDetect.Visibility.NONE,
            creatorVisibility = JsonAutoDetect.Visibility.NONE,
            fieldVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
    @JsonIgnoreProperties({"X", "Y", "Zero", "zero"})
    private interface Vector2MixIn{}

    private static class JsonEntity{
        @JsonProperty
        public Entity entity;
        @JsonProperty
        public Long[] compIDs;

        public JsonEntity(){

        }

        public JsonEntity(Entity entity, Long[] compIDs){
            this.entity = entity;
            this.compIDs = compIDs;
        }
    }

    private static class JsonWorld{
        @JsonProperty
        public ArrayList<JsonEntity> entities = new ArrayList<>();
        @JsonProperty
        public ArrayList<Component> allComps = new ArrayList<>();

        public JsonWorld(){

        }

        public void clear(){
            entities = new ArrayList<>();
            allComps = new ArrayList<>();
        }
    }
}
