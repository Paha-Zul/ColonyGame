package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Color;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.PrebuiltTasks;
import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.DataBuilder;
import com.mygdx.game.helpers.EventSystem;
import com.mygdx.game.helpers.Tree;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.helpers.managers.DataManager;
import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.helpers.timer.Timer;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.interfaces.IOwnable;
import com.mygdx.game.ui.PlayerInterface;

import java.util.function.Consumer;

/**
 * Created by Paha on 1/17/2015.
 */
public class Colonist extends Component implements IInteractable, IOwnable{
    private Colony colony;
    private Inventory inventory;
    private Stats stats;
    private Skills skills;
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

//        getBehManager().getBehaviourStates().addState("idle", (BlackBoard blackBoard, BehaviourManagerComp behComp) -> PrebuiltTasks::idleTask);

        this.inventory = this.getComponent(Inventory.class);
        this.stats = this.getComponent(Stats.class);
        this.skills = this.getComponent(Skills.class);
        this.manager = this.getComponent(BehaviourManagerComp.class);
        this.manager.getBlackBoard().moveSpeed = 200f;

        EventSystem.onEntityEvent(this.owner, "damage", onDamage);
        EventSystem.onEntityEvent(this.owner, "attacking_group", onAttackingEvent);

        this.createStats();
        this.createBehaviourButtons();
        this.createSkills();
    }

    //Creates the stats for this colonist.
    private void createStats(){
        //Create these 4 stats.
        Stats.Stat healthStat = stats.addStat("health", 100, 100);
        Stats.Stat foodStat = stats.addStat("food", 100, 100);
        Stats.Stat waterStat = stats.addStat("water", 20, 100);
        stats.addStat("energy", 100, 100).color = Color.YELLOW;

        healthStat.color = Color.GREEN;
        foodStat.color = Color.RED;
        waterStat.color = Color.CYAN;

        foodStat.effect = "feed";
        waterStat.effect = "thirst";

        //Add some timers.
        //Subtract food every 5 seconds and try to eat when it's too low.
        stats.addTimer(new RepeatingTimer(5f, () -> {
            foodStat.addToCurrent(-1);
            //If under 20, try to eat.
            if(foodStat.getCurrVal() <= 20) {
                getBehManager().getBlackBoard().itemEffect = "feed";
                getBehManager().getBlackBoard().itemEffectAmount = 1;
                getBehManager().changeTaskQueued("consume");
            }
        }));

        //Subtract water every 10 seconds and try to drink when it's too low.
        stats.addTimer(new RepeatingTimer(10f, () -> {
            waterStat.addToCurrent(-1);
            //If under 20, try to drink.
            if (waterStat.getCurrVal() <= 20) {
                getBehManager().getBlackBoard().itemEffect = "thirst";
                getBehManager().getBlackBoard().itemEffectAmount = 1;
                getBehManager().changeTaskQueued("consume");
            }
        })); //Subtract water every 10 seconds.

        //If food or water is 0, subtract health.
        Timer timer = stats.addTimer(new RepeatingTimer(5f, null));
        timer.setCallback(() -> {
            //If out of food OR water, degrade health.
            if (stats.getStat("food").getCurrVal() <= 0 || stats.getStat("water").getCurrVal() <= 0) {
                stats.getStat("health").addToCurrent(-1);
                timer.setLength(5f);
                //If we have both food AND water, improve health.
            } else if (stats.getStat("food").getCurrVal() > 0 && stats.getStat("water").getCurrVal() > 0) {
                stats.getStat("health").addToCurrent(1);
                timer.setLength(10f);
            }
        });

        healthStat.onZero = onZero;
    }

    //Creates all the buttons for the colonists behaviours.
    private void createBehaviourButtons(){
        this.getBehManager().getBlackBoard().attackDamage = 30f;
        this.getBehManager().getBlackBoard().attackRange = 500f;

        Tree taskTree = this.getBehManager().getTaskTree();

        Tree.TreeNode[] nodeList = taskTree.addNode("root", "gather", "hunt", "explore", "idle");

        //For each node, we set up a TaskInfo object and assign it to the node's userData field.
        for(Tree.TreeNode node : nodeList) {
            BehaviourManagerComp.TaskInfo taskInfo = new BehaviourManagerComp.TaskInfo(node.nodeName);
            taskInfo.callback = () -> getBehManager().changeTaskImmediate(node.nodeName);
            taskInfo.userData = DataManager.getData(node.nodeName+"Style", GUI.GUIStyle.class);
            if(taskInfo.userData == null) taskInfo.userData = DataManager.getData("blankStyle", GUI.GUIStyle.class);

            node.userData = taskInfo;
        }

        nodeList = taskTree.addNode("gather", "food", "water", "herbs", "wood", "stone", "metal");

        for(Tree.TreeNode node : nodeList) {
            BehaviourManagerComp.TaskInfo taskInfo = new BehaviourManagerComp.TaskInfo(node.nodeName);
            taskInfo.callback = () -> {
                taskInfo.active = !taskInfo.active;
                getBehManager().getBlackBoard().resourceTypeTags.toggleTag(node.nodeName);
            };
            taskInfo.userData = DataManager.getData("blankStyle", GUI.GUIStyle.class);

            node.userData = taskInfo;
        }

        getBehManager().getBehaviourStates().addState("gather", false, PrebuiltTasks::gatherResource);
        getBehManager().getBehaviourStates().addState("explore", false, PrebuiltTasks::exploreUnexplored);
        getBehManager().getBehaviourStates().addState("consume", false, PrebuiltTasks::consumeTask);

        EventSystem.onEntityEvent(this.owner, "task_started", args -> {
            Task task = (Task)args[0];
            if(task.getName().equals("exploreUnexplored")) task.getBlackboard().target = this.getColony().getEntityOwner();
        });
    }

    private void createSkills(){
        this.skills.addSkill("foraging", 10, 100, 2.25f, 0.1f);
        this.skills.addSkill("lumberjack", 10, 100, 2.25f, 0.1f);
        this.skills.addSkill("hunting", 10, 100, 2.25f, 0.1f);
        this.skills.addSkill("exploration", 10, 100, 2.25f, 1.5f);
        this.skills.addSkill("butchering", 10, 100, 2.25f, 0.1f);
        this.skills.addSkill("mining", 10, 100, 2.25f, 0.1f);
    }

    private Consumer<Object[]> onDamage = args -> {
        Entity entity = (Entity) args[0];
        float amount = (float) args[1];

        Stats.Stat health = this.stats.getStat("health");
        if (health == null) return;

        health.addToCurrent(amount);
        if(this.manager != null) {
            this.manager.getBlackBoard().target = entity;
            this.manager.changeTaskImmediate("attackTarget");
        }
    };

    //The callback for when our health is 0.
    private Functional.Callback onZero = () -> {
        this.owner.getTags().removeTag("alive");
        this.owner.transform.setRotation(90f);
        this.stats.clearTimers();
        this.owner.internalDestroyComponent(BehaviourManagerComp.class);
        this.manager = null;
        GridComponent gridComp = this.getComponent(GridComponent.class);
        gridComp.setActive(false);
        ColonyGame.worldGrid.removeViewer(gridComp);
    };

    private Consumer<Object[]> onAttackingEvent = args -> {
        Group attackingGroup = (Group)args[0];
        if(attackingGroup.getLeader().getTags().hasTag("boss")) {
            DataBuilder.JsonPlayerEvent event = DataManager.getData("bossencounter", DataBuilder.JsonPlayerEvent.class);
            event.eventTarget = this.getEntityOwner();
            event.eventTargetOther = attackingGroup.getLeader();
            PlayerInterface.getInstance().newPlayerEvent(event);
        }
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
        this.getEntityOwner().name = firstName;
    }

    @Override
    public Skills getSkills() {
        return this.skills;
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
    public void addedToColony(Colony colony) {
        this.colony = colony;
    }

    @Override
    public Colony getOwningColony() {
        return this.colony;
    }

    @Override
    public void destroy(Entity destroyer) {
        super.destroy(destroyer);
    }
}
