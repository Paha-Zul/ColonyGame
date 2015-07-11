package com.mygdx.game.util;

/**
 * Created by brad on 7/11/15.
 */
public class StateTree extends Tree{
    private TreeNode currTreeNode = null;

    public StateTree(String treeName, String rootName) {
        super(treeName, rootName);
    }

    /**
     * Moves up to the parent of the current tree node. If the parent is null,
     * the current node does not change.
     * @return The node that is the new current node.
     */
    public TreeNode moveUp(){
        TreeNode node = this.currTreeNode.parent;
        if(node != null) this.currTreeNode = node;
        return this.currTreeNode;
    }

    /**
     * Moves down to a child of the current node. If the child could not be found (is null),
     * the current node does not change.
     * @param nodeName The name of the child node.
     * @return The new current node.
     */
    public TreeNode moveDowntoChild(String nodeName){
        TreeNode node = this.currTreeNode.getChild(nodeName);
        if(node != null) this.currTreeNode = node;
        return this.currTreeNode;
    }
}
