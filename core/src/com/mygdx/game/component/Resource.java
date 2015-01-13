package com.mygdx.game.component;

/**
 * Created by Paha on 1/10/2015.
 */
public class Resource extends Component{
    private String resourceType = "";
    private int maxResources = 100;
    private int currResources = 100;

    public Resource(String resourceType) {
        super();

        this.resourceType = resourceType;
        this.setActive(false);
    }

    public Resource() {
        this("NothingResource");
    }

    public int getMaxResources(){
        return this.maxResources;
    }

    public int getCurrResources(){
        return this.currResources;
    }

    public String getResourceType(){
        return this.resourceType;
    }

    public void setMaxResources(int amt){
        this.maxResources = amt;
    }

    public void setCurrResources(int amt){
        this.currResources = amt;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void update(float delta) {
        super.update(delta);
    }

    @Override
    public void destroy() {
        super.destroy();
    }

}
