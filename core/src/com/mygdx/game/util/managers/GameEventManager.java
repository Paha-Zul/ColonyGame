package com.mygdx.game.util.managers;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.behaviourtree.BlackBoard;
import com.mygdx.game.component.BehaviourManagerComp;
import com.mygdx.game.component.Component;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.GH;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Paha on 5/24/2015.
 * Manages GameEvents for Entities.
 */
public class GameEventManager {
    private static HashMap<String, GameEvent> eventMap = new HashMap<>(10);
    private static HashMap<String, GameEvent> eventTargetToEventMap = new HashMap<>();

    /**
     * Adds a GameEvent to this manager.
     * @param gameEvent The GameEvent to add.
     */
    public static void addGameEvent(DataBuilder.JsonGameEvent gameEvent){
        GameEvent newGameEvent = new GameEvent(gameEvent);
        eventMap.put(gameEvent.eventName, newGameEvent);
        if(gameEvent.eventTarget != null) eventTargetToEventMap.put(gameEvent.eventTarget, newGameEvent);
    }

    /**
     * Triggers a GameEvent, setting the eventTarget and eventTargetOther as null.
     * @param name The name of the event.
     * @return The GameEvent that was triggered.
     */
    public static GameEvent triggerGameEvent(String name){
        return triggerGameEvent(name, null, null);
    }

    /**
     * Triggers a GameEvent by a name. Does nothing if the GameEvent is already triggered but still returns it.
     * @param name The name of the GameEvent.
     * @param eventTarget The Entity target of the Event, ie: A miner has gone crazy! (the miner is the target)
     * @param otherTarget The other target of the Event. A Colonist has encountered the Big Bad Wolf! (Big Bad Wolf is the other target).
     * @return The GameEvent that was triggered.
     */
    public static GameEvent triggerGameEvent(String name, Entity eventTarget, Entity otherTarget){
        GameEvent event =  eventMap.get(name);
        event.entityTargetTeams = new Array<>();
        if(!event.triggered) {
            event.triggered = true;
            Array<Entity> list1 = new Array<>();
            Array<Entity> list2 = new Array<>();
            list1.add(eventTarget);
            list1.add(otherTarget);
            event.entityTargetTeams.add(list1);
            event.entityTargetTeams.add(list2);
        }
        return event;
    }

    /**
     * Triggers a GameEvent, setting the eventTargetOther as null.
     * @param name The name of the event.
     * @param eventTarget The Entity target of the Event, ie: A miner has gone crazy! (the miner is the target)
     * @return The GameEvent that was triggered.
     */
    public static GameEvent triggerGameEvent(String name, Entity eventTarget){
        return triggerGameEvent(name, eventTarget, null);
    }

    /**
     * Triggers a GameEvent by a name. Does nothing if the GameEvent is already triggered but still returns it.
     * @param name The name of the GameEvent.
     * @param entityLists Lists of entities to be used.
     * @return The GameEvent that was triggered.
     */
    public static GameEvent triggerGameEvent(String name, Array<Entity>... entityLists){
        GameEvent event =  eventMap.get(name);
        event.entityTargetTeams = new Array<>();
        int randTargetAmount;

        if(!event.triggered) {

            for(int i=0;i<event.gameEventData.sides;i++){
                Array<Entity> list = entityLists[i];
                if(event.gameEventData.randRanges.length > i)
                    randTargetAmount = GH.getRandRange(event.gameEventData.randRanges[i][0], event.gameEventData.randRanges[i][1]);
                else randTargetAmount = 1;

                event.entityTargetTeams.add(getRandomEntries(randTargetAmount, list));
            }

            event.triggered = true;
        }

        return event;
    }

