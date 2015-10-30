package com.mygdx.game.component;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.Task;
import com.mygdx.game.component.collider.CircleCollider;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.interfaces.IOwnable;
import com.mygdx.game.ui.PlayerInterface;
import com.mygdx.game.util.StateSystem;
import com.mygdx.game.util.StateTree;
import com.mygdx.game.util.Tree;
import com.mygdx.game.util.gui.GUI;
import com.mygdx.game.util.managers.DataManager;
import com.mygdx.game.util.managers.GameEventManager;
import com.mygdx.game.util.managers.MessageEventSystem;
import com.mygdx.game.util.timer.RepeatingTimer;
import com.mygdx.game.util.timer.Timer;
import gnu.trove.map.hash.TLongObjectHashMap;

import java.util.LinkedList;
import java.util.function.Consumer;

/**
 * Created by Paha on 1/17/2015.
 */
public class Colonist extends Component implements IInteractable, IOwnable{
    @JsonIgnore
    private Colony colony;
    @JsonIgnore
    private Inventory inventory;
    @JsonIgnore
    private Stats stats;
    @JsonIgnore
    private BehaviourManagerComp manager;
    @JsonIgnore
    private Effects effects;
    @JsonIgnore
    private Equipment equipment;
    @JsonIgnore
    private Collider collider;
    @JsonProperty
    private String firstName, lastName;
    @JsonProperty
    private boolean alert = false;
    @JsonIgnore
    private Fixture fixture;
    @JsonIgnore
    private Functional.Callback deathCallback = null;

    //The onDamage event when this colonist takes damage.
    @JsonIgnore
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
    @JsonIgnore
    private Functional.Callback onZero = () -> {
        this.deathCallback = () -> {
            this.owner.getTags().removeTags("alive", "alert", "selected");
            this.owner.getTransform().setRotation(90f);
            this.owner.destroyComponent(BehaviourManagerComp.class);
            this.manager = null;
            this.collider.getBody().destroyFixture(this.fixture); //TODO THIS PROBABLY WILL BREAK IT!
            this.stats.clearTimers();
            GridComponent gridComp = this.getComponent(GridComponent.class);
            gridComp.setActive(false);
            ColonyGame.instance.worldGrid.removeViewer(gridComp);
            this.setActive(false); //Disable the update tick. We won't be active for now...
        };
    };

    //The function for when a "attack" signal is sent to this colonist.
    @JsonIgnore
    private Consumer<Object[]> onAttackingEvent = args -> {
        Group attackingGroup = (Group)args[0];
        if(attackingGroup.getLeader().getTags().hasTag("boss"))
            PlayerInterface.getInstance().newPlayerEvent(GameEventManager.triggerGameEvent("bossencounter", this.getEntityOwner(), attackingGroup.getLeader()));
    };

    @JsonIgnore
    private Consumer<Object[]> onBeingAttacked = args -> {
        Entity other = (Entity)args[0];
        this.manager.getBlackBoard().attackList.add(other);
        Consumer<Entity> callForHelp = ent -> {
            if(ent.getTags().hasTags("colonist", "alive")){
                Colonist col = ent.getComponent(Colonist.class);
                if(col.getOwningColony() == this.getOwningColony()){
                    col.getBehManager().getBlackBoard().target = other;
                    if(!col.getBehManager().getBehaviourStates().getCurrState().stateName.equals("attackTarget"))
                        col.getBehManager().changeTaskImmediate("attackTarget");
                }
            }
        };

        ColonyGame.instance.worldGrid.performOnEntityInRadius(callForHelp , null, 20, ColonyGame.instance.worldGrid.getIndex(this.owner));
    };

