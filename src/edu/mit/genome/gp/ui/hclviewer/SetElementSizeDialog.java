package edu.mit.genome.gp.ui.hclviewer;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class SetElementSizeDialog extends JDialog {
	private boolean result = false;

	private JLabel widthLabel, heightLabel;
	private JTextField widthTextField, heightTextField;

	private int width, height;


	public SetElementSizeDialog(JFrame parent, int elementWidth, int elementHeight) {
		super(parent, true);
		setTitle("Set Element Size");
		this.width = elementWidth;
		this.height = elementHeight;
		widthLabel = new JLabel("Element Width (Pixels): ");
		widthTextField = new JTextField(10);
		widthTextField.setText(String.valueOf(elementWidth));
		heightLabel = new JLabel("Element Height (Pixels): ");
		heightTextField = new JTextField(10);
		heightTextField.setText(String.valueOf(elementHeight));
		JButton okButton = new JButton("OK");

		okButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					try {
						width = Integer.parseInt(widthTextField.getText());
						height = Integer.parseInt(heightTextField.getText());
						result = true;
					} catch(NumberFormatException nfe) {
					}
					dispose();
				}
			});
		JButton cancelButton = new JButton("Cancel");

		cancelButton.addActionListener(
			new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});

		Container content = getContentPane();
		content.setLayout(new GridBagLayout());
		GBA gba = new GBA();
		gba.add(content, widthLabel, 0, 0, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(content, widthTextField, 1, 0, 1, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(content, heightLabel, 0, 1, 1, 1, 0, 0, GBA.NONE, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(content, heightTextField, 1, 1, 2, 1, 1, 0, GBA.H, GBA.C, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(content, cancelButton, 0, 2, 1, 1, 0, 0, GBA.NONE, GBA.W, new Insets(5, 5, 5, 5), 0, 0);
		gba.add(content, okButton, 1, 2, 1, 1, 0, 0, GBA.NONE, GBA.E, new Insets(5, 5, 5, 5), 0, 0);

		setResizable(false);
		widthTextField.grabFocus();
		getRootPane().setDefaultButton(okButton);
		pack();
	}

	public int getElementWidth() {
		return width;
	}

	public int getElementHeight() {
		return height;
	}

	/**
	 *  Pops open a modal dialog that prompts the user for element size.
	 *
	 * @return    true if the user provided valid input and selected OK, false
	 *      otherwise.
	 */
	public boolean showInputDialog() {
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation((screenSize.width - getSize().width) / 2, (screenSize.height - getSize().height) / 2);
		show();
		return result;
	}

}

