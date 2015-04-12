package com.mygdx.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.*;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.helpers.*;
import com.mygdx.game.helpers.gui.GUI;
import com.mygdx.game.helpers.timer.RepeatingTimer;
import com.mygdx.game.helpers.timer.Timer;
import com.mygdx.game.helpers.worldgeneration.WorldGen;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.interfaces.IGUI;
import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.server.ServerPlayer;

import java.util.ArrayList;

/**
 * Created by Bbent_000 on 12/25/2014.
 */
public class PlayerInterface extends UI implements IGUI, InputProcessor {
    private SpriteBatch batch;
    private Texture background, UIBackgroundBase, UIBackgroundTop;
    private World world;
    private ServerPlayer gameScreen;

    private State buttonState = new State();

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

    private Texture[] gatherButtonTextures, exploreButtonTextures, huntButtonTextures, blankButtonTextures;
    private GUI.GUIStyle gatherStyle = new GUI.GUIStyle();
    private GUI.GUIStyle exploreStyle = new GUI.GUIStyle();
    private GUI.GUIStyle huntStyle = new GUI.GUIStyle();
    private GUI.GUIStyle blankStyle = new GUI.GUIStyle();

    private GUI.GUIStyle UIStyle;

    private Interactable interactable = null;

    private Timer FPSTimer;

    private Vector2 testPoint = new Vector2();
    private Entity selected = null;

    private Color gray = new Color(Color.BLACK);

    private Texture blueSquare = ColonyGame.assetManager.get("blueSquare", Texture.class);

    private void loadHuntButtonStyle(){
        huntStyle.normal = ColonyGame.assetManager.get("huntbutton_normal", Texture.class);
        huntStyle.moused = ColonyGame.assetManager.get("huntbutton_moused", Texture.class);
        huntStyle.clicked = ColonyGame.assetManager.get("huntbutton_clicked", Texture.class);
    }

    //For selecting a single unit.
    private QueryCallback callback = fixture -> {
        Collider.ColliderInfo info = (Collider.ColliderInfo)fixture.getUserData();
        if(info.tags.hasTag(Constants.COLLIDER_CLICKABLE) && fixture.testPoint(testPoint.x, testPoint.y)){
            this.selected = info.owner;
            this.interactable = this.selected.getComponent(Interactable.class);
            return false;
        }

        return true;
    };

    //For selecting multiple units.
    private ArrayList<UnitProfile> selectedList = new ArrayList<>();
    private QueryCallback selectionCallback = fixture -> {
        Collider.ColliderInfo selectedInfo = (Collider.ColliderInfo)fixture.getUserData();
        //If not null, the entity is a colonist, and the collider is clickable.
        if(selectedInfo != null && selectedInfo.owner.hasTag(Constants.ENTITY_COLONIST) && selectedInfo.tags.hasTag(Constants.COLLIDER_CLICKABLE)) {
            UnitProfile profile = new UnitProfile(); //Make a unit profile.
            profile.entity = this.selected = selectedInfo.owner; //Get the Entity that we clicked.
            profile.interactable = this.interactable = selectedInfo.owner.getComponent(Interactable.class); //Get teh interactable Component.
            selectedList.add(profile);
            return true;
        }
        return true;
    };