    //When one of our fixtures collides with something.
    @JsonIgnore
    private Consumer<Object[]> onCollideStart = args -> {
        Fixture myFixture = (Fixture)args[0];
        Fixture otherFixture = (Fixture)args[1];

        Collider.ColliderInfo myInfo = (Collider.ColliderInfo)myFixture.getUserData();
        Collider.ColliderInfo otherInfo = (Collider.ColliderInfo)otherFixture.getUserData();

        //If a body (not a sensor) is hitting our 'attack_sensor' fixture and it's an animal
        if(myInfo.tags.hasTag("attack_sensor") && otherInfo.tags.hasTag("entity") && otherInfo.owner.getTags().hasTag("animal")){
            Animal animal = otherInfo.owner.getComponent(Animal.class);
            if(animal == null) return;
            if(animal.getAnimalRef().aggressive)
                this.manager.getBlackBoard().attackList.add(otherInfo.owner);
        }
    };

    //When one of our fixtures collides with something.
    @JsonIgnore
    private Consumer<Object[]> onCollideEnd = args -> {
        Fixture myFixture = (Fixture)args[0];
        Fixture otherFixture = (Fixture)args[1];

        Collider.ColliderInfo myInfo = (Collider.ColliderInfo)myFixture.getUserData();
        Collider.ColliderInfo otherInfo = (Collider.ColliderInfo)otherFixture.getUserData();

        //If a (entity) fixture is hitting our 'attack_sensor' fixture and it's an animal
        if(myInfo.tags.hasTag("attack_sensor") && otherInfo.owner.getTags().hasTag("animal") && otherInfo.tags.hasTag("entity")){
            Animal animal = otherInfo.owner.getComponent(Animal.class);
            if(animal == null) return;
            if(animal.getAnimalRef().aggressive)
                this.manager.getBlackBoard().attackList.remove(otherInfo.owner);
        }
    };

    public Colonist() {
        super();
    }

    @Override
    public void save() {

    }

    @Override
    public void initLoad(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        super.initLoad(entityMap, compMap);
        this.setupColonist(); //Set up things about the colonist.
    }

    @Override
    public void load(TLongObjectHashMap<Entity> entityMap, TLongObjectHashMap<Component> compMap) {
        super.load(entityMap, compMap);
        this.configureStats(); //Configure the stats.
        this.makeCollider(); //Make the collider.
    }

    @Override
    public void init() {
        super.init();
        this.initLoad(null, null);
    }

    @Override
    public void start() {
        super.start();
        this.createStats(); //Create the stats.
        this.load(null, null);
    }

    @Override
    public void update(float delta) {
        super.update(delta);

        //If alert and we have something in the attack list and we are not attacking already, let's attack.
        if(alert && this.manager.getBlackBoard().attackList.size() > 0){
            Entity ent = this.manager.getBlackBoard().attackList.peek();
            if(!ent.getTags().hasTag("alive"))
                this.manager.getBlackBoard().attackList.removeFirst();
            else{
                this.getBehManager().getBlackBoard().target = ent;
                if(!this.getBehManager().getBehaviourStates().getCurrState().stateName.equals("attackTarget"))
                    this.getBehManager().changeTaskImmediate("attackTarget");
            }
        }else if(this.getBehManager().getBehaviourStates().isCurrState("idle") && !this.inventory.isEmpty()){
            //TODO Since tools are in the inventory, we can ignore them while transferring but this will try to run every update.
            if(!this.inventory.hasItemTypeOnly("tool")){
                this.getBehManager().changeTaskImmediate("returnItems");
            }else if(this.equipment.hasTools()){
                this.getBehManager().changeTaskImmediate("returnTools");
            }
        }

        effects.testAndSetEffect("starving", this.stats);
        effects.testAndSetEffect("dehydrated", this.stats);

        //If the callback for death is ready, call it and reset it.
        if(this.deathCallback != null) {
            this.deathCallback.callback();
            this.deathCallback = null;
        }
    }

    @Override
    public void setToDestroy() {
        super.setToDestroy();
    }

    @Override
    public void destroy(Entity destroyer) {
        super.destroy(destroyer);

        this.stats = null;
        this.colony = null;
        this.inventory = null;
        this.manager = null;
        this.effects = null;
        this.equipment = null;
        this.collider = null;
        this.deathCallback = null;
    }

