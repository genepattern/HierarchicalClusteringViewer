/*
    Copyright @ 1999-2003, The Institute for Genomic Research (TIGR).
    All rights reserved.
  */
package edu.mit.genome.gp.ui.hclviewer.colorconverter;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.event.*;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GradientPaint;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.GridBagConstraints;
import javax.swing.*;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JOptionPane;

/**
 *  Supplies option dialog for selection of expression color scheme
 *
 * @author     jgould
 * @created    August 14, 2003
 */
public class AbsoluteColorSchemeSelectionDialog extends javax.swing.JDialog {

	// Variables declaration - do not modify//GEN-BEGIN:variables
	private javax.swing.ButtonGroup chanelSelectionGroup;
	private javax.swing.JPanel channelSelectionPanel;
	private javax.swing.JRadioButton negativeColorButton;
	private javax.swing.JRadioButton positiveColorButton;
	private javax.swing.JPanel actionButtonPanel;
	private javax.swing.JButton okButton;
	private javax.swing.JButton cancelButton;
	private javax.swing.JColorChooser colorChooser;
	private javax.swing.JPanel gradientPreviewPanel;
	// End of variables declaration//GEN-END:variables
	private Color maxColor = Color.red;
	private Color minColor = Color.green;
	private Color neutralColor = Color.black;
	private PreviewPanel previewer;
	private int result = 0;
//	private javax.swing.JCheckBox neutralColorCheckBox;
	private JRadioButton neutralColorButton = new JRadioButton();
	/**
	 *  Creates new form ColorSchemeSelectionDialog
	 *
	 * @param  parent        parent Frame
	 * @param  modal         modal selection for dialog
	 * @param  minColor      Description of the Parameter
	 * @param  maxColor      Description of the Parameter
	 * @param  neutralColor  Description of the Parameter
	 */
	public AbsoluteColorSchemeSelectionDialog(java.awt.Frame parent, Color _minColor,  Color _maxColor,  Color _neutralColor) {
		super(parent, true);
		this.setTitle("Color Scheme Selection");
		this.neutralColor = _neutralColor;
		this.maxColor = _maxColor;
		this.minColor =_minColor;
		previewer = new PreviewPanel();
		initComponents();
		//this.negativeColorButton.setFocusPainted(false);
		//this.positiveColorButton.setFocusPainted(false);
	//	this.neutralColorButton
		/*neutralColorCheckBox = new javax.swing.JCheckBox("Use Black as Neutral Color", true);
		neutralColorCheckBox.setFocusPainted(false);
		
		neutralColorCheckBox.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					if(neutralColorCheckBox.isSelected()) {
						AbsoluteColorSchemeSelectionDialog.this.neutralColor = Color.black;
					} else {
						AbsoluteColorSchemeSelectionDialog.this.neutralColor = Color.white;
					}
					previewer.refreshPreview();
				}
			});

		this.channelSelectionPanel.add(neutralColorCheckBox, new GridBagConstraints(0, 1, 2, 0, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(0, 50, 0, 0), 0, 0));
		this.channelSelectionPanel.validate();
		*/
		
		this.colorChooser.setPreviewPanel(new JPanel());
		this.gradientPreviewPanel.add(this.previewer, BorderLayout.CENTER);
		this.colorChooser.getSelectionModel().addChangeListener(previewer);
		setSize(450, 465);

		this.okButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					result = JOptionPane.OK_OPTION;
					setVisible(false);
				}
			});

		this.cancelButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					result = JOptionPane.CANCEL_OPTION;
					setVisible(false);
				}
			});
	}

	public Color getNeutralColor() {
		return neutralColor;
	}

	public Color getMaxColor() {
		return maxColor;
	}

	public Color getMinColor() {
		return minColor;
	}

	/**
	 *  This method is called from within the constructor to initialize the form.
	 */
	private void initComponents() {//GEN-BEGIN:initComponents
		chanelSelectionGroup = new javax.swing.ButtonGroup();
		channelSelectionPanel = new javax.swing.JPanel();
		negativeColorButton = new javax.swing.JRadioButton();
		positiveColorButton = new javax.swing.JRadioButton();
		actionButtonPanel = new javax.swing.JPanel();
		okButton = new javax.swing.JButton();
		cancelButton = new javax.swing.JButton();
		colorChooser = new javax.swing.JColorChooser();
		this.colorChooser.setPreviewPanel(previewer);
		gradientPreviewPanel = new javax.swing.JPanel();

		getContentPane().setLayout(new java.awt.GridBagLayout());
		java.awt.GridBagConstraints gridBagConstraints1;

		setModal(true);
	
		addWindowListener(
			new java.awt.event.WindowAdapter() {
				public void windowClosing(java.awt.event.WindowEvent evt) {
					closeDialog(evt);
				}
			});

		channelSelectionPanel.setLayout(new java.awt.GridBagLayout());
		java.awt.GridBagConstraints gridBagConstraints2;

		negativeColorButton.setSelected(true);
		negativeColorButton.setText("Minimum Color");
		chanelSelectionGroup.add(negativeColorButton);
		gridBagConstraints2 = new java.awt.GridBagConstraints();
		channelSelectionPanel.add(negativeColorButton, gridBagConstraints2);

		positiveColorButton.setText("Maximum Color");
		chanelSelectionGroup.add(positiveColorButton);
		gridBagConstraints2 = new java.awt.GridBagConstraints();
		channelSelectionPanel.add(positiveColorButton, gridBagConstraints2);
		
		neutralColorButton.setText("Neutral Color");
		chanelSelectionGroup.add(neutralColorButton);
		gridBagConstraints2 = new java.awt.GridBagConstraints();
		channelSelectionPanel.add(neutralColorButton, gridBagConstraints2);
		
		gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
		getContentPane().add(channelSelectionPanel, gridBagConstraints1);

		actionButtonPanel.setLayout(new java.awt.GridBagLayout());
		java.awt.GridBagConstraints gridBagConstraints3;

		okButton.setText(" Apply Color Scheme");
		okButton.setFocusPainted(false);
		okButton.setSelected(true);
		gridBagConstraints3 = new java.awt.GridBagConstraints();
		gridBagConstraints3.gridx = 0;
		gridBagConstraints3.gridy = 0;
		gridBagConstraints3.insets = new java.awt.Insets(10, 0, 10, 10);
		actionButtonPanel.add(okButton, gridBagConstraints3);

		cancelButton.setText("Cancel");
		gridBagConstraints3 = new java.awt.GridBagConstraints();
		gridBagConstraints3.gridx = 1;
		gridBagConstraints3.gridy = 0;
		gridBagConstraints3.insets = new java.awt.Insets(10, 10, 10, 0);
		actionButtonPanel.add(cancelButton, gridBagConstraints3);

		gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 3;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		getContentPane().add(actionButtonPanel, gridBagConstraints1);

		gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		getContentPane().add(colorChooser, gridBagConstraints1);

		gradientPreviewPanel.setLayout(new java.awt.BorderLayout());

		gradientPreviewPanel.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.EtchedBorder(), "Gradient Preview"));
		gradientPreviewPanel.setPreferredSize(new java.awt.Dimension(200, 70));
		gradientPreviewPanel.setMinimumSize(new java.awt.Dimension(200, 70));
		gridBagConstraints1 = new java.awt.GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridy = 2;
		gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
		getContentPane().add(gradientPreviewPanel, gridBagConstraints1);

		pack();
	}//GEN-END:initComponents

	/**
	 *  Closes the dialog
	 *
	 * @param  evt  Description of the Parameter
	 */
	private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
		setVisible(false);
		dispose();
	}//GEN-LAST:event_closeDialog

	/**
	 *  Shows the dialog.
	 *
	 * @return    Description of the Return Value
	 */
	public int showModal() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width) / 2, (screenSize.height - getSize().height) / 2);
		show();
		return result;
	}


	/**
	 *  Panel which displays the current color scheme gradient
	 *
	 * @author     jgould
	 * @created    August 14, 2003
	 */
	public class PreviewPanel extends JPanel implements ChangeListener {

		BufferedImage maxGradient;
		BufferedImage minGradient;

		public PreviewPanel() {
			refreshPreview();
			super.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED), "Gradient Preview"));
			setSize(200, 70);
			setPreferredSize(new Dimension(200, 70));
			setVisible(true);
		}

		/**
		 *  Handles color change events
		 *
		 * @param  changeEvent  Description of the Parameter
		 */
		public void stateChanged(javax.swing.event.ChangeEvent changeEvent) {
			Color newColor = colorChooser.getColor();
			if(newColor == null) {
				return;
			}

			if(positiveColorButton.isSelected()) {
				maxColor = newColor;
				maxGradient = createGradientImage(neutralColor, maxColor);
			} else if(negativeColorButton.isSelected()) {
				minColor = newColor;
				minGradient = createGradientImage(minColor, neutralColor);
			} else {
				neutralColor = newColor;
				maxGradient = createGradientImage(neutralColor, maxColor);
				minGradient = createGradientImage(minColor, neutralColor);
			}
			repaint();
		}

		/**  Refreshes gradients with current color */
		public void refreshPreview() {
			maxGradient = createGradientImage(neutralColor, maxColor);
			minGradient = createGradientImage(minColor, neutralColor);
			repaint();
		}

		/**
		 *  Paints dialog
		 *
		 * @param  g  Description of the Parameter
		 */
		public void paint(Graphics g) {
			super.paintComponent(g);
			g.setColor(neutralColor); // hack for 1.3 on os x
			g.fillRect(0, 0, getWidth(), getHeight());
			g.drawImage(minGradient, 0, 0, this.getWidth() / 2, this.getHeight(), null);
			g.drawImage(maxGradient, this.getWidth() / 2, 0, this.getWidth() / 2, this.getHeight(), null);
		}

		/**
		 *  Creates a gradient image given specified <CODE>Color</CODE>(s)
		 *
		 * @param  color1  <CODE>Color</CODE> to display at left side of gradient
		 * @param  color2  <CODE>Color</CODE> to display at right side of gradient
		 * @return         returns a gradient image
		 */
		private BufferedImage createGradientImage(Color color1, Color color2) {
			BufferedImage image = (BufferedImage) java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().createCompatibleImage(256, 1);
			Graphics2D graphics = image.createGraphics();
			GradientPaint gp = new GradientPaint(0, 0, color1, 255, 0, color2);
			graphics.setPaint(gp);
			graphics.drawRect(0, 0, 255, 1);
			return image;
		}

		/**
		 *  Returns the current positive gradient image
		 *
		 * @return    Returned positive gradient
		 */
		public BufferedImage getPositiveGradient() {
			return maxGradient;
		}

		/**
		 *  Returns the current positive gradient image
		 *
		 * @return    negative gradient image
		 */
		public BufferedImage getNegativeGradient() {
			return minGradient;
		}

	}

}

