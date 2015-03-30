package com.mygdx.game.component;

import com.badlogic.gdx.math.Vector2;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.Grid;
import com.mygdx.game.interfaces.Functional;

import java.util.LinkedList;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class BlackBoard extends Component{
    public Grid.GridInstance colonyGrid;
    public Grid.Node targetNode;
    public Entity target;
    public LinkedList<Vector2> path;
    public Resource targetResource;

    //Transfering variables
    public boolean transferAll = false;
    public int takeAmount = 0;
    public Inventory toInventory;
    public Inventory fromInventory;
    public String itemNameToTake;

    //Related to idleTask jobs
    public float baseIdleTime = 2f;
    public float randomIdleTime = 2f;
    public int idleDistance = 1;

    //Attack stuff
    public float attackRange = 5f;
    public float disBeforeRepath = 5f;

    //My stuff
    public Inventory myInventory;
    public float moveSpeed = 100f;

    public BlackBoard() {

        this.setActive(false);
    }

    public Entity getTarget(){
        return target;
    }
}
