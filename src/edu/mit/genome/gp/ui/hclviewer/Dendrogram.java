package edu.mit.genome.gp.ui.hclviewer;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.*;
import java.util.*;
import java.awt.image.*;

//FIXME have common superclass
public class Dendrogram extends PixPanelForDend {
	// the position of the leftmost leaf is 1 leaf nodes start at 1 and increase by 1. Position(parent) = (Position(left child) + Position(right child))/2
	TreeNode root;
	short orientation;
	Color nodeColor = new Color(0, 0, 128);
	Color selectedNodeColor = Color.yellow;
	TreeNode selectedNode = null;// the root of the selected branch
	Map selectedNodes = new HashMap();
	NodeSelectionListener nodeSelectionListener;
	int numLeaves = 0;

	public static short TOP_ORIENTATION = 1;
	public static short LEFT_ORIENTATION = 2;
	
	/** min and max distances or correlations */
	double min = Double.MAX_VALUE;
	double max = Double.MIN_VALUE;
	
	public static void main(String[] args) {
		JFrame f = new JFrame();
		
		TreeNode left= new TreeNode(null, null, 1, "1");
		left.correlation = -20.733477;
		left.position = 0;
		
		TreeNode right= new TreeNode(null, null, 1, "2");
		right.correlation = -22.908957;
		right.position = 10;
		
		TreeNode root= new TreeNode(left, right, 1, "3");
		root.correlation = -23.378288;
		
		Dendrogram d = new Dendrogram(root, Dendrogram.LEFT_ORIENTATION);
		d.setLeafNodeSpacing(0,4);
		f.getContentPane().add(d, BorderLayout.CENTER);
		f.setSize(400,400);
		f.show();
	}
	
