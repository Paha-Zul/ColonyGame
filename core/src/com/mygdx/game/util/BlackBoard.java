package com.mygdx.game.util;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.component.BehaviourManagerComp;
import com.mygdx.game.component.Inventory;
import com.mygdx.game.component.Resource;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.util.timer.Timer;

import java.util.LinkedList;

/**
 * Created by Bbent_000 on 12/31/2014.
 */
public class BlackBoard{
    public Grid.GridInstance colonyGrid;
    public Grid.Node targetNode;
    public Entity target;
    public LinkedList<Vector2> path;
    public Resource targetResource;

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

    //My stuff
    public Inventory myInventory;
    public float moveSpeed = 100f;
    public BehaviourManagerComp myManager;

    //Random
    public int counter;

    public BlackBoard() {

    }

    public Entity getTarget(){
        return target;
    }

    public static class ItemTransfer{
        public boolean transferAll;
        public boolean transferAmount;
        public boolean transferMany;
        public boolean takingReserved;

        public int itemAmountToTransfer;

        public String itemNameToTransfer;
        public Array<String> itemNamesToTransfer;
        public Array<Integer> itemAmountsToTransfer;

        public Inventory toInventory;
        public Inventory fromInventory;

        public void reset(){
            transferAll = transferAmount = transferMany = takingReserved = false;
            itemAmountToTransfer = 0;

            itemNamesToTransfer = null;
            itemAmountsToTransfer = null;
            itemNameToTransfer = null;

            toInventory = fromInventory = null;
        }
    }
}