    /**
     * Takes a passed in list and grabs a number of random entries in the list and returns as a new list.
     * @param amount The amount of items to pull from the list.
     * @param list The list of values. Each element added to the list that is returned is removed from this list, so it is modified.
     * @return A new list containing random entries from the list.
     */
    private static Array<Entity> getRandomEntries(int amount, Array<Entity> list){
        if(list.size <= amount) return list;

        Array<Entity> newList = new Array<>();
        for(int i=0;i<amount;i++) newList.add(list.removeIndex(MathUtils.random(list.size - 1)));

        return newList;
    }

    /**
     * Triggers a GameEvent by a name. Does nothing if the GameEvent is already triggered but still returns it.
     * @param name The name of the GameEvent.
     * @param componentList A list of lists of Components to get the Entity owner from.
     * @return The GameEvent that was triggered.
     */
    public static GameEvent triggerGameEventByComponents(String name, Array<Component>... componentList){
        GameEvent event =  eventMap.get(name);
        event.entityTargetTeams = new Array<>();
        int randTargetAmount;

        if(!event.triggered) {
            //For each side, get some random Entities.
            for(int i=0;i<event.gameEventData.sides;i++){
                Array<Component> list;
                //If we have more than one list, get the ith list.
                if(componentList.length > 1) list = componentList[i];
                else list = componentList[0]; //Otherwise, always get 0 cause we are using one list.

                //If we have a range to random between, then get a random range! Otherwise, use 1.
                if(event.gameEventData.randRanges.length > i)
                    randTargetAmount = GH.getRandRange(event.gameEventData.randRanges[i][0], event.gameEventData.randRanges[i][1]);
                else randTargetAmount = 1;

                event.entityTargetTeams.add(getRandomEntriesByComponent(randTargetAmount, list));
            }

            event.triggered = true;
        }

        return event;
    }

    /**
     * Takes a passed in list and grabs a number of random entries in the list and returns as a new list.
     * @param amount The amount of items to pull from the list.
     * @param list The list of values. Each element added to the list that is returned is removed from this list, so it is modified.
     * @return A new list containing random entries from the list.
     */
    private static <T extends Component> Array<Entity> getRandomEntriesByComponent(int amount, Array<T> list){
        Array<Entity> newList = new Array<>();
        //TODO What happens if the list is empty?

        if(list.size <= amount){
            list.forEach(comp -> newList.add(comp.getEntityOwner()));
            return newList;
        }

        for(int i=0;i<amount;i++)
            newList.add(list.removeIndex(MathUtils.random(list.size - 1)).getEntityOwner());

        return newList;
    }

    /**
     * Triggers an event.
     * @param name The name of the event.
     * @param singleList A single list to pull Entities from.
     * @return The GameEvent that was triggered.
     */
    public static GameEvent triggerGameEvent(String name, Array<Entity> singleList){
        GameEvent event =  eventMap.get(name);
        event.entityTargetTeams = new Array<>();
        int randTargetAmount=0;

        if(!event.triggered) {

            for(int i=0;i<event.gameEventData.sides;i++){
                if(event.gameEventData.randRanges.length > i)
                    randTargetAmount = GH.getRandRange(event.gameEventData.randRanges[i][0], event.gameEventData.randRanges[i][1]);
                else randTargetAmount = 1;

                //We get some random Entities from a list and add them as a new list.
                event.entityTargetTeams.add(getRandomEntries(randTargetAmount, singleList));
            }

            event.triggered = true;
        }

        return event;
    }

