package edu.mit.genome.gp.ui.hclviewer;

public class NodeSelectionEvent extends java.util.EventObject {
	int index1, index2;
	
	public NodeSelectionEvent(Dendrogram source, int index1, int index2) {
		super(source);	
		this.index1 = index1;
		this.index2 = index2;
	}
	
	public int getIndex1() {
		return index1;
	}
	
	public int getIndex2() {
		return index2;
	}
}