    /**
     * A player interface Component that will display information on the screen.
     * @param batch The SpriteBatch for drawing to the screen.
     * @param world The Box2D world. We need to know about this for clicking on objects.
     */
    public PlayerInterface(SpriteBatch batch, ColonyGame game, ServerPlayer gameScreen, World world) {
        super(batch, game);
        this.batch = batch;
        this.world = world;
        this.gameScreen = gameScreen;

        this.background = ColonyGame.assetManager.get("background", Texture.class);
        this.UIBackgroundBase = ColonyGame.assetManager.get("UIBackground_base", Texture.class);
        this.UIBackgroundTop = ColonyGame.assetManager.get("UIBackground_top", Texture.class);

        this.buttonRect.set(0, Gdx.graphics.getHeight() - 100, 200, 100);

        Functional.Callback callback = () -> this.FPS = 1/Gdx.graphics.getDeltaTime();
        this.FPSTimer = new RepeatingTimer(0.5d, callback);

        //Loads and configures stuff about buttons tyles.
        loadHuntButtonStyle();

        this.generateFonts();

        gatherButtonTextures = new Texture[3];
        gatherStyle.normal = gatherButtonTextures[0] = ColonyGame.assetManager.get("axebutton_normal", Texture.class);
        gatherStyle.moused = gatherButtonTextures[1] = ColonyGame.assetManager.get("axebutton_moused", Texture.class);
        gatherStyle.clicked = gatherButtonTextures[2] = ColonyGame.assetManager.get("axebutton_clicked", Texture.class);

        exploreButtonTextures = new Texture[3];
        exploreStyle.normal = exploreButtonTextures[0] = ColonyGame.assetManager.get("explorebutton_normal", Texture.class);
        exploreStyle.moused = exploreButtonTextures[1] = ColonyGame.assetManager.get("explorebutton_moused", Texture.class);
        exploreStyle.clicked = exploreButtonTextures[2] = ColonyGame.assetManager.get("explorebutton_clicked", Texture.class);

        huntButtonTextures = new Texture[3];
        huntStyle.normal = huntButtonTextures[0] = ColonyGame.assetManager.get("huntbutton_normal", Texture.class);
        huntStyle.moused = huntButtonTextures[1] = ColonyGame.assetManager.get("huntbutton_moused", Texture.class);
        huntStyle.clicked = huntButtonTextures[2] = ColonyGame.assetManager.get("huntbutton_clicked", Texture.class);

        blankButtonTextures = new Texture[3];
        blankStyle.normal = blankButtonTextures[0] = ColonyGame.assetManager.get("blankbutton_normal", Texture.class);
        blankStyle.moused = blankButtonTextures[1] = ColonyGame.assetManager.get("blankbutton_moused", Texture.class);
        blankStyle.clicked = blankButtonTextures[2] = ColonyGame.assetManager.get("blankbutton_clicked", Texture.class);

        gatherStyle.font.setColor(new Color(126f / 255f, 75f / 255f, 27f / 255f, 1));
        exploreStyle.font.setColor(new Color(126f / 255f, 75f / 255f, 27f / 255f, 1));
        huntStyle.font.setColor(126f / 255f, 75f / 255f, 27f / 255f, 1);

        Gdx.input.setInputProcessor(this);

        buttonState.addState("main", true);
        buttonState.addState("gather_list");
        buttonState.addState("hunt_list");
        buttonState.setCurrState("main");
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
    }

