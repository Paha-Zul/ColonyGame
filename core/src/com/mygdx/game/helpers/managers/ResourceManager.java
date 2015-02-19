package com.mygdx.game.helpers.managers;

import com.mygdx.game.component.Resource;

import java.util.HashMap;

/**
 * Created by Paha on 2/19/2015.
 */
public class ResourceManager {
    private static HashMap<String, Resource> resourceMap = new HashMap<>(20);

    static{

    }

    public static Resource getResourceByname(String name){
        Resource resource = resourceMap.get(name);
        if(resource == null)
            throw new RuntimeException("Resource of name '"+name+"' does not exist.");

        Resource tmpRes = new Resource(resource);

        return tmpRes;
    }

    public static boolean doesResourceExist(String name){
        if(resourceMap.get(name) == null)
            return false;

        return true;
    }

    public static void addResourceInstance(Resource resource){
        resourceMap.put(resource.getResourceName(), resource);
    }

}
