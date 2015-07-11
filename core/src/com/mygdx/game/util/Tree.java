package com.mygdx.game.util;

import com.badlogic.gdx.utils.Array;

import java.util.LinkedList;

/**
 * A basic, no specialization, tree.
 */
public class Tree {
    private String treeName;
    private TreeNode root;
    private LinkedList<TreeNode> queue = new LinkedList<>() ;

    public Tree(String treeName, String rootName){
        this.treeName = treeName;
        this.root = new TreeNode(rootName);
    }

    /**
     * Adds a node to the Tree.
     * @param parent The parent compName of the node to add the node to.
     * @param newNodeName The new node's compName.
     * @return The TreeNode that was created to add to the tree, or null if the parent was not found.
     */
    public TreeNode addNode(String parent, String newNodeName){
        TreeNode parentNode = getNode(node -> node.nodeName.equals(parent));
        if(parentNode == null) return null;
        return parentNode.addChild(new TreeNode(newNodeName));
    }

    /**
     * Adds multiple new nodes to the tree.
     * @param parent The compName of the parent node to add the new nodes to.
     * @param nodeNames The names of the new nodes to add.
     * @return This Tree object.
     */
    public TreeNode[] addNode(String parent, String... nodeNames){
        TreeNode parentNode = getNode(node -> node.nodeName.equals(parent));
        if(parentNode == null) return null;

        Array<TreeNode> nodeList = new Array<>();
        for(String name : nodeNames)
            nodeList.add(parentNode.addChild(new TreeNode(name)));

        return nodeList.toArray(TreeNode.class);
    }

    /**
     * Gets a node by performing a breadth-first search on the tree.
     * @param test The Predicate function to test each node.
     * @return The TreeNode if one was found that passed the Predicate function, null otherwise.
     */
    public TreeNode getNode(java.util.function.Predicate<TreeNode> test) {
        if (this.root == null)
            return null;

        //Breadth first search....
        queue.clear();
        queue.add(this.root);

        while(!queue.isEmpty()){
            TreeNode node = queue.remove();
            if(test.test(node))
                return node;

            for(TreeNode child : node.children)
                queue.add(child);
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        //Depth first search...
        queue.clear();
        queue.add(root);

        while(!queue.isEmpty()){
            TreeNode node = queue.remove();

            if(node.children.size > 0) str.append(node.nodeName).append("->(");

            for(int i=0; i < node.children.size; i++) {
                queue.add(node.children.get(i));
                str.append(node.children.get(i).nodeName);
                if(i != node.children.size-1) str.append(",");
            }

            if(node.children.size > 0) str.append(")");
            str.append(" ");
        }

        return str.toString();
    }

    /**
     * The node of the tree.
     */
    public static class TreeNode {
        public String nodeName;
        public TreeNode parent;
        public Object userData;

        private Array<TreeNode> children = new Array<>();

        public TreeNode(String nodeName){
            this.nodeName = nodeName;
        }

        /**
         * Adds a child node to this node.
         * @param treeNode The new node to add.
         * @return The node that was added as a child.
         */
        public TreeNode addChild(TreeNode treeNode){
            this.children.add(treeNode);
            treeNode.parent = this;
            return treeNode;
        }

        /**
         * Searches this node's children for a node with 'nodeName'.
         * @param nodeName The name of the child node to find.
         * @return The node if it was found, null otherwise.
         */
        public TreeNode getChild(String nodeName){
            for(TreeNode treeNode : children)
                if(nodeName.equals(treeNode.nodeName))
                    return treeNode;

            return null;
        }

        /**
         * @return True if this node has children, false otherwise.
         */
        public boolean hasChildren(){
            return children.size > 0;
        }

        /**
         * @return A list of the children.
         */
        public Array<TreeNode> getChildren(){
            return this.children;
        }
    }
}


