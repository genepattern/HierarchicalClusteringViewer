package edu.mit.genome.gp.ui.hclviewer;
import javax.swing.JPanel;

import java.awt.*;
import java.awt.geom.*;

public class PixPanelForDend extends JPanel {
	private double ymax, ymin, xmax, xmin;
	private int topGutter, leftGutter, bottomGutter, rightGutter;
	private double xPixPerUnit = 2, yPixPerUnit = 1;
	boolean yscale = false;
	boolean xscale = false;
	
	/*boolean scaleFirstTime = false;
	boolean firstTime = true;
	*/
	private AffineTransform pixelTransform;
	
	private int width, height;

	public PixPanelForDend() {
		setBackground(java.awt.Color.white);
	}
	
	protected void setComputeYScale(boolean b) {
		yscale = b;	
	}
	
	protected void setComputeXScale(boolean b) {
		xscale = b;	
	}
	
	protected AffineTransform getPixelTransform() {
		return pixelTransform;	
	}

	protected void setMinMaxX(double xmin, double xmax) {
		this.xmin = xmin;
		this.xmax = xmax;
			pixelTransform = new AffineTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin * xPixPerUnit + leftGutter,
				ymax * yPixPerUnit + topGutter);
	}

	public int getIntXPixPerUnit() {
		return (int) xPixPerUnit;
	}
	
	public double getXPixPerUnit() {
		return  xPixPerUnit;
	}

	public int getIntYPixPerUnit() {
		return (int) yPixPerUnit;
	}

	public void zoomIn() {
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
			pixelTransform = new AffineTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin * xPixPerUnit + leftGutter,
				ymax * yPixPerUnit + topGutter);
	}

	public void zoomOut() {
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
			pixelTransform = new AffineTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin * xPixPerUnit + leftGutter,
				ymax * yPixPerUnit + topGutter);
	}


	public void setXPixPerUnit(double x) {
		xPixPerUnit = x;
			pixelTransform = new AffineTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin * xPixPerUnit + leftGutter,
				ymax * yPixPerUnit + topGutter);
	//	xscale = false;
	}

	public void setYPixPerUnit(double y) {
		yPixPerUnit = y;
			pixelTransform = new AffineTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin * xPixPerUnit + leftGutter,
				ymax * yPixPerUnit + topGutter);
	//	yscale = false;
	}

	public void setPixPerUnit(double x, double y) {
		xPixPerUnit = x;
		yPixPerUnit = y;
			pixelTransform = new AffineTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin * xPixPerUnit + leftGutter,
				ymax * yPixPerUnit + topGutter);
	//	yscale = false;
	//	xscale = false;
	}

	/*protected void setYScale(boolean b) {
		yscale = b;
	}

	protected void setXScale(boolean b) {
		xscale = b;
	}*/

	public void setMinMax(double xmin, double xmax, double ymin, double ymax) {
		this.xmin = xmin;
		this.xmax = xmax;
		this.ymin = ymin;
		this.ymax = ymax;
		pixelTransform = new AffineTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin * xPixPerUnit + leftGutter,
				ymax * yPixPerUnit + topGutter);
	}

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		width = getWidth();
		height = getHeight();

		
		
		if(yscale) {
			yPixPerUnit = (height - bottomGutter - topGutter - 1) / (ymax - ymin);// the y scale in pixels
		}
		if(xscale) {
			xPixPerUnit = (width - leftGutter - rightGutter - 1) / (xmax - xmin);
		}
	
		//pixelTransform.getMatrix(pixelMatrix);

		/*if(firstTime) {
			firstTime = false;
			if(scaleFirstTime) {
				yscale = false;
				xscale = false;
			}
		}*/
	}


	

	protected void setTopGutter(int top) {
		topGutter = top;	
			pixelTransform = new AffineTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin * xPixPerUnit + leftGutter,
				ymax * yPixPerUnit + topGutter);
	}
	
	protected void setLeftGutter(int left) {
		leftGutter = left;	
			pixelTransform = new AffineTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin * xPixPerUnit + leftGutter,
				ymax * yPixPerUnit + topGutter);
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
			pixelTransform = new AffineTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin * xPixPerUnit + leftGutter,
				ymax * yPixPerUnit + topGutter);
	}

	protected void setMinMaxY(double ymin, double ymax) {
		this.ymin = ymin;
		this.ymax = ymax;
			pixelTransform = new AffineTransform(xPixPerUnit, 0, 0, -yPixPerUnit, -xmin * xPixPerUnit + leftGutter,
				ymax * yPixPerUnit + topGutter);

		// AffineTransform(float m00, float m10, float m01, float m11, float m02, float m12)
		/*
		
				[ x']   [  m00  m01  m02  ] [ x ]   [ m00x + m01y + m02 ]
	[ y'] = [  m10  m11  m12  ] [ y ] = [ m10x + m11y + m12 ]
	[ 1 ]   [   0    0    1   ] [ 1 ]   [         1         ]
 
		*/
	}

	/**
	 *  Converts pixel to x world units.
	 *
	 * @param  pix
	 * @return      x panel units
	 */
	public double pixToX(int pix) {
		//return xmin + pix/xPixPerUnit ;
		return xmin + (pix - leftGutter) / xPixPerUnit;
	}

	/**
	 *  Converts x from world to pixel units.
	 *
	 * @param  x
	 * @return    the pixel value of the x coordinate
	 */
	public int xToPix(double x) {
		double pix = (x - xmin) * xPixPerUnit + leftGutter;
		//double pix = pixelMatrix[0] * x + pixelMatrix[4];
		if(pix > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		if(pix < Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		}
		//return  (int)Math.round(pix);
		return (int) Math.floor((float) pix);//gives better registration with affine transformation
	}

	/**
	 *  Converts pixel to x world units.
	 *
	 * @param  pix
	 * @return      x panel units
	 */
	public double pixToY(int pix) {
		return ymax - (pix - topGutter) / yPixPerUnit;
	}

	/**
	 *  Converts y from world to pixel units.
	 *
	 * @param  y
	 * @return    the pixel value of the y coordinate
	 */
	 
	public int yToPix(double y) {
		double pix = (ymax - y) * yPixPerUnit + topGutter;
	//	double pix = pixelMatrix[3] * y + pixelMatrix[5];
		if(pix > Integer.MAX_VALUE) {
			return Integer.MAX_VALUE;
		}
		if(pix < Integer.MIN_VALUE) {
			return Integer.MIN_VALUE;
		}
		//return  (int)Math.round(pix);
		return (int) Math.floor((float) pix);//gives better registration with affine transformation
	}
}

