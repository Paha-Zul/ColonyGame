package com.mygdx.game.helpers;

/**
 * Created by Paha on 4/8/2015.
 */
public class Tags {
    private int tagMask;

    /**
     * Adds a tag to this Entity.
     * @param tag The tag to add.
     */
    public void addTag(int tag){
        this.tagMask |= (1 << tag); //OR the tag to the mask.
    }

    /**
     * Removes a tag from this Entity.
     * @param tag The tag to remove.
     */
    public void removeTag(int tag){
        this.tagMask &= ~(1 << tag); //AND the inverted tag to zero it out.
    }

    /**
     * Clears/removes all tags from this Tag.
     */
    public void clearTags(){
        this.tagMask = 0b0;
    }

    /**
     * Checks if this Entity has the tag passed in.
     * @param tag The tag to check for.
     * @return True if this Entity has the tag, false otherwise.
     */
    public boolean hasTag(int tag){
        return ((1 << tag) & tagMask) == (1 << tag);
    }

    /**
     * Checks if the Entity has multiple tags.
     * @param tagsToCheck The tags to check for.
     * @return True if this Entity has all the tags, false otherwise.
     */
    public boolean hasTags(int[] tagsToCheck){
        int tags = 0b0; //0 in binary
        for (int aTagsToCheck : tagsToCheck) tags |= (1 << aTagsToCheck);
        return (tags & tagMask) == tags;
    }
}
