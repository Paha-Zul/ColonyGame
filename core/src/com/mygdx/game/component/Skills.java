package com.mygdx.game.component;

/**
 * Created by Paha on 1/30/2015.
 */
public class Skills extends Component{
    private int woodCutting, stoneCutting, building, cooking;

    public Skills() {
        super();

        this.setActive(false);
    }

    @Override
    public void start() {
        super.start();
    }


    //region Setters
    public void setWoodCutting(int woodCutting) {
        this.woodCutting = woodCutting;
    }

    public void setStoneCutting(int stoneCutting) {
        this.stoneCutting = stoneCutting;
    }


    public void setBuilding(int building) {
        this.building = building;
    }


    public void setCooking(int cooking) {
        this.cooking = cooking;
    }
    //endregion

    //region Getters
    public int getWoodCutting() {
        return woodCutting;
    }
    public int getStoneCutting() {
        return stoneCutting;
    }

    public int getBuilding() {
        return building;
    }

    public int getCooking() {
        return cooking;
    }
    //endregion


    @Override
    public void destroy() {
        super.destroy();
    }
}
