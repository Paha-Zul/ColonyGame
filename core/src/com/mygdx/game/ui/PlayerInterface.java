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
import com.mygdx.game.util.*;
import com.mygdx.game.util.gui.*;
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
 * The PlayerInterface handles all the GUI rendering to the screen to display information for the player.
 */
public class PlayerInterface extends UI implements IGUI, InputProcessor {
    public boolean paused = false;
    public static boolean active = false;
    public float gameSpeed = 1;

    private TextureRegion background;
    private World world;
    private WindowManager windowManager;

    private Array<Button> buttonList;

    private boolean drawingInfo = false;
    private boolean drawingProfiler = false;
    private boolean mouseDown = false;
    private boolean dragging = false;
    private boolean drawGrid = false;
    public boolean renderWorld = true;

    private Rectangle buttonRect = new Rectangle();

    private float FPS = 0;

    private static final float camMoveSpeed = 100f;
    private static final float camZoomSpeed = 5f;

    private Rectangle bottomLeftRect = new Rectangle();
    private Rectangle selectionBox = new Rectangle();

    private GUI.GUIStyle gatherStyle = new GUI.GUIStyle();
    private GUI.GUIStyle exploreStyle = new GUI.GUIStyle();
    private GUI.GUIStyle huntStyle = new GUI.GUIStyle();
    private GUI.GUIStyle blankStyle = new GUI.GUIStyle();
    private GUI.GUIStyle skillsStyle = new GUI.GUIStyle();
    private GUI.GUIStyle gameSpeedStyle = new GUI.GUIStyle();
    private GUI.GUIStyle notificationStyle = new GUI.GUIStyle();
    private GUI.GUIStyle mousedOverNotiStyle = new GUI.GUIStyle();
    public GUI.GUIStyle UIStyle;
    private Timer FPSTimer;

    private Vector2 testPoint = new Vector2(); //A reusable vector
    private boolean newlySelected = false; //If we have selected a new entity, this will stop the left click from getting rid of it.
    private UnitProfile selectedProfile = null; //The currently selected UnitProfile.
    private Array<UnitProfile> selectedProfileList = new Array<>(); //The list of selected UnitProfiles.

    private Color gray = new Color(Color.BLACK);
    public Texture blueSquare;
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

        this.windowManager = new WindowManager();
        this.windowManager.addWindow(new SelectedWindow(this));
        this.windowManager.addWindow(new ColonyWindow(this));

        this.buttonList = new Array<>();

        this.eventDescStyle = new GUI.GUIStyle();
        eventDescStyle.background = new TextureRegion(ColonyGame.assetManager.get("eventWindowDescriptionBackground", Texture.class));

        this.eventTitleStyle = new GUI.GUIStyle();
        this.eventTitleStyle.background = new TextureRegion(ColonyGame.assetManager.get("eventWindowTitleBackground", Texture.class));

        this.stage = new Stage(new ScreenViewport(ColonyGame.UICamera));
        this.blueSquare = ColonyGame.assetManager.get("blueSquare", Texture.class);

        this.background = new TextureRegion(ColonyGame.assetManager.get("background", Texture.class));

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

        this.windowManager.update(this.batch);
        for(Button button : this.buttonList)
            if(button.render(ColonyGame.batch)) break;

        this.moveCamera(); //Move the camera
        this.drawSelectionBox(); //Draws the selection box.
        this.drawDebugInfo(height); //Draws some debug information.
        this.drawTerrainInfo(this.bottomLeftRect); //Draws information about the moused over terrain piece.
        this.drawGameSpeed(screenW, screenH, batch, this.gameSpeedStyle);
        this.drawInvAmounts(screenW, screenH, batch);
        this.drawCurrentNotifications(screenW, screenH, delta);

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

    /**
     * Draws the current event that is being presented to the player while the game is paused.
     * @param width The width of the game screen. This will be use to calculate the event window widht.
     * @param height The height of the game screen. Used to calculate the event window height.
     */
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
                if(GUI.Button(this.batch, choice, windowX + (i+1)*spacing + i*buttonWidth, windowY + windowHeight*0.01f, buttonWidth, buttonHeight, blankStyle) == GUI.JUSTUP){
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

        for(int i=0;i<this.selectedProfileList.size;i++)
            if(this.selectedProfileList.get(i).entity == entity) {
                this.selectedProfileList.removeIndex(i);
                break;
            }

        if(this.selectedProfile == null && this.selectedProfileList.size > 0)
            this.selectedProfile = this.selectedProfileList.get(0);
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
     * Displays an inventory.
     * @param inventory The Inventory to get data from.
     * @param rect The Rectangle to display the inventory inside of.
     */
    public void drawInventory(Inventory inventory, Rectangle rect){
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
        return this.checkMousedOver();
    }

    /**
     * Checks if the GUI is moused over or not.
     */
    private boolean checkMousedOver(){
        return this.windowManager.isMousedOver();
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

    /**
     * Triggers a new Event for the player. THis will pause the game and set the current event to the event passed in. It will also focus the
     * camera on the event.eventTarget if event.focusOnEvent is set to true.
     * @param playerEvent The PlayerEvent to set as the current event.
     */
    public void newPlayerEvent(DataBuilder.JsonPlayerEvent playerEvent){
        this.paused = playerEvent.pauseGame;
        this.currentEvent = playerEvent;
        if(playerEvent.focusOnEvent) ColonyGame.camera.position.set(playerEvent.eventTarget.getTransform().getPosition().x, playerEvent.eventTarget.getTransform().getPosition().y, 0);
    }

    /**
     * Sets the selectedEntity entity for viewing. This will create a new UnitProfile for the Entity (which will get the Interactable Component from it)
     * and set it as the current selected profile.
     * @param entity The Entity to set as the selectedEntity Entity.
     */
    public UnitProfile setSelectedEntity(Entity entity){
        this.newlySelected = true;
        this.selectedProfile = new UnitProfile(entity);
        this.selectedProfile.entity.getTags().addTag("selected");
        this.selectedProfile.interactable = entity.getComponent(Interactable.class); //Get the selectedProfile Component.
        this.selectedProfileList.add(this.selectedProfile);
        return this.selectedProfile;
    }

    /**
     * Sets the selected profile.
     * @param profile The UnitProfile to set as the selected profile.
     */
    public void setSelectedProfile(UnitProfile profile){
        this.selectedProfile = profile;
    }

    public UnitProfile getSelectedProfile(){
        return this.selectedProfile;
    }

    public Array<UnitProfile> getSelectedProfileList(){
        return this.selectedProfileList;
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
    }

    @Override
    public void resize(int width, int height) {
        this.windowManager.resize(width, height);

        this.buttonRect.set(0, Gdx.graphics.getHeight() - 100, 200, 100);

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
        else if(keycode == Input.Keys.F5) { //F5 - save
            SaveGameHelper.saveWorld();
        }else if(keycode == Input.Keys.F6){ //F6 - load
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
        //This is when we release the mouse. If we are on the UI or dragging a selection box, simply return false.
        if(this.isOnUI())
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
                if (this.selectedProfileList.size < 1) {
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

    public static class UnitProfile{
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
