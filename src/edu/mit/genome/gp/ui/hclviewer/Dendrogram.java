/*
  The Broad Institute
  SOFTWARE COPYRIGHT NOTICE AGREEMENT
  This software and its documentation are copyright (2003-2006) by the
  Broad Institute/Massachusetts Institute of Technology. All rights are
  reserved.

  This software is supplied without any warranty or guaranteed support
  whatsoever. Neither the Broad Institute nor MIT can be responsible for its
  use, misuse, or functionality.
*/


package edu.mit.genome.gp.ui.hclviewer;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

public class Dendrogram extends ZoomPanel {
	// the position of the leftmost leaf is 1. Leaf nodes position start at 1 and increase by 1. Position(parent) = (Position(left child) + Position(right child))/2
	TreeNode root;
	Color nodeColor = new Color(0, 0, 128);
	Color selectedNodeColor = Color.yellow;
	TreeNode selectedNode = null;// the root of the selected branch
	Map selectedNodes = new HashMap();
	int numLeaves = 0;

	/**  min and max distances or correlations */
	double minTreeHeight = Double.MAX_VALUE;
	double maxTreeHeight = -Double.MAX_VALUE;

	EventListenerList nodeSelectionListeners = new EventListenerList();

	public Dendrogram(TreeNode root, int orientation) {
		super();
		this.root = root;
		setLeafNodeSpacing(0, 10);
		addMouseListener(new DendrogramMouseListener());
		calculateInternalNodePositions(root);
		setOrientation(orientation);
	}


	/**
	 *  if orientation is vertical start is the x location in pixels to start
	 *  drawing the left-most leaf if orientation is horizontal start is the y
	 *  location in pixels to start drawing the left-most leaf
	 *
	 * @param  start  The new leafNodeSpacing value
	 * @param  width  The new leafNodeSpacing value
	 */
	public void setLeafNodeSpacing(int start, int width) {
		if(getOrientation() == SwingConstants.HORIZONTAL) {
			setYPixPerUnit(width);
			setTopGutter(start);
		} else {
			setXPixPerUnit(width);
			setLeftGutter(start);
		}

	}


	/**
	 *  Sets the orientation for this dendrogram. Typically gene trees have a
	 *  SwingConstants.HORIZONTAL orientation and array trees have a
	 *  SwingConstants.VERTICAL orientation.
	 *
	 * @param  orientation  The new orientation value
	 */
	public void setOrientation(int orientation) {
		super.setOrientation(orientation);
		if(orientation == SwingConstants.VERTICAL) {
			setMinMaxY(maxTreeHeight, minTreeHeight);
			setComputeYScale(true);// autoscale y

			setMinMaxX(-0.5, numLeaves - 0.5);
		} else if(orientation == SwingConstants.HORIZONTAL) {
			setMinMaxX(minTreeHeight, maxTreeHeight);
			setComputeXScale(true);// autoscale x
			setMinMaxY(-0.5, numLeaves - 0.5);
		} else {
			throw new IllegalArgumentException("Unknown orientation.");
		}
	}


	public static void main(String[] args) {
		JFrame f = new JFrame();
		TreeNode left = new TreeNode(null, null, 1, "1"); // FIXME ?? height of node > heat of parent
		left.position = 0;
		TreeNode right = new TreeNode(null, null, 1, "2");
		right.position = 1;
		TreeNode root = new TreeNode(left, right, 0.5, "3");
		Dendrogram d = new Dendrogram(root, SwingConstants.VERTICAL);
		d.setTopGutter(10);
		d.setBottomGutter(10);
		f.getContentPane().add(d);
		f.setSize(400, 400);
		f.show();

	}


	/**
	 *  Adds a listener to be notified when a node selection is changed
	 *
	 * @param  l  The new nodeSelectionListener value
	 */
	public void addNodeSelectionListener(NodeSelectionListener l) {
		nodeSelectionListeners.add(NodeSelectionListener.class, l);
	}


	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		Line2D.Double line = new Line2D.Double();
		//root.draw(this, g2);