    /**
     * Sets the field data for a particular group.
     * @param fieldDataList The list of fields and data to set for a particular group.
     * @param group The group that the fields will be applying to.
     */
    public static void setFields(String[][] fieldDataList, Array<Entity> group){
        //For each Entity in the group, get it's blackboard and apply the variable
        group.forEach(ent -> {
            BlackBoard blackBoard = ent.getComponent(BehaviourManagerComp.class).getBlackBoard();
            //The second dimension is the variable combo, third dimension contains the name and value of the variable.
            for (String[] fieldData : fieldDataList) {
                String name = fieldData[0];
                String type = fieldData[1];
                String value = fieldData[2];

                try {
                    Field field = blackBoard.getClass().getDeclaredField(name);//Get the field.
                    //If we are not setting a string, it must be a number.
                    if (!type.equals("String")) {
                        if(type.equals("int")) field.set(blackBoard, Integer.parseInt(value));
                        if(type.equals("double")) field.set(blackBoard, Double.parseDouble(value));
                    }
                    //If we failed the double parse, set it as a string.
                    else field.set(blackBoard, value);

                } catch (NoSuchFieldException | IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void setFields(String[][] variableData, BlackBoard blackBoard){
        //The second dimension is the variable combo, third dimension contains the name and value of the variable.
        for (String[] variable : variableData) {
            String name = variable[0]; //Name of the field.
            String value = variable[1]; //Value of the field.

            try {
                Field field = blackBoard.getClass().getDeclaredField(name);//Get the field.
                //Here we try to parse to a double first. The .set() method will convert the double into whatever variable is needed.
                //This basically tests if its a number first.
                if (GH.tryParseDouble(value)) field.set(blackBoard, Double.parseDouble(value));

                    //If we failed the double parse, set it as a string.
                else field.set(blackBoard, value);

            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Gets a GameEvent from this manager.
     * @param name The name of the event to get.
     * @return The GameEvent if found, null otherwise.
     */
    public static GameEvent getGameEvent(String name){
        return eventMap.get(name);
    }

    public static boolean isGameEventTriggered(String name){
        return eventMap.get(name).triggered;
    }

    /**
     * Generates a description using the data from the GameEvent passed in,
     * @param event The GameEvent to use for data.
     * @return A String which is the GameEvent's description.
     */
    public static String generateEventDescription(GameEventManager.GameEvent event){
        //TODO Fix this!
        StringBuilder builder = new StringBuilder();
        for(String desc : event.gameEventData.eventDescription) builder.append(desc);

        String original = builder.toString();
        builder.setLength(0);
        String[] tokens = original.split(" ");

        for (String token : tokens) {
            if(token.equals("%et")) token = event.entityTargetTeams.get(0).get(0).name;
            else if(token.equals("%eot")) token = event.entityTargetTeams.get(1).get(0).name;
            else if(token.matches("%lie\\d")) {
                // the pattern we want to search for
                Pattern p = Pattern.compile("\\d+");
                Matcher m = p.matcher(token);

                // if we find a match, get the group
                if (m.find()){
                    // we're only looking for one group, so get it
                    String num = m.group(0);
                    token = buildListOfNames(event.entityTargetTeams.get(Integer.parseInt(num)));
                }else
                    token = buildListOfNames(event.entityTargetTeams.get(0));
            }

            builder.append(token).append(" ");
        }

        return builder.toString();
    }

    /**
     * Takes a list of Entites and builds their names into a well structured list (Tom and Betty or Tom, Will, and Betty)
     * @param list The list of Entities to get names from.
     * @return A String of the names.
     */
    private static String buildListOfNames(Array<Entity> list){
        if(list == null || list.size == 0) return null;

        StringBuilder listOfTargets = new StringBuilder("");
        int length = list.size;

        //If we have more than 2...
        if(list.size > 2) {
            for (int i = 0; i < length; i++) {
                Entity target = list.get(i);
                listOfTargets.append(target.name);
                if (i < length - 1) listOfTargets.append(", ");
                if (i == length - 2) listOfTargets.append("and ");
            }

        //If we have exactly 2...
        }else if(list.size == 2){
           listOfTargets.append(list.get(0).name).append(" and ").append(list.get(1).name);

        //Otherwise we have 1...
        }else{
            listOfTargets.append(list.get(0).name);
        }

        return listOfTargets.toString();
    }

    public static class GameEvent{
        public DataBuilder.JsonGameEvent gameEventData;
        public boolean triggered = false;
        public Array<Array<Entity>> entityTargetTeams;

        public GameEvent(DataBuilder.JsonGameEvent gameEventData){
            this.gameEventData = gameEventData;
        }
    }
}
