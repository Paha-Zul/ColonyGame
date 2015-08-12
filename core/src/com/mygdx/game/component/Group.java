package com.mygdx.game.component;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.mygdx.game.entity.Entity;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;

/**
 * Created by Paha on 5/8/2015.
 *
 * <p>A group of Entities with a leader.</p>
 */
public class Group extends Component{
    @JsonIgnore
    private Entity leader;
    @JsonIgnore
    private Array<Entity> groupList = new Array<>();
    @JsonProperty
    private boolean stayNearLeader = true, attackLeaderTarget = true;

    public Group(){

        this.setActive(false);
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void save() {

    }

    @Override
    public void load() {

    }

    public Entity addEntityToGroup(Entity entity){
        this.groupList.add(entity);
        return entity;
    }

    public boolean removeEntityFromGroup(Entity entity){
        return this.groupList.removeValue(entity, true);
    }

    /**
     * Sets the leader of this group and returns it.
     * @param leader The Entity leader of this group.
     * @return The Entity that was assigned as the leader of this group.
     */
    @JsonIgnore
    public Entity setLeader(Entity leader){
        return this.leader = leader;
    }

    @JsonIgnore
    public void setStayNearLeader(boolean stayNearLeader) {
        this.stayNearLeader = stayNearLeader;
    }

    @JsonIgnore
    public void setAttackLeaderTarget(boolean attackLeaderTarget) {
        this.attackLeaderTarget = attackLeaderTarget;
    }

    /**
     * Gets the leader for this group. If the leader is not alive (more filters to add later), then an Entity is pulled from the group
     * and assigned as the new leader. If there are no Entities left in the group, returns null as the leader.
     * @return The Entity leader of this group. Null if the group is empty and no valid leader can be set.
     */
    @JsonIgnore
    public Entity getLeader(){
        if(!leader.getTags().hasTag("alive") || this.leader == null) getNewLeader();
        return this.leader;
    }

    @JsonIgnore
    public Array<Entity> getGroupList() {
        return groupList;
    }

    @JsonIgnore
    public boolean isStayNearLeader() {
        return stayNearLeader;
    }

    @JsonIgnore
    public boolean isAttackLeaderTarget() {
        return attackLeaderTarget;
    }

    /**
     * Replaces the leader with a random one from the group. If there are no Entities left in the group,
     * assigns it to null.
     */
    @JsonIgnore
    private void getNewLeader(){
        if(groupList.size > 0) {
            int rand = MathUtils.random(groupList.size-1);
            Entity newLeader = groupList.get(rand);
            groupList.removeIndex(rand);
            this.setLeader(newLeader);
        }else
            this.leader = null;
    }

    /**
     * Do not use this function for this Component. Since this Component attaches to many different Entities, the owner could be any Entity in the group.
     * @return The wrong Entity or most of the time, null.
     */
    @Deprecated
    @Override
    public Entity getEntityOwner() {
        return super.getEntityOwner();
    }

    @Override
    @JsonIgnore
    public void setToDestroy() {
        super.setToDestroy();
    }

    @Override
    public void destroy(Entity destroyer) {
        //If it is the leader, we need to find a new one! Otherwise, remove it from the group.
        if(destroyer == this.leader) getNewLeader();
        else removeEntityFromGroup(destroyer);

        if(groupList.size == 0 && this.leader == null)
            super.destroy(destroyer);
    }


}
