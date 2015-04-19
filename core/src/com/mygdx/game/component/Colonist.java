package com.mygdx.game.component;

import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.EventSystem;
import com.mygdx.game.helpers.StringTable;
import com.mygdx.game.helpers.Tree;
import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.helpers.timer.Timer;
import com.mygdx.game.interfaces.IInteractable;

import java.util.function.Consumer;

/**
 * Created by Paha on 1/17/2015.
 */
public class Colonist extends Component implements IInteractable{
    private Colony colony;
    private Inventory inventory;
    private Stats stats;
    private BehaviourManagerComp manager;
    private String firstName, lastName;

    public Colonist() {
        super();
        this.setActive(false);
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void start() {
        super.start();

        this.inventory = this.getComponent(Inventory.class);
        this.stats = this.getComponent(Stats.class);
        this.manager = this.getComponent(BehaviourManagerComp.class);
        this.manager.getBlackBoard().moveSpeed = 200f;

        EventSystem.registerEntityEvent(this.owner, "damage", onDamage);

        this.createStats();
        this.createBehaviourButtons();
    }

    private void createStats(){
        stats.addStat("health", 100, 100);
        stats.addStat("food", 100, 100);
        stats.addStat("water", 20, 100);
        stats.addStat("energy", 100, 100);

        stats.addTimer(new RepeatingTimer(5f, () -> stats.getStat("food").addToCurrent(-1))); //Subtract food every 5 seconds
        stats.addTimer(new RepeatingTimer(10f, () -> stats.getStat("water").addToCurrent(-1))); //Subtract water every 10 seconds.

        //If food or water is 0, subtract health.
        Timer timer = stats.addTimer(new RepeatingTimer(5f, null));
        timer.setCallback(() -> {
            if (stats.getStat("food").getCurrVal() <= 0 || stats.getStat("water").getCurrVal() <= 0) {
                stats.getStat("health").addToCurrent(-1);
                timer.setLength(5f);
            }else if(stats.getStat("food").getCurrVal() > 0 && stats.getStat("water").getCurrVal() > 0){
                stats.getStat("health").addToCurrent(1);
                timer.setLength(10f);
            }
        });
    }

    //Creates all the buttons for the colonists behaviours.
    private void createBehaviourButtons(){
        this.getBehManager().getBlackBoard().attackDamage = 30f;
        this.getBehManager().getBlackBoard().attackRange = 500f;

        Tree taskTree = this.getBehManager().getTaskTree();

        Tree.TreeNode[] nodeList = taskTree.addNode("root", "gather", "hunt", "explore");

        for(Tree.TreeNode node : nodeList) {
            BehaviourManagerComp.TaskState taskState = new BehaviourManagerComp.TaskState(node.nodeName);
            taskState.callback = () -> getBehManager().changeTask(node.nodeName);
            node.userData = taskState;
        }

        nodeList = taskTree.addNode("gather", "food", "water", "herbs", "wood");

        for(Tree.TreeNode node : nodeList) {
            BehaviourManagerComp.TaskState taskState = new BehaviourManagerComp.TaskState(node.nodeName);
            taskState.callback = () -> {
                taskState.toggled = !taskState.toggled;
                getBehManager().getBlackBoard().resourceTypeTags.toggleTag(StringTable.getString("resource_type", node.nodeName));
            };
            node.userData = taskState;
        }
    }

    private Consumer<Object[]> onDamage = args -> {
        Entity entity = (Entity) args[0];
        float amount = (float) args[1];

        Stats.Stat stat = this.stats.getStat("health");
        if (stat == null) return;

        stat.addToCurrent(amount);
        this.manager.getBlackBoard().target = entity;
        this.getBehManager().attack();
    };

    public Colony getColony() {
        return colony;
    }

    public void setColony(Colony colony) {
        this.colony = colony;
    }

    public void setName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public Skills getSkills() {
        return null;
    }

    @Override
    public String getName() {
        return this.firstName;
    }

    @Override
    public Inventory getInventory(){
        return this.inventory;
    }

    @Override
    public Stats getStats(){
        return this.stats;
    }

    @Override
    public String getStatsText() {
        return null;
    }

    @Override
    public BehaviourManagerComp getBehManager() {
        return this.manager;
    }

    @Override
    public void destroy() {
        super.destroy();
    }
}
