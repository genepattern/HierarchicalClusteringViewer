package edu.mit.genome.gp.ui.hclviewer.colorconverter;

public abstract class ColorConverterHeader extends javax.swing.JPanel {

	/**
	 *  Updates the size of this header. The header should draw inside
	 *  drawableWidth. Note that drawableWidth can be different than the actual
	 *  size of the header.
	 *
	 * @param  drawableWidth  Description of the Parameter
	 * @return                height of the header
	 */
	public abstract int updateSize(int drawableWidth);
	
}

