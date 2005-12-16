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

/**
 *  NodeSelectionListener defines the interface for an object that listens to
 *  changes in the selection of a node in a Dendrogram.
 *
 * @author     jgould
 * @created    August 19, 2003
 */
interface NodeSelectionListener extends java.util.EventListener {

	/**
	 * @param  e  notifies the listener of the range of selected leaves
	 */
	void nodeSelectionChanged(NodeSelectionEvent e);

}

