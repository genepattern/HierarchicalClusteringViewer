package edu.mit.genome.gp.ui.hclviewer;

import edu.mit.genome.gp.ui.hclviewer.colorconverter.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;
import java.awt.image.*;
import java.util.*;

import java.awt.event.*;
import java.awt.print.*;

import java.io.*;
import javax.swing.event.*;
import edu.mit.genome.dataobj.jg.*;
/**
 *  Issues: -when at bottom of scroll pane and zoom out, image is too high,
 *  currently using hack to fix this -Add should just draw matrix, gene names,
 *  and sample names -preview panel -if normalize by row, then should have
 *  separate header for each row
 *
 *@author     jgould
 *@created    August 13, 2003
 */

public class HCL extends PixPanel implements NodeSelectionListener {
	int pinkOGramWidth, pinkOGramHeight;

	int ny, nx;
	double minValue;
	double maxValue;

	JPanel pinkOGramAndGeneNamesPanel = new JPanel();
	GeneNames geneNamesDrawer;

	JPanel pink_geneTree_sampleTreePanel = new SrollablePanel();

	JScrollPane scrollPane;
	final int INITIAL_X_PIX_PER_UNIT = 4;
	final int INITIAL_Y_PIX_PER_UNIT = 4;
	Dataset matrix;

	JPanel headerPanel = new JPanel();
	// contains sample names and legend if it exists
	SampleNames sampleNamesDrawer;

	ColorConverterHeader header;

	Dendrogram sampleTree;
	Dendrogram geneTree;

	int leftSelectedSampleIndex = -1;
	int rightSelectedSampleIndex = -1;

	int topSelectedGeneIndex = -1;
	int bottomSelectedGeneIndex = -1;

	boolean showFlyOverText = true;
	boolean showRowLabels = true;
	boolean showColumnLabels = true;

	ColorConverter colorConverter;

	Action zoomInAction, zoomOutAction;

	JPanel topPanel;
	// column header for scroll pane

