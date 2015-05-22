package com.mygdx.game.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.Component;
import com.mygdx.game.entity.Entity;
import gnu.trove.map.hash.TLongObjectHashMap;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.introspect.VisibilityChecker;

import java.io.IOException;
import java.util.ArrayList;

public class SaveGameHelper {

    //Information about components linked to an entity.
    @JsonIgnoreProperties({"size", "ordered", "iterable", "predicateIterable"})
    public static class JsonComponents{
        @JsonProperty
        long entityID;
        @JsonProperty
        ArrayList<Long> newComponentList = new ArrayList<>();
        @JsonProperty
        ArrayList<Long> activeComponentList = new ArrayList<>();
        @JsonProperty
        ArrayList<Long> inactiveComponentList = new ArrayList<>();
        @JsonProperty
        ArrayList<Long> scalableComponents = new ArrayList<>();
        @JsonProperty
        ArrayList<Long> destroyComponentList = new ArrayList<>();

        public JsonComponents() {

        }
    }

    private static class JsonWorld{
        @JsonProperty
        public ArrayList<Entity> entities;
        @JsonProperty
        public ArrayList<JsonComponents> entComps;
        @JsonProperty
        public ArrayList<Component> allComps;

        public JsonWorld(){

        }

        public void clear(){
            entities = null;
            entComps = null;
            allComps = null;
        }
    }

    private static class LoadedJsonWorld{
        @JsonProperty
        public ArrayList<Entity> entities;
        @JsonProperty
        public ArrayList<JsonComponents> entComps;
        @JsonProperty
        public ArrayList<Component> allComps;

        public LoadedJsonWorld(){

        }

        public void clear(){
            entities = null;
            entComps = null;
            allComps = null;
        }
    }

    public static JsonWorld world = new JsonWorld();

    private static ObjectMapper mapper;

    private static TLongObjectHashMap<Entity> giantEntityMap = new TLongObjectHashMap<>(10000, 0.75f);
    private static TLongObjectHashMap<Component> giantCompMap = new TLongObjectHashMap<>(100000, 0.75f);
    private static TLongObjectHashMap<JsonComponents> giantCompContainerMap = new TLongObjectHashMap<>(10000, 0.75f);

    public static void saveWorld() {
        Json json = new Json();
        mapper = new ObjectMapper();
        mapper.setVisibilityChecker(VisibilityChecker.Std.defaultInstance().withFieldVisibility(JsonAutoDetect.Visibility.ANY).withGetterVisibility(JsonAutoDetect.Visibility.NONE));
        mapper.getSerializationConfig().addMixInAnnotations(Array.class, MixIn.class);
        mapper.getSerializationConfig().addMixInAnnotations(Vector2.class, MixIn.class);

        jsonEntities();

        FileHandle file = Gdx.files.local("game.sav");
        file.writeString("", false); //Clear the file.

        Runnable task = () -> {
            try {
                writeFile(file, mapper.defaultPrettyPrintingWriter().writeValueAsString(world));
                world.clear();
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        ColonyGame.threadPool.submit(task);

        //writeFile(file, json.toJson(entComps, JsonComponents[].class));
        //writeFile(file, json.toJson(allComps, Component[].class));
    }

    private static void jsonEntities(){
        ArrayList<Entity> entList = new ArrayList<>(100); //The entity list.
        ArrayList<JsonComponents> entCompList = new ArrayList<>(100); //The component lists for each entity.
        ArrayList<Component> compList = new ArrayList<>(100); //All of the existing components in the game.

        for(Array<Entity> list : ListHolder.getEntityList())
            for(Entity ent : list) {
                entList.add(ent);

                Entity.Components comps = ent.getComponents(); //Get the components object from the entity.
                JsonComponents entComps = new JsonComponents(); //Holds minimal information about the components object.
                entComps.entityID = ent.getID();

                /*
                * For each list of components, lets store them... This stores them by ID for relinking later.
                */

                for(Component comp : comps.getNewComponentList()) {
                    entComps.newComponentList.add(comp.getCompID()); //Store each by ID.
                    compList.add(comp); //Add the comp to this list.
                }
                entComps.newComponentList.trimToSize();

                for(Component comp : comps.getActiveComponentList()) {
                    entComps.activeComponentList.add(comp.getCompID()); //Store each by ID.
                    compList.add(comp); //Add the comp to this list.
                }
                entComps.activeComponentList.trimToSize();

                for(Component comp : comps.getInactiveComponentList()) {
                    entComps.inactiveComponentList.add(comp.getCompID()); //Store each by ID.
                    compList.add(comp); //Add the comp to this list.
                }
                entComps.inactiveComponentList.trimToSize();

                for(Component comp : comps.getDestroyComponentList()) {
                    entComps.destroyComponentList.add(comp.getCompID()); //Store each by ID.
                    compList.add(comp); //Add the comp to this list.
                }
                entComps.destroyComponentList.trimToSize();

                for(Component comp : comps.getScalableComponents()) {
                    entComps.scalableComponents.add(comp.getCompID()); //Store each by ID.
                    compList.add(comp); //Add the comp to this list.
                }
                entComps.scalableComponents.trimToSize();

                entCompList.add(entComps);
            }

        world.entComps = entCompList;
        world.allComps = compList;
        world.entities = entList;
    }

    public static void loadWorld(){
        loadWorld(Gdx.files.local("game.sav"));
    }

    private static void loadWorld(FileHandle file) {
        String save = readFile(file);
        LoadedJsonWorld world = new LoadedJsonWorld();
        try {
            world = mapper.readValue(file.file(), LoadedJsonWorld.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Done!");
        for(Component comp : world.allComps) giantCompMap.put(comp.getCompID(), comp);
        for(JsonComponents comps : world.entComps) giantCompContainerMap.put(comps.entityID, comps);
        for(Entity ent : world.entities) giantEntityMap.put(ent.getID(), ent);
        System.out.println("Done2!");
    }

    public static void writeFile(FileHandle file, String s) {
        //file.writeString(com.badlogic.gdx.utils.Base64Coder.encodeString(s), false);
        file.writeString(s, true);
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

    private abstract class MixIn{

        @JsonIgnore int size;
        @JsonIgnore boolean ordered;
        @JsonIgnore boolean zero;
        @JsonIgnore boolean unit;
        @JsonIgnore Object iterable;
        @JsonIgnore Object predicateIterable;

        @JsonIgnore abstract int getSize();
        @JsonIgnore abstract boolean isOrdered();
        @JsonIgnore abstract boolean isZero();
        @JsonIgnore abstract boolean isUnit();
        @JsonIgnore abstract Object getIterable();
        @JsonIgnore abstract Object getPredicateIterable();
    }
}