		java.util.List q = new LinkedList();
		q.add(root);
		while(!q.isEmpty()) {
			TreeNode currentNode = (TreeNode) q.remove(0);
			TreeNode leftChild = currentNode.left;
			TreeNode rightRhild = currentNode.right;
			if(leftChild != null && leftChild.left != null && leftChild.right != null) {
				q.add(leftChild);
			}
			if(rightRhild != null && rightRhild.left != null && rightRhild.right != null) {
				q.add(rightRhild);
			}
			double currentNodeHeight = currentNode.height;
			double left_child_height = (leftChild == null) ? 0 : leftChild.height;//FIXME check this, don't think need if
			double right_child_height = (rightRhild == null) ? 0 : rightRhild.height;
			double pixHeight = yToPix(currentNodeHeight);

			double xPix1 = xToPix(leftChild.position);// when left orientation gives y pixel coordinate
			double xPix2 = xToPix(rightRhild.position);
			if(selectedNodes.get(currentNode.id) != null) {
				g2.setColor(selectedNodeColor);
			} else {
				g2.setColor(nodeColor);
			}

			if(getOrientation() == SwingConstants.VERTICAL) {
				line.setLine(xPix1, pixHeight, xPix1, yToPix(left_child_height));// draw line to left child
			} else {
				line.setLine(pixHeight, xPix1, yToPix(left_child_height), xPix1);
			}
			g2.draw(line);
			if(getOrientation() == SwingConstants.VERTICAL) {
				line.setLine(xPix2, pixHeight, xPix2, yToPix(right_child_height));// draw line to right child
			} else {
				line.setLine(pixHeight, xPix2, yToPix(right_child_height), xPix2);
			}
			g2.draw(line);
			if(getOrientation() == SwingConstants.VERTICAL) {
				line.setLine(xPix1, pixHeight, xPix2, pixHeight);// connect
			} else {
				line.setLine(pixHeight, xPix1, pixHeight, xPix2);
			}
			g2.draw(line);

		}

	}


	/**  Deselects all nodes */
	public void clearSelection() {
		selectedNode = null;
		selectedNodes.clear();
	}


	private void updateSelectedNodes(TreeNode selectedNode) {
		java.util.List tempQ = new LinkedList();
		tempQ.add(selectedNode);
		while(!tempQ.isEmpty()) {
			TreeNode node = (TreeNode) tempQ.remove(0);
			selectedNodes.put(node.id, Boolean.TRUE);
			TreeNode left = node.left;
			TreeNode right = node.right;
			if(left != null && (left.left != null || left.right != null)) {// don't add leaf nodes to q
				tempQ.add(left);
			}
			if(right != null && (right.left != null || right.right != null)) {// don't add leaf nodes to q
				tempQ.add(right);
			}
		}
	}


	/**
	 *  a postorder traveral of the tree to calculate internal node positions
	 *
	 * @param  node  Description of the Parameter
	 */
	private void calculateInternalNodePositions(TreeNode node) {
		if(node.left == null) {// leaf node
			minTreeHeight = Math.min(minTreeHeight, node.height);
			maxTreeHeight = Math.max(maxTreeHeight, node.height);
			numLeaves++;
		} else {
			calculateInternalNodePositions(node.left);
			calculateInternalNodePositions(node.right);
			//node.height = 1.0 - ((1.0 - node.correlation) / 2.0);
			node.height = Math.min(node.height, node.left.height);
			node.height = Math.min(node.height, node.right.height);
			node.position = (node.left.position + node.right.position) / 2.0;
			minTreeHeight = Math.min(minTreeHeight, node.height);
			maxTreeHeight = Math.max(maxTreeHeight, node.height);
		}

	}



	private void notifyListeners(int minIndex, int maxIndex) {
		Object[] listeners = nodeSelectionListeners.getListenerList();
		NodeSelectionEvent event = new NodeSelectionEvent(this, minIndex, maxIndex);
		for(int i = listeners.length - 2; i >= 0; i -= 2) {
			if(listeners[i] == NodeSelectionListener.class) {
				((NodeSelectionListener) listeners[i + 1]).nodeSelectionChanged(event);
			}
		}
	}

	/**
	 * @author       jgould
	 * @created      August 12, 2003
	 * @param  root  Description of the Parameter
	 */
	private class DendrogramMouseListener extends MouseAdapter {
		int xMouseClick, yMouseClick;


		public void mouseClicked(MouseEvent e) {

			xMouseClick = e.getX();
			yMouseClick = e.getY();
			if(getOrientation() == SwingConstants.HORIZONTAL) {
				int temp = yMouseClick;
				yMouseClick = xMouseClick;
				xMouseClick = temp;
			}
			selectedNode = null;
			selectedNodes.clear();
			java.util.List q = new LinkedList();
			q.add(root);
			while(!q.isEmpty()) {// FIXME prune search space by starting at leaves within 5 pixels of click and going up tree
				TreeNode currentNode = (TreeNode) q.remove(0);
				TreeNode leftChild = currentNode.left;
				TreeNode rightRhild = currentNode.right;

				double pixHeight = yToPix(currentNode.height);
				double xPix1 = xToPix(leftChild.position);

				double xPix2 = xToPix(rightRhild.position);
				if(isSelected((xPix1 + xPix2) / 2, pixHeight)) {// calculate average of xpix1 and xpix2 to get center
					selectedNode = currentNode;
					updateSelectedNodes(currentNode);
					break;
				}
				if(leftChild != null && leftChild.left != null && leftChild.right != null) {
					q.add(leftChild);
				}
				if(rightRhild != null && rightRhild.left != null && rightRhild.right != null) {
					q.add(rightRhild);
				}
			}

			int rightIndex = -1;
			int leftIndex = -1;
			if(selectedNode != null) {
				TreeNode temp = selectedNode;
				while(temp.left != null) {
					temp = temp.left;
				}

				rightIndex = (int) temp.position;

				temp = selectedNode;
				while(temp.right != null) {
					temp = temp.right;
				}
				leftIndex = (int) temp.position;
			}
			notifyListeners(leftIndex, rightIndex);

			repaint();

		}


		private boolean isSelected(double xPix1, double pixHeight) {
			// mouse click can be within 5 pixels in any direction of a node to select it
			/*
			    if(xMouseClick == -1 || yMouseClick == -1) {
			    return false;
			    }
			  */
			double xPixDistance = xMouseClick - xPix1;
			double yPixDistance = yMouseClick - pixHeight;
			double pixDistance = Math.sqrt(xPixDistance * xPixDistance + yPixDistance * yPixDistance);
			//	System.out.println("xMouseClick " + xMouseClick + " xPix1 " + xPix1 + " yMouseClick " + pixHeight);
			if(pixDistance <= 5) {

				return true;
			}
			return false;
		}


		private void preOrderTraversal(TreeNode node) {
			if(node.left == null) {
				return;
			}
			double pixHeight = yToPix(node.height);
			double xPix1 = xToPix(node.left.position);
			double xPix2 = xToPix(node.right.position);
			if(isSelected((xPix1 + xPix2) / 2, pixHeight)) {// calculate average of xpix1 and xpix2 to get center
				selectedNode = node;
			} else {
				preOrderTraversal(node.left);
				preOrderTraversal(node.right);
			}
		}

	}

}

