package ui.tab;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import physics.Settings;
import ui.SimulationAnimation;
import ui.util.FileIO;
import ui.util.yaml.YamlParser;

public class FileTab extends Box {

	private Component parent;
	private SimulationAnimation simulationAnimation;
	private JFileChooser fc;
	private JButton openButton;
	private JButton saveButton;
	private JButton applyButton;
	private JTextArea fileTextArea;

	public FileTab(Component parent, SimulationAnimation simulationAnimation) {
		super(BoxLayout.PAGE_AXIS);
		this.parent = parent;
		this.simulationAnimation = simulationAnimation;

		fc = new JFileChooser();
		File workingDirectory = new File(System.getProperty("user.dir"));
		File inputDirectory = new File(workingDirectory, "input");
		if (inputDirectory.exists()) {
			fc.setCurrentDirectory(inputDirectory);
		} else {
			fc.setCurrentDirectory(workingDirectory);
		}

		openButton = new JButton("Open...");
		openButton.addActionListener(new OpenButtonListener());
		saveButton = new JButton("Save...");
		saveButton.addActionListener(new SaveButtonListener());
		applyButton = new JButton("Apply");
		applyButton.addActionListener(new ApplyButtonListener());
		fileTextArea = new JTextArea();
		JScrollPane scrollpane = new JScrollPane(fileTextArea);

		Box buttonBox = Box.createHorizontalBox();
		buttonBox.add(openButton);
		buttonBox.add(saveButton);
		buttonBox.add(applyButton);

		this.add(buttonBox);
		this.add(scrollpane);
	}

	class OpenButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {

			int returnVal = fc.showOpenDialog(parent);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				try {
					String content = FileIO.readFile(file);
					fileTextArea.setText(content);
					applyTextAreaSettings();
				} catch (IOException e) {
					// TODO Error message
				}
			} else {
				// Open command cancelled by user
			}
		}
	}

	class SaveButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {

			int returnVal = fc.showSaveDialog(parent);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				if (file.exists()) {
					// Show confirmation dialog
					int response = JOptionPane.showConfirmDialog(
							parent,
							"Are you sure you want to override existing file?",
							"Confirm", JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (response == JOptionPane.YES_OPTION) {
						// Ok, proceed
					} else if (response == JOptionPane.NO_OPTION) {
						return;
					} else if (response == JOptionPane.CLOSED_OPTION) {
						return;
					}
				}
				String string = fileTextArea.getText();
				try {
					FileIO.writeFile(file, string);
				} catch (IOException e) {
					// TODO Error message
				}
			} else {
				// Save command cancelled by user
			}
		}
	}

	class ApplyButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent event) {
			applyTextAreaSettings();
		}
	}

	/**
	 * Apply the settings from the text area and restart the simulation.
	 */
	private void applyTextAreaSettings() {
		String string = fileTextArea.getText();
		Settings settings = new Settings();
		YamlParser parser = new YamlParser(settings);
		parser.parseString(string);
		simulationAnimation.resetAnimation(settings);
	}

}
