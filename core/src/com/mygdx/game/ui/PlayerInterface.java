package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.behaviourtree.PrebuiltTasks;
import com.mygdx.game.component.*;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.*;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.helpers.managers.DataManager;
import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.helpers.timer.Timer;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.interfaces.IGUI;
import com.mygdx.game.interfaces.IInteractable;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 12/25/2014.
 */
public class PlayerInterface extends UI implements IGUI, InputProcessor {
    public boolean paused = false;
    public static boolean active = false;
    public float gameSpeed = 1;

    private TextureRegion background, UIBackgroundBase, UIBackgroundTop;
    private World world;

    private StateSystem buttonStateSystem = new StateSystem();

    private boolean drawingInfo = false;
    private boolean drawingProfiler = false;
    private boolean mouseDown = false;
    private boolean dragging = false;
    private boolean drawGrid = false;

    private Rectangle buttonRect = new Rectangle();
    private Rectangle uiBackgroundBaseRect = new Rectangle();
    private Rectangle uiBackgroundTopRect = new Rectangle();
    private float FPS = 0;

    private static final float camMoveSpeed = 100f;
    private static final float camZoomSpeed = 5f;

    private final float mainHeight = 0.12f;
    private final float topHeight = 0.03f;
    private final float infoWidth = 0.13f;
    private final float statusWidth = 0.135f;
    private final float tabsWidth = 0.262f;
    private final float ordersWidth = 0.479f;

    private float barW = 100, barH = 20;

    private Rectangle infoTopRect = new Rectangle();
    private Rectangle statusTopRect = new Rectangle();
    private Rectangle tabsTopRect = new Rectangle();
    private Rectangle ordersTopRect = new Rectangle();

    private Rectangle infoRect = new Rectangle();
    private Rectangle statusRect = new Rectangle();
    private Rectangle tabsRect = new Rectangle();
    private Rectangle ordersRect = new Rectangle();

    private Rectangle bottomLeftRect = new Rectangle();

    private Rectangle selectionBox = new Rectangle();
    private Rectangle profileButtonRect = new Rectangle();

    private Rectangle orderButtonRect = new Rectangle();

    private GUI.GUIStyle gatherStyle = new GUI.GUIStyle();
    private GUI.GUIStyle exploreStyle = new GUI.GUIStyle();
    private GUI.GUIStyle huntStyle = new GUI.GUIStyle();
    private GUI.GUIStyle blankStyle = new GUI.GUIStyle();
    private GUI.GUIStyle skillsStyle = new GUI.GUIStyle();
    private GUI.GUIStyle UIStyle;
    private Timer FPSTimer;

    private Vector2 testPoint = new Vector2(); //A reusable vector
    private static boolean newlySelected = false; //If we have selected a new entity, this will stop the left click from getting rid of it.
    private static UnitProfile selectedProfile = null; //The currently selected UnitProfile.
    private ArrayList<UnitProfile> selectedProfileList = new ArrayList<>(); //The list of selected UnitProfiles.

    private Color gray = new Color(Color.BLACK);
    private Texture blueSquare;
    private Tree.TreeNode currStateNode = null;
    private Stage stage;
    private static PlayerInterface playerInterface;

    private DataBuilder.JsonPlayerEvent currentEvent;
    private GUI.GUIStyle eventDescStyle, eventTitleStyle;

    private TextureRegion whiteTexture;

    private void loadHuntButtonStyle(){
        huntStyle.normal = ColonyGame.assetManager.get("huntbutton_normal", Texture.class);
        huntStyle.moused = ColonyGame.assetManager.get("huntbutton_moused", Texture.class);
        huntStyle.clicked = ColonyGame.assetManager.get("huntbutton_clicked", Texture.class);
    }

    //For selecting a single unit.
    private QueryCallback callback = fixture -> {
        Collider.ColliderInfo info = (Collider.ColliderInfo)fixture.getUserData();
        if(info.tags.hasTag(Constants.COLLIDER_CLICKABLE) && fixture.testPoint(testPoint.x, testPoint.y)){
            setSelectedEntity(info.owner);
            return false;
        }

        return true;
    };


