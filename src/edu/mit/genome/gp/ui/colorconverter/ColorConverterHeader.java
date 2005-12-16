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

