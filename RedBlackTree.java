import java.util.ArrayList;
import java.util.List;

public class RedBlackTree {
    private static final boolean RED = true;
    private static final boolean BLACK = false;

    static class Node {
        int userID;
        int seatID;
        boolean color;
        Node left, right, parent;

        Node(int userID, int seatID) {
            this.userID = userID;
            this.seatID = seatID;
            this.color = RED;
        }
    }

    private Node root;

    /**
     * Rotate the node to the left. This is used to balance the tree when a node is inserted.
     * @param node the node to rotate
     */
    private void rotateLeft(Node node) {
        Node rightChild = node.right;
        node.right = rightChild.left;

        if (rightChild.left != null) {
            rightChild.left.parent = node;
        }

        rightChild.parent = node.parent;

        if (node.parent == null) {
            // If the node is the root, set the root to the right child
            root = rightChild;
        } else if (node == node.parent.left) {
            // If the node is the left child of its parent, set the left child of the parent to the right child
            node.parent.left = rightChild;
        } else {
            // If the node is the right child of its parent, set the right child of the parent to the right child
            node.parent.right = rightChild;
        }

        // Set the left child of the right child to the node
        rightChild.left = node;
        node.parent = rightChild;
    }

    /**
     * Rotate the node to the right. This is used to balance the tree when a node is inserted.
     * @param node the node to rotate
     */
    private void rotateRight(Node node) {
        // The right child of the node is the left child of its parent
        Node leftChild = node.left;
        // Set the left child of the node to the right child of its left child
        node.left = leftChild.right;

        // Set the parent of the right child of the left child to the node
        if (leftChild.right != null) {
            leftChild.right.parent = node;
        }

        // Set the parent of the left child to the parent of the node
        leftChild.parent = node.parent;

        // If the node is the root, set the root to the left child
        if (node.parent == null) {
            root = leftChild;
        } else if (node == node.parent.right) {
            // If the node is the right child of its parent, set the right child of the parent to the left child
            node.parent.right = leftChild;
        } else {
            // If the node is the left child of its parent, set the left child of the parent to the left child
            node.parent.left = leftChild;
        }

        // Set the right child of the left child to the node
        leftChild.right = node;
        // Set the parent of the node to the left child
        node.parent = leftChild;
    }

    /**
     * Inserts a new node with the given userID and seatID into the tree.
     * The tree is then balanced using the fixInsert method.
     * @param userID the ID of the user
     * @param seatID the ID of the seat
     */
    public void insert(int userID, int seatID) {
        // Create a new node with the given userID and seatID
        Node node = new Node(userID, seatID);
        Node parent = null;
        Node current = root;

        // Traverse the tree to find the correct position for the new node
        while (current != null) {
            parent = current;
            if (userID < current.userID) {
                current = current.left;
            } else {
                current = current.right;
            }
        }

        // Set the parent of the new node
        node.parent = parent;

        // Insert the new node as a child of its parent
        if (parent == null) {
            root = node; // Tree was empty, new node is now the root
        } else if (userID < parent.userID) {
            parent.left = node; // New node is the left child
        } else {
            parent.right = node; // New node is the right child
        }

        // Rebalance the tree after insertion
        rebalanceTreeAfterInsert(node);
    }