    /**
     * Sets up the colonist, grabbing needed components, adding events to the event system, and calling
     * createBehaviourButtons(), createBehaviourStates(), createRangeSensor(), createEffects().
     */
    private void setupColonist(){
        this.inventory = this.getComponent(Inventory.class);
        this.inventory.setMaxAmount(10);
        this.stats = this.getComponent(Stats.class);
        this.manager = this.getComponent(BehaviourManagerComp.class);
        this.manager.getBlackBoard().moveSpeed = 200f;
        this.collider = this.getComponent(Collider.class);
        this.equipment = this.getComponent(Equipment.class);
        this.effects = this.getComponent(Effects.class);

        MessageEventSystem.onEntityEvent(this.owner, "damage", onDamage);
        MessageEventSystem.onEntityEvent(this.owner, "attacking_group", onAttackingEvent);
        MessageEventSystem.onEntityEvent(this.owner, "collide_start", onCollideStart);
        MessageEventSystem.onEntityEvent(this.owner, "collide_end", onCollideEnd);
        MessageEventSystem.onEntityEvent(this.owner, "attacking", onBeingAttacked);

        this.createBehaviourButtons();
        this.createBehaviourStates();
        this.createRangeSensor();
        this.createEffects();
    }

    /**
     * Creates all the buttons for the colonists behaviours.
     */
    private void createBehaviourButtons(){
        this.getBehManager().getBlackBoard().attackDamage = 30f;
        this.getBehManager().getBlackBoard().attackRange = 500f;

        StateTree<BehaviourManagerComp.TaskInfo> taskTree = this.getBehManager().getTaskTree();

        Tree.TreeNode<BehaviourManagerComp.TaskInfo>[] nodeList = taskTree.addNode("root", "gather", "hunt", "explore", "build", "idle", "sleep", "craftItem");

        //For each node, we set up a TaskInfo object and assign it to the node's userData field.
        for(Tree.TreeNode node : nodeList) {
            BehaviourManagerComp.TaskInfo taskInfo = new BehaviourManagerComp.TaskInfo(node.nodeName);
            taskInfo.callback = () -> {
                getBehManager().changeTaskImmediate(node.nodeName); //Change the task.
                taskTree.moveDownToChild(node.nodeName, true); //Move to this node.
            };
            taskInfo.userData = DataManager.getData(node.nodeName+"Style", GUI.GUIStyle.class);
            if(taskInfo.userData == null) taskInfo.userData = DataManager.getData("blankStyle", GUI.GUIStyle.class);

            node.userData = taskInfo;
        }

        //Add a list of nodes under the "gather" node.
        nodeList = taskTree.addNode("gather", "food", "water", "herbs", "wood", "stone", "metal");

        //For each node we added, do stuff...
        for(Tree.TreeNode<BehaviourManagerComp.TaskInfo> node : nodeList) {
            BehaviourManagerComp.TaskInfo taskInfo = new BehaviourManagerComp.TaskInfo(node.nodeName);
            taskInfo.callback = () -> {
                taskInfo.active = !taskInfo.active;
                getBehManager().getBlackBoard().resourceTypeTags.toggleTag(node.nodeName);
            };
            taskInfo.userData = DataManager.getData("blankStyle", GUI.GUIStyle.class);

            node.userData = taskInfo;
        }

        //Add a back button in the tree.
        Tree.TreeNode<BehaviourManagerComp.TaskInfo> back = taskTree.addNode("gather", "back");
        BehaviourManagerComp.TaskInfo taskInfo = new BehaviourManagerComp.TaskInfo(back.nodeName);
        taskInfo.callback = taskTree::moveUp;
        back.userData = taskInfo;

        MessageEventSystem.onEntityEvent(this.owner, "task_started", args -> {
            Task task = (Task) args[0];
            if (task.getName().equals("exploreUnexplored"))
                task.getBlackboard().target = this.getOwningColony().getEntityOwner();
        });
    }

