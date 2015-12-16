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
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.mygdx.game.ColonyGame;
import com.mygdx.game.component.*;
import com.mygdx.game.component.collider.Collider;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.Functional;
import com.mygdx.game.interfaces.IGUI;
import com.mygdx.game.util.*;
import com.mygdx.game.util.gui.Button;
import com.mygdx.game.util.gui.GUI;
import com.mygdx.game.util.managers.DataManager;
import com.mygdx.game.util.managers.GameEventManager;
import com.mygdx.game.util.managers.NotificationManager;
import com.mygdx.game.util.managers.WindowManager;
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
    private static final float camMoveSpeed = 100f;
    private static final float camZoomSpeed = 5f;
    public static boolean active = false;
    private static PlayerInterface playerInterface;
    public boolean paused = false;
    public float gameSpeed = 1;
    public boolean renderWorld = true;
    public GUI.GUIStyle UIStyle;
    public Texture blueSquare;
    public Stage stage;
    public boolean drawingColony = false;
    private TextureRegion background;
    private World world;
    private WindowManager windowManager;
    private Array<Button> buttonList;
    private boolean drawingInfo = false;
    private boolean drawingProfiler = false;
    private boolean mouseDown = false;
    private boolean dragging = false;
    private boolean drawGrid = false;
    private Rectangle buttonRect = new Rectangle();
    private float FPS = 0;
    private Rectangle bottomLeftRect = new Rectangle();
    private Rectangle selectionBox = new Rectangle();
    private GUI.GUIStyle gatherStyle;
    private GUI.GUIStyle exploreStyle;
    private GUI.GUIStyle huntStyle;
    private GUI.GUIStyle blankStyle;
    private GUI.GUIStyle skillsStyle;
    private GUI.GUIStyle gameSpeedStyle;
    private GUI.GUIStyle notificationStyle;
    private GUI.GUIStyle mousedOverNotiStyle;
    private Timer FPSTimer;
    private Vector2 testPoint = new Vector2(); //A reusable vector
    private boolean newlySelected = false; //If we have selected a new entity, this will stop the left click from getting rid of it.
    private UnitProfile selectedProfile = null; //The currently selected UnitProfile.
    private Array<UnitProfile> selectedProfileList = new Array<>(); //The list of selected UnitProfiles.
    private Color gray = new Color(Color.BLACK);
    private GameEventManager.GameEvent currentEvent;
    private GUI.GUIStyle eventDescStyle, eventTitleStyle;
    private TextureRegion whiteTexture;
    private NotificationManager.Notification mousedOverNotification = null;
    private boolean extendedTooltip;
    private Timer extendedTooltipTimer = new OneShotTimer(2f, () -> extendedTooltip = true);

    private HashMap<Integer, Array<Functional.Callback>> keyEventMap = new HashMap<>();


    //For selecting a single unit.
    private QueryCallback callback = fixture -> {
        Collider.ColliderInfo info = (Collider.ColliderInfo)fixture.getUserData();
        if(info.tags.hasTag(Constants.COLLIDER_CLICKABLE) && fixture.testPoint(testPoint.x, testPoint.y) && info.owner.getTags().hasTag("selectable")){
            this.setSelectedEntity(info.owner);
            return false;
        }

        return true;
    };
    //For selecting many units
    private QueryCallback selectionCallback = fixture -> {
        Collider.ColliderInfo selectedInfo = (Collider.ColliderInfo)fixture.getUserData();

        //If not null, the entity is a colonist, and the collider is clickable.
        if(selectedInfo != null && selectedInfo.owner.getTags().hasTags("colonist", "alive", "selectable") && selectedInfo.tags.hasTag(Constants.COLLIDER_CLICKABLE)) {
            this.setSelectedEntity(selectedInfo.owner); //Set our selectedEntity
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

        this.gatherStyle = new GUI.GUIStyle();
        this.exploreStyle = new GUI.GUIStyle();
        this.huntStyle = new GUI.GUIStyle();
        this.blankStyle = new GUI.GUIStyle();
        this.skillsStyle = new GUI.GUIStyle();
        this.gameSpeedStyle = new GUI.GUIStyle();
        this.notificationStyle = new GUI.GUIStyle();
        this.mousedOverNotiStyle = new GUI.GUIStyle();

        this.buttonList = new Array<>();

        this.eventDescStyle = new GUI.GUIStyle();
        eventDescStyle.background = new TextureRegion(ColonyGame.instance.assetManager.get("eventWindowDescriptionBackground", Texture.class));

        this.eventTitleStyle = new GUI.GUIStyle();
        this.eventTitleStyle.background = new TextureRegion(ColonyGame.instance.assetManager.get("eventWindowTitleBackground", Texture.class));

        this.blueSquare = ColonyGame.instance.assetManager.get("blueSquare", Texture.class);

        this.background = new TextureRegion(ColonyGame.instance.assetManager.get("background", Texture.class));

        this.buttonRect.set(0, Gdx.graphics.getHeight() - 100, 200, 100);

        Functional.Callback callback = () -> this.FPS = 1/Gdx.graphics.getDeltaTime();
        this.FPSTimer = new RepeatingTimer(0.5d, callback);

        //Loads and configures stuff about buttons styles.
        loadHuntButtonStyle();

        this.generateFonts();

        gatherStyle.normal =  DataManager.getTextureFromAtlas("axebutton_normal", "buttons");
        gatherStyle.moused = DataManager.getTextureFromAtlas("axebutton_moused", "buttons");
        gatherStyle.clicked = DataManager.getTextureFromAtlas("axebutton_clicked", "buttons");
        gatherStyle.active = DataManager.getTextureFromAtlas("axebutton_clicked", "buttons");

        DataManager.addData("gatherStyle", gatherStyle, GUI.GUIStyle.class);

        exploreStyle.normal = DataManager.getTextureFromAtlas("explorebutton_normal", "buttons");
        exploreStyle.moused = DataManager.getTextureFromAtlas("explorebutton_moused", "buttons");
        exploreStyle.clicked = DataManager.getTextureFromAtlas("explorebutton_clicked", "buttons");
        exploreStyle.active = DataManager.getTextureFromAtlas("explorebutton_clicked", "buttons");

        DataManager.addData("exploreStyle", exploreStyle, GUI.GUIStyle.class);

        huntStyle.normal = DataManager.getTextureFromAtlas("huntbutton_normal", "buttons");
        huntStyle.moused = DataManager.getTextureFromAtlas("huntbutton_moused", "buttons");
        huntStyle.clicked = DataManager.getTextureFromAtlas("huntbutton_clicked", "buttons");
        huntStyle.active = DataManager.getTextureFromAtlas("huntbutton_clicked", "buttons");

        DataManager.addData("huntStyle", huntStyle, GUI.GUIStyle.class);

        blankStyle.normal = DataManager.getTextureFromAtlas("blankbutton_normal", "buttons");
        blankStyle.moused = DataManager.getTextureFromAtlas("blankbutton_moused", "buttons");
        blankStyle.clicked = DataManager.getTextureFromAtlas("blankbutton_clicked", "buttons");
        blankStyle.active = DataManager.getTextureFromAtlas("blankbutton_clicked", "buttons");

        DataManager.addData("blankStyle", blankStyle, GUI.GUIStyle.class);

        gameSpeedStyle.background = new TextureRegion(ColonyGame.instance.assetManager.get("eventWindowBackground", Texture.class));
        notificationStyle.background = new TextureRegion(ColonyGame.instance.assetManager.get("notificationIcon", Texture.class));
        notificationStyle.font = DataManager.getData("changelogFont", BitmapFont.class);

        gatherStyle.font.setColor(new Color(126f / 255f, 75f / 255f, 27f / 255f, 1));
        exploreStyle.font.setColor(new Color(126f / 255f, 75f / 255f, 27f / 255f, 1));
        huntStyle.font.setColor(126f / 255f, 75f / 255f, 27f / 255f, 1);

        //Multiplex the stage and this interface.
        this.stage = new Stage(new ScreenViewport(ColonyGame.instance.UICamera));
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this.stage);
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);

        ColonyGame.instance.camera.zoom = 1.5f;

        this.windowManager = new WindowManager();
        this.windowManager.addWindowToSelfManagingList(new SelectedWindow(this));

        this.makeBuildButton();
    }

    private void loadHuntButtonStyle(){
        huntStyle.normal = DataManager.getTextureFromAtlas("huntbutton_normal", "buttons");
        huntStyle.moused = DataManager.getTextureFromAtlas("huntbutton_moused", "buttons");
        huntStyle.clicked = DataManager.getTextureFromAtlas("huntbutton_clicked", "buttons");
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

    private void makeBuildButton(){
        TextureRegionDrawable up = new TextureRegionDrawable(DataManager.getTextureFromAtlas("defaultButton_normal", "buttons"));
        TextureRegionDrawable over = new TextureRegionDrawable(DataManager.getTextureFromAtlas("defaultButton_moused", "buttons"));
        TextureRegionDrawable down = new TextureRegionDrawable(DataManager.getTextureFromAtlas("defaultButton_clicked", "buttons"));

        TextButton.TextButtonStyle style = new TextButton.TextButtonStyle(up, down, up, this.UIStyle.font);
        style.over = style.checkedOver = over;

        TextButton button = new TextButton("Build", style);
        button.setPosition(0,0);
        this.stage.addActor(button);

        button.addListener(new InputListener() {
            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                windowManager.addWindowIfNotExistByTarget(PlacingConstructionWindow.class, null, PlayerInterface.getInstance());
                return true;
            }
        });
    }

    public static PlayerInterface getInstance(){
        if(playerInterface == null) playerInterface = new PlayerInterface(ColonyGame.instance.batch, ColonyGame.instance.world);
        return playerInterface;
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
                this.selectedProfileList.get(i).entity.getTags().removeTag("selected");
                this.selectedProfileList.removeIndex(i);
                break;
            }

        if(this.selectedProfile == null && this.selectedProfileList.size > 0)
            this.selectedProfile = this.selectedProfileList.get(0);
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

    public void makePreviewTable(DataBuilder.JsonRecipe recipe, Table previewWindow){
        previewWindow.clear();

        String description;
        DataBuilder.JsonItem item = DataManager.getData(recipe.name, DataBuilder.JsonItem.class);
        DataBuilder.JsonBuilding building = null;
        if(item == null) {
            building = DataManager.getData(recipe.name, DataBuilder.JsonBuilding.class);
            description = "A building";
        }else
            description = item.getDescription();

        /*
         This area creates the item's icon.
         */
        Image image = new Image(recipe.icon);
        image.setSize(32, 32);

        previewWindow.add(image).expandX().maxSize(32).top();
        previewWindow.row();

        /*
         * This area creates the description area and puts the item description in.
         */
        Label.LabelStyle style = new Label.LabelStyle(this.UIStyle.font, Color.BLACK);
        Label label = new Label(description, style);

        previewWindow.add(label).expand().top();
        previewWindow.row();

        /*
         * This area creates the 'item' list with icons and amounts.
         */
        Table itemTable = new Table();
        previewWindow.add(itemTable).left();

        Label itemsLabel = new Label("items:", style);
        itemTable.add(itemsLabel).prefHeight(32).padRight(10).padLeft(5);

        //Adds the icons.
        for(ItemNeeded itemNeeded : recipe.materialsForCrafting){
            DataBuilder.JsonItem _itemRef = DataManager.getData(itemNeeded.itemName, DataBuilder.JsonItem.class);
            Image icon = new Image(_itemRef.iconTexture);
            icon.setSize(32, 32);
            itemTable.add(icon).maxSize(32);
        }

        itemTable.row();
        itemTable.add().expandX().fillX(); //Add an empty cell to be under the itemsLabel.

        //Adds the amounts
        for(ItemNeeded itemNeeded : recipe.materialsForCrafting){
            Label amountLabel = new Label(""+itemNeeded.amountNeeded, style);
            amountLabel.setAlignment(Align.center);
            itemTable.add(amountLabel).expandX().fillX();
        }

        previewWindow.row();

        /*
         * This area creates hte 'raw' item list with icons and amounts.
         */
        Table rawTable = new Table();
        previewWindow.add(rawTable).left();

        Label rawLabel = new Label("raw:", style);
        rawTable.add(rawLabel).prefHeight(32).padRight(10).padLeft(5);

        //Adds the icons
        for(ItemNeeded itemNeeded : recipe.rawForCrafting){
            DataBuilder.JsonItem _itemRef = DataManager.getData(itemNeeded.itemName, DataBuilder.JsonItem.class);
            Image icon = new Image(_itemRef.iconTexture);
            icon.setSize(32, 32);
            rawTable.add(icon).maxSize(32);
        }
        rawTable.row();
        rawTable.add().expandX().fillX(); //Add an empty cell to be under the rawLabel
        //Adds the amounts
        for(ItemNeeded itemNeeded : recipe.rawForCrafting){
            Label amountLabel = new Label(""+itemNeeded.amountNeeded, style);
            amountLabel.setAlignment(Align.center);
            rawTable.add(amountLabel).expandX().fillX();
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
        GUI.Texture(this.whiteTexture, batch, outerX, y, width, height);

        //Draw the inner rectangle (shrink it by 2 inches on all sides, 'padding')
        batch.setColor(Color.GREEN);
        float newWidth = (currVal/maxVal)*(width-4);
        GUI.Texture(this.whiteTexture, batch, innerX, y + 2, newWidth, height - 4);

        GUI.Label((int) currVal + "/" + (int) maxVal, batch, outerX, y, width, height);
    }

    /**
     * Triggers a new Event for the player. THis will pause the game and set the current event to the event passed in. It will also focus the
     * camera on the event.eventTarget if event.focusOnEvent is set to true.
     * @param gameEvent The GameEvent to set as the current event.
     */
    public void newPlayerEvent(GameEventManager.GameEvent gameEvent){
        DataBuilder.JsonGameEvent event = gameEvent.gameEventData;
        this.paused = event.pauseGame;
        this.currentEvent = gameEvent;
        if(event.focusOnEvent) ColonyGame.instance.camera.position.set(gameEvent.entityTargetTeams.get(0).get(0).getTransform().getPosition().x,
                gameEvent.entityTargetTeams.get(0).get(0).getTransform().getPosition().y, 0);
    }

    /**
     * Sets the selectedEntity entity for viewing. This will create a new UnitProfile for the Entity (which will get the Interactable Component from it)
     * and set it as the current selected profile.
     * Also adds the new profile to the selectedProfile list.
     * @param entity The Entity to set as the selectedEntity Entity.
     */
    public UnitProfile setSelectedEntity(Entity entity){
        this.newlySelected = true;
        this.selectedProfile = new UnitProfile(entity);
        this.selectedProfile.entity.getTags().addTag("selected");
        this.selectedProfile.interactable = entity.getComponent(Interactable.class); //Get the selectedProfile Component.
        this.selectedProfileList.add(this.selectedProfile); //Add it to the list.
        return this.selectedProfile;
    }

    /**
     * @return The UnitProfile that is actively displaying information on the UI.
     */
    public UnitProfile getSelectedProfile(){
        return this.selectedProfile;
    }

    /**
     * Sets the selected profile.
     * @param profile The UnitProfile to set as the selected profile.
     */
    public void setSelectedProfile(UnitProfile profile){
        this.selectedProfile = profile;
    }

    /**
     * @return A list of selected UnitProfiles which include the Entity and other information.
     */
    public Array<UnitProfile> getSelectedProfileList(){
        return this.selectedProfileList;
    }

    /**
     * @return The WindowManager that controls windows.
     */
    public WindowManager getWindowManager(){
        return this.windowManager;
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
        ColonyGame.instance.listHolder.addGUI(this);
    }

    @Override
    public void render(float delta, SpriteBatch batch) {
        super.render(delta, batch);
        int screenW = Gdx.graphics.getWidth(), screenH = Gdx.graphics.getHeight();

        batch.setProjectionMatrix(ColonyGame.instance.UICamera.combined);
        GUI.font.setColor(Color.WHITE);

        int height = Gdx.graphics.getHeight();
        FPSTimer.update(delta);

        for(Button button : this.buttonList)
            if(button.render(ColonyGame.instance.batch)) break;

        this.moveCamera(); //Move the camera
        this.drawSelectionBox(); //Draws the selection box.
        this.drawDebugInfo(height); //Draws some debug information.
        this.drawTerrainInfo(this.bottomLeftRect); //Draws information about the moused over terrain piece.
        this.drawGameSpeed(screenW, screenH, batch, this.gameSpeedStyle);
        this.drawCurrentNotifications(screenW, screenH, delta);
        this.windowManager.update(this.batch);

        if(this.drawingProfiler) Profiler.drawDebug(batch, 200, height - 20);

        //Draw the grid squares if enabled.
        if(drawGrid) {
            ColonyGame.instance.worldGrid.debugDraw();
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
     * Moves the camera when keys are pressed.
     */
    private void moveCamera(){
        if(Gdx.input.isKeyPressed(Input.Keys.W))
            ColonyGame.instance.camera.translate(0, Gdx.graphics.getDeltaTime()*camMoveSpeed);
        if(Gdx.input.isKeyPressed(Input.Keys.S))
            ColonyGame.instance.camera.translate(0, -Gdx.graphics.getDeltaTime()*camMoveSpeed);
        if(Gdx.input.isKeyPressed(Input.Keys.A))
            ColonyGame.instance.camera.translate(-Gdx.graphics.getDeltaTime()*camMoveSpeed, 0);
        if(Gdx.input.isKeyPressed(Input.Keys.D))
            ColonyGame.instance.camera.translate(Gdx.graphics.getDeltaTime()*camMoveSpeed, 0);
    }

    /**
     * Draws the selection box if the user is dragging a selection box.
     */
    private void drawSelectionBox(){
        if(mouseDown) {
            //Get the start point.
            Vector3 pos = new Vector3(selectionBox.x, selectionBox.y, 0);
            Vector3 start = ColonyGame.instance.camera.project(new Vector3(pos.x, pos.y, 0));

            //Get the end point.
            pos.set(selectionBox.x + selectionBox.getWidth(), selectionBox.y + selectionBox.getHeight(), 0);
            Vector3 end = ColonyGame.instance.camera.project(new Vector3(pos.x, pos.y, 0));
            gray.a = 0.5f;

            //Save the color from the batch, apply the gray, draw the texture (with color), and reset the original color.
            Color saved = this.batch.getColor();
            this.batch.setColor(gray);
            this.batch.draw(WorldGen.whiteTex, start.x, start.y, end.x - start.x, end.y - start.y);
            this.batch.setColor(saved);
        }
    }

    /**
     * Draws info (like FPS) on the screen.
     * @param height The height of the screen.
     */
    private void drawDebugInfo(int height){
        if(this.drawingInfo) {
            GUI.Text("FPS: " + FPS, this.batch, 0, height - 20);
            GUI.Text("Zoom: " + ColonyGame.instance.camera.zoom, this.batch, 0, height - 40);
            GUI.Text("Resolution: " + Gdx.graphics.getDesktopDisplayMode().width + "X" + Gdx.graphics.getDesktopDisplayMode().height, this.batch, 0, height - 60);
            GUI.Text("NumTrees: " + WorldGen.getInstance().numTrees(), this.batch, 0, height - 80);
            GUI.Text("NumTiles: " + ColonyGame.instance.worldGrid.getWidth() * ColonyGame.instance.worldGrid.getHeight(), this.batch, 0, height - 100);
            GUI.Text("NumGridCols(X): " + ColonyGame.instance.worldGrid.getWidth(), this.batch, 0, height - 120);
            GUI.Text("NumGridRows(Y): " + ColonyGame.instance.worldGrid.getHeight(), this.batch, 0, height - 140);
        }
    }

    /**
     * Gets the terrain tile and displays its information.
     * @param rect The rectangle to draw the terrain info in.
     */
    private void drawTerrainInfo(Rectangle rect){
        OrthographicCamera camera = ColonyGame.instance.camera;
        Vector3 mouseCoords = camera.unproject(new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0));
        Grid.GridInstance grid = ColonyGame.instance.worldGrid;

        GUI.Texture(this.background, this.batch, rect);
        int index[] = grid.getIndex(mouseCoords.x, mouseCoords.y);
        Grid.Node node = grid.getNode(index);
        if(node == null) return;
        Grid.TerrainTile tile = grid.getNode(index).getTerrainTile();
        if(tile == null) return;
        GUI.Label(tile.tileRef.category, this.batch, rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
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

    private void drawCurrentNotifications(int width, int height, float delta){
        Array<NotificationManager.Notification> list = ColonyGame.instance.notificationManager.getActiveNotifications();
        float x = width - 80f, y = height*0.4f;
        float labelW = 50f, labelH = 50f;
        NotificationManager.Notification newlyMousedOver = null; //This is like a flag.

        notificationStyle.wrap = true;
        for(int i=0;i<list.size;i++){
            NotificationManager.Notification notification = list.get(i);

            //GUI.Texture(whiteTexture, batch, x, y, labelW, labelH);
            if(GUI.Label(notification.name, this.batch, x, y, labelW, labelH, notificationStyle) > 0){
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

    //Draws the box2D debug.
    private void drawBox2DDebug(){
        this.batch.end();
        this.batch.begin();
        ColonyGame.instance.debugRenderer.render(ColonyGame.instance.world, ColonyGame.instance.camera.combined);
        this.batch.end();
        this.batch.begin();
    }

    /**
     * Draws the current event that is being presented to the player while the game is paused.
     * @param width The width of the game screen. This will be use to calculate the event window widht.
     * @param height The height of the game screen. Used to calculate the event window height.
     */
    private void drawCurrentEvent(int width, int height){
        //TODO Probably should redo this into a separate event window.
        if(this.currentEvent != null){
            float windowWidth = width/3.2f, windowHeight = height/2.7f;
            float windowX = width/2 - windowWidth/2, windowY = height/2 - windowHeight/2;

            float titleWidth = windowWidth*0.4f, titleHeight = windowHeight*0.1f;
            float titleX = windowX+ windowWidth*0.05f, titleY = windowY + windowHeight - titleHeight - windowHeight*0.03f;

            float descWidth = windowWidth*0.9f, descHeight = windowHeight*0.6f;
            float descX = windowX + windowWidth*0.05f, descY = windowY + windowHeight*0.25f;

            GUI.Texture(new TextureRegion(ColonyGame.instance.assetManager.get("eventWindowBackground", Texture.class)), this.batch, windowX, windowY, windowWidth, windowHeight);

            this.eventDescStyle.padding(10);
            this.eventDescStyle.multiline = true;
            this.eventDescStyle.wrap = true;
            this.eventDescStyle.alignment = Align.topLeft;
            GUI.Label(this.currentEvent.gameEventData.eventDisplayName, this.batch, titleX, titleY, titleWidth, titleHeight, eventTitleStyle);
            GUI.Label(GameEventManager.generateEventDescription(this.currentEvent), this.batch, descX, descY, descWidth, descHeight, eventDescStyle);

            float buttonWidth = windowWidth*0.3f, buttonHeight = 75;
            float spacing = (windowWidth - this.currentEvent.gameEventData.choices.length*buttonWidth)/(this.currentEvent.gameEventData.choices.length+1);
            this.blankStyle.wrap = true;

            DataBuilder.JsonGameEvent data = this.currentEvent.gameEventData;

            //Here we display the choices as a button. When clicked, it will trigger some sort of behaviour or action.
            for(int choiceIndex=0; choiceIndex < data.choices.length; choiceIndex++){
                String choice = data.choices[choiceIndex]; //Cache the current choice.
                String eventType = data.type;
                String[][] beh = data.behaviours[choiceIndex]; //TODO Need to deal with multiple behaviours
                int sides = data.sides;

                if(GUI.Button(this.batch, choice, windowX + (choiceIndex+1)*spacing + choiceIndex*buttonWidth, windowY + windowHeight*0.01f, buttonWidth, buttonHeight, blankStyle) == GUI.JUSTUP){
                    //TODO This area has to be redone to handle different types of events. What if multiple people?
                    if(eventType.equals("neutral")){

                        //Set fields if applicable
                        if(data.setFields != null) {
                            //If there are fields to be set, set them for each applicable group.
                            for (int groupIndex = 0; groupIndex < this.currentEvent.entityTargetTeams.size; groupIndex++) {
                                final String[][] fieldData = data.setFields[choiceIndex][groupIndex];
                                GameEventManager.setFields(fieldData, this.currentEvent.entityTargetTeams.get(groupIndex));
                            }
                        }

                        //Set resource type tags if applicable
                        if(data.setResourceTypeTags != null) {
                            for (int groupIndex = 0; groupIndex < this.currentEvent.entityTargetTeams.size; groupIndex++){
                                final String[] tags = data.setResourceTypeTags[choiceIndex][groupIndex]; //Get the tags for the particular team
                                this.currentEvent.entityTargetTeams.get(groupIndex).forEach(ent -> {
                                    BehaviourManagerComp comp = ent.getComponent(BehaviourManagerComp.class);
                                    comp.getBlackBoard().resourceTypeTags.clearTags();
                                    comp.getBlackBoard().resourceTypeTags.addTags(tags);
                                });
                            }
                        }

                        //Set the behaviours!
                        for(int groupIndex=0;groupIndex<this.currentEvent.entityTargetTeams.size;groupIndex++) {
                            final String behaviour = beh[groupIndex][0];
                            for(Entity ent : this.currentEvent.entityTargetTeams.get(groupIndex)){
                                ent.getComponent(BehaviourManagerComp.class).changeTaskImmediate(behaviour, true);
                            }
                            System.out.println();
                        }
                    }else if(eventType.equals("encounter")){

                    }

                    this.gameSpeed = 1f; //Reset game speed
                    this.paused = false; //Unpause
                    this.currentEvent = null;
                    return;
                }
            }
        }
    }

    @Override
    public void destroy() {
        super.destroy();
        this.batch = null;
        selectedProfile = null;
        this.world = null;
        this.buttonRect = null;
    }

    public void addKeyEvent(int key, Functional.Callback callback){
        Array<Functional.Callback> list = this.keyEventMap.get(key);
        if(list == null){
            list = new Array<>();
            this.keyEventMap.put(key, list);
        }
        list.add(callback);
    }

    @Override
    public boolean keyDown(int keycode) {
        Array<Functional.Callback> list = keyEventMap.get(keycode);
        if(list != null) list.forEach(Functional.Callback::callback);

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
                if(profile.interactable.getInteractable().getComponent().getEntityOwner().getTags().hasTag("colonist"))
                    ((Colonist)profile.interactable.getInteractable().getComponent()).setAlert(alert);
            }

        }else if(keycode == Input.Keys.I) { //I - toggle drawing colony inventory.
            this.windowManager.addWindowIfNotExistByTarget(ColonyWindow.class, null, this);

        }else if(keycode == Input.Keys.ESCAPE){ //ESCAPE - exits windows and such.
            if(!this.windowManager.removeTopMostWindow()) //If there was nothing to close.
                this.clearSelectedProfileList();
        }else
            return false;

        return true;
    }

    //Reveals the entire map by adding a viewer to every tile.
    private void revealMap(){
        Grid.GridInstance grid = ColonyGame.instance.worldGrid;
        Grid.VisibilityTile[][] visMap = grid.getVisibilityMap();
        for (Grid.VisibilityTile[] aVisMap : visMap) {
            for (Grid.VisibilityTile anAVisMap : aVisMap) {
                anAVisMap.addViewer();
            }
        }
    }

    /**
     * Deselects all selected profiles and the current selected profile and clears both.
     */
    public void clearSelectedProfileList(){
        for(UnitProfile profile : this.selectedProfileList){
            profile.entity.getTags().removeTag("selected");
        }

        this.selectedProfile = null;
        this.selectedProfileList = new Array<>();
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
                Vector3 worldCoords = ColonyGame.instance.camera.unproject(new Vector3(screenX, screenY, 0));
                startDragging(worldCoords.x, worldCoords.y);
            }
        }
        return false;
    }

    /**
     * If the GUI is moused over or not.
     * @return True if moused over, false otherwise.
     */
    public boolean isOnUI(){
        return this.checkMousedOver();
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
     * Checks if the GUI is moused over or not.
     */
    private boolean checkMousedOver(){
        return this.windowManager.isMousedOver();
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        //This is when we release the mouse. If we are on the UI or dragging a selection box, simply return false.
        if(this.isOnUI())
            return false;

        this.mouseDown = false;
        Vector3 worldCoords = ColonyGame.instance.camera.unproject(new Vector3(screenX, screenY, 0));

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

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if(mouseDown) {
            Vector3 worldCoords = ColonyGame.instance.camera.unproject(new Vector3(screenX, screenY, 0));
            drag(worldCoords.x, worldCoords.y);
        }

        return false;
    }

    /**
     * Active dragging.
     * @param x The current X location.
     * @param y The current Y location.
     */
    private void drag(float x, float y){
        selectionBox.set(selectionBox.x, selectionBox.y, x - selectionBox.x, y - selectionBox.y);
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        ColonyGame.instance.camera.zoom += amount*Gdx.graphics.getDeltaTime()*camZoomSpeed;
        if(ColonyGame.instance.camera.zoom < 0) ColonyGame.instance.camera.zoom = 0;
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
