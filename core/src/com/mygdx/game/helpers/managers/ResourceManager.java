package com.mygdx.game.helpers.managers;

import com.mygdx.game.component.Resource;
import com.mygdx.game.helpers.DataBuilder;

import java.util.HashMap;

/**
 * Created by Paha on 2/19/2015.
 */
public class ResourceManager {
    private static HashMap<String, DataBuilder.JsonResource> resourceMap = new HashMap<>(20);

    public static Resource getResourceByname(String name){
        DataBuilder.JsonResource resource = resourceMap.get(name);
        if(resource == null)
            throw new RuntimeException("Resource of name '"+name+"' does not exist.");

        return new Resource(resource);
    }

    public static DataBuilder.JsonResource getJsonResourceByName(String name){
        DataBuilder.JsonResource resource = resourceMap.get(name);
        if(resource == null)
            throw new RuntimeException("Resource of name '"+name+"' does not exist.");

        return resource;
    }

    public static boolean doesResourceExist(String name){
        return (resourceMap.get(name) == null);
    }

    public static void addResourceInstance(DataBuilder.JsonResource resource){
        resourceMap.put(resource.resourceName, resource);
    }

}