    @Override
    public void render(float delta, SpriteBatch batch) {
        super.render(delta, batch);
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

        //Draw stuff about the selected entity.
        if((this.selected != null && this.interactable != null) || this.selectedList.size() > 0){
            GUI.Texture(this.UIBackgroundBase, this.uiBackgroundBaseRect, this.batch);
            GUI.Texture(this.UIBackgroundTop, this.uiBackgroundTopRect, this.batch);
            this.drawMultipleProfiles(this.ordersRect);
            this.drawSelected();
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
            this.batch.draw(WorldGen.getInstance().whiteTex, start.x, start.y, end.x - start.x, end.y - start.y);
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
     * @param height
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
        GUI.Label(tile.category, this.batch, rect.x, rect.getY(), rect.getWidth()*0.5f, rect.getHeight()*0.5f);
    }

    /**
     * Draws the multiple selected profiles.
     * @param rect The rectangle to draw the information inside of.
     */
    private void drawMultipleProfiles(Rectangle rect){
        //If there is more than one unit selected.. display in a group format.
        if(selectedList.size() > 1){
            profileButtonRect.set(rect.getX() + rect.getWidth() - 115, rect.getY() + rect.getHeight() - 20, 50, 20);

            //For each profile, draw a button to access each individual entity.
            for(UnitProfile profile : selectedList) {
                if(GUI.Button(profileButtonRect, profile.interactable.getInteractable().getName(), this.batch)){ //Draw the button.
                    this.selected = profile.entity;
                    this.interactable = profile.interactable;
                }

                //If we go too far down the screen, move over.
                profileButtonRect.setY(profileButtonRect.getY() - 22);
                if(profileButtonRect.y <= rect.getY() + 10)
                    profileButtonRect.set(rect.getX() + rect.getWidth() - 65, rect.getY() + rect.getHeight() - 20, 50, 20);

            }
        }
    }

    /**
     * Displays the selected Entity.
     */
    private void drawSelected(){
        //Make sure the interactable we have selected isn't null!
        if(this.interactable.getInteractable() != null){
            IInteractable interactable = this.interactable.getInteractable(); //Get the interactable!

            //If it has a name, draw the name...
            if(interactable.getName() != null)
                GUI.Label(interactable.getName(), this.batch, this.infoTopRect, this.UIStyle);

            //If it has stats, draw the stats...
            if(interactable.getStats() != null){
                //GUI.Texture(statusRect, ColonyGame.assetManager.get("menuButton_normal", Texture.class), this.batch);
                Stats stats = interactable.getStats();

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
            }else if(interactable.getStatsText() != null){
                GUI.Label("Resources", this.batch, this.statusTopRect, this.UIStyle);
                this.UIStyle.multiline = true;
                this.UIStyle.alignment = Align.topLeft;
                this.UIStyle.paddingLeft = 10;
                this.UIStyle.paddingTop = 5;
                GUI.Label(interactable.getStatsText(), this.batch, this.statusRect, this.UIStyle);
                this.UIStyle.paddingLeft = 0;
                this.UIStyle.paddingTop = 0;
                this.UIStyle.alignment = Align.center;
                this.UIStyle.multiline = false;
            }

            //If it has an inventory, draw the inventory...
            if(interactable.getInventory() != null){
                //GUI.Texture(tabsRect, ColonyGame.assetManager.get("menuButton_normal", Texture.class), this.batch);

                GUI.Label("Inventory", this.batch, this.tabsTopRect, this.UIStyle);
                ArrayList<Inventory.InventoryItem> itemList = interactable.getInventory().getItemList();
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

            //If it's a humanoid that we can control, draw some order buttons and its current path.
            if(this.interactable.interType.equals("humanoid")){
                GUI.Label("Orders", this.batch, this.ordersTopRect, this.UIStyle);

                //GUI.Texture(ordersRect, ColonyGame.assetManager.get("menuButton_normal", Texture.class), this.batch);
                if(interactable.getBehManager() != null) {
                    drawButtons(interactable);

                    //Set to world camera and draw the path lines.
                    batch.setProjectionMatrix(ColonyGame.camera.combined);
                    BehaviourManagerComp.Line[] lines = interactable.getBehManager().getLines();
                    for(BehaviourManagerComp.Line line : lines)
                        batch.draw(blueSquare, line.startX, line.startY, 0, 0, line.width, 0.1f, 1, 1, line.rotation, 0, 0, blueSquare.getWidth(), blueSquare.getHeight(), false, false);

                    //Set back to UI camera.
                    batch.setProjectionMatrix(ColonyGame.UICamera.combined);
                    GUI.Label(interactable.getBehManager().getCurrentTaskName(), this.batch, this.ordersRect.x, this.ordersRect.y + 70, this.ordersRect.width, this.ordersRect.height - 50, this.UIStyle);
                }
            }
        }
    }

    //Draws the buttons for each selected colonist that we have control of.
    private void drawButtons(IInteractable interactable){
        if(buttonState.isState("main")) {
            float middleX = ordersRect.x + ordersRect.width/2;
            float bottomY = ordersRect.y + 25;
            float start = 75 + 50/2;

            //Draw the gather button.
            orderButtonRect.set(middleX - start, bottomY, 50, 50);
            if (GUI.Button(orderButtonRect, "", this.batch, this.gatherStyle)) {
                for(UnitProfile profile : selectedList)
                    profile.interactable.getInteractable().getBehManager().gather();
                buttonState.setCurrState("gather_list");
            }

            //Draw the explore button.
            orderButtonRect.set(middleX, bottomY, 50, 50);
            if (GUI.Button(orderButtonRect, "", this.batch, this.exploreStyle)) {
                interactable.getBehManager().explore();
            }

            //Draw the hunt button.
            orderButtonRect.set(middleX + start, bottomY, 50, 50);
            if (GUI.Button(orderButtonRect, "", this.batch, this.huntStyle)) {
                interactable.getBehManager().searchAndAttack();
                buttonState.setCurrState("hunt_list");
            }

        //If we are in the 'gather_list' state, then list the gather tasks.
        }else if(buttonState.isState("gather_list")){
            Array<BehaviourManagerComp.TaskState> taskList = interactable.getBehManager().getTaskStates();

            //Set some position variables.
            float width = (ordersRect.getWidth()/(taskList.size+1));
            float height = ordersRect.y + 25;
            float x = ordersRect.x;

            //For each available task...
            for(int i=0;i<taskList.size;i++){
                BehaviourManagerComp.TaskState taskState = taskList.get(i); //Get the taskState
                orderButtonRect.set(x + (i+1)*width, height, 50, 50); //Set the location.
                this.blankStyle.toggled = taskState.toggled; //Set the value of the toggle.
                if(this.blankStyle.toggled) this.blankStyle.normal = this.blankButtonTextures[2]; //If toggled, set the icon to 'clicked' for the normal state. Makes it look toggled!

                //The button press...
                if (GUI.Button(orderButtonRect, taskState.taskName, this.batch, this.blankStyle)) {
                    int tag = StringTable.getString("resource_type", taskState.taskName);   //Get the tag
                    interactable.getBehManager().getBlackBoard().resourceTypeTags.toggleTag(tag); //Toggle the tag
                    taskState.toggled = !taskState.toggled;                                 //Toggle the toggle!

                    //Foreach profile, tell them to do the same thing and sync them up with the currently selected interactable.
                    for(UnitProfile profile : selectedList) {
                        //Get the behaviour component and add or remove the tag.
                        BehaviourManagerComp profileBeh = profile.interactable.getInteractable().getBehManager();
                        if(taskState.toggled) profileBeh.getBlackBoard().resourceTypeTags.addTag(tag); //Toggle the tag
                        else profileBeh.getBlackBoard().resourceTypeTags.removeTag(tag); //Toggle the tag

                        //Match up the other interactables with the currently selected one.
                        profileBeh.getTaskState(taskState.taskName).toggled = taskState.toggled;
                    }
                }

                this.blankStyle.toggled = false; //Reset this value.
                this.blankStyle.normal = this.blankButtonTextures[0];
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
        GUI.Texture(WorldGen.getInstance().whiteTex, outerX, y, width, height, batch);

        //Draw the inner rectangle (shrink it by 2 inches on all sides, 'padding')
        batch.setColor(Color.GREEN);
        float newWidth = (currVal/maxVal)*(width-4);
        GUI.Texture(WorldGen.getInstance().whiteTex, innerX, y + 2, newWidth, height - 4, batch);

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
        selectionBox.set(selectionBox.x, selectionBox.y, x - selectionBox.x, y - selectionBox.y);
        Vector2 center = new Vector2();
        selectionBox.getCenter(center);
        float halfWidth = Math.abs(selectionBox.getWidth()/2);
        float halfHeight = Math.abs(selectionBox.getHeight()/2);
        selectionBox.set(center.x - halfWidth, center.y - halfHeight, center.x + halfWidth, center.y + halfHeight);
        this.world.QueryAABB(this.selectionCallback, selectionBox.x, selectionBox.y, selectionBox.getWidth(), selectionBox.getHeight());
        this.dragging = false;
    }

    /**
     * If the GUI is moused over or not.
     * @return True if moused over, false otherwise.
     */
    public boolean isOnUI(){
        boolean windowActive = ((this.selected != null && this.interactable != null) || this.selectedList.size() > 0);
        return this.checkMousedOver() && windowActive;
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

    @Override
    public void destroy() {
        super.destroy();
        this.background.dispose();
        this.batch = null;
        this.interactable = null;
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

        this.bottomLeftRect.set(width - 100, 0, 100, height*0.05f);
    }

    @Override
    public void addToList() {
        ListHolder.addGUI(this);
    }

    @Override
    public boolean keyDown(int keycode) {
        if(keycode == Input.Keys.F1)
            this.drawingInfo = !this.drawingInfo;
        else if(keycode == Input.Keys.F2)
            Profiler.enabled = this.drawingProfiler = !this.drawingProfiler;
        else if(keycode == Input.Keys.F3) {
            this.drawGrid = !this.drawGrid;
        }else if(keycode == Input.Keys.SPACE)
            this.gameScreen.setPaused(!this.gameScreen.getPaused());
        else if(keycode == Input.Keys.F4)
            this.revealMap();
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
            this.selectedList.clear();
            this.buttonState.setToDefaultState();
            this.selected = null;
            this.interactable = null;
            this.finishDragging(worldCoords.x, worldCoords.y);
            if(this.selectedList.size() < 1) {
                this.testPoint.set(worldCoords.x, worldCoords.y);
                this.world.QueryAABB(this.callback, worldCoords.x - 0.01f, worldCoords.y - 0.011f, worldCoords.x + 0.01f, worldCoords.y + 0.01f);
            }
            return true;
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

    private class UnitProfile{
        public Entity entity;
        public Interactable interactable;
        public GridComponent gridComp;
    }


}
