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


/*
    Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
    All rights reserved.
  */
package edu.mit.genome.gp.ui.hclviewer;

import java.awt.*;

public final class GBA {
	public final static int B = GridBagConstraints.BOTH;
	public final static int C = GridBagConstraints.CENTER;
	public final static int E = GridBagConstraints.EAST;
	public final static int H = GridBagConstraints.HORIZONTAL;
	public final static int NONE = GridBagConstraints.NONE;
	public final static int N = GridBagConstraints.NORTH;
	public final static int NE = GridBagConstraints.NORTHEAST;
	public final static int NW = GridBagConstraints.NORTHWEST;
	public final static int RELATIVE = GridBagConstraints.RELATIVE;
	public final static int REMAINDER = GridBagConstraints.REMAINDER;
	public final static int S = GridBagConstraints.SOUTH;
	public final static int SE = GridBagConstraints.SOUTHEAST;
	public final static int SW = GridBagConstraints.SOUTHWEST;
	public final static int V = GridBagConstraints.VERTICAL;
	public final static int W = GridBagConstraints.WEST;

	private static GridBagConstraints c = new GridBagConstraints();

	public void add(Container container, Component component, int x, int y, int width, int height) {
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.gridheight = height;
		c.weightx = 0;
		c.weighty = 0;
		c.fill = GBA.NONE;
		c.anchor = GBA.C;
		c.insets = new Insets(0, 0, 0, 0);
		c.ipadx = 0;
		c.ipady = 0;
		container.add(component, c);
	}

	public void add(Container container, Component component, int x, int y, int width, int height,
			int weightx, int weighty, int fill, int anchor) {
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.gridheight = height;
		c.weightx = weightx;
		c.weighty = weighty;
		c.fill = fill;
		c.anchor = anchor;
		c.insets = new Insets(0, 0, 0, 0);
		c.ipadx = 0;
		c.ipady = 0;
		container.add(component, c);
	}

	public void add(Container container, Component component, int x, int y, int width, int height,
			int weightx, int weighty, int fill, int anchor, Insets insets, int ipadx, int ipady) {
		c.gridx = x;
		c.gridy = y;
		c.gridwidth = width;
		c.gridheight = height;
		c.weightx = weightx;
		c.weighty = weighty;
		c.fill = fill;
		c.anchor = anchor;
		c.insets = insets;
		c.ipadx = ipadx;
		c.ipady = ipady;
		container.add(component, c);
	}
}