	public Dendrogram(TreeNode root, short orientation) {
		super();
		this.root = root;
		addMouseListener(new DendrogramMouseListener());
		calculateLeafPositions(0, root);
		calculateInternalNodePositions(root);
		
		setOrientation(orientation);
		setBackground(Color.white);
	
/*	try {
		selectedNode = findCommonAncestor(1, 2);
		System.out.println("selectedNode " + selectedNode.id);
		updateSelectedNodes(selectedNode);
		
	} catch(Exception e){}
		//debug
		
		    System.out.println("numLeaves " + numLeaves + " leafNodes.size() " + leafNodes.size());
		    for(int i = 0, size = leafNodes.size(); i < size; i++) {
		    TreeNode t = (TreeNode) leafNodes.get(i);
		    System.out.println(t.id);
		    }
		 */
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
		
	
/*	private TreeNode findCommonAncestor(double position1, double position2) {
		TreeNode leftFinger = root;
		TreeNode rightFinger = root;
		
		TreeNode beforeLeftFinger = null;
		
		while(leftFinger==rightFinger) { // find the 1st node where pointers diverge
			beforeLeftFinger = leftFinger;
			if(leftFinger.position < position1) {
				System.out.println("leftFinger = leftFinger.right;");
				leftFinger = leftFinger.right;
			} else if(leftFinger.position > position1){ // if(leftFinger.position > index1) {
				System.out.println("leftFinger = leftFinger.left;");
				leftFinger = leftFinger.left;
			} 
			
			if(rightFinger.position < position2) {
				System.out.println("rightFinger = rightFinger.right;");
				rightFinger = rightFinger.right;
			} else if(rightFinger.position > position2) { // if(leftFinger.position > index1) {
				System.out.println("rightFinger = rightFinger.left;");
				rightFinger = rightFinger.left;
			} 
		}
		return leftFinger;
		
	} */

	/**
	 *  Sets the listener to be notified when a node selection is changed
	 *
	 * @param  l  The new nodeSelectionListener value
	 */
	public void setNodeSelectionListener(NodeSelectionListener l) {
		nodeSelectionListener = l;
	}

	public int xToPix(double x) {
		if(orientation == TOP_ORIENTATION ) {
			return super.xToPix(x);
		}
		return super.yToPix(x);
		//	double pix = (x - xmin) * xPixPerUnit + leftGutter;
		//	return (int) Math.round(pix);
	}

	public int yToPix(double y) {
		if(orientation == TOP_ORIENTATION ) {
			return super.yToPix(y);
			//return (int) (getHeight() * y);
		}
		return super.xToPix(y);
		//return (int) (getWidth() * y);
	}

	/**
	 *  Sets the orientation for this dendrogram. Typically gene trees have a left
	 *  orientation and array trees have a top orientation.
	 *
	 * @param  o  The new orientation value
	 */
	private void setOrientation(short orientation) {
		this.orientation = orientation;
//		int numLeaves = leafNodes.size();

		if(orientation == TOP_ORIENTATION) {
			//setMinMaxY(0, 1);  
			setMinMaxY(max, min);
			setComputeYScale(true);
			setMinMaxX(-0.5, numLeaves - 0.5);
			
		} else if(orientation == LEFT_ORIENTATION) {
			setMinMaxY(-0.5, numLeaves - 0.5);
			setMinMaxX(min, max);
			setComputeXScale(true);
		} else {
			throw new IllegalArgumentException("Unknown orientation.");	
		}
	}

	/**
	 *  if orientation is vertical start is the x location in pixels to start
	 *  drawing the left-most leaf if orientation is horizontal start is the y
	 *  location in pixels to start drawing the left-most leaf
	 *
	 * @param  start  The new leafNodeSpacing value
	 * @param  width  The new leafNodeSpacing value
	 */
	public void setLeafNodeSpacing(double start, double width) {
		if(orientation == LEFT_ORIENTATION ) {
			setYPixPerUnit(width);
			setTopGutter((int) start);//FIXME

			//	setPreferredSize(new Dimension(400, (int)(topGutter+leafNodes.size()*yPixPerUnit)));
		} else {
			setXPixPerUnit(width);
			setLeftGutter((int) start);

			//	setPreferredSize(new Dimension((int)(leftGutter+leafNodes.size()*xPixPerUnit), 400));
		}

	}

	/**
	 *  a postorder traveral of the tree to calculate leaf positions
	 *
	 * @param  pos   Description of the Parameter
	 * @param  node  Description of the Parameter
	 * @return       Description of the Return Value
	 */
	private int calculateLeafPositions(int pos, TreeNode node) {
		if(node.left != null) {
		//	pos = 
			calculateLeafPositions(pos, node.left);
		}
		if(node.right != null) {
			//pos = 
			calculateLeafPositions(pos, node.right);
		} else {
			numLeaves++;
		//	node.position = pos;
			pos++;
			min = Math.min(min, node.correlation);
			max = Math.max(max, node.correlation);
			
		}
		
		return pos;
		
	}

	private void calculateInternalNodePositions(TreeNode node) {
		if(node.left == null) {
			return;
		}

		calculateInternalNodePositions(node.left);
		calculateInternalNodePositions(node.right);
	 	//node.height = 1.0 - ((1.0 - node.correlation) / 2.0);
		node.height = node.correlation; // FIXME
		node.height = Math.min(node.height, node.left.height);
		node.height = Math.min(node.height, node.right.height);
		node.position = (node.left.position + node.right.position) / 2.0;
		
		min = Math.min(min, node.correlation);
		max = Math.max(max, node.correlation);

	}


	public void  paintComponent(Graphics g) {
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
			double left_child_height = (leftChild == null) ? 0 : leftChild.height;
			double right_child_height = (rightRhild == null) ? 0 : rightRhild.height;
			double pixHeight = yToPix(currentNodeHeight);
			double xPix1 = xToPix(leftChild.position); // when left orientation gives y pixel coordinate
			double xPix2 = xToPix(rightRhild.position);

	//		System.out.println("leftChild.position " + leftChild.position + " xPix1 " + xPix1);
	//		System.out.println("rightRhild.position " + rightRhild.position + " xPix2 " + xPix2);
			if(selectedNodes.get(currentNode.id) != null) {
				g2.setColor(selectedNodeColor);
			} else {
				g2.setColor(nodeColor);
			}

			if(orientation == TOP_ORIENTATION ) {
				line.setLine(xPix1, pixHeight, xPix1, yToPix(left_child_height));// draw line to left child
			} else {
				line.setLine(pixHeight, xPix1, yToPix(left_child_height), xPix1);
			}
			g2.draw(line);
			if(orientation == TOP_ORIENTATION ) {
				line.setLine(xPix2, pixHeight, xPix2, yToPix(right_child_height));// draw line to right child
			} else {
				line.setLine(pixHeight, xPix2, yToPix(right_child_height), xPix2);
			}
			g2.draw(line);
			if(orientation == TOP_ORIENTATION ) {
				line.setLine(xPix1, pixHeight, xPix2, pixHeight);// connect
			} else {
				line.setLine(pixHeight, xPix1, pixHeight, xPix2);
			}
			g2.draw(line);

		}
	
	}

	/**  Deselects all nodes  */
	public void clearSelection() {
		selectedNode = null;
		selectedNodes.clear();
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
			if(orientation == LEFT_ORIENTATION ) {
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

			if(nodeSelectionListener != null) {
				int index1 = -1;
				int index2 = -1;
				if(selectedNode != null) {
					TreeNode temp = selectedNode;
					while(temp.left != null) {
						temp = temp.left;
					}

					index1 = (int) temp.position;

					temp = selectedNode;
					while(temp.right != null) {
						temp = temp.right;
					}
					index2 = (int) temp.position;
				}
				nodeSelectionListener.nodeSelectionChanged(new NodeSelectionEvent(Dendrogram.this, index1, index2));
			}
			repaint();

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

	}



}

