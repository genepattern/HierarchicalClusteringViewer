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

public class NodeSelectionEvent extends java.util.EventObject {
	int firstIndex, lastIndex;
	
	public NodeSelectionEvent(Object source, int firstIndex, int lastIndex) {
		super(source);	
		this.firstIndex = firstIndex;
		this.lastIndex = lastIndex;
	}
	
	public int getFirstIndex() {
		return firstIndex;
	}
	
	public int getLastIndex() {
		return lastIndex;
	}
}
