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