	AlphaComposite SRC_OVER_COMPOSITE = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);


	public HCL(Dataset matrix, double minValue, double maxValue, Dendrogram sampleTree, Dendrogram geneTree) {
		this.matrix = matrix;
		geneNamesDrawer = new GeneNames();
		sampleNamesDrawer = new SampleNames();
		this.minValue = minValue;
		this.maxValue = maxValue;

		nx = matrix.getColumnDimension();
		ny = matrix.getRowDimension();
		this.sampleTree = sampleTree;
		this.geneTree = geneTree;
		if(sampleTree != null) {
			sampleTree.setNodeSelectionListener(this);
		}
		if(geneTree != null) {
			geneTree.setNodeSelectionListener(this);
		}
		ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
		toolTipManager.registerComponent(this);

		setBackground(Color.white);
		setMinMax(0, nx - 1, 0, ny);

		setPixPerUnit(INITIAL_X_PIX_PER_UNIT, INITIAL_Y_PIX_PER_UNIT);

		pinkOGramAndGeneNamesPanel.setLayout(new GridBagLayout());
		pinkOGramAndGeneNamesPanel.setBackground(Color.white);
		pinkOGramAndGeneNamesPanel.add(this, new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		pinkOGramAndGeneNamesPanel.add(geneNamesDrawer, new GridBagConstraints(1, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		headerPanel.setLayout(new BorderLayout());
		headerPanel.setBackground(Color.white);
		headerPanel.add(sampleNamesDrawer, BorderLayout.SOUTH);

		KeyStroke up = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_UP, 0);
		// zoom in
		KeyStroke down = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DOWN, 0);
		// zoom out

		zoomInAction =
			new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					zoomIn();

				}
			};

		zoomOutAction =
			new AbstractAction() {
				public void actionPerformed(ActionEvent e) {
					if(getXPixPerUnit() > 2) {
						// 2 is min size
						zoomOut();
					}
				}
			};

		pink_geneTree_sampleTreePanel.setLayout(new GridBagLayout());
		pink_geneTree_sampleTreePanel.setBackground(Color.white);
		final int rows = sampleTree == null ? 1 : 2;
		final int cols = geneTree == null ? 3 : 4;
		if(sampleTree != null) {
			;
			//pink_geneTree_sampleTreePanel.add(sampleTree, new GridBagConstraints(cols - 3, rows - 2, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		if(geneTree != null) {
			pink_geneTree_sampleTreePanel.add(geneTree, new GridBagConstraints(cols - 4, rows - 1, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		}
		pink_geneTree_sampleTreePanel.add(pinkOGramAndGeneNamesPanel, new GridBagConstraints(cols - 3, rows - 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));

		topPanel = new JPanel(new BorderLayout());
		topPanel.setBackground(Color.white);
		topPanel.add(headerPanel, BorderLayout.NORTH);

		if(sampleTree != null) {
			topPanel.add(sampleTree, BorderLayout.SOUTH);
		}
		scrollPane = new JScrollPane(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPane.getViewport().setScrollMode(JViewport.SIMPLE_SCROLL_MODE);
		scrollPane.setViewportView(pink_geneTree_sampleTreePanel);
		scrollPane.revalidate();

		scrollPane.setColumnHeaderView(topPanel);
		/*
		 *  following does not work under OS X 1.4
		 *  scrollPane.getInputMap().put(up, "zoom in");
		 *  scrollPane.getActionMap().put("zoom in", zoomInAction);
		 *  scrollPane.getInputMap().put(down, "zoom out");
		 *  scrollPane.getActionMap().put("zoom out", zoomOutAction);
		 */
		scrollPane.registerKeyboardAction(zoomInAction, "zoom in", up, JComponent.WHEN_IN_FOCUSED_WINDOW);
		scrollPane.registerKeyboardAction(zoomOutAction, "zoom out", down, JComponent.WHEN_IN_FOCUSED_WINDOW);
		setColorConverter(new RowColorConverter(ColorResponse.LINEAR, getMatrix()));

	}


	/*
	 *  public int print(Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException {
	 *  if(pageIndex >= 1) {  // only one page available
	 *  return Printable.NO_SUCH_PAGE;
	 *  }
	 *  if(g == null) {
	 *  return Printable.NO_SUCH_PAGE;
	 *  }
	 *  Graphics2D g2     = (Graphics2D) g;
	 *  double     scalex = pageFormat.getImageableWidth() / (double) getWidth();
	 *  double     scaley = pageFormat.getImageableHeight() / (double) getHeight();
	 *  double     scale  = Math.min(scalex, scaley);
	 *  g2.translate((int) pageFormat.getImageableX(), (int) pageFormat.getImageableY());
	 *  g2.scale(scale, scale);
	 *  paintEverything(g2);
	 *  return Printable.PAGE_EXISTS;
	 *  }
	 */
	public ColorConverter getColorConverter() {
		return colorConverter;
	}


	public float getMinValue() {
		return (float) minValue;
		// FIXME
	}


	public float getMaxValue() {
		return (float) maxValue;
		// FIXME
	}


	/**
	 *  sets the color converter to use to convert values in this pinkogram to
	 *  colors.
	 *
	 *@param  converter  The new colorConverter value
	 */
	public void setColorConverter(ColorConverter converter) {
		colorConverter = converter;
		if(header != null) {
			headerPanel.remove(header);
		}
		if(colorConverter.hasHeader()) {
			header = colorConverter.getHeader();
			headerPanel.add(header, BorderLayout.NORTH);
		} else {
			header =
				new ColorConverterHeader() {
					public int updateSize(int i) {
						return 0;
					}
				};
			header.setVisible(false);
		}

		updateSize();
	}


	public Dataset getMatrix() {
		return matrix;
	}


	public void zoomIn() {
		super.zoomIn();
		updateSize();

	}


	public void zoomOut() {
		super.zoomOut();
		updateSize();
	}


	public boolean isShowingFlyOverText() {
		return showFlyOverText;
	}


	public void setShowFlyOverText(boolean b) {
		if(showFlyOverText != b && b) {
			ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
			toolTipManager.registerComponent(this);
		} else if(showFlyOverText != b && !b) {
			ToolTipManager toolTipManager = ToolTipManager.sharedInstance();
			toolTipManager.unregisterComponent(this);
		}
		showFlyOverText = b;

	}


	public boolean isShowingRowLabels() {
		return showRowLabels;
	}


	public void setShowRowLabels(boolean b) {
		if(geneNamesDrawer != null && showRowLabels != b) {
			geneNamesDrawer.setVisible(b);
			geneNamesDrawer.revalidate();
			geneNamesDrawer.repaint();
		}
		showRowLabels = b;
	}


	public boolean isShowingColumnLabels() {
		return showColumnLabels;
	}


	public void setShowColumnLabels(boolean b) {
		if(sampleNamesDrawer != null && showColumnLabels != b) {
			sampleNamesDrawer.setVisible(b);
			repaint();
		}

		showColumnLabels = b;
	}


	/**
	 *  invoked when user changes selection of genes with the mouse
	 *
	 *@param  clearGeneTreeSelection  Description of the Parameter
	 */
	void geneSelectionChanged(boolean clearGeneTreeSelection) {
		geneNamesDrawer.repaint();
		if(clearGeneTreeSelection && geneTree != null) {
			geneTree.clearSelection();
			geneTree.repaint();
		}
		repaint();
	}


	/**
	 *  invoked when user changes selection of samples with the mouse
	 *
	 *@param  clearSampleTreeSelection  Description of the Parameter
	 */
	void sampleSelectionChanged(boolean clearSampleTreeSelection) {
		sampleNamesDrawer.repaint();
		if(clearSampleTreeSelection && sampleTree != null) {
			sampleTree.clearSelection();
			sampleTree.repaint();
		}

		repaint();
	}


	public void nodeSelectionChanged(NodeSelectionEvent e) {
		int index1 = e.getIndex1();
		int index2 = e.getIndex2();
		Object source = e.getSource();
		if(source == sampleTree) {
			leftSelectedSampleIndex = index1;
			rightSelectedSampleIndex = index2 + 1;
			sampleSelectionChanged(false);
		} else {
			bottomSelectedGeneIndex = index1;
			topSelectedGeneIndex = index2 + 1;
			geneSelectionChanged(false);
		}

	}


	public void updateSize() {
		pinkOGramWidth = getIntXPixPerUnit() * nx;
		pinkOGramHeight = getIntYPixPerUnit() * ny;
		int totalWidth = pinkOGramWidth;
		if(sampleTree != null) {
			sampleTree.setLeafNodeSpacing(0, Math.abs(getIntYPixPerUnit()));
			sampleTree.setPreferredSize(new Dimension(pinkOGramWidth, 200));
		}
		if(geneTree != null) {
			int tWidth = 200;
			geneTree.setLeafNodeSpacing(0, getIntXPixPerUnit());
			geneTree.setPreferredSize(new Dimension(tWidth, pinkOGramHeight));
			geneTree.setMaximumSize(new Dimension(tWidth, pinkOGramHeight));
			geneTree.setMinimumSize(new Dimension(tWidth, pinkOGramHeight));
			geneTree.setSize(new Dimension(tWidth, pinkOGramHeight));
		}
		setPreferredSize(new Dimension(pinkOGramWidth, pinkOGramHeight));
		setMaximumSize(new Dimension(pinkOGramWidth, pinkOGramHeight));
		setMinimumSize(new Dimension(pinkOGramWidth, pinkOGramHeight));
		setSize(pinkOGramWidth, pinkOGramHeight);

		geneNamesDrawer.updateSize();
		totalWidth += geneNamesDrawer.getWidth();

		int leftGutter = 0;
		if(geneTree != null) {
			leftGutter = geneTree.getPreferredSize().width;
			totalWidth += leftGutter;
		}

		topPanel.setBorder(new javax.swing.border.EmptyBorder(0, leftGutter, 0, 0));
		int height = header.updateSize(getIntXPixPerUnit() * nx);
		header.setPreferredSize(new Dimension(totalWidth, height + 4));
		header.setSize(new Dimension(totalWidth, height + 4));

		if(sampleNamesDrawer != null) {
			sampleNamesDrawer.updateSize(totalWidth);
		}

		topPanel.revalidate();


		pink_geneTree_sampleTreePanel.repaint();
		sampleNamesDrawer.revalidate();
		sampleNamesDrawer.doLayout();

		(((SrollablePanel)
				pink_geneTree_sampleTreePanel)).setHeight(pinkOGramHeight);
		pink_geneTree_sampleTreePanel.revalidate();

		headerPanel.repaint();

		scrollPane.revalidate();

		scrollPane.getViewport().doLayout();
		scrollPane.doLayout();

		scrollPane.repaint();
	}


	public String getToolTipText(MouseEvent e) {

		int x = (int) pixToX(e.getX());
		int y = (int) pixToY(e.getY());

		StringBuffer sb = new StringBuffer();
		String rowId = matrix.getRowName(y);
		String columnId = matrix.getName(x);
		sb.append(rowId);
		if(y >= 0 && y < ny && x >= 0 && x < nx) {

			sb.append(rowId);
			sb.append(", ");
			sb.append(columnId);
			//	sb.append("<br>");
			//	sb.append(matrix.getRowDescription(y));
			//	sb.append("<br>");
			//	sb.append(matrix.getDescription(x));
			//	sb.append("<br>");

			return "<html> " + sb.toString() + "<br>Value: " + String.valueOf(matrix.get(y, x)) + "</html>";
		}
		return null;
	}


	private final String wordWrap(String text, final int limit) {
		StringBuffer stringbuf = new StringBuffer();
		if(text != null && text.length() > limit) {
			stringbuf.append(text);
			final String BR = "<br>";
			//start at the last insert and go backwards
			for(int i = (text.length() / limit) * limit; i > 0; i -= limit) {
				//reverse loop
				stringbuf.insert(i, BR);
			}
			return stringbuf.toString();
		}
		return text;
	}


	public JComponent getComponent() {
		return scrollPane;
	}


	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		draw(g);
	}


	public void draw(Graphics g) {
		int xCellSize = getIntXPixPerUnit();
		int yCellSize = Math.abs(getIntYPixPerUnit());
		//Graphics2D g2 = (Graphics2D)g;
		//g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		//g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
		// only paint cells that are showing
		Rectangle bounds = g.getClipBounds();
		int top = ny;
		int bottom = 0;
		int left = 0;
		int right = nx;
		if(bounds != null) {
			top = (int) Math.min(ny, ny - bounds.y / Math.abs(getIntYPixPerUnit()));
			// top must <= ny
			bottom = (int) Math.max(0, ny - 2 - (bounds.y + bounds.height) / Math.abs(getIntYPixPerUnit()) + 1);
			// bottom must be >= 0
			left = (int) Math.max(0, bounds.x / getIntXPixPerUnit());
			right = (int) Math.min(nx, (bounds.x + bounds.width) / getIntXPixPerUnit() + 1);
		}
		for(int iy = bottom; iy < top; iy++) {
			for(int ix = left; ix < right; ix++) {
				float val = (float) matrix.get(iy, ix);
				Color c = colorConverter.getColor(iy, ix);
				g.setColor(c);
				int xpix = xToPix(ix);
				int ypix = yToPix(iy + 1);
				g.fillRect(xpix, ypix, xCellSize, yCellSize);
			}

		}

		Rectangle sampleRect = null;
		if(leftSelectedSampleIndex >= 0) {
			// samples are selected

			int ystart = 0;
			int yend = 0;
			if(bottomSelectedGeneIndex >= 0) {
				//genes are selected
				ystart = yToPix(topSelectedGeneIndex);
				yend = yToPix(bottomSelectedGeneIndex);
			} else {
				ystart = yToPix(top);
				yend = yToPix(bottom);
			}

			int xstart = xToPix(leftSelectedSampleIndex);
			int xend = xToPix(rightSelectedSampleIndex);
			sampleRect = new Rectangle(xstart, ystart, xend - xstart, yend - ystart);
		}

		Rectangle geneRect = null;
		if(bottomSelectedGeneIndex >= 0) {
			// genes are selected
			int xstart = 0;
			int xend = 0;
			if(leftSelectedSampleIndex >= 0) {
				// samples are selected //leftSelectedSampleIndex > left &&
				xstart = xToPix(leftSelectedSampleIndex);
				xend = xToPix(rightSelectedSampleIndex);
			} else {
				xstart = xToPix(left);
				xend = xToPix(right);
			}

			int bottom_ypix = yToPix(bottomSelectedGeneIndex);

			int top_ypix = yToPix(topSelectedGeneIndex);

			geneRect = new Rectangle(xstart, top_ypix, xend - xstart, bottom_ypix - top_ypix);

		}

		Rectangle r = null;
		if(geneRect != null && sampleRect != null) {
			r = geneRect.union(sampleRect);
		} else if(geneRect != null) {
			r = geneRect;
		} else if(sampleRect != null) {
			r = sampleRect;
		}
		if(r != null) {
			g.setColor(Color.yellow);
			Graphics2D g2 = (Graphics2D) g;
			g2.setComposite(SRC_OVER_COMPOSITE);
			g2.fill(r);
		}
		//	if(showGrid)grid.draw(pinkOGramPanel, g);
//		long elapsed = System.currentTimeMillis() - start;
		//System.out.println("elapsed " + elapsed/1000.0);
	}

// size of header and gene tree


	public BufferedImage heatMapSnapshot(org.apache.batik.transcoder.image.ImageTranscoder transcoder) {
		int heatMapWidth = getIntXPixPerUnit() * nx;
		int heatMapHeight = getIntYPixPerUnit() * ny;

		sampleNamesDrawer.updateSize(heatMapWidth);
		geneNamesDrawer.updateSize();
		int totalWidth = heatMapWidth;
		totalWidth += 100;
		// for gene names
		int totalHeight = heatMapHeight;
		totalHeight += 100;
		// for sample names

		final BufferedImage image =transcoder.createImage(totalWidth, totalHeight); // new BufferedImage(totalWidth, totalHeight, BufferedImage.TYPE_INT_ARGB);
		
		Graphics2D g = image.createGraphics();

		g.setColor(Color.white);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());

		g.setColor(Color.black);
		g.setFont(new Font("monospaced", Font.PLAIN, getIntXPixPerUnit()));
		java.awt.geom.AffineTransform temp = g.getTransform();
		sampleNamesDrawer.draw(g, true);

		g.setTransform(temp);
		g.translate(0, 100);
		this.draw(g);
		g.translate(heatMapWidth, 0);
		g.setFont(new Font("monospaced", Font.PLAIN, getIntYPixPerUnit()));
		g.setColor(Color.black);
		geneNamesDrawer.draw(g);

		g.dispose();
		return image;
	}


	/**
	 *  creates a snapshot of this pinkogram
	 *
	 *@return    the snapshot
	 */
	public BufferedImage snapshot() {
		updateSize();
		int width = this.getPreferredSize().width + geneNamesDrawer.getPreferredSize().width;
		int geneTreeWidth = 0;
		if(geneTree != null) {
			geneTreeWidth = geneTree.getPreferredSize().width;
			width += geneTreeWidth;
		}
		int imageHeight = getIntYPixPerUnit() * ny;
		int headerHeight = headerPanel.getHeight();
		imageHeight += headerHeight;

		final BufferedImage image = new BufferedImage(width, imageHeight, BufferedImage.TYPE_INT_ARGB);
		//TYPE_3BYTE_BGR
		Graphics2D g = image.createGraphics();
		g.setColor(Color.white);
		g.fillRect(0, 0, image.getWidth(), image.getHeight());

		headerPanel.paint(g);
		// header panel contains sample tree and sample names
		g.translate(0, headerHeight);
		pinkOGramAndGeneNamesPanel.paint(g);
		g.dispose();
		return image;
	}



	class GeneNames extends JPanel {
		//implements Scrollable {
		int maxWidth;
		boolean visible = true;
		int lastYIndex;


		public GeneNames() {
			setBackground(Color.white);
			//	setAutoscrolls(true);
			addMouseListener(
				new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						// start of selection
						int y = e.getY();
						topSelectedGeneIndex = (int) pixToY(y);
						// if click on 0th cell, top = 1 and bottom = 0
						topSelectedGeneIndex++;
						bottomSelectedGeneIndex = topSelectedGeneIndex - 1;
						lastYIndex = y;
						geneSelectionChanged(true);
					}
				});

			addMouseMotionListener(
				new MouseMotionAdapter() {
					public void mouseDragged(MouseEvent e) {
						updateMouse(e);
						Rectangle r = null;
						if(e.getY() <= 0) {
							r = new Rectangle(e.getX(), e.getY() - getIntYPixPerUnit(), getIntXPixPerUnit(), getIntYPixPerUnit());
							// this rectangle becomes visible

						} else {
							r = new Rectangle(e.getX(), e.getY(), getIntXPixPerUnit(), getIntYPixPerUnit());
							// this rectangle becomes visible
							((JPanel) e.getSource()).scrollRectToVisible(r);
						}
						geneSelectionChanged(true);
					}
				});
		}


		private void updateMouse(MouseEvent e) {
			int y = e.getY();
			int index = (int) pixToY(y);
			// when moving up, update top index if click > top, else update bottom index

			if(index > lastYIndex) {
				if(lastYIndex < topSelectedGeneIndex && index >= topSelectedGeneIndex) {
					//crossover
					bottomSelectedGeneIndex = topSelectedGeneIndex - 1;
					topSelectedGeneIndex = index;
				} else if(index >= topSelectedGeneIndex) {
					//System.out.println("1");
					topSelectedGeneIndex = index;
				} else {
					//System.out.println("2");
					bottomSelectedGeneIndex = index;
				}
			}
			// when moving down, update bottom index if click < bottom, else update top index
			else {

				if(lastYIndex > bottomSelectedGeneIndex && index <= bottomSelectedGeneIndex) {
					//crossover
					topSelectedGeneIndex = bottomSelectedGeneIndex + 1;
					bottomSelectedGeneIndex = index;
				} else if(index <= bottomSelectedGeneIndex) {
					bottomSelectedGeneIndex = index;
					//	System.out.println("3");
				} else {
					topSelectedGeneIndex = index;
					//System.out.println("4");
				}
			}

			lastYIndex = index;
		}


		public void setVisible(boolean visible) {
			this.visible = visible;
			super.setVisible(visible);
		}


		public void updateSize() {
			setFont(new Font("monospaced", Font.PLAIN, Math.abs(getIntYPixPerUnit())));
			maxWidth = 0;
			Graphics g = getGraphics();
			if(g == null) {
				return;
			}
			FontMetrics fm = g.getFontMetrics();
			for(int i = 0; i < ny; i++) {
				String s = matrix.getRowName(i);
				int w = fm.stringWidth(s);
				maxWidth = Math.max(maxWidth, w);
			}
			setFont(new Font("monospaced", Font.PLAIN, Math.abs(getIntYPixPerUnit())));
			setPreferredSize(new Dimension(20 + maxWidth, getHeight()));
			setSize(new Dimension(20 + maxWidth, getHeight()));
			setMinimumSize(new Dimension(20 + maxWidth, getHeight()));
			setMaximumSize(new Dimension(20 + maxWidth, getHeight()));
			g.dispose();
		}


		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			if(!visible) {
				return;
			}
			draw(g);
		}


		public void draw(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;

			FontMetrics fm = g2.getFontMetrics();
			int h = fm.getAscent();
			Rectangle bounds = g.getClipBounds();
			int top = ny;
			int bottom = 0;
			int left = 0;
			int right = nx;
			if(bounds != null) {
				top = (int) Math.min(ny, ny - bounds.y / Math.abs(getIntYPixPerUnit()));
				// top must <= ny
				bottom = (int) Math.max(0, ny - 2 - (bounds.y + bounds.height) / Math.abs(getIntYPixPerUnit()) + 1);
				// bottom must be >= 0
				left = (int) Math.max(0, bounds.x / getIntXPixPerUnit());
				right = (int) Math.min(nx, (bounds.x + bounds.width) / getIntXPixPerUnit() + 1);
			}
			for(int i = bottom; i < top; i++) {
				String s = matrix.getRowName(i);
				g2.drawString(s, 0, yToPix(i + 1) + h);
				// fix height
			}

			if(bottomSelectedGeneIndex != -1) {
				g2.setComposite(SRC_OVER_COMPOSITE);
				g.setColor(Color.yellow);
				g.fillRect(0, yToPix(topSelectedGeneIndex), getWidth(), yToPix(bottomSelectedGeneIndex) - yToPix(topSelectedGeneIndex));
			}
		}
	}


	class SampleNames extends JPanel {
		int maxWidth;
		int lastXIndex;
		int gutter = 0;
		// FIXME delete this variable

//	    scrollPane.scrollRectToVisible(new Rectangle(new Point(0, canvas.getHeight())));

		public SampleNames() {
			setBackground(Color.white);
			setPreferredSize(new Dimension(100, 100));
			addMouseListener(
				new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						// start of selection
						int x = e.getX() - gutter;

						leftSelectedSampleIndex = (int) pixToX(x);
						// if click on 0th cell, left = 0 and right = 1

						rightSelectedSampleIndex = leftSelectedSampleIndex + 1;

						if(leftSelectedSampleIndex < 0 || rightSelectedSampleIndex > nx) {
							leftSelectedSampleIndex = -1;
						}

						//System.out.println("rightSelectedSampleIndex " + rightSelectedSampleIndex + " nx " + nx);
						lastXIndex = x;
						sampleSelectionChanged(true);
					}

					/*
					 *  public void mouseReleased(MouseEvent e) {// end selection
					 *  updateMouse(e);
					 *  HCL.this.repaint();
					 *  repaint();
					 *  }
					 */
				});

			addMouseMotionListener(
				new MouseMotionAdapter() {
					public void mouseDragged(MouseEvent e) {
						updateMouse(e);
						//	Rectangle r = new Rectangle(e.getX(), e.getY(), getIntXPixPerUnit() * 10, getIntYPixPerUnit() * 10);// this rectangle becomes visible
						//((JPanel) e.getSource()).scrollRectToVisible(r);
						sampleSelectionChanged(true);
					}
				});
		}


		private void updateMouse(MouseEvent e) {
			int x = e.getX() - gutter;
			int index = (int) pixToX(x);
			if(index < 0) {
				index = 0;
			} else if(index >= nx) {
				index = nx;
			}

			//	System.out.println("index " + index + " leftSelectedSampleIndex " + leftSelectedSampleIndex + " rightSelectedSampleIndex " + rightSelectedSampleIndex);
			// when moving right
			if(index > lastXIndex) {
				if(lastXIndex < rightSelectedSampleIndex && index >= rightSelectedSampleIndex) {
					//crossover
					leftSelectedSampleIndex = rightSelectedSampleIndex - 1;
					rightSelectedSampleIndex = index;
				} else if(index >= rightSelectedSampleIndex) {
					//	System.out.println("1");
					rightSelectedSampleIndex = index;
				} else {
					//	System.out.println("2");
					leftSelectedSampleIndex = index;
				}
			}
			// when moving down, update bottom index if click < bottom, else update top index
			else {

				if(lastXIndex > leftSelectedSampleIndex && index <= leftSelectedSampleIndex) {
					//crossover
					rightSelectedSampleIndex = leftSelectedSampleIndex + 1;
					leftSelectedSampleIndex = index;
				} else if(index <= leftSelectedSampleIndex) {
					leftSelectedSampleIndex = index;
					//	System.out.println("3");
				} else {
					rightSelectedSampleIndex = index;
					//	System.out.println("4");
				}
			}

			lastXIndex = index;
		}


		void updateSize(int width) {
			setFont(new Font("monospaced", Font.PLAIN, getIntXPixPerUnit()));
			Graphics g = getGraphics();
			if(g == null) {
				return;
			}
			FontMetrics fm = g.getFontMetrics();
			maxWidth = 0;
			for(int i = 0; i < nx; i++) {
				String s = matrix.getName(i);
				if(s != null) {
					int w = fm.stringWidth(s);
					maxWidth = Math.max(maxWidth, w);
				}
			}
			setPreferredSize(new Dimension(width, maxWidth + 10));
			setSize(new Dimension(width, maxWidth + 10));
			setMinimumSize(new Dimension(width, maxWidth + 10));
			setMaximumSize(new Dimension(width, maxWidth + 10));
			g.dispose();
		}


		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			draw(g, false);
		}


		public void draw(Graphics g, boolean forSnapShot) {
			Graphics2D g2 = (Graphics2D) g;
			FontMetrics fm = g2.getFontMetrics();
			g2.rotate(Math.toRadians(-90));

			int y = -getHeight() + 5;
			if(forSnapShot) {
				// FIXME incredibly bad hack
				y = -95;
			}

			int descent = fm.getDescent();

			float x = (float) (getIntXPixPerUnit() / 2) + gutter;

			for(int i = 0; i < nx; i++) {
				String s = matrix.getName(i);
				if(s != null) {
					g2.drawString(s, y, descent + x);
					x += (float) getIntXPixPerUnit();
				}
			}

			if(leftSelectedSampleIndex >= 0) {
				g.setColor(Color.yellow);
				g2.rotate(Math.toRadians(90));
				g2.setComposite(SRC_OVER_COMPOSITE);

				int xstart = xToPix(leftSelectedSampleIndex);
				//System.out.println("getIntXPixPerUnit " + getIntXPixPerUnit() + " gutter " + gutter + " xstart " + xstart);
				int xend = xToPix(rightSelectedSampleIndex);

				g.fillRect(xstart + gutter, 0, xend - xstart, getHeight());

			}
		}
	}


	private class SrollablePanel extends JPanel implements Scrollable {
		Dimension preferredScrollableViewportSize = new Dimension(450, 400);

		int height;


		public SrollablePanel() {
			setAutoscrolls(true);
		}


		public void setHeight(int i) {
			height = i;
		}


		public Dimension getPreferredSize() {
			Dimension d = super.getPreferredSize();
			d.height = height;
			return d;
		}


		public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
			//System.out.println("getScrollableUnitIncrement");
			if(orientation == SwingConstants.HORIZONTAL) {
				return 100;
			}
			return getIntYPixPerUnit();
		}


		public Dimension getPreferredScrollableViewportSize() {
			return preferredScrollableViewportSize;
		}


		public boolean getScrollableTracksViewportHeight() {
			//	System.out.println("getScrollableTracksViewportHeight");
			return false;
		}


		public boolean getScrollableTracksViewportWidth() {
			//	System.out.println("getScrollableTracksViewportWidth");
			return false;
		}


		public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation,
				int direction) {

			if(orientation == SwingConstants.VERTICAL) {
				int rh = getIntYPixPerUnit();
				return (rh > 0) ? Math.max(rh, (visibleRect.height / rh) * rh) : visibleRect.height;
			} else {
				return visibleRect.width;
			}
		}

	}
}

