package com.mygdx.game.helpers.managers;

import com.badlogic.gdx.utils.Array;
import com.mygdx.game.component.Component;
import com.mygdx.game.component.GraphicIdentity;
import com.mygdx.game.component.Transform;
import com.mygdx.game.entity.Entity;
import com.mygdx.game.interfaces.IDelayedDestroyable;
import com.mygdx.game.interfaces.IScalable;
import gnu.trove.map.hash.TDoubleObjectHashMap;

/**
 * Created by Paha on 5/21/2015.
 */
public class ComponentManager {
    private static TDoubleObjectHashMap<ComponentContainer> idToComponentMap = new TDoubleObjectHashMap<>();

    public static ComponentContainer getComponents(Double id){
        ComponentContainer container = idToComponentMap.get(id);
        if(container == null){
            container = new ComponentContainer(id);
            idToComponentMap.put(id, container);
        }

        return container;
    }

    public static ComponentContainer getComponents(Entity entity){
        return getComponents(entity.getID());
    }

    public static void destroyComponents(double ID){
        ComponentContainer container = idToComponentMap.get(ID);
        container.destroy(ID);
    }

    public static void destroyComponents(Entity entity){
        destroyComponents(entity.getID());
    }

    public static void updateComponents(Entity entity, float delta){
        updateComponents(delta, idToComponentMap.get(entity.getID()));
    }

    private static void updateComponents(float delta, ComponentContainer components){
        //Only update if active.
        if(components.destroyComponentList.size > 0) {
            for (Component comp : components.destroyComponentList) components.internalDestroyComponent(comp); //Destory the component
            components.destroyComponentList.clear(); //Clear the list.
        }

        //Start all new components.
        if (components.newComponentList.size > 0) {
            Array<Component> newCompCopy = new Array<>(components.newComponentList);
            //Call start on all new Components. This is where the component can access other
            //components on this Entity.
            newCompCopy.forEach(com.mygdx.game.component.Component::start);

            components.newComponentList.clear(); //Clear the new Component list.
        }

        Array<Component> activeCompCopy = new Array<>(components.activeComponentList);
        //Update all Components
        for (Component comp : activeCompCopy) {
            comp.update(delta);
            comp.lateUpdate(delta);
        }
    }

    public static class ComponentContainer implements IDelayedDestroyable{
        protected Array<Component> activeComponentList = new Array<>();
        protected Array<Component> inactiveComponentList = new Array<>();
        protected Array<Component> destroyComponentList = new Array<>();
        protected Array<Component> newComponentList = new Array<>();
        protected Array<IScalable> scalableComponents = new Array<>();

        protected boolean setToDestroy, destroyed;

        public Transform transform;
        public GraphicIdentity identity;

        protected double ownerID;

        public ComponentContainer(double id){
            this.ownerID = id;
        }

        /**
         * Adds a component to this Entity. The Component is immediately added to the Entity but the 'start' method is not called
         * until the next update frame for the Entity. This means any values set in the 'start' method are not set in the same frame.
         * @param comp The Component to add to this Entity.
         * @param <T> The Component class interType of the component being added.
         * @return The Component that was added.
         */
        @SuppressWarnings("unchecked")
        public final <T extends Component> T addComponent(Component comp){
            comp.init(ownerID); //Initialize the component with this Entity as the ownerID.
            this.newComponentList.add(comp); //Add it to the new list for the start() method.
            //Add it to the active or inactive list.
            if(comp.isActive()) this.activeComponentList.add(comp);
            else this.inactiveComponentList.add(comp);

            return (T) comp;
        }

        /**
         * Retrieves a Component from this Entity.
         * @param c The Component class interType to retrieve.
         * @return The Component if it was found, otherwise null.
         */
        @SuppressWarnings("unchecked")
        public final <T extends Component> T getComponent(Class<T> c){
            for(Component comp : this.inactiveComponentList){
                if(comp.getClass() == c)
                    return (T)comp;
            }

            for(Component comp : this.activeComponentList){
                if(comp.getClass() == c)
                    return (T)comp;
            }

            return null;
        }

        /**
         * Removes a Component from this Entity.
         * @param cls The class interType of the Component to remove.
         */
        public <T extends Component> void internalDestroyComponent(Class<T> cls){
            //Search the inactive list.
            for (Component comp : this.inactiveComponentList)
                if (comp.getClass() == cls) {
                    destroyComponentList.add(comp);
                    return;
                }

            //Search the active list.
            for (Component comp : this.activeComponentList)
                if (comp.getClass() == cls) {
                    destroyComponentList.add(comp);
                    return;
                }
        }

        /**
         * Destroys the passed in component and removes it from this Entity.
         * @param component The Component to destroy and remove.
         */
        public void destroyComponent(Component component){
            destroyComponentList.add(component);
        }

        /**
         *	Destroys and removes a Component from this Entity.
         * @param component The Component to destroy and remove.
         */
        private void internalDestroyComponent(Component component){
            if(component.isActive())
                this.activeComponentList.removeValue(component, false);
            else
                this.inactiveComponentList.removeValue(component, false);

            component.destroy(ownerID);
        }

        /**
         * Removes a Component from this Entity.
         * @param comp The Component to remove.
         * @return True if the Component was removed, false otherwise.
         */
        public boolean removeComponent(Component comp){
            if(comp.isActive()) return this.inactiveComponentList.removeValue(comp, false);
            return this.activeComponentList.removeValue(comp, false);
        }

        public Transform getTransform(){
            return this.transform;
        }

        public GraphicIdentity getIdentity(){
            return this.identity;
        }

        public final void registerScalable(IScalable scalable){
            this.scalableComponents.add(scalable);
        }

        public final void scaleComponents(float scale){
            for(IScalable scalable : this.scalableComponents)
                scalable.scale(scale);
        }

        @Override
        public void setToDestroy() {

        }

        @Override
        public void destroy(double ownerID) {
            //Destroy all children
            this.transform.getChildren().forEach(transform -> transform.destroy(transform.getOwnerID()));

            //Remove myself from any parent.
            if(this.transform.parent != null){
                this.transform.parent.removeChild(this.transform);
                this.transform.parent = null;
            }

            //Destroy active components.
            this.activeComponentList.forEach(comp -> comp.destroy(ownerID));

            //Destroy inactive components
            this.inactiveComponentList.forEach(comp -> comp.destroy(ownerID));

            //Clear both lists.
            this.activeComponentList.clear();
            this.inactiveComponentList.clear();

            //Destroy and clear identity.
            if(this.identity != null) {
                this.identity.destroy(ownerID);
                this.identity = null;
            }

            //Destroy and clear transform.
            this.transform.destroy(ownerID);
            this.transform = null;
        }

        @Override
        public boolean isDestroyed() {
            return false;
        }

        @Override
        public boolean isSetToBeDestroyed() {
            return false;
        }
    }
}
