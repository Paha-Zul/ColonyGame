package com.mygdx.game.helpers;

        import com.mygdx.game.behaviourtree.Task;
        import com.mygdx.game.behaviourtree.action.FindPath;
        import com.mygdx.game.behaviourtree.action.MoveTo;
        import com.mygdx.game.behaviourtree.composite.Sequence;
        import com.mygdx.game.behaviourtree.control.ParentTaskController;

        import java.util.HashMap;

/**
 * Created by Paha on 1/24/2015.
 */
public class BehaviourManager {
    private static HashMap<String, Task> taskMap = new HashMap<>();

    static{
        buildTasks();
    }

    public static Task GetTaskByName(String name){
        return taskMap.get(name);
    }

    private static void buildTasks(){
        MoveTo();
    }

    private static void MoveTo(){
        //Get the target node/Entity.
        //Find the path.
        //Move to the target.

        Sequence sequence = new Sequence("MoveTo", null);

        FindPath findPath = new FindPath("FindPath", null);
        MoveTo followPath = new MoveTo("FollowPath", null);

        ((ParentTaskController)(sequence.getControl())).addTask(findPath);
        ((ParentTaskController)(sequence.getControl())).addTask(followPath);

        taskMap.put("MoveTo", sequence);

    }
}
