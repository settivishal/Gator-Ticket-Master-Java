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

    private void rotateLeft(Node node) {
        Node rightChild = node.right;
        node.right = rightChild.left;

        if (rightChild.left != null) {
            rightChild.left.parent = node;
        }

        rightChild.parent = node.parent;

        if (node.parent == null) {
            root = rightChild;
        } else if (node == node.parent.left) {
            node.parent.left = rightChild;
        } else {
            node.parent.right = rightChild;
        }

        rightChild.left = node;
        node.parent = rightChild;
    }

    private void rotateRight(Node node) {
        Node leftChild = node.left;
        node.left = leftChild.right;

        if (leftChild.right != null) {
            leftChild.right.parent = node;
        }

        leftChild.parent = node.parent;

        if (node.parent == null) {
            root = leftChild;
        } else if (node == node.parent.right) {
            node.parent.right = leftChild;
        } else {
            node.parent.left = leftChild;
        }

        leftChild.right = node;
        node.parent = leftChild;
    }

    public void insert(int userID, int seatID) {
        Node node = new Node(userID, seatID);
        Node parent = null;
        Node current = root;

        while (current != null) {
            parent = current;
            if (userID < current.userID) {
                current = current.left;
            } else {
                current = current.right;
            }
        }

        node.parent = parent;

        if (parent == null) {
            root = node;
        } else if (userID < parent.userID) {
            parent.left = node;
        } else {
            parent.right = node;
        }

        fixInsert(node);
    }

    private void fixInsert(Node node) {
        while (node != root && node.parent.color == RED) {
            if (node.parent == node.parent.parent.left) {
                Node uncle = node.parent.parent.right;

                if (uncle != null && uncle.color == RED) {
                    node.parent.color = BLACK;
                    uncle.color = BLACK;
                    node.parent.parent.color = RED;
                    node = node.parent.parent;
                } else {
                    if (node == node.parent.right) {
                        node = node.parent;
                        rotateLeft(node);
                    }
                    node.parent.color = BLACK;
                    node.parent.parent.color = RED;
                    rotateRight(node.parent.parent);
                }
            } else {
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
        root.color = BLACK;
    }

    public Node find(int userID) {
        Node current = root;
        while (current != null) {
            if (userID == current.userID) {
                return current;
            }
            current = userID < current.userID ? current.left : current.right;
        }
        return null;
    }

    public void delete(int userID) {
        Node node = find(userID);
        if (node == null)
            return;

        Node x, y;
        if (node.left == null || node.right == null) {
            y = node;
        } else {
            y = successor(node);
        }

        if (y.left != null) {
            x = y.left;
        } else {
            x = y.right;
        }

        if (x != null) {
            x.parent = y.parent;
        }

        if (y.parent == null) {
            root = x;
        } else if (y == y.parent.left) {
            y.parent.left = x;
        } else {
            y.parent.right = x;
        }

        if (y != node) {
            node.userID = y.userID;
            node.seatID = y.seatID;
        }

        if (y.color == BLACK) {
            fixDelete(x, y.parent);
        }
    }

    private Node successor(Node node) {
        if (node.right != null) {
            node = node.right;
            while (node.left != null) {
                node = node.left;
            }
            return node;
        }

        Node parent = node.parent;
        while (parent != null && node == parent.right) {
            node = parent;
            parent = parent.parent;
        }
        return parent;
    }

    private void fixDelete(Node node, Node parent) {
        while (node != root && (node == null || node.color == BLACK)) {
            if (node == parent.left) {
                Node sibling = parent.right;

                if (sibling.color == RED) {
                    sibling.color = BLACK;
                    parent.color = RED;
                    rotateLeft(parent);
                    sibling = parent.right;
                }

                if ((sibling.left == null || sibling.left.color == BLACK) &&
                        (sibling.right == null || sibling.right.color == BLACK)) {
                    sibling.color = RED;
                    node = parent;
                    parent = node.parent;
                } else {
                    if (sibling.right == null || sibling.right.color == BLACK) {
                        if (sibling.left != null) {
                            sibling.left.color = BLACK;
                        }
                        sibling.color = RED;
                        rotateRight(sibling);
                        sibling = parent.right;
                    }
                    sibling.color = parent.color;
                    parent.color = BLACK;
                    if (sibling.right != null) {
                        sibling.right.color = BLACK;
                    }
                    rotateLeft(parent);
                    node = root;
                }
            } else {
                Node sibling = parent.left;

                if (sibling.color == RED) {
                    sibling.color = BLACK;
                    parent.color = RED;
                    rotateRight(parent);
                    sibling = parent.left;
                }

                if ((sibling.right == null || sibling.right.color == BLACK) &&
                        (sibling.left == null || sibling.left.color == BLACK)) {
                    sibling.color = RED;
                    node = parent;
                    parent = node.parent;
                } else {
                    if (sibling.left == null || sibling.left.color == BLACK) {
                        if (sibling.right != null) {
                            sibling.right.color = BLACK;
                        }
                        sibling.color = RED;
                        rotateLeft(sibling);
                        sibling = parent.left;
                    }
                    sibling.color = parent.color;
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

    public List<Node> inorderTraversal() {
        List<Node> result = new ArrayList<>();
        inorderTraversalHelper(root, result);
        return result;
    }

    private void inorderTraversalHelper(Node node, List<Node> result) {
        if (node != null) {
            inorderTraversalHelper(node.left, result);
            result.add(node);
            inorderTraversalHelper(node.right, result);
        }
    }
}