    private QueryCallback selectionCallback = fixture -> {
        Collider.ColliderInfo selectedInfo = (Collider.ColliderInfo)fixture.getUserData();

        //If not null, the entity is a colonist, and the collider is clickable.
        if(selectedInfo != null && selectedInfo.owner.hasTag(Constants.ENTITY_COLONIST) && selectedInfo.tags.hasTag(Constants.COLLIDER_CLICKABLE)) {
            UnitProfile profile = new UnitProfile(); //Make a unit profile.
            setSelectedEntity(selectedInfo.owner); //Set our selectedEntity
            profile.entity = selectedInfo.owner; //Get the Entity that we clicked.
            profile.interactable = selectedInfo.owner.getComponent(Interactable.class); //Get teh selectedProfile Component.
            selectedProfileList.add(profile); //Add it to the list.
            return true;
        }
        return true;
    };

    /**
     * A player interface Component that will display information on the screen.
     * @param batch The SpriteBatch for drawing to the screen.
     * @param world The Box2D world. We need to know about this for clicking on objects.
     */
    public PlayerInterface(SpriteBatch batch, World world) {
        super(batch);
        this.world = world;
        this.whiteTexture = new TextureRegion(WorldGen.whiteTex);

        this.eventDescStyle = new GUI.GUIStyle();
        eventDescStyle.background = new TextureRegion(ColonyGame.assetManager.get("eventWindowDescriptionBackground", Texture.class));

        this.eventTitleStyle = new GUI.GUIStyle();
        this.eventTitleStyle.background = new TextureRegion(ColonyGame.assetManager.get("eventWindowTitleBackground", Texture.class));

        this.stage = new Stage(new ScreenViewport(ColonyGame.UICamera));
        this.blueSquare = ColonyGame.assetManager.get("blueSquare", Texture.class);

        this.background = new TextureRegion(ColonyGame.assetManager.get("background", Texture.class));
        this.UIBackgroundBase = new TextureRegion(ColonyGame.assetManager.get("UIBackground_base", Texture.class));
        this.UIBackgroundTop = new TextureRegion(ColonyGame.assetManager.get("UIBackground_top", Texture.class));

        this.buttonRect.set(0, Gdx.graphics.getHeight() - 100, 200, 100);

        Functional.Callback callback = () -> this.FPS = 1/Gdx.graphics.getDeltaTime();
        this.FPSTimer = new RepeatingTimer(0.5d, callback);

        //Loads and configures stuff about buttons tyles.
        loadHuntButtonStyle();

        this.generateFonts();

        gatherStyle.normal =  ColonyGame.assetManager.get("axebutton_normal", Texture.class);
        gatherStyle.moused = ColonyGame.assetManager.get("axebutton_moused", Texture.class);
        gatherStyle.clicked = ColonyGame.assetManager.get("axebutton_clicked", Texture.class);
        gatherStyle.active = ColonyGame.assetManager.get("axebutton_clicked", Texture.class);

        DataManager.addData("gatherStyle", gatherStyle, GUI.GUIStyle.class);

        exploreStyle.normal = ColonyGame.assetManager.get("explorebutton_normal", Texture.class);
        exploreStyle.moused = ColonyGame.assetManager.get("explorebutton_moused", Texture.class);
        exploreStyle.clicked = ColonyGame.assetManager.get("explorebutton_clicked", Texture.class);
        exploreStyle.active = ColonyGame.assetManager.get("explorebutton_clicked", Texture.class);

        DataManager.addData("exploreStyle", exploreStyle, GUI.GUIStyle.class);

        huntStyle.normal = ColonyGame.assetManager.get("huntbutton_normal", Texture.class);
        huntStyle.moused = ColonyGame.assetManager.get("huntbutton_moused", Texture.class);
        huntStyle.clicked = ColonyGame.assetManager.get("huntbutton_clicked", Texture.class);
        huntStyle.active = ColonyGame.assetManager.get("huntbutton_clicked", Texture.class);

        DataManager.addData("huntStyle", huntStyle, GUI.GUIStyle.class);

        blankStyle.normal = ColonyGame.assetManager.get("blankbutton_normal", Texture.class);
        blankStyle.moused = ColonyGame.assetManager.get("blankbutton_moused", Texture.class);
        blankStyle.clicked = ColonyGame.assetManager.get("blankbutton_clicked", Texture.class);
        blankStyle.active = ColonyGame.assetManager.get("blankbutton_clicked", Texture.class);

        DataManager.addData("blankStyle", blankStyle, GUI.GUIStyle.class);

        gatherStyle.font.setColor(new Color(126f / 255f, 75f / 255f, 27f / 255f, 1));
        exploreStyle.font.setColor(new Color(126f / 255f, 75f / 255f, 27f / 255f, 1));
        huntStyle.font.setColor(126f / 255f, 75f / 255f, 27f / 255f, 1);

        buttonStateSystem.addState("main", true);
        buttonStateSystem.addState("gather");
        buttonStateSystem.addState("hunt");
        buttonStateSystem.setCurrState("main");

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);
    }

    private void generateFonts(){
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Trajan Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 12;
        BitmapFont topFont = generator.generateFont(parameter);
        generator.dispose();
        topFont.setColor(126f / 255f, 75f / 255f, 27f / 255f, 1);

        UIStyle = new GUI.GUIStyle();
        UIStyle.font = topFont;
        gatherStyle.font = topFont;
        exploreStyle.font = topFont;
        DataManager.addData("UIFont", topFont, BitmapFont.class);
    }

    @Override
    public void render(float delta, SpriteBatch batch) {
        super.render(delta, batch);
        batch.setProjectionMatrix(ColonyGame.UICamera.combined);
        GUI.font.setColor(Color.WHITE);

        int height = Gdx.graphics.getHeight();
        FPSTimer.update(delta);

        this.moveCamera(); //Move the camera
        this.drawSelectionBox(); //Draws the selection box.
        this.drawDebugInfo(height); //Draws some debug information.
        this.drawTerrainInfo(this.bottomLeftRect); //Draws information about the moused over terrain piece.

        if(this.drawingProfiler)
            Profiler.drawDebug(batch, 200, height - 20);

        //Draw the grid squares if enabled.
        if(drawGrid) {
            ColonyGame.worldGrid.debugDraw();
            drawBox2DDebug();
        }

        //Draw stuff about the selectedEntity entity.
        if(selectedProfile != null || this.selectedProfileList.size() > 0){
            GUI.Texture(this.UIBackgroundBase, this.uiBackgroundBaseRect, this.batch);
            GUI.Texture(this.UIBackgroundTop, this.uiBackgroundTopRect, this.batch);
            this.drawMultipleProfiles(this.ordersRect);
            this.drawSelected();
        }

        drawCurrentEvent(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.end();
        batch.begin();
        stage.act(delta);
        stage.draw();
        batch.end();
        batch.begin();
    }

    private void drawCurrentEvent(int width, int height){
        if(this.currentEvent != null){
            float windowWidth = width/3.2f, windowHeight = height/2.7f;
            float windowX = width/2 - windowWidth/2, windowY = height/2 - windowHeight/2;

            float titleWidth = windowWidth*0.4f, titleHeight = windowHeight*0.1f;
            float titleX = windowX+ windowWidth*0.05f, titleY = windowY + windowHeight - titleHeight - windowHeight*0.03f;

            float descWidth = windowWidth*0.9f, descHeight = windowHeight*0.6f;
            float descX = windowX + windowWidth*0.05f, descY = windowY + windowHeight*0.25f;

            GUI.Texture(new TextureRegion(ColonyGame.assetManager.get("eventWindowBackground", Texture.class)), windowX, windowY, windowWidth, windowHeight, this.batch);

            eventDescStyle.padding(10);
            eventDescStyle.multiline = true;
            eventDescStyle.wrap = true;
            eventDescStyle.alignment = Align.topLeft;
            GUI.Label(this.currentEvent.eventDisplayName, this.batch, titleX, titleY, titleWidth, titleHeight, eventTitleStyle);
            GUI.Label(GH.generateEventDescription(this.currentEvent), this.batch, descX, descY, descWidth, descHeight, eventDescStyle);

            float buttonWidth = windowWidth*0.3f, buttonHeight = 75;
            float spacing = (windowWidth - this.currentEvent.choices.length*buttonWidth)/(this.currentEvent.choices.length+1);
            blankStyle.wrap = true;

            for(int i=0;i<this.currentEvent.choices.length;i++){
                String choice = this.currentEvent.choices[i];
                if(GUI.Button(this.batch, choice, windowX + (i+1)*spacing + i*buttonWidth, windowY + windowHeight*0.01f, buttonWidth, buttonHeight, blankStyle)){
                    BehaviourManagerComp comp = this.currentEvent.eventTarget.getComponent(BehaviourManagerComp.class);
                    if(comp == null) return;
                    comp.getBlackBoard().target = this.currentEvent.eventTargetOther;
                    comp.changeTaskImmediate(this.currentEvent.behaviours[i]);
                    this.gameSpeed = 1f; //Reset game speed
                    this.paused = false; //Unpaude
                    this.currentEvent = null;
                    return;
                }
            }
        }
    }

    /**
     * Checks if the GUI is moused over or not.
     */
    private boolean checkMousedOver(){
        //Determines if any UI is moused over or not.
        return this.uiBackgroundBaseRect.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()) ||
                this.uiBackgroundBaseRect.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
    }

    /**
     * Draws the selection box if the user is dragging a selection box.
     */
    private void drawSelectionBox(){
        if(mouseDown) {
            //Get the start point.
            Vector3 pos = new Vector3(selectionBox.x, selectionBox.y, 0);
            Vector3 start = ColonyGame.camera.project(new Vector3(pos.x, pos.y, 0));

            //Get the end point.
            pos.set(selectionBox.x + selectionBox.getWidth(), selectionBox.y + selectionBox.getHeight(), 0);
            Vector3 end = ColonyGame.camera.project(new Vector3(pos.x, pos.y, 0));
            gray.a = 0.5f;

            //Save the color from the batch, apply the gray, draw the texture (with color), and reset the original color.
            Color saved = this.batch.getColor();
            this.batch.setColor(gray);
            this.batch.draw(WorldGen.whiteTex, start.x, start.y, end.x - start.x, end.y - start.y);
            this.batch.setColor(saved);
        }
    }

    /**
     * Moves the camera when keys are pressed.
     */
    private void moveCamera(){
        if(Gdx.input.isKeyPressed(Input.Keys.W))
            ColonyGame.camera.translate(0, Gdx.graphics.getDeltaTime()*camMoveSpeed);
        if(Gdx.input.isKeyPressed(Input.Keys.S))
            ColonyGame.camera.translate(0, -Gdx.graphics.getDeltaTime()*camMoveSpeed);
        if(Gdx.input.isKeyPressed(Input.Keys.A))
            ColonyGame.camera.translate(-Gdx.graphics.getDeltaTime()*camMoveSpeed, 0);
        if(Gdx.input.isKeyPressed(Input.Keys.D))
            ColonyGame.camera.translate(Gdx.graphics.getDeltaTime()*camMoveSpeed, 0);
    }

    /**
     * Draws info (like FPS) on the screen.
     * @param height The height of the screen.
     */
    private void drawDebugInfo(int height){
        if(this.drawingInfo) {
            GUI.Text("FPS: " + FPS, this.batch, 0, height - 20);
            GUI.Text("Zoom: " + ColonyGame.camera.zoom, this.batch, 0, height - 40);
            GUI.Text("Resolution: " + Gdx.graphics.getDesktopDisplayMode().width + "X" + Gdx.graphics.getDesktopDisplayMode().height, this.batch, 0, height - 60);
            GUI.Text("NumTrees: " + WorldGen.getInstance().numTrees(), this.batch, 0, height - 80);
            GUI.Text("NumTiles: " + ColonyGame.worldGrid.getWidth()*ColonyGame.worldGrid.getHeight(), this.batch, 0, height - 100);
            GUI.Text("NumGridCols(X): " + ColonyGame.worldGrid.getWidth(), this.batch, 0, height - 120);
            GUI.Text("NumGridRows(Y): " + ColonyGame.worldGrid.getHeight(), this.batch, 0, height - 140);
        }
    }

    /**
     * Gets the terrain tile and displays its information.
     * @param rect The rectangle to draw the terrain info in.
     */
    private void drawTerrainInfo(Rectangle rect){
        OrthographicCamera camera = ColonyGame.camera;
        Vector3 mouseCoords = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        Grid.GridInstance grid = ColonyGame.worldGrid;

        GUI.Texture(this.background, rect, this.batch);
        int index[] = grid.getIndex(mouseCoords.x, mouseCoords.y);
        Grid.Node node = grid.getNode(index);
        if(node == null) return;
        Grid.TerrainTile tile = grid.getNode(index).getTerrainTile();
        if(tile == null) return;
        GUI.Label(tile.category, this.batch, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
    }

    /**
     * Draws the multiple selectedEntity profiles.
     * @param rect The rectangle to draw the information inside of.
     */
    private void drawMultipleProfiles(Rectangle rect){
        //If there is more than one unit selectedEntity.. display in a group format.
        if(selectedProfileList.size() > 1){
            profileButtonRect.set(rect.getX() + rect.getWidth() - 115, rect.getY() + rect.getHeight() - 20, 50, 20);

            //For each profile, draw a button to access each individual entity.
            for(UnitProfile profile : selectedProfileList) {
                //Draw the button for the individual profile. If clicked, make it our selected profile.
                if(GUI.Button(this.batch, profileButtonRect, profile.interactable.getInteractable().getName()))
                    selectedProfile = profile;

                //If we go too far down the screen, move over.
                profileButtonRect.setY(profileButtonRect.getY() - 22);
                if(profileButtonRect.y <= rect.getY() + 10)
                    profileButtonRect.set(rect.getX() + rect.getWidth() - 65, rect.getY() + rect.getHeight() - 20, 50, 20);

            }
        }
    }

    /**
     * Displays the selectedEntity Entity.
     */
    private void drawSelected(){
        //Make sure the selectedProfile we have selectedEntity isn't null!
        if(selectedProfile != null){
            IInteractable innerInter = selectedProfile.interactable.getInteractable(); //Get the selectedProfile!

            //If it has a name, draw the name...
            if(innerInter.getName() != null) {
                GUI.Label(innerInter.getName(), this.batch, this.infoTopRect, this.UIStyle);
            }

            //If it has stats, draw the stats...
            if(innerInter.getStats() != null){
                //GUI.Texture(statusRect, ColonyGame.assetManager.get("menuButton_normal", Texture.class), this.batch);
                Stats stats = innerInter.getStats();

                GUI.Label("Stats", this.batch, this.statusTopRect, this.UIStyle);
                ArrayList<Stats.Stat> list = stats.getStatList();
                float space = ((statusRect.height - list.size()*20)/list.size()+1)/2;
                float x = statusRect.x + 10;
                float y = statusRect.y + statusRect.height - 20;

                for(int i=0;i<list.size();i++){
                    Stats.Stat stat = list.get(i);
                    drawBar(stat.name, x, y-(i+1)*space - 20*i, 100, 20, stat.getCurrVal(), stat.getMaxVal());
                }

            //If no stats, maybe it has stat text?
            }else if(innerInter.getStatsText() != null){
                GUI.Label("Resources", this.batch, this.statusTopRect, this.UIStyle);
                this.UIStyle.multiline = true;
                this.UIStyle.alignment = Align.topLeft;
                this.UIStyle.paddingLeft = 10;
                this.UIStyle.paddingTop = 5;
                GUI.Label(innerInter.getStatsText(), this.batch, this.statusRect, this.UIStyle);
                this.UIStyle.paddingLeft = 0;
                this.UIStyle.paddingTop = 0;
                this.UIStyle.alignment = Align.center;
                this.UIStyle.multiline = false;
            }

            //If it has an inventory, draw the inventory...
            if(innerInter.getInventory() != null){
                //GUI.Texture(tabsRect, ColonyGame.assetManager.get("menuButton_normal", Texture.class), this.batch);

                GUI.Label("Inventory", this.batch, this.tabsTopRect, this.UIStyle);
                ArrayList<Inventory.InventoryItem> itemList = innerInter.getInventory().getItemList();
                this.UIStyle.alignment = Align.topLeft;
                this.UIStyle.paddingLeft = 5;
                this.UIStyle.paddingTop = 0;
                for(int i=0;i<itemList.size();i++){
                    Inventory.InventoryItem item = itemList.get(i);
                    GUI.Label(item.itemRef.getDisplayName(), this.batch, this.tabsRect.x, this.tabsRect.y, this.tabsRect.width, this.tabsRect.height - 20 - i*10, this.UIStyle);
                    GUI.Label(""+item.getAmount(), this.batch, this.tabsRect.x + 100, this.tabsRect.y, 100, this.tabsRect.height - 20 - i*10, this.UIStyle);
                }
                this.batch.setColor(Color.WHITE);
                this.UIStyle.alignment = Align.center;
                this.UIStyle.paddingLeft = 0;
                this.UIStyle.paddingTop = 0;
            }

            if(innerInter.getBehManager() != null){
                GUI.Label(innerInter.getBehManager().getCurrentTaskName(), this.batch, this.ordersRect.x, this.ordersRect.y + 70, this.ordersRect.width, this.ordersRect.height - 50, this.UIStyle);

                //If it's a humanoid that we can control, draw some order buttons and its current path.
                if(selectedProfile.interactable.interType.equals("humanoid")){
                    GUI.Label("Orders", this.batch, this.ordersTopRect, this.UIStyle);

                    //GUI.Texture(ordersRect, ColonyGame.assetManager.get("menuButton_normal", Texture.class), this.batch);
                    if(innerInter.getBehManager() != null) {
                        drawButtons(innerInter);

                        //Set to world camera and draw the path lines.
                        batch.setProjectionMatrix(ColonyGame.camera.combined);
                        BehaviourManagerComp.Line[] lines = innerInter.getBehManager().getLines();
                        for(BehaviourManagerComp.Line line : lines)
                            batch.draw(blueSquare, line.startX, line.startY, 0, 0, line.width, 0.1f, 1, 1, line.rotation, 0, 0, blueSquare.getWidth(), blueSquare.getHeight(), false, false);

                        //Set back to UI camera.
                        batch.setProjectionMatrix(ColonyGame.UICamera.combined);

                    }
                }
            }
        }
    }

    //Draws the buttons for each selectedEntity colonist that we have control of.
    private void drawButtons(IInteractable interactable){
        //If this selectedProfile was recently selectedEntity, this will be null. Set it to the root of the currently selectedEntity selectedProfile.
        if(currStateNode == null)
            currStateNode = interactable.getBehManager().getTaskTree().getNode(node -> node.nodeName.equals("root"));

        //Set some position variables.
        float width = (ordersRect.getWidth()/(currStateNode.getChildren().size+1));
        float height = ordersRect.y + 25;
        float x = ordersRect.x;

        //Get the children of the current root node. This will display all children buttons of our current selection.
        Array<Tree.TreeNode> nodeList = currStateNode.getChildren();
        for(int i=0;i<nodeList.size;i++) {
            //Get the task node and its user data.
            Tree.TreeNode currTaskNode = nodeList.get(i);
            BehaviourManagerComp.TaskInfo taskInfo = (BehaviourManagerComp.TaskInfo)currTaskNode.userData;

            //Get the GUIStyle from the taskInfo's object data. If it's null, get the default style from the GUI class.
            Object userData = taskInfo.userData;
            GUI.GUIStyle style;
            if(userData != null) {
                style = (GUI.GUIStyle) userData;
                style.activated = taskInfo.active;
            }else
                style = GUI.defaultGUIStyle;

            //Set the location and draw the button. If clicked, we need to do some tricky things...
            orderButtonRect.set(x + (i + 1) * width, height, 50, 50);
            if (GUI.Button(this.batch, currTaskNode.nodeName, orderButtonRect, style)) {
                taskInfo.doCallback();

                //For each profile selectedEntity, tell them to gather.
                for (UnitProfile profile : selectedProfileList) {
                    if(profile.entity == selectedProfile.entity) continue;

                    //Get the BehaviourComponent and TreeNode.
                    BehaviourManagerComp comp = profile.interactable.getInteractable().getBehManager();
                    Tree.TreeNode treeNode = comp.getTaskTree().getNode(node -> node.nodeName.equals(currTaskNode.nodeName));
                    BehaviourManagerComp.TaskInfo profTaskInfo = (BehaviourManagerComp.TaskInfo)treeNode.userData;

                    //Do the callback and sync the active booleans. Assign this profile's active state to the main selectedEntity profile.
                    profTaskInfo.doCallback();
                    ((BehaviourManagerComp.TaskInfo) treeNode.userData).active = taskInfo.active; //Toggle the treeNode.

                    //If it's toggled, add the tag, otherwise, remove the tag.
                    if(((BehaviourManagerComp.TaskInfo) treeNode.userData).active) comp.getBlackBoard().resourceTypeTags.addTag(StringTable.getString("resource_type", treeNode.nodeName));
                    else comp.getBlackBoard().resourceTypeTags.removeTag(StringTable.getString("resource_type", treeNode.nodeName));
                }

                //Get the node from the gather TreeNode. If it has children, set the currStateNode
                Tree.TreeNode tmpNode = interactable.getBehManager().getTaskTree().getNode(node -> node.nodeName.equals(currTaskNode.nodeName));
                if (tmpNode.hasChildren()) {
                    buttonStateSystem.setCurrState(currTaskNode.nodeName);
                    currStateNode = tmpNode;
                }
            }
        }
    }

    /**
     * Draws a bar for a current and maximum value.
     * @param text The text to put as a label for the bar.
     * @param x The x location to start at.
     * @param y The Y location to start at.
     * @param width The width of the bar.
     * @param height The height of the bar.
     * @param currVal THe current value.
     * @param maxVal The maximum value.
     */
    private void drawBar(String text, float x, float y, float width, float height, float currVal, float maxVal){
        this.UIStyle.alignment = Align.left;
        GUI.Label(text, batch, x, y, 50, height, this.UIStyle);
        this.UIStyle.alignment = Align.center;

        float outerX = x + 80;
        float innerX = x + 80 + 2;

        //Draw the out rectangle
        batch.setColor(Color.BLACK);
        GUI.Texture(this.whiteTexture, outerX, y, width, height, batch);

        //Draw the inner rectangle (shrink it by 2 inches on all sides, 'padding')
        batch.setColor(Color.GREEN);
        float newWidth = (currVal/maxVal)*(width-4);
        GUI.Texture(this.whiteTexture, innerX, y + 2, newWidth, height - 4, batch);

        GUI.Label((int)currVal+"/"+(int)maxVal, batch, outerX, y, width, height);
    }

    /**
     * The start of the dragging.
     * @param x The X location.
     * @param y The Y location.
     */
    private void startDragging(float x, float y){
        selectionBox.set(x, y, 0, 0);
        this.dragging = true;
    }

    /**
     * Active dragging.
     * @param x The current X location.
     * @param y The current Y location.
     */
    private void drag(float x, float y){
        selectionBox.set(selectionBox.x, selectionBox.y, x - selectionBox.x, y - selectionBox.y);
    }

    /**
     * When the mouse button is released and dragging stops.
     * @param x The X location.
     * @param y The Y location.
     */
    private void finishDragging(float x, float y){
        //Set the final selection box.
        selectionBox.set(selectionBox.x, selectionBox.y, x - selectionBox.x, y - selectionBox.y);

        //For easier understanding, we get the center and use half widths to get the bounds.
        Vector2 center = new Vector2();
        selectionBox.getCenter(center);
        float halfWidth = Math.abs(selectionBox.getWidth()/2);
        float halfHeight = Math.abs(selectionBox.getHeight()/2);
        selectionBox.set(center.x - halfWidth, center.y - halfHeight, center.x + halfWidth, center.y + halfHeight);

        //Query the world and set dragging to false.
        this.world.QueryAABB(this.selectionCallback, selectionBox.x, selectionBox.y, selectionBox.getWidth(), selectionBox.getHeight());
        this.dragging = false;
    }

    /**
     * If the GUI is moused over or not.
     * @return True if moused over, false otherwise.
     */
    public boolean isOnUI(){
        boolean windowActive = ((selectedProfile != null && selectedProfile != null) || this.selectedProfileList.size() > 0);
        return this.checkMousedOver() && windowActive;
    }

    /**
     * Sets the selectedEntity entity for viewing.
     * @param entity The Entity to set as the selectedEntity Entity.
     */
    public static void setSelectedEntity(Entity entity){
        newlySelected = true;
        selectedProfile = new UnitProfile(entity);
    }

    //Reveals the entire map by adding a viewer to every tile.
    private void revealMap(){
        Grid.GridInstance grid = ColonyGame.worldGrid;
        Grid.VisibilityTile[][] visMap = grid.getVisibilityMap();
        for (Grid.VisibilityTile[] aVisMap : visMap) {
            for (Grid.VisibilityTile anAVisMap : aVisMap) {
                anAVisMap.addViewer();
            }
        }
    }

    //Draws the box2D debug.
    private void drawBox2DDebug(){
        this.batch.end();
        this.batch.begin();
        ColonyGame.debugRenderer.render(ColonyGame.world, ColonyGame.camera.combined);
        this.batch.end();
        this.batch.begin();
    }

    public void newPlayerEvent(DataBuilder.JsonPlayerEvent playerEvent){
        this.paused = playerEvent.pauseGame;
        this.currentEvent = playerEvent;
        if(playerEvent.focusOnEvent) ColonyGame.camera.position.set(playerEvent.eventTarget.transform.getPosition().x, playerEvent.eventTarget.transform.getPosition().y, 0);
    }

    public static PlayerInterface getInstance(){
        if(playerInterface == null) playerInterface = new PlayerInterface(ColonyGame.batch, ColonyGame.world);
        return playerInterface;
    }

    @Override
    public void destroy() {
        super.destroy();
        this.batch = null;
        selectedProfile = null;
        this.world = null;
        this.buttonRect = null;
        this.infoRect = null;
    }

    @Override
    public void resize(int width, int height) {
        this.buttonRect.set(0, Gdx.graphics.getHeight() - 100, 200, 100);

        this.uiBackgroundBaseRect.set(0, 0, width, height * mainHeight);
        this.infoRect.set(0, 0, width * infoWidth, height* mainHeight);
        this.statusRect.set(infoRect.x + infoRect.width, 0, width*statusWidth, height*mainHeight);
        this.tabsRect.set(statusRect.x + statusRect.width, 0, width * tabsWidth, height * mainHeight);
        this.ordersRect.set(tabsRect.x + tabsRect.width, 0, width - (tabsRect.x + tabsRect.width), height * mainHeight);

        this.uiBackgroundTopRect.set(0, uiBackgroundBaseRect.y + uiBackgroundBaseRect.height, width, height * topHeight);
        this.infoTopRect.set(uiBackgroundTopRect.x, uiBackgroundTopRect.y, width * infoWidth, uiBackgroundTopRect.height); //The top info area
        this.statusTopRect.set(infoTopRect.x + infoTopRect.width, uiBackgroundTopRect.y, width*statusWidth, uiBackgroundTopRect.height); //The top status area
        this.tabsTopRect.set(statusTopRect.x + statusTopRect.width, uiBackgroundTopRect.y, width * tabsWidth, uiBackgroundTopRect.height); //The top tabs area
        this.ordersTopRect.set(tabsTopRect.x + tabsTopRect.width, uiBackgroundTopRect.y, width - (tabsRect.x + tabsRect.width), uiBackgroundTopRect.height); //The top orders ares

        this.bottomLeftRect.set(width - 100, 0, 100, height * 0.05f);
        this.stage.getViewport().update(width, height);
    }

    @Override
    public void addToList() {
        ListHolder.addGUI(this);
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.F1) //F1 - draw info
            this.drawingInfo = !this.drawingInfo;
        else if(keycode == Input.Keys.F2) //F2 - draw profiler
            Profiler.enabled = this.drawingProfiler = !this.drawingProfiler;
        else if(keycode == Input.Keys.F3) { //F3 - draw grid
            this.drawGrid = !this.drawGrid;
        }else if(keycode == Input.Keys.SPACE) //SPACE - toggle paused
            this.paused = !this.paused;
        else if(keycode == Input.Keys.F4) //F4 - reveal map
            this.revealMap();
        else if(keycode == Input.Keys.PLUS)
            gameSpeed*=2;
        else if(keycode == Input.Keys.MINUS)
            gameSpeed*=0.5f;
        else
            return false;

        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if(button == Input.Buttons.LEFT) {
            if(!this.isOnUI()) {
                this.mouseDown = true;
                Vector3 worldCoords = ColonyGame.camera.unproject(new Vector3(screenX, screenY, 0));
                startDragging(worldCoords.x, worldCoords.y);
            }
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if(this.isOnUI() && !this.dragging)
            return false;

        this.mouseDown = false;
        Vector3 worldCoords = ColonyGame.camera.unproject(new Vector3(screenX, screenY, 0));

        if(button == Input.Buttons.LEFT){
            this.selectedProfileList.clear();
            this.buttonStateSystem.setToDefaultState();
            this.currStateNode = null;
            this.finishDragging(worldCoords.x, worldCoords.y);

            //Otherwise, null out our existing selection and try to get a new one where we clicked.
            if(!newlySelected) {
                newlySelected = false;
                selectedProfile = null;

                //Try to get a selection of Entities. If not, maybe we clicked just one?
                if (this.selectedProfileList.size() < 1) {
                    this.testPoint.set(worldCoords.x, worldCoords.y);
                    this.world.QueryAABB(this.callback, worldCoords.x - 0.01f, worldCoords.y - 0.011f, worldCoords.x + 0.01f, worldCoords.y + 0.01f);
                }
            }

            newlySelected = false;
            return true;

        //On right click, let's tell everyone we have selected to move!
        }if(button == Input.Buttons.RIGHT){
            for(UnitProfile profile : selectedProfileList) {
                BehaviourManagerComp comp = profile.interactable.getInteractable().getBehManager();
                if (comp != null) {
                    comp.getBlackBoard().target = null;
                    comp.getBlackBoard().targetNode = comp.getBlackBoard().colonyGrid.getNode(new Vector2(worldCoords.x, worldCoords.y));
                    comp.changeTaskImmediate(PrebuiltTasks.moveTo(comp.getBlackBoard()));
                }
            }
        }

        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(mouseDown) {
            Vector3 worldCoords = ColonyGame.camera.unproject(new Vector3(screenX, screenY, 0));
            drag(worldCoords.x, worldCoords.y);
        }

        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        ColonyGame.camera.zoom += amount*Gdx.graphics.getDeltaTime()*camZoomSpeed;
        if(ColonyGame.camera.zoom < 0) ColonyGame.camera.zoom = 0;
        return false;
    }

    private static class UnitProfile{
        public Entity entity;
        public Interactable interactable;
        public GridComponent gridComp;

        public UnitProfile(){

        }

        public UnitProfile(Entity entity){
            this.entity = entity;
            this.interactable = entity.getComponent(Interactable.class);
            this.gridComp = entity.getComponent(GridComponent.class);
        }
    }


}
