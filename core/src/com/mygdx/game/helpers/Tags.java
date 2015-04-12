package com.mygdx.game.helpers;

import gnu.trove.list.array.TIntArrayList;

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
     * Toggles the tag bit in this Tags object.
     * @param tag The tag to toggle.
     */
    public void toggleTag(int tag){
        tagMask ^= 1 << tag;
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

    /**
     * Checks if any of the passed in tags exists in this Tags object.
     * @param tagsToCheck The integer array of tags to check
     * @return True if any of the tagsToCheck exists in this Tags object.
     */
    public boolean hasAnyTag(int[] tagsToCheck){
        for (int tag : tagsToCheck) {
            int shift = 1 << tag;
            if((shift & tagMask) == shift)
                return true;
        }

        return false;
    }

    /**
     * Gets the tags as an integer array from this Tags object.
     * @return An integer array containing the integer tags.
     */
    public int[] getTags(){
        TIntArrayList tagList = new TIntArrayList();
        for(int i=1;i<33;i++){
            if(((tagMask >> i) & 1) == 1)
                tagList.add((int)Math.pow(2, i-1));
        }

        return tagList.toArray();
    }

    public final int getTagMask(){
        return this.tagMask;
    }

    /**
     * @return True if this Tags object has no entries in its tags.
     */
    public boolean isEmpty(){
        return tagMask == 0;
    }

    @Override
    public String toString() {
        return ""+tagMask;
    }
}
