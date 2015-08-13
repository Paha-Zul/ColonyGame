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
import com.mygdx.game.component.*;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.interfaces.IGUI;
import com.mygdx.game.interfaces.IInteractable;
import com.mygdx.game.util.*;
import com.mygdx.game.util.gui.GUI;
import com.mygdx.game.util.managers.DataManager;
import com.mygdx.game.util.managers.NotificationManager;
import com.mygdx.game.util.managers.PlayerManager;
import com.mygdx.game.util.timer.OneShotTimer;
import com.mygdx.game.util.timer.RepeatingTimer;
import com.mygdx.game.util.timer.Timer;
import com.mygdx.game.util.worldgeneration.WorldGen;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Bbent_000 on 12/25/2014.
 */
public class PlayerInterface extends UI implements IGUI, InputProcessor {
    public boolean paused = false;
    public static boolean active = false;
    public float gameSpeed = 1;

    private TextureRegion background, UIBackgroundBase, UIBackgroundTop;
    private World world;

    private boolean drawingInfo = false;
    private boolean drawingProfiler = false;
    private boolean mouseDown = false;
    private boolean dragging = false;
    private boolean drawGrid = false;
    public boolean renderWorld = true;

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

    private Rectangle reusableImageLabelRect = new Rectangle();

    private GUI.GUIStyle gatherStyle = new GUI.GUIStyle();
    private GUI.GUIStyle exploreStyle = new GUI.GUIStyle();
    private GUI.GUIStyle huntStyle = new GUI.GUIStyle();
    private GUI.GUIStyle blankStyle = new GUI.GUIStyle();
    private GUI.GUIStyle skillsStyle = new GUI.GUIStyle();
    private GUI.GUIStyle gameSpeedStyle = new GUI.GUIStyle();
    private GUI.GUIStyle notificationStyle = new GUI.GUIStyle();
    private GUI.GUIStyle mousedOverNotiStyle = new GUI.GUIStyle();
    private GUI.GUIStyle UIStyle;
    private Timer FPSTimer;

    private Vector2 testPoint = new Vector2(); //A reusable vector
    private boolean newlySelected = false; //If we have selected a new entity, this will stop the left click from getting rid of it.
    private UnitProfile selectedProfile = null; //The currently selected UnitProfile.
    private ArrayList<UnitProfile> selectedProfileList = new ArrayList<>(); //The list of selected UnitProfiles.

    private Color gray = new Color(Color.BLACK);
    private Texture blueSquare;
    private Stage stage;
    private static PlayerInterface playerInterface;

    private DataBuilder.JsonPlayerEvent currentEvent;
    private GUI.GUIStyle eventDescStyle, eventTitleStyle;

    private TextureRegion whiteTexture;
    private NotificationManager.Notification mousedOverNotification = null;

    private boolean extendedTooltip;
    private Timer extendedTooltipTimer = new OneShotTimer(2f, () -> extendedTooltip = true);

    /**
     * Stuff for drawing colony
     */
    public boolean drawingColony = false;
    private Rectangle colonyScreenRect = new Rectangle();
    private TextureRegion colonyScreen;

    private void loadHuntButtonStyle(){
        huntStyle.normal = ColonyGame.assetManager.get("huntbutton_normal", Texture.class);
        huntStyle.moused = ColonyGame.assetManager.get("huntbutton_moused", Texture.class);
        huntStyle.clicked = ColonyGame.assetManager.get("huntbutton_clicked", Texture.class);
    }

    //For selecting a single unit.
    private QueryCallback callback = fixture -> {
        Collider.ColliderInfo info = (Collider.ColliderInfo)fixture.getUserData();
        if(info.tags.hasTag(Constants.COLLIDER_CLICKABLE) && fixture.testPoint(testPoint.x, testPoint.y) && info.owner.getTags().hasTag("selectable")){
            setSelectedEntity(info.owner);
            return false;
        }

        return true;
    };