    /**
     * Creates the behaviour states for the Colonist.
     */
    private void createBehaviourStates(){
        getBehManager().getBehaviourStates().addState("moveTo", false, new StateSystem.DefineTask("idle", "idle")).setRepeat(false);
        getBehManager().getBehaviourStates().addState("gather", false, new StateSystem.DefineTask("gather", "idle")).setRepeat(true).setCanBeSavedAsLast(true);
        getBehManager().getBehaviourStates().addState("explore", false, new StateSystem.DefineTask("explore", "idle")).setRepeat(true);
        getBehManager().getBehaviourStates().addState("consume", false, new StateSystem.DefineTask("idle", "idle")).setRepeat(false).setRepeatLastState(true);
        getBehManager().getBehaviourStates().addState("attackTarget", false, new StateSystem.DefineTask("attackTarget", "idle")).setRepeat(false);
        getBehManager().getBehaviourStates().addState("hunt", false, new StateSystem.DefineTask("hunt", "idle")).setRepeat(true);
        getBehManager().getBehaviourStates().addState("returnTools", false, new StateSystem.DefineTask("idle", "idle")).setRepeat(false);
        getBehManager().getBehaviourStates().addState("build", false, new StateSystem.DefineTask("build", "idle")).setRepeat(true).setCanBeSavedAsLast(true);
        getBehManager().getBehaviourStates().addState("returnItems", false, new StateSystem.DefineTask("idle", "idle")).setRepeat(false);
        getBehManager().getBehaviourStates().addState("sleep", false, new StateSystem.DefineTask("idle", "idle")).setRepeat(false).setRepeatLastState(true);
        getBehManager().getBehaviourStates().addState("craftItem", false, new StateSystem.DefineTask("idle", "idle")).setRepeat(true).setCanBeSavedAsLast(true);
    }

    @JsonIgnore
    //Creates a range sensor for when we get a ranged weapon.
    private void createRangeSensor(){
        if(this.collider == null || this.collider.getBody() == null || this.collider.getBody() == null) return;
        CircleShape shape = new CircleShape();
        shape.setRadius(0f);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.isSensor = true;
        fixtureDef.shape = shape;
        this.fixture = this.collider.getBody().createFixture(fixtureDef);
        Collider.ColliderInfo info = new Collider.ColliderInfo(this.owner);
        info.tags.addTag("attack_sensor");
        this.fixture.setUserData(info);
        shape.dispose();
    }

    @JsonIgnore
    private void createEffects(){
        this.effects.addNewEffect("starving", "Starving", "starvation_effect", (Stats stats) -> stats.getStat("food").getCurrVal() <= 0);
        this.effects.addNewEffect("dehydrated", "Dehydrated", "dehydration_effect", (Stats stats) -> stats.getStat("water").getCurrVal() <= 0);
        this.effects.addNewEffect("sleepy", "Sleepy", "sleepy_effect", (Stats stats) -> stats.getStat("energy").getCurrVal() <= 20);
    }

    private void makeCollider(){
        CircleCollider collider = getComponent(CircleCollider.class);
        if(collider == null) collider = this.addComponent(new CircleCollider());
        collider.setupBody(BodyDef.BodyType.DynamicBody, ColonyGame.instance.world, this.owner.getTransform().getPosition(), 1, true, true);
    }

    /**
     * Creates the stats for this colonist. Also calls configureStats()
     */
    private void createStats(){
        //Create these 4 stats.
        Stats.Stat healthStat = stats.addStat("health", 100, 100);
        Stats.Stat foodStat = stats.addStat("food", 5, 100);
        Stats.Stat waterStat = stats.addStat("water", 1, 100);
        Stats.Stat energyStat = stats.addStat("energy", 100, 100);

        this.configureStats();
    }

