package edu.mit.genome.gp.ui.hclviewer;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.geom.*;
import java.util.*;
import java.awt.image.*;

public class TreeNode {
	TreeNode left;
	TreeNode right;
	double position;// x coordinate
	double height;// y coordinate 
	Object id;
	
	static Line2D.Double line = new Line2D.Double();

	/**
	 * @param  left         the left child
	 * @param  right        the right child
	 * @param  correlation  correlation, this should be 1 for leaf nodes
	 * @param  id           id 
	 */
	public TreeNode(TreeNode left, TreeNode right, double height, Object id) {
		this.left = left;
		this.right = right;
		this.height = height;
		this.id = id;
	}


/*	public void draw(Dendrogram dendrogram, Graphics2D g) {
		if(dendrogram.selectedNode == this) {
			g.setColor(Color.yellow);
		}
		TreeNode leftChild = this.left;
		TreeNode rightRhild = this.right;

		double currentNodeHeight = this.height;

		double left_child_height = (leftChild == null) ? 0 : leftChild.height;

		double right_child_height = (rightRhild == null) ? 0 : rightRhild.height;

		double pixHeight = dendrogram.yToPix(currentNodeHeight);

		double xPix1 = dendrogram.xToPix(leftChild.position);
		double xPix2 = dendrogram.xToPix(rightRhild.position);


		if(dendrogram.orientation == Dendrogram.Orientation.TOP) {
			line.setLine(xPix1, pixHeight, xPix1, dendrogram.yToPix(left_child_height));// draw line to left child
		} else {
			line.setLine(pixHeight, xPix1, dendrogram.yToPix(left_child_height), xPix1);
		}

		g.draw(line);

		if(dendrogram.orientation == Dendrogram.Orientation.TOP) {
			line.setLine(xPix2, pixHeight, xPix2, dendrogram.yToPix(right_child_height));// draw line to right child
		} else {
			line.setLine(pixHeight, xPix2, dendrogram.yToPix(right_child_height), xPix2);
		}
		g.draw(line);

		if(dendrogram.orientation == Dendrogram.Orientation.TOP) {
			line.setLine(xPix1, pixHeight, xPix2, pixHeight);// connect
		} else {
			line.setLine(pixHeight, xPix1, pixHeight, xPix2);
		}

		g.draw(line);
		if(leftChild != null && leftChild.left != null && leftChild.right != null) {
			leftChild.draw(dendrogram, g);
		}
		if(rightRhild != null && rightRhild.left != null && rightRhild.right != null) {
			rightRhild.draw(dendrogram, g);
		}
	} */

}

