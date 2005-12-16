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
import java.awt.*;
import java.awt.geom.*;
import javax.swing.*;
import java.text.NumberFormat;
import org.genepattern.data.matrix.*;


/**
 *  converts float values to a color from a color map. Performs normalization by
 *  row.
 *
 * @author     KOhm
 * @author     jgould
 * @created    August 12, 2003
 */
public final class RowColorConverter implements ColorConverter {

	/**  the minimum value */
	private float min = 0;
	/**  the maximum value */
	private float max = 8000;
	/**  the mean value */
	private float mean = Float.NEGATIVE_INFINITY;
	/**  use log scale */
	private ColorResponse response;
	/**  the color table */
	private final Color[] colors;
	/**  the boundry values used to determine which color to associate a value */
	private final float[] slots;

	private static int[] defaultColorMap = {0x4500ad, 0x2700d1, 0x6b58ef, 0x8888ff, 0xc7c1ff,
			0xd5d5ff, 0xffc0e5, 0xff8989, 0xff7080, 0xff5a5a, 0xef4040, 0xd60c00};

	private ColorConverterHeader header;
	DoubleMatrix2D matrix;
	/** the last row for which max, min, and mean were computed */
	int lastRow = -1;
	static Color missingColor = new Color(128, 128, 128);

	/**
	 *  constructs a new ColorConverter
	 *
	 * @param  colormap  array of rgb values. Each element contains an RGB value
	 *      consisting of the red component in bits 16-23, the green component in
	 *      bits 8-15, and the blue component in bits 0-7.
	 * @param  matrix    Description of the Parameter
	 * @param  response  Description of the Parameter
	 */
	public RowColorConverter(int[] colormap, ColorResponse response, DoubleMatrix2D matrix) {
		Color[] colors = new Color[colormap.length];
		for(int i = 0; i < colormap.length; ++i) {
			colors[i] = new Color(colormap[i]);
		}
		this.colors = colors;
		this.slots = new float[colormap.length];
		this.response = response;
		if(response != ColorResponse.LINEAR && response != ColorResponse.LOG) {
			throw new IllegalArgumentException("Unkown ColorResponse (" + response + ")");
		}
		this.matrix = matrix;
	}


	public RowColorConverter(ColorResponse response, DoubleMatrix2D matrix) {
		this(defaultColorMap, response, matrix);
	}

	/**
	 *  helper method to calculate the slots considering the color response
	 *
	 * @param  min     Description of the Parameter
	 * @param  max     Description of the Parameter
	 * @param  mean    Description of the Parameter
	 * @param  values  Description of the Parameter
	 */
	private void calculateSlots(final float min, final float max, final float mean, final float[] values) {
		if(response == ColorResponse.LOG) {
			computeLogScaleSlots(min, max, mean, values);
		} else {
			computeLinearSlots(min, max, mean, values);
		}

	}


	/**
	 *  Calculates the min, max, and mean for the specified row in the matrix.
	 *
	 * @param  rowNumber  Description of the Parameter
	 */
	private void calculateRowStats(int rowNumber) {
		float theMin;
		float theMax;
		float theMean;
		int num = matrix.getColumnCount();
		theMin = Float.POSITIVE_INFINITY;
		theMax = Float.NEGATIVE_INFINITY;
		theMean = 0;
		int numDataPoints = 0; // some arrays might not have data for all genes
		for(int i = 0; i < num; i++) {
			float tmpVal = (float) matrix.get(rowNumber, i);
			if(Float.isNaN(tmpVal)) {
				continue;	
			}
			if(tmpVal < theMin) {
				theMin = tmpVal;
			}
			if(tmpVal > theMax) {
				theMax = tmpVal;
			}
			theMean += tmpVal;
			numDataPoints++;
		}
		theMean /= numDataPoints;
		
		this.min = theMin;
		this.max = theMax;
		this.mean = theMean;
		calculateSlots(min, max, mean, slots);
	}


	/**
	 *  conputes the log scaled slot values
	 *
	 * @param  real_min   Description of the Parameter
	 * @param  real_max   Description of the Parameter
	 * @param  real_mean  Description of the Parameter
	 * @param  slots      Description of the Parameter
	 */
	private void computeLogScaleSlots(final float real_min, final float real_max, final float real_mean, final float[] slots) {
		//cannot take log of 0 or negatives
		final float min = 1f;
		//cannot take log of 0 or negatives
		final float max = real_max - real_min + min;
		//cannot take log of 0 or negatives
		final float mean = real_mean - real_min + min;
		final float range = (float) (Math.log(max) - Math.log(min));
		final int num = slots.length;
		final float inc = range / (float) num;
		float log_val = inc;

		final float adjustment = real_min - min;
		for(int i = 0; i < num; i++) {
			slots[i] = (float) Math.exp(log_val) + adjustment;
			log_val += inc;
		}
		//System.out.println("final log_val="+(log_val - inc));
	}