    /**
     * Configures the stats for the Colonist, things like colors, effects, and timers.
     */
    private void configureStats(){
        Stats.Stat healthStat = stats.getStat("health");
        Stats.Stat foodStat = stats.getStat("food");
        Stats.Stat waterStat = stats.getStat("water");
        Stats.Stat energyStat = stats.getStat("energy");

        healthStat.color = Color.GREEN;
        foodStat.color = Color.RED;
        waterStat.color = Color.CYAN;
        energyStat.color = Color.YELLOW;

        foodStat.effect = "feed";
        waterStat.effect = "thirst";

        //Add some timers.
        //Subtract food every 5 seconds and try to eat when it's too low.
        stats.addTimer(new RepeatingTimer(5f, () -> {
            foodStat.addToCurrent(-1);
            //If under 20, try to eat.
            if(foodStat.getCurrVal() <= 20 && !getBehManager().getBehaviourStates().isCurrState("consume")) {
                getBehManager().getBlackBoard().itemEffect = "feed";
                getBehManager().getBlackBoard().itemEffectAmount = 1;
                getBehManager().changeTaskQueued("consume");
            }
        }));

        //Subtract water every 10 seconds and try to drink when it's too low.
        stats.addTimer(new RepeatingTimer(10f, () -> {
            waterStat.addToCurrent(-1);
            //If under 20, try to drink.
            if (waterStat.getCurrVal() <= 20 && !getBehManager().getBehaviourStates().isCurrState("consume")) {
                getBehManager().getBlackBoard().itemEffect = "thirst";
                getBehManager().getBlackBoard().itemEffectAmount = 1;
                getBehManager().changeTaskQueued("consume");
            }
        })); //Subtract water every 10 seconds.

        //Subtract energy every so often...
        stats.addTimer(new RepeatingTimer(3f, () -> {
            energyStat.addToCurrent(-1);
            //If under 20, sleep!
            if (energyStat.getCurrVal() <= 20 && !getBehManager().getBehaviourStates().isCurrState("sleep")) {
                getBehManager().changeTaskQueued("sleep");
            }
        })); //Subtract energy

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

    @JsonIgnore
    /**
     * Toggles the attack range sensor on this colonist.
     * @return True if alert was set to true, false if alert was set to false.
     */
    public boolean toggleRangeSensor(){
        return this.setAlert(!this.alert);
    }

    @JsonIgnore
    public boolean setAlert(boolean alert){
        //If alert is active and we are setting it to not active.
        if(this.alert && !alert) {
            this.manager.getBlackBoard().attackList = new LinkedList<>();
            this.fixture.getShape().setRadius(0);
            this.owner.getTags().removeTag("alert");
        //If alert is not active and we are setting it to active.
        }else if(!this.alert && alert) {
            this.fixture.getShape().setRadius(20f);
            this.owner.getTags().addTag("alert");
        }

        return this.alert = alert;
    }

    @JsonIgnore
    public void setName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.getEntityOwner().name = firstName;
    }

    @JsonProperty("firstName")
    public String getFirstName() {
        return firstName;
    }

    @JsonProperty("firstName")
    public void setFirstName(String firstName){
        this.firstName = firstName;
    }

    @JsonProperty("lastName")
    public String getLastName() {
        return lastName;
    }

    @JsonProperty("lastName")
    public void setLastName(String lastName){
        this.lastName = lastName;
    }

    public Effects getEffects(){
        return this.effects;
    }

    public boolean isAlert() {
        return alert;
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
    public String getName() {
        return this.firstName;
    }

    @Override
    public BehaviourManagerComp getBehManager() {
        return this.manager;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    @Override
    public Constructable getConstructable() {
        return null;
    }

    @Override
    public CraftingStation getCraftingStation() {
        return null;
    }

    @Override
    public Building getBuilding() {
        return null;
    }

    @Override
    public Enterable getEnterable() {
        return null;
    }

    @Override
    public void addedToColony(Colony colony) {
        this.colony = colony;
    }

    @Override
    public Colony getOwningColony() {
        return this.colony;
    }

}
