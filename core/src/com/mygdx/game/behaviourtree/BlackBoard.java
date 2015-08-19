package com.mygdx.game.behaviourtree;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.component.*;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.Grid;
import com.mygdx.game.util.ItemNeeded;
import com.mygdx.game.util.Tags;
import com.mygdx.game.util.timer.Timer;

import java.util.LinkedList;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class BlackBoard{
    public Grid.GridInstance colonyGrid;
    public Grid.Node targetNode;
    public Entity target; //TODO Maybe we should get rid of target? Or at least make the pathfinding not use target... it results in tons of hard to find bugs when targetNode is still set and it uses that instead of the intended target.
    public Enterable enterable;
    public LinkedList<Vector2> path;
    public Resource targetResource;
    public CraftingStation targetCraftingStation;

    //Related to gathering resources
    public Tags resourceTypeTags = new Tags("resource");

    //Transferring variables
    public final ItemTransfer itemTransfer = new ItemTransfer();

    //Consuming task stuff
    public String itemEffect;
    public int itemEffectAmount;

    //Related to idleTask jobs
    public float baseIdleTime = 2f;
    public float randomIdleTime = 2f;
    public int idleDistance = 1;

    //Moving stuff
    public float followDis = 0f;
    public int myDisToTarget = 0;

    //Attack stuff
    public float attackRange = 200f;
    public float disBeforeRepath = 5f;
    public float attackDamage = 10f;
    public Timer attackTimer = null;

    //Sleep
    public float timeToSleep = 5f;

    //Construction stuff
    public Constructable constructable;

    //My stuff
    public Inventory myInventory;
    public float moveSpeed = 100f;
    public BehaviourManagerComp myManager;

    //Random
    public int counter;

    //Getting stuff from colony
    public Class<?> clazzType;
    public String[] tagsToSearch;

    public BlackBoard() {

    }

    public Entity getTarget(){
        return target;
    }

    public static class ItemTransfer{
        public boolean takingReserved;  //If we are taking from a reserve
        public boolean reserveToTake;   //If we should be reserving the item to take.

        public Array<String> itemTypesToIgnore;
        public Array<ItemNeeded> itemsToTransfer;

        public Inventory toInventory;
        public Inventory fromInventory;

        public void reset(){
            this.takingReserved = false;

            this.itemsToTransfer = new Array<>();

            this.toInventory = this.fromInventory = null;
            this.itemTypesToIgnore = new Array<>();
        }
    }
}