    /**
     * Rebalance the tree after a node is inserted. The tree is self-balancing, meaning that the color of the nodes is adjusted to ensure that the tree remains balanced.
     * @param node the inserted node
     */
    private void rebalanceTreeAfterInsert(Node node) {
        while (node != root && node.parent.color == RED) {
            // If the parent is the left child of the grandparent
            if (node.parent == node.parent.parent.left) {
                Node siblingOfParent = node.parent.parent.right;

                // If the sibling of the parent is also red, change the parent and its sibling to black, and the grandparent to red. Move the node up one level to the grandparent.
                if (siblingOfParent != null && siblingOfParent.color == RED) {
                    node.parent.color = BLACK;
                    siblingOfParent.color = BLACK;
                    node.parent.parent.color = RED;
                    node = node.parent.parent;
                } else {
                    // If the node is the right child of the parent, rotate the parent to the left first.
                    if (node == node.parent.right) {
                        node = node.parent;
                        rotateLeft(node);
                    }
                    // Then, change the parent to black, the grandparent to red, and rotate the grandparent to the right.
                    node.parent.color = BLACK;
                    node.parent.parent.color = RED;
                    rotateRight(node.parent.parent);
                }
            } else {
                // If the parent is the right child of the grandparent, do the same as above, but with the colors reversed.
                Node siblingOfParent = node.parent.parent.left;

                if (siblingOfParent != null && siblingOfParent.color == RED) {
                    node.parent.color = BLACK;
                    siblingOfParent.color = BLACK;
                    node.parent.parent.color = RED;
                    node = node.parent.parent;
                } else {
                    if (node == node.parent.left) {
                        node = node.parent;
                        rotateRight(node);
                    }
                    node.parent.color = BLACK;
                    node.parent.parent.color = RED;
                    rotateLeft(node.parent.parent);
                }
            }
        }
        // Finally, make sure the root is black.
        root.color = BLACK;
    }

    /**
     * Find the node with the given user ID in the tree.
     * @param userID the user ID to search for
     * @return the node with the given user ID, or null if no such node exists
     */
    public Node findNode(int userID) {
        Node current = root;
        while (current != null) {
            // If the user ID of the current node is equal to the given user ID,
            // return the current node.
            if (userID == current.userID) {
                return current;
            }

            // If the user ID of the current node is greater than the given user ID,
            // move to the left child. Otherwise, move to the right child.
            current = userID < current.userID ? current.left : current.right;
        }

        // If we reach here, the node with the given user ID was not found.
        return null;
    }

    /**
     * Deletes the node with the specified userID from the tree.
     * @param userID the ID of the user whose node is to be deleted
     */
    public void deleteNode(int userID) {
        // Find the node with the given userID
        Node node = findNode(userID);
        if (node == null)
            return;

        Node replacementChild, nodeToRemove;
        // Determine the node to be removed
        if (node.left == null || node.right == null) {
            nodeToRemove = node;
        } else {
            nodeToRemove = successorNode(node);
        }

        // Set replacementChild to the non-null child of nodeToRemove, if any
        if (nodeToRemove.left != null) {
            replacementChild = nodeToRemove.left;
        } else {
            replacementChild = nodeToRemove.right;
        }

        // Update parent references
        if (replacementChild != null) {
            replacementChild.parent = nodeToRemove.parent;
        }

        // Update the root if necessary
        if (nodeToRemove.parent == null) {
            root = replacementChild;
        } else if (nodeToRemove == nodeToRemove.parent.left) {
            nodeToRemove.parent.left = replacementChild;
        } else {
            nodeToRemove.parent.right = replacementChild;
        }

        // Copy nodeToRemove's data to the node if necessary
        if (nodeToRemove != node) {
            node.userID = nodeToRemove.userID;
            node.seatID = nodeToRemove.seatID;
        }

        // Fix the tree balance if nodeToRemove was black
        if (nodeToRemove.color == BLACK) {
            rebalanceTreeAfterDelete(replacementChild, nodeToRemove.parent);
        }
    }

    /**
     * Finds the successor of a given node in the tree.
     * If the node has a right child, the successor is the leftmost node in the right subtree. Otherwise, the successor is the parent of the node, or one of its parents if the node is the rightmost node in its subtree.
     * @param node the node to find the successor of
     * @return the successor of the node
     */
    private Node successorNode(Node node) {
        if (node.right != null) {
            // If the node has a right child, the successor is the leftmost node in the right subtree
            node = node.right;
            while (node.left != null) {
                node = node.left;
            }
            return node;
        }

        // Otherwise, the successor is the parent of the node, or one of its parents if the node is the rightmost node in its subtree
        Node parent = node.parent;
        while (parent != null && node == parent.right) {
            node = parent;
            parent = parent.parent;
        }
        return parent;
    }