    //For selecting many units
    private QueryCallback selectionCallback = fixture -> {
        Collider.ColliderInfo selectedInfo = (Collider.ColliderInfo)fixture.getUserData();

        //If not null, the entity is a colonist, and the collider is clickable.
        if(selectedInfo != null && selectedInfo.owner.getTags().hasTags("colonist", "alive", "selectable") && selectedInfo.tags.hasTag(Constants.COLLIDER_CLICKABLE)) {
            UnitProfile profile = setSelectedEntity(selectedInfo.owner); //Set our selectedEntity
            selectedProfileList.add(profile); //Add it to the list.
            return true;
        }
        return true;
    };

    /**
     * A player interface Component that will display information on the screen.
     * @param batch The SpriteBatch for drawing to the screen.
     * @param world The Box2D saveContainer. We need to know about this for clicking on objects.
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

        gameSpeedStyle.background = new TextureRegion(ColonyGame.assetManager.get("eventWindowBackground", Texture.class));
        notificationStyle.background = new TextureRegion(ColonyGame.assetManager.get("notificationIcon", Texture.class));
        notificationStyle.font = DataManager.getData("changelogFont", BitmapFont.class);

        gatherStyle.font.setColor(new Color(126f / 255f, 75f / 255f, 27f / 255f, 1));
        exploreStyle.font.setColor(new Color(126f / 255f, 75f / 255f, 27f / 255f, 1));
        huntStyle.font.setColor(126f / 255f, 75f / 255f, 27f / 255f, 1);

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(stage);
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);

        ColonyGame.camera.zoom = 1.5f;

        this.colonyScreen = new TextureRegion(ColonyGame.assetManager.get("eventWindowBackground", Texture.class));
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
        int screenW = Gdx.graphics.getWidth(), screenH = Gdx.graphics.getHeight();

        batch.setProjectionMatrix(ColonyGame.UICamera.combined);
        GUI.font.setColor(Color.WHITE);

        int height = Gdx.graphics.getHeight();
        FPSTimer.update(delta);

        this.moveCamera(); //Move the camera
        this.drawSelectionBox(); //Draws the selection box.
        this.drawDebugInfo(height); //Draws some debug information.
        this.drawTerrainInfo(this.bottomLeftRect); //Draws information about the moused over terrain piece.
        this.drawGameSpeed(screenW, screenH, batch, this.gameSpeedStyle);
        this.drawSelectedEntity();
        this.drawInvAmounts(screenW, screenH, batch);
        this.drawCurrentNotifications(screenW, screenH, delta);
        this.drawColonyScreen();

        if(this.drawingProfiler) Profiler.drawDebug(batch, 200, height - 20);

        //Draw the grid squares if enabled.
        if(drawGrid) {
            ColonyGame.worldGrid.debugDraw();
            drawBox2DDebug();
        }

        drawCurrentEvent(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        batch.end();
        batch.begin();
        stage.act(delta);
        stage.draw();
        batch.end();
        batch.begin();
    }

    /**
     * Checks if the GUI is moused over or not.
     */
    private boolean checkMousedOver(){
        //Determines if any UI is moused over or not.
        return this.uiBackgroundBaseRect.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY()) ||
                this.uiBackgroundBaseRect.contains(Gdx.input.getX(), Gdx.graphics.getHeight() - Gdx.input.getY());
    }

    private void drawInvAmounts(int width, int height, SpriteBatch batch){
        PlayerManager.Player player = PlayerManager.getPlayer("Player");
        if(player != null) {
            batch.setColor(Color.WHITE);
            HashMap<String, Inventory.InventoryItem> inv = PlayerManager.getPlayer("Player").colony.getGlobalInv();
            StringBuilder builder = new StringBuilder();
            builder.append("Overall Items:\n");
            int counter = 0;
            for (Inventory.InventoryItem item : inv.values()) {
                if (item.getAmount(false) != 0) {
                    builder.append(item.getAmount(false)).append(" ").append(item.itemRef.getDisplayName()).append("\n");
                    counter++;
                }
            }

            gameSpeedStyle.multiline = true;
            gameSpeedStyle.alignment = Align.top;
            gameSpeedStyle.paddingTop = 5;
            GUI.Label(builder.toString(), batch, 0, height * 0.2f, width * 0.07f, height * 0.2f, gameSpeedStyle);
            gameSpeedStyle.paddingTop = 0;
        }
    }

    private void drawCurrentNotifications(int width, int height, float delta){
        Array<NotificationManager.Notification> list = NotificationManager.getActiveNotifications();
        float x = width - 80f, y = height*0.4f;
        float labelW = 50f, labelH = 50f;
        NotificationManager.Notification newlyMousedOver = null; //This is like a flag.

        notificationStyle.wrap = true;
        for(int i=0;i<list.size;i++){
            NotificationManager.Notification notification = list.get(i);

            //GUI.Texture(whiteTexture, batch, x, y, labelW, labelH);
            if(GUI.Label(notification.name, this.batch, x, y, labelW, labelH, notificationStyle)){
                newlyMousedOver = notification; //Set the flag(ish) variable.

                mousedOverNotiStyle.wrap = true;
                String toolTip = extendedTooltip ? notification.extendedTooltip : notification.quickTooltip;
                GUI.Label(toolTip, batch, Gdx.input.getX() - 250, height - Gdx.input.getY() - 50, 200, 100, mousedOverNotiStyle);
            }
            y += labelH + 10f;
        }

        //If we are still moused over the same notification, increase the timer.
        if(this.mousedOverNotification != null && this.mousedOverNotification == newlyMousedOver) extendedTooltipTimer.update(delta);
        else {
            this.extendedTooltip = false;
            extendedTooltipTimer.restart();
        }
        this.mousedOverNotification = newlyMousedOver;
    }

    private void drawSelectedEntity(){
        //Draw stuff about the selectedEntity entity.
        if(selectedProfile != null || this.selectedProfileList.size() > 0){
            GUI.Texture(this.UIBackgroundBase, this.batch, this.uiBackgroundBaseRect);
            GUI.Texture(this.UIBackgroundTop, this.batch, this.uiBackgroundTopRect);
            this.drawMultipleProfiles(this.ordersRect);
            this.drawSelected();
        }
    }

    private void drawCurrentEvent(int width, int height){
        if(this.currentEvent != null){
            float windowWidth = width/3.2f, windowHeight = height/2.7f;
            float windowX = width/2 - windowWidth/2, windowY = height/2 - windowHeight/2;

            float titleWidth = windowWidth*0.4f, titleHeight = windowHeight*0.1f;
            float titleX = windowX+ windowWidth*0.05f, titleY = windowY + windowHeight - titleHeight - windowHeight*0.03f;

            float descWidth = windowWidth*0.9f, descHeight = windowHeight*0.6f;
            float descX = windowX + windowWidth*0.05f, descY = windowY + windowHeight*0.25f;

            GUI.Texture(new TextureRegion(ColonyGame.assetManager.get("eventWindowBackground", Texture.class)), this.batch, windowX, windowY, windowWidth, windowHeight);

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
                if(GUI.Button(this.batch, choice, windowX + (i+1)*spacing + i*buttonWidth, windowY + windowHeight*0.01f, buttonWidth, buttonHeight, blankStyle) == GUI.UP){
                    BehaviourManagerComp comp = this.currentEvent.eventTarget.getComponent(BehaviourManagerComp.class);
                    if(comp == null) return;
                    comp.getBlackBoard().target = this.currentEvent.eventTargetOther;
                    comp.changeTaskImmediate(this.currentEvent.behaviours[i]);
                    this.gameSpeed = 1f; //Reset game speed
                    this.paused = false; //Unpause
                    this.currentEvent = null;
                    return;
                }
            }
        }
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
     * Attempts to deselect this entity from the selected list.
     * @param entity The Entity to attempt to deselect.
     */
    public void deselectEntity(Entity entity){
        if(this.selectedProfile != null && entity == this.selectedProfile.entity) {
            this.selectedProfile.entity.getTags().removeTag("selected");
            this.selectedProfile = null;
        }

        for(int i=0;i<=this.selectedProfileList.size();i++)
            if(this.selectedProfileList.get(i).entity == entity) {
                this.selectedProfileList.remove(i);
                break;
            }

        if(this.selectedProfile == null && this.selectedProfileList.size() > 0)
            this.selectedProfile = this.selectedProfileList.get(0);
    }

    private void drawColonyScreen(){
        if(!this.drawingColony) return;

        GUI.Texture(this.colonyScreen, this.batch, this.colonyScreenRect);
        this.drawColonyInventory(PlayerManager.getPlayer("Player").colony.getInventory(), this.colonyScreenRect);
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

        GUI.Texture(this.background, this.batch, rect);
        int index[] = grid.getIndex(mouseCoords.x, mouseCoords.y);
        Grid.Node node = grid.getNode(index);
        if(node == null) return;
        Grid.TerrainTile tile = grid.getNode(index).getTerrainTile();
        if(tile == null) return;
        GUI.Label(tile.tileRef.category, this.batch, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
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
            for(int i=0;i<selectedProfileList.size();i++) {
                UnitProfile profile = selectedProfileList.get(i);
                if(!profile.entity.getTags().hasTag("alive")){
                    profile.entity.getTags().removeTag("selected");
                    selectedProfileList.remove(i);
                    i--;
                    continue;
                }

                //Draw the button for the individual profile. If clicked, make it our selected profile.
                if(GUI.Button(this.batch, profileButtonRect, profile.interactable.getInteractable().getName()) == GUI.UP)
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
            if(selectedProfileList.size() > 1 && !selectedProfile.entity.getTags().hasTag("alive")){
                selectedProfileList.forEach(profile -> {if(profile.entity != selectedProfile.entity) selectedProfile = profile;});
            }

            IInteractable innerInter = selectedProfile.interactable.getInteractable(); //Get the selectedProfile!
            if(innerInter == null) {
                selectedProfile = null;
                return;
        }

            //If it has a compName, draw the compName...
            if(innerInter.getName() != null) {
                GUI.Label(innerInter.getName(), this.batch, this.infoTopRect, this.UIStyle);
            }

            //If it has stats, draw the stats...
            if(innerInter.getStats() != null){
                GUI.Label("Stats", this.batch, this.statusTopRect, this.UIStyle);

                //GUI.Texture(statusRect, ColonyGame.assetManager.get("menuButton_normal", Texture.class), this.batch);
                Stats stats = innerInter.getStats();
                Rectangle rect = this.statusRect;

                ArrayList<Stats.Stat> list = stats.getStatList();
                float space = ((rect.height - list.size()*20)/list.size()+1)/2;
                float x = rect.x + 10;
                float y = rect.y + rect.height - 20;
                float barWidth = rect.getWidth()*0.4f;
                float barHeight = barWidth*0.2f;

                for(int i=0;i<list.size();i++){
                    Stats.Stat stat = list.get(i);
                    drawBar(stat.name, x, y-(i+1)*space - 20*i, barWidth, barHeight, stat.getCurrVal(), stat.getMaxVal());
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
                this.drawInventory(innerInter.getInventory(), this.tabsRect);
            }

            if(innerInter.getBehManager() != null){
                GUI.Label("currTask: "+innerInter.getBehManager().getCurrentTaskName(), this.batch, this.ordersRect.x, this.ordersRect.y + 70, this.ordersRect.width, this.ordersRect.height - 50, this.UIStyle);
                GUI.Label("nextTask: "+innerInter.getBehManager().getNextTaskName(), this.batch, this.ordersRect.x, this.ordersRect.y + 60, this.ordersRect.width, this.ordersRect.height - 50, this.UIStyle);
                GUI.Label("currState: "+innerInter.getBehManager().getBehaviourStates().getCurrState().stateName, this.batch, this.ordersRect.x, this.ordersRect.y + 50, this.ordersRect.width, this.ordersRect.height - 50, this.UIStyle);

                //If it's a humanoid that we can control, draw some order buttons and its current path.
                if(selectedProfile.interactable.interType.equals("humanoid")){
                    GUI.Label("Orders", this.batch, this.ordersTopRect, this.UIStyle);

                    //GUI.Texture(ordersRect, ColonyGame.assetManager.get("menuButton_normal", Texture.class), this.batch);
                    if(innerInter.getBehManager() != null) {
                        drawBehaviourButtons(innerInter);

                        //Set to saveContainer camera and draw the path lines.
                        batch.setProjectionMatrix(ColonyGame.camera.combined);
                        BehaviourManagerComp.Line[] lines = innerInter.getBehManager().getLines();
                        for(BehaviourManagerComp.Line line : lines)
                            batch.draw(blueSquare, line.startX, line.startY, 0, 0, line.width, 0.1f, 1, 1, line.rotation, 0, 0, blueSquare.getWidth(), blueSquare.getHeight(), false, false);

                        //Set back to UI camera.
                        batch.setProjectionMatrix(ColonyGame.UICamera.combined);

                    }
                }
            }

            //If it has a constructable...
            if(innerInter.getConstructable() != null){
                Constructable constructable = innerInter.getConstructable();
                Rectangle rect = this.ordersRect;
                GUI.Label("constructing", this.batch, rect.x, rect.y + rect.height-20, rect.width, 20);
                GUI.Label("Progress", this.batch, rect.x, rect.y + rect.height - 40, rect.width, 20);
                GUI.DrawBar(this.batch, rect.x + rect.width/2 - 50, rect.y + rect.height - 60, 100, 20, constructable.getPercentageDone(), true, null, null);

                GUI.Label("Items needed:", this.batch, rect.x, rect.y + rect.height - 50 - 10, 50, 100);
                Array<ItemNeeded> list = constructable.getItemsNeeded();
                for(int i=0;i<list.size;i++) {
                    this.reusableImageLabelRect.set(rect.x, rect.y + rect.height - 50 - i*25, 25, 25);
                    GUI.ImageLabel(DataManager.getData(list.get(i).itemName, DataBuilder.JsonItem.class).iconTexture, ""+list.get(i).amountNeeded, this.batch, this.reusableImageLabelRect, 100);
                }

            }
        }
    }

    /**
     * Displays an inventory.
     * @param inventory The Inventory to get data from.
     * @param rect The Rectangle to display the inventory inside of.
     */
    private void drawInventory(Inventory inventory, Rectangle rect){
        batch.setColor(Color.WHITE);
        ArrayList<Inventory.InventoryItem> itemList = inventory.getItemList();
        this.UIStyle.alignment = Align.center;
        this.UIStyle.paddingTop = 0;
        int topOffset = this.UIStyle.paddingLeft = 10, iconSize =  32;
        float leftOffset = iconSize/1.5f;
        float labelHeight = iconSize, labelWidth = 200;

        //Starting X and Y pos.
        float xPos = rect.x;
        float yPos = rect.y + rect.height-iconSize;

        //Draw each item.
        for(Inventory.InventoryItem item : itemList){
            String maxItemAmount = item.getMaxAmount() != Integer.MAX_VALUE ? ""+item.getMaxAmount() : "?";
            //Draw the label of the amount and the icon.
            GUI.Label(""+item.getAmount(false)+"/"+maxItemAmount+"(a:"+item.getAvailable()+"/r:"+item.getReserved()+"/otw:"+item.getOnTheWay(), this.batch, xPos, yPos, labelWidth, labelHeight, this.UIStyle);
            GUI.Texture(item.itemRef.iconTexture, batch, xPos, yPos, iconSize, iconSize);

            //Increment the yPos, if we're too far down, reset Y and shift X.
            yPos-=iconSize;
            if(yPos < rect.y){
                xPos+= labelWidth;
                yPos = rect.y+rect.height-iconSize;
            }
        }

        //Reset color and padding/alignment
        this.batch.setColor(Color.WHITE);
        this.UIStyle.alignment = Align.center;
        this.UIStyle.paddingLeft = 0;
        this.UIStyle.paddingTop = 0;
    }

    private void drawColonyInventory(Inventory inventory, Rectangle rect){
        //We need to use the global list of items.
        Array<String> itemList = DataBuilder.JsonItem.allItems;

        batch.setColor(Color.WHITE);
        this.UIStyle.alignment = Align.center;
        this.UIStyle.paddingTop = 0;
        int iconSize =  32;
        float labelWidth = 50;

        float craftButtonWidth = 50, craftButtonHeight = 32*0.5f;

        //Starting X and Y pos.
        float xPos = rect.x + 10;
        float yPos = rect.y + rect.height - iconSize - 10;
        this.UIStyle.background = new TextureRegion(ColonyGame.assetManager.get("background", Texture.class));
        TextureRegion _icon;

        //Draw each item.
        for(int i=0;i<itemList.size;i++){
            //Get the itemName and itemAmount and the _icon
            String itemName = itemList.get(i);
            int itemAmount = inventory.getItemAmount(itemName);
            String label = ""+itemAmount;
            DataBuilder.JsonItem _itemRef = DataManager.getData(itemName, DataBuilder.JsonItem.class);
            _icon = _itemRef.iconTexture;

            //Draw the label of the amount and the icon.
            GUI.ImageLabel(_icon, label, this.batch, xPos, yPos, iconSize, iconSize, labelWidth, this.UIStyle);
            if(!_itemRef.getItemCategory().equals("raw")) {
                int state = GUI.Button(this.batch, "Craft", xPos + iconSize + labelWidth + 10, yPos, craftButtonWidth, iconSize, null);
            }

            //Increment the yPos, if we're too far down, reset Y and shift X.
            yPos -= iconSize + 5;
            if(yPos < rect.y){
                xPos += iconSize + labelWidth + craftButtonWidth + 20;
                yPos = rect.y+rect.height - iconSize- 10;
            }
        }

        //Reset color and padding/alignment
        this.batch.setColor(Color.WHITE);
        this.UIStyle.alignment = Align.center;
        this.UIStyle.paddingLeft = 0;
        this.UIStyle.paddingTop = 0;
        this.UIStyle.background = null;
    }

    //Draws the buttons for each selectedEntity colonist that we have control of.
    private void drawBehaviourButtons(IInteractable interactable){
        StateTree<BehaviourManagerComp.TaskInfo> tree = interactable.getBehManager().getTaskTree();

        //TODO Need to make sure multiple selections work since this was changed.

        //Set some position variables.
        float width = (ordersRect.getWidth()/(tree.getCurrentTreeNode().getChildren().size+1));
        float height = ordersRect.y + 25;
        float x = ordersRect.x;

        //Get the children of the current root node. This will display all children buttons of our current selection.
        Array<Tree.TreeNode<BehaviourManagerComp.TaskInfo>> nodeList = tree.getCurrentTreeNode().getChildren();
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
            if (GUI.Button(this.batch, currTaskNode.nodeName, orderButtonRect, style) == GUI.UP) {
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
                    if(((BehaviourManagerComp.TaskInfo) treeNode.userData).active) comp.getBlackBoard().resourceTypeTags.addTag(treeNode.nodeName);
                    else comp.getBlackBoard().resourceTypeTags.removeTag(treeNode.nodeName);
                }

                //Get the node from the gather TreeNode. If it has children, set the currStateNode
                Tree.TreeNode tmpNode = interactable.getBehManager().getTaskTree().getNode(node -> node.nodeName.equals(currTaskNode.nodeName));
                if (tmpNode.hasChildren()) {
                    tree.setCurrentTreeNode(tmpNode);
                }
            }
        }
    }

    /**
     * Draws the game speed window.
     * @param screenW The screen width.
     * @param screenH The screen height.
     * @param batch The SpriteBatch to draw with.
     * @param style The GUIStyle to use.
     */
    private void drawGameSpeed(int screenW, int screenH, SpriteBatch batch, GUI.GUIStyle style){
        style.alignment = Align.center;
        String text;
        if(paused) text = "PAUSED";
        else text = "x"+gameSpeed+" speed";
        GUI.Label(text, batch, screenW - 100, screenH - 50, 100, 50, style);
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
        GUI.Texture(this.whiteTexture, batch, outerX, y, width, height);

        //Draw the inner rectangle (shrink it by 2 inches on all sides, 'padding')
        batch.setColor(Color.GREEN);
        float newWidth = (currVal/maxVal)*(width-4);
        GUI.Texture(this.whiteTexture, batch, innerX, y + 2, newWidth, height - 4);

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

        //Query the saveContainer and set dragging to false.
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
    public UnitProfile setSelectedEntity(Entity entity){
        this.newlySelected = true;
        this.selectedProfile = new UnitProfile(entity);
        this.selectedProfile.entity.getTags().addTag("selected");
        this.selectedProfile.interactable = entity.getComponent(Interactable.class); //Get the selectedProfile Component.
        return this.selectedProfile;
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
        if(playerEvent.focusOnEvent) ColonyGame.camera.position.set(playerEvent.eventTarget.getTransform().getPosition().x, playerEvent.eventTarget.getTransform().getPosition().y, 0);
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

        this.colonyScreenRect.set(width / 2 - 300, height / 2 - 200, 600, 400);

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
            this.renderWorld = !this.renderWorld;
        else if(keycode == Input.Keys.F3) { //F3 - draw grid
            this.drawGrid = !this.drawGrid;
        }else if(keycode == Input.Keys.SPACE) //SPACE - toggle paused
            this.paused = !this.paused;
        else if(keycode == Input.Keys.F4) //F4 - reveal map
            this.revealMap();
        else if(keycode == Input.Keys.F5) {
            SaveGameHelper.saveWorld();
        }else if(keycode == Input.Keys.F6){
            SaveGameHelper.loadWorld();
            this.paused = true;
        }else if(keycode == Input.Keys.PLUS) //+ - increase game speed

            gameSpeed*=2;
        else if(keycode == Input.Keys.MINUS)//- - decrease game speed
            gameSpeed*=0.5f;
        else if(keycode == Input.Keys.T) { //T - toggle the colonists (if we have a colonist selected) 'alert' mode.
            boolean alert = false;
            if (selectedProfile != null)
                if (selectedProfile.interactable.getInteractable().getComponent().getEntityOwner().getTags().hasTag("colonist"))
                    alert = ((Colonist) selectedProfile.interactable.getInteractable().getComponent()).toggleRangeSensor();

            for(UnitProfile profile : this.selectedProfileList){
                ((Colonist)profile.interactable.getInteractable().getComponent()).setAlert(alert);
            }

        }else if(keycode == Input.Keys.I) { //T - toggle the colonists (if we have a colonist selected) 'alert' mode.
            this.drawingColony = !this.drawingColony;
        }else
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
            this.selectedProfileList.forEach(profile -> profile.entity.getTags().removeTag("selected"));
            this.selectedProfileList.clear();
            this.finishDragging(worldCoords.x, worldCoords.y);

            //If we don't have something that was selected before this point, follow through with our click test.
            if(!newlySelected) {
                newlySelected = false;
                if(selectedProfile != null) selectedProfile.entity.getTags().removeTag("selected");
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
                    comp.changeTaskImmediate("moveTo");
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
