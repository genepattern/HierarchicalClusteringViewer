package edu.mit.genome.gp.ui.hclviewer.colorconverter;

public interface ColorConverter {

	public java.awt.Color getColor(int row, int column);
	public abstract boolean hasHeader();
	public ColorConverterHeader getHeader();
}