    /**
     * Rebalances the tree after a node is deleted. The tree is self-balancing, meaning that the color of the nodes is adjusted to ensure that the tree remains balanced.
     * @param node the node to fix the tree balance for
     * @param parent the parent of the node
     */
    private void rebalanceTreeAfterDelete(Node node, Node parent) {
        while (node != root && (node == null || node.color == BLACK)) {
            if (node == parent.left) {
                // If the node is the left child of the parent, get the right sibling node
                Node siblingNode = parent.right;

                if (siblingNode.color == RED) {
                    // If the sibling node is red, swap the colors of the parent and sibling node
                    siblingNode.color = BLACK;
                    parent.color = RED;
                    rotateLeft(parent);
                    siblingNode = parent.right;
                }

                // If the sibling node is black, check if the sibling node has a red child
                if ((siblingNode.left == null || siblingNode.left.color == BLACK) &&
                        (siblingNode.right == null || siblingNode.right.color == BLACK)) {
                    siblingNode.color = RED;
                    // If the sibling node has no red children, color the sibling node red
                    node = parent;
                    parent = node.parent;
                } else {
                    if (siblingNode.right == null || siblingNode.right.color == BLACK) {
                        if (siblingNode.left != null) {
                        // If the sibling node's right child is black, color the sibling node's left child black
                            siblingNode.left.color = BLACK;
                        }
                        siblingNode.color = RED;
                        rotateRight(siblingNode);
                        siblingNode = parent.right;
                    }
                    siblingNode.color = parent.color;

                    // Swap the colors of the parent and sibling node
                    parent.color = BLACK;
                    if (siblingNode.right != null) {
                        siblingNode.right.color = BLACK;
                    }
                    rotateLeft(parent);
                    node = root;
                }
            } else {
                Node siblingNode = parent.left;
                // If the node is the right child of the parent, get the left sibling node

                if (siblingNode.color == RED) {
                    siblingNode.color = BLACK;
                    // If the sibling node is red, swap the colors of the parent and sibling node
                    parent.color = RED;
                    rotateRight(parent);
                    siblingNode = parent.left;
                }

                if ((siblingNode.right == null || siblingNode.right.color == BLACK) &&(siblingNode.left == null || siblingNode.left.color == BLACK)) {
                    siblingNode.color = RED;
                    node = parent;
                    parent = node.parent;
                } else {
                    if (siblingNode.left == null || siblingNode.left.color == BLACK) {
                        if (siblingNode.right != null) {
                            siblingNode.right.color = BLACK;
                        }
                        siblingNode.color = RED;
                        rotateLeft(siblingNode);
                        siblingNode = parent.left;
                    }
                    siblingNode.color = parent.color;

                    // Swap the colors of the parent and sibling node
                    parent.color = BLACK;
                    if (siblingNode.left != null) {
                        siblingNode.left.color = BLACK;
                    }
                    rotateRight(parent);
                    node = root;
                }
            }
        }
        if (node != null) {
            node.color = BLACK;
        }
    }

    /**
     * Performs an inorder traversal of the Red-Black Tree.
     * This method returns a list of nodes in the tree sorted by their in-order sequence.
     * @return a list of nodes in inorder traversal order
     */
    public List<Node> inorderTraversal() {
        List<Node> result = new ArrayList<>();
        // Helper method is called to perform the recursion
        inorderTraversalHelper(root, result);
        return result;
    }

    /**
     * Helper method for performing an inorder traversal of the Red-Black Tree.
     * This method recursively traverses the tree and adds each node to the result list.
     * @param node the current node in the traversal
     * @param result the list of nodes to add to
     */
    private void inorderTraversalHelper(Node node, List<Node> result) {
        if (node != null) {
            // Recursively traverse the left subtree
            inorderTraversalHelper(node.left, result);
            // Add the current node to the result list
            result.add(node);
            // Recursively traverse the right subtree
            inorderTraversalHelper(node.right, result);
        }
    }
}
