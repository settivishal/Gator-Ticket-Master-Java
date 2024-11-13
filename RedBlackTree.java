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

        // Fix the tree balance after insertion
        fixInsert(node);
    }

    /**
     * Fixes the tree balance after a node is inserted. The tree is
     * self-balancing, meaning that the color of the nodes is adjusted
     * to ensure that the tree remains balanced.
     *
     * @param node the inserted node
     */
    private void fixInsert(Node node) {
        while (node != root && node.parent.color == RED) {
            // If the parent is the left child of the grandparent
            if (node.parent == node.parent.parent.left) {
                Node uncle = node.parent.parent.right;

                // If the uncle is also red, change the parent and uncle to black, and the grandparent to red. Move the node up one level to the grandparent.
                if (uncle != null && uncle.color == RED) {
                    node.parent.color = BLACK;
                    uncle.color = BLACK;
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
                Node uncle = node.parent.parent.left;

                if (uncle != null && uncle.color == RED) {
                    node.parent.color = BLACK;
                    uncle.color = BLACK;
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
    public Node find(int userID) {
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
     * 
     * @param userID the ID of the user whose node is to be deleted
     */
    public void delete(int userID) {
        // Find the node with the given userID
        Node node = find(userID);
        if (node == null)
            return;

        Node x, y;
        // Determine the node to be removed (y)
        if (node.left == null || node.right == null) {
            y = node;
        } else {
            y = successor(node);
        }

        // Set x to the non-null child of y, if any
        if (y.left != null) {
            x = y.left;
        } else {
            x = y.right;
        }

        // Update parent references
        if (x != null) {
            x.parent = y.parent;
        }

        // Update the root if necessary
        if (y.parent == null) {
            root = x;
        } else if (y == y.parent.left) {
            y.parent.left = x;
        } else {
            y.parent.right = x;
        }

        // Copy y's data to the node if necessary
        if (y != node) {
            node.userID = y.userID;
            node.seatID = y.seatID;
        }

        // Fix the tree balance if y was black
        if (y.color == BLACK) {
            fixDelete(x, y.parent);
        }
    }

    /**
     * Finds the successor of a given node in the tree.
     * If the node has a right child, the successor is the leftmost node in the right subtree.
     * Otherwise, the successor is the parent of the node, or one of its parents if the node is the rightmost node in its subtree.
     * 
     * @param node the node to find the successor of
     * @return the successor of the node
     */
    private Node successor(Node node) {
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
     * Fixes the tree balance after a node is deleted. The tree is
     * self-balancing, meaning that the color of the nodes is adjusted
     * to ensure that the tree remains balanced.
     * 
     * @param node the node to fix the tree balance for
     * @param parent the parent of the node
     */
    private void fixDelete(Node node, Node parent) {
        while (node != root && (node == null || node.color == BLACK)) {
            if (node == parent.left) {
                // If the node is the left child of the parent, get the right sibling
                Node sibling = parent.right;

                if (sibling.color == RED) {
                    // If the sibling is red, swap the colors of the parent and sibling
                    sibling.color = BLACK;
                    parent.color = RED;
                    rotateLeft(parent);
                    sibling = parent.right;
                }

                // If the sibling is black, check if the sibling has a red child
                if ((sibling.left == null || sibling.left.color == BLACK) &&
                        (sibling.right == null || sibling.right.color == BLACK)) {
                    sibling.color = RED;
                    // If the sibling has no red children, color the sibling red
                    node = parent;
                    parent = node.parent;
                } else {
                    if (sibling.right == null || sibling.right.color == BLACK) {
                        if (sibling.left != null) {
                        // If the sibling's right child is black, color the sibling's left child black
                            sibling.left.color = BLACK;
                        }
                        sibling.color = RED;
                        rotateRight(sibling);
                        sibling = parent.right;
                    }
                    sibling.color = parent.color;

                    // Swap the colors of the parent and sibling
                    parent.color = BLACK;
                    if (sibling.right != null) {
                        sibling.right.color = BLACK;
                    }
                    rotateLeft(parent);
                    node = root;
                }
            } else {
                Node sibling = parent.left;
                // If the node is the right child of the parent, get the left sibling

                if (sibling.color == RED) {
                    sibling.color = BLACK;
                    // If the sibling is red, swap the colors of the parent and sibling
                    parent.color = RED;
                    rotateRight(parent);
                    sibling = parent.left;
                }

                if ((sibling.right == null || sibling.right.color == BLACK) &&
                // If the sibling is black, check if the sibling has a red child
                        (sibling.left == null || sibling.left.color == BLACK)) {
                    sibling.color = RED;
                    // If the sibling has no red children, color the sibling red
                    node = parent;
                    parent = node.parent;
                } else {
                    if (sibling.left == null || sibling.left.color == BLACK) {
                        if (sibling.right != null) {
                        // If the sibling's left child is black, color the sibling's right child black
                            sibling.right.color = BLACK;
                        }
                        sibling.color = RED;
                        rotateLeft(sibling);
                        sibling = parent.left;
                    }
                    sibling.color = parent.color;

                    // Swap the colors of the parent and sibling
                    parent.color = BLACK;
                    if (sibling.left != null) {
                        sibling.left.color = BLACK;
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
     *
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
     * 
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
