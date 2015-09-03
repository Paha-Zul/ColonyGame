package com.mygdx.game.component;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.util.DataBuilder;
import com.mygdx.game.util.managers.DataManager;
import com.mygdx.game.util.managers.EventSystem;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.HashMap;

/**
 * Created by Paha on 5/25/2015.
 */
public class Equipment extends Component{

    @JsonIgnore
    DataBuilder.JsonItem head, body, arms, hands, feet;
    @JsonIgnore
    HashMap<String, DataBuilder.JsonTool> tools = new HashMap<>();

    public Equipment() {
        super();
    }

    @Override
    public void start() {
        super.start();

        //When an item is added to the inventory of this Component's Entity owner, check if it's a tool.
        //If so, add it to our tools.
        EventSystem.onEntityEvent(this.owner, "added_item", (args) -> {
            DataBuilder.JsonItem item = (DataBuilder.JsonItem)args[0];
            int amt = (int)args[1];

            //Cast to a tool and add it.
            if(item.getItemType().equals("tool")){
                DataBuilder.JsonTool tool = (DataBuilder.JsonTool)item;
                tools.put(tool.getItemName(), tool);
            }
        });

        //When an item is added to the inventory of this Component's Entity owner, check if it's a tool.
        //If so, add it to our tools.
        EventSystem.onEntityEvent(this.owner, "removed_item", (args) -> {
            DataBuilder.JsonItem item = (DataBuilder.JsonItem)args[0];
            int amt = (int)args[1];

            //Remove the tool.
            if(item.getItemType().equals("tool"))
                tools.remove(item.getItemName());
        });

        this.setActive(false);
    }

    public boolean hasTool(String toolName){
        return this.tools.containsKey(toolName);
    }

    public boolean hasTools(){
        return this.tools.size() > 0;
    }

    @JsonProperty("toolNames")
    public String[] getToolNames(){
        Array<String> toolNames = new Array<>();
        for(DataBuilder.JsonTool tool : tools.values())
            toolNames.add(tool.getItemName());
        return toolNames.toArray(String.class);
    }

    @JsonProperty("toolNames")
    private void setToolNames(String[] toolNames){
        for(String tool : toolNames){
            DataBuilder.JsonItem item = DataManager.getData(tool, DataBuilder.JsonItem.class);
            tools.put(item.getItemName(), (DataBuilder.JsonTool)item);
        }
    }
}
