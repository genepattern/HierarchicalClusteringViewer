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