	/**
	 *  computes the linear (almost) slot values
	 *
	 * @param  min    Description of the Parameter
	 * @param  max    Description of the Parameter
	 * @param  mean   Description of the Parameter
	 * @param  slots  Description of the Parameter
	 */
	private void computeLinearSlots(final float min, final float max, final float mean, final float[] slots) {
	//	final boolean min_to_max = true;
		final float ave = (mean == Float.NEGATIVE_INFINITY ? (max - min) / 2 : mean);
		final int num = slots.length;
		final int halfway = slots.length / 2;
		
	//	if(min_to_max) {
			// calc the range min -> ave
			final float inc2 = (ave - min) / halfway;
			float lin_val = min;
			for(int i = 0; i < halfway; i++) {
				lin_val += inc2;
				slots[i] = lin_val;
			}

			// ave -> max
			final float inc = (max - ave) / (num - halfway);
			lin_val = ave;
			for(int i = halfway; i < num; i++) {
				lin_val += inc;
				slots[i] = lin_val;
			}
//		}
		/*} else {// max -> min
			// max -> ave
			final float inc = (ave - max) / (num - halfway);
			lin_val = max;
			for(int i = 0; i < halfway; i++) {
				slots[i] = lin_val;
				lin_val += inc;// actually is adding a negative
			}

			// calc the range ave -> min
			final float inc2 = (min - ave) / halfway;
			lin_val = ave;
			for(int i = halfway; i < num; i++) {
				slots[i] = lin_val;
				lin_val += inc2;
			}
		}*/
	}

	/**
	 *  gets the color for the specified entry in the matrix. Getting colors in a
	 *  loop row-by-row is quicker than column-by-column.
	 *
	 * @param  row     row index of matrix
	 * @param  column  column index of matrix
	 * @return         The color value
	 */
	public Color getColor(int row, int column) {
		if(lastRow != row) {
			calculateRowStats(row);
			lastRow = row;
		}
		float val = (float) matrix.get(row, column);
		if(Float.isNaN(val)) {
			return missingColor;	
		}
		final int num = slots.length - 1;
		if(val >= slots[num]) {
			return colors[num];
		}
		for(int i = num; i > 0; i--) {// rev loop
			if(slots[i] > val && val > slots[i - 1]) {//assumes slots[i] > slots[i - 1]
				return colors[i];
			}
		}
		return colors[0];// all the rest
	}


	/**
	 *  gets the color at the index
	 *
	 * @param  i  Description of the Parameter
	 * @return    The colorAt value
	 */
	public Color getColorAt(int i) {
		return colors[i];
	}

	/**
	 *  returns the number of colors
	 *
	 * @return    The colorCount value
	 */
	public int getColorCount() {
		return colors.length;
	}

	/**
	 *  gets a copy of the slots array including the first element
	 *
	 * @return    The slots value
	 */
	public float[] getSlots() {
		float[] new_slots = new float[slots.length + 1];
		new_slots[0] = min;
		System.arraycopy(slots, 0, new_slots, 1, slots.length);
		return new_slots;
	}


	public boolean hasHeader() {
		return false;	
	}
	public ColorConverterHeader getHeader() {
	/*	if(header == null) {
			header = new RowColorConverterHeader();
		}
		return header;
		*/
		return null;
	}

	/**
	 * @author     KOhm
	 * @author     jgould
	 * @created    August 12, 2003
	 */
	private class RowColorConverterHeader extends ColorConverterHeader {
		
		private NumberFormat format_dot_one = NumberFormat.getInstance();
		int leftGutter;
		int drawableWidth;
		int imageHeight = 10;

		public RowColorConverterHeader() {
			setBackground(Color.white);
			format_dot_one.setMaximumFractionDigits(1);
			format_dot_one.setMinimumFractionDigits(1);
		}

		public int updateSize(int drawableWidth) {
		//	this.leftGutter = leftGutter;
			this.drawableWidth = drawableWidth;
			return imageHeight + 20;
		}


		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g;
			final int num_colors = getColorCount();

			float step = (float) (drawableWidth) / num_colors;
			g2.setFont(new Font("monospaced", Font.PLAIN, (int) (step/4)));
			FontMetrics fm = g2.getFontMetrics();
			int ascent = fm.getAscent();
			float xpos = leftGutter;
			Rectangle2D.Double rect = new Rectangle2D.Double();
			rect.width = step;
			rect.height = imageHeight;
			float[] values = getSlots();
			for(int i = 0; i < num_colors; i++) {
				g2.setColor(getColorAt(i));
				rect.x = xpos;
				g2.fill(rect);
				String label = format_dot_one.format(values[i]);
				int stringWidth = fm.stringWidth(label);
				float yLabelPos = imageHeight + ascent + 4;
				g2.setColor(Color.black);
				g2.drawString(label, xpos+stringWidth/2f, yLabelPos);
				xpos += step;
			}

		}

	}

}

