package edu.mit.genome.gp.ui.hclviewer.colorconverter;
import java.awt.image.BufferedImage;
import java.awt.*;
import javax.swing.*;

import edu.mit.genome.gp.ui.hclviewer.FloatMatrix;

public class AbsoluteColorConverter implements ColorConverter {
	static Color missingColor = new Color(128, 128, 128);
	BufferedImage maxColorImage = createGradientImage(Color.black, Color.red);
	BufferedImage minColorImage = createGradientImage(Color.green, Color.black);
	FloatMatrix matrix;
	ColorConverterHeader header;
	float min, max;
	Color maxColor, minColor, neutralColor;

	public AbsoluteColorConverter(Color minColor, Color maxColor, Color neutralColor, FloatMatrix matrix, float min, float max) {
		this.matrix = matrix;
		maxColorImage = createGradientImage(neutralColor, maxColor);
		minColorImage = createGradientImage(minColor, neutralColor);
		this.min = min;
		this.max = max;
		this.maxColor = maxColor;
		this.minColor = minColor;
		this.neutralColor = neutralColor;
	}

	public Color getMaxColor() {
		return maxColor;
	}

	public Color getMinColor() {
		return minColor;
	}

	public Color getNeutralColor() {
		return neutralColor;
	}

	/**
	 *  Creates a gradient image with specified initial colors.
	 *
	 * @param  color1  Description of the Parameter
	 * @param  color2  Description of the Parameter
	 * @return         Description of the Return Value
	 */
	private BufferedImage createGradientImage(Color color1, Color color2) {
		BufferedImage image = (BufferedImage) java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(256, 1);
		Graphics2D graphics = image.createGraphics();
		GradientPaint gp = new GradientPaint(0, 0, color1, 255, 0, color2);
		graphics.setPaint(gp);
		graphics.drawRect(0, 0, 255, 1);
		return image;
	}

	public Color getColor(int row, int column) {
		float value = matrix.getElement(row, column);
		if(Float.isNaN(value)) {
			return missingColor;
		}
		float maximum = value < 0 ? this.min : this.max;
		int colorIndex = (int) (255 * value / maximum);
		colorIndex = colorIndex > 255 ? 255 : colorIndex;
		int rgb = value < 0 ? minColorImage.getRGB(255 - colorIndex, 0) : maxColorImage.getRGB(colorIndex, 0);
		return new Color(rgb);
	}

	public ColorConverterHeader getHeader() {
		if(header == null) {
			header = new AbsoluteHeader();
		}
		return header;
	}

	public boolean hasHeader() {
		return true;
	}

	private class AbsoluteHeader extends ColorConverterHeader {
		final int IMAGE_HEIGHT = 20;
		//int leftGutter;
		int drawableWidth;
		int height;

		public AbsoluteHeader() {
			setBackground(Color.white);
			setFont(new Font("monospaced", Font.BOLD, 12));

		}

// getIntXPixPerUnit() * nx; leftGutter = geneTree.getWidth();
		public int updateSize(int drawableWidth) {
			//	setPreferredSize(new Dimension(width, IMAGE_HEIGHT + 20));
			this.drawableWidth = drawableWidth;
			//	this.leftGutter = leftGutter;
			if(height == 0) {
				Graphics g = getGraphics();
				if(g == null) {
					return 0;
				}
				FontMetrics fm = g.getFontMetrics();
				g.dispose();
				height = IMAGE_HEIGHT + fm.getHeight();
			}
			return height;
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			int width2 = drawableWidth / 2;
			Graphics2D g2 = (Graphics2D) g;
			g.setColor(neutralColor); // hack for 1.3 on os x
			g.fillRect(0, 0, drawableWidth, IMAGE_HEIGHT);
			g.drawImage(minColorImage, 0, 0, width2, IMAGE_HEIGHT, null);
			g.drawImage(maxColorImage, width2, 0, width2, IMAGE_HEIGHT, null);
			g.setColor(Color.black);
			FontMetrics fm = g.getFontMetrics();
			g.drawString(String.valueOf(min), 0, IMAGE_HEIGHT + fm.getAscent());
			int textWidth = fm.stringWidth(String.valueOf(max));
			g.drawString(String.valueOf(max), drawableWidth - textWidth, IMAGE_HEIGHT + fm.getAscent());
		}
	}
}

