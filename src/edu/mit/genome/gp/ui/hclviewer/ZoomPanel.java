package edu.mit.genome.gp.ui.hclviewer;

import java.awt.Graphics;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public class ZoomPanel extends JPanel {
	private double ymax, ymin, xmax, xmin;
	private int topGutter, leftGutter, bottomGutter, rightGutter;
	private double xPixPerUnit = 2, yPixPerUnit = 1;
	private boolean yscale = false;
	private boolean xscale = false;
	private int orientation = SwingConstants.VERTICAL;


	protected ZoomPanel() {
		setBackground(java.awt.Color.white);
	}


	/**
	 *  Sets the orientation of the panel
	 *
	 * @param  orientation  The new orientation value
	 */
	protected void setOrientation(int orientation) {
		if(orientation != SwingConstants.VERTICAL && orientation != SwingConstants.HORIZONTAL) {
			throw new IllegalArgumentException("Illegal Orienation.");
		}
		this.orientation = orientation;
	}


	protected void setXPixPerUnit(double x) {
		xPixPerUnit = x;
	}


	protected void setYPixPerUnit(double y) {
		yPixPerUnit = y;
	}


	protected void setPixPerUnit(double x, double y) {
		xPixPerUnit = x;
		yPixPerUnit = y;
	}


	protected void setMinMax(double xmin, double xmax, double ymin, double ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
	}

	protected int getOrientation() {
		return orientation;
	}


	protected int getXPixPerUnitAsInt() {
		return (int) xPixPerUnit;
	}


	protected double getXPixPerUnit() {
		return xPixPerUnit;
	}


	protected int getYPixPerUnitAsInt() {
		return (int) yPixPerUnit;
	}


	protected int xToPixAsInt(double x) {
		return (int) Math.floor(xToPix(x));
	}
	
	protected int yToPixAsInt(double y) {
		return (int) Math.floor(yToPix(y));
	}
	/*
	    protected AffineTransform getPixelTransform() {
	    return pixelTransform;
	    }
	  */
	/**
	 *  Converts x from world to pixel units.
	 *
	 * @param  x
	 * @return    the pixel value of the x coordinate
	 */
	protected double xToPix(double x) {
		if(orientation == SwingConstants.VERTICAL) {
			return _xToPix(x);
		}
		return _yToPix(x);
	}


	/**
	 *  Converts y from world to pixel units.
	 *
	 * @param  y
	 * @return    the pixel value of the y coordinate
	 */
	protected double yToPix(double y) {
		if(orientation == SwingConstants.VERTICAL) {
			return _yToPix(y);
		}
		return _xToPix(y);
	}
	
	protected void zoomIn() {
		if(xPixPerUnit < 0) {
			xPixPerUnit -= 2;
		} else {
			xPixPerUnit += 2;
		}

		if(yPixPerUnit < 0) {
			yPixPerUnit -= 2;
		} else {
			yPixPerUnit += 2;
		}
		
	}
	
	protected void zoomOut() {
		if(xPixPerUnit < 0) {
			xPixPerUnit += 2;
		} else {
			xPixPerUnit -= 2;
		}

		if(yPixPerUnit < 0) {
			yPixPerUnit += 2;
		} else {
			yPixPerUnit -= 2;
		}
	
	}


	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		int width = getWidth();
		int height = getHeight();
		if(yscale) {
			yPixPerUnit = (height - bottomGutter - topGutter - 1) / (ymax - ymin);
		}
		if(xscale) {
			xPixPerUnit = (width - leftGutter - rightGutter - 1) / (xmax - xmin);
		}
	}


	/**
	 *  Converts pixel to x world units.
	 *
	 * @param  pix  Description of the Parameter
	 * @return      x panel units
	 */

	protected double pixToX(int pix) {
		// return xmin + pix/xPixPerUnit ;
		return xmin + (pix - leftGutter) / xPixPerUnit;
	}


	/**
	 *  Converts pixel to x world units.
	 *
	 * @param  pix  Description of the Parameter
	 * @return      x panel units
	 */

	protected double pixToY(int pix) {
		return ymax - (pix - topGutter) / yPixPerUnit;
	}


	protected void setComputeYScale(boolean b) {
		yscale = b;
	}


	protected void setComputeXScale(boolean b) {
		xscale = b;
	}


	protected void setMinMaxX(double xmin, double xmax) {
		this.xmin = xmin;
		this.xmax = xmax;
	}


	protected void setTopGutter(int top) {
		topGutter = top;
	}


	protected void setLeftGutter(int left) {
		leftGutter = left;
	}


	protected void setRightGutter(int right) {
		rightGutter = right;
	}


	protected void setBottomGutter(int bottom) {
		bottomGutter = bottom;
	}


	protected void setGutters(int left, int top, int right, int bottom) {
		leftGutter = left;
		topGutter = top;
		rightGutter = right;
		bottomGutter = bottom;
	}


	protected void setMinMaxY(double ymin, double ymax) {
		this.ymin = ymin;
		this.ymax = ymax;
	}

	private double _xToPix(double x) {
		double pix = (x - xmin) * xPixPerUnit + leftGutter;
		return pix;
	}

	private double _yToPix(double y) {
		return (ymax - y) * yPixPerUnit + topGutter;
	}
}

