/*
 Customization  of  simulator  PIXI  for  3D
 */
package ui;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;

import javax.swing.event.*;

import physics.Debug;
import physics.Simulation;
import physics.force.*;
import physics.movement.boundary.ParticleBoundaryType;
import physics.solver.*;
import physics.solver.relativistic.*;
import ui.panel.AnimationPanel;
import ui.panel.Particle2DPanel;
import ui.panel.Particle3DPanel;
import ui.panel.PhaseSpacePanel;
import ui.panel.ElectricFieldPanel;
import ui.tab.FileTab;

/**
 * Displays the animation of particles.
 */
public class MainControlApplet extends JApplet {

	private JButton startButton;
	private JButton stopButton;
	private JButton resetButton;

	private JSlider speedSlider;
	private JSlider stepSlider;

	private JSlider  dragSlider;

	private JSlider efieldXSlider;
	private JSlider efieldYSlider;
	private JSlider bfieldZSlider;
	private JSlider gfieldXSlider;
	private JSlider gfieldYSlider;

	private JCheckBox framerateCheck;
	private JCheckBox currentgridCheck;
	private JCheckBox drawFieldsCheck;
	private JCheckBox calculateFieldsCheck;
	private JCheckBox relativisticCheck;

	private JTextField xboxentry;
	private JTextField yboxentry;
	private JTextField zboxentry;

	private JComboBox initComboBox;
	private JComboBox algorithmComboBox;
	private JCheckBox traceCheck;
	private JComboBox collisionComboBox;
	private JComboBox collisionAlgorithm;

	private JRadioButton hardBoundaries;
	private JRadioButton periodicBoundaries;

	private JTabbedPane tabs;
	private JSplitPane splitPane;

	private SimulationAnimation simulationAnimation;

	private Particle2DPanel particlePanel;
	private Particle3DPanel particle3DPanel;
	private PhaseSpacePanel phaseSpacePanel;
	private ElectricFieldPanel electricFieldPanel;

	private static final double speedSliderScaling = 0.07;
	private static final double stepSliderScaling = 0.01;
	private static final double dragSliderScaling = 0.01;
	private static final double exSliderScaling = 0.5;
	private static final double eySliderScaling = 0.5;
	private static final double bzSliderScaling = 0.05;
	private static final double gxSliderScaling = 0.01;
	private static final double gySliderScaling = 0.01;

	private ConstantForce force = null;

	String[] initStrings = {
			"10 Particulas Azar",
			"100 random particles",
			"1000 random particles",
			"10000 random particles",
			"Single particle in gravity",
			"Single particle in el. Field",
			"3 part. in magnetic field",
            "Pair of particles",
            "Two stream instability",
            "Weibel instability",
            "One particle test",
            "Wave propagation test",
            "Two particles in 3D",
            "Two stream instability in 3D",
            "Weibel instability in 3D"};

	String[] solverString = {
			"Euler Richardson",
			"LeapFrog",
			"LeapFrog Damped",
			"LeapFrog Half Step",
			"Boris",
			"Boris Damped",
			"Semi Implicit Euler",
			"Euler"};

	String[] collisionsString = {
			"No collisions",
			"All particles",
			"Sweep & Prune"
	};

	String[] collisionalgorithmString = {
			"Simple collision",
			"With vectors",
			"With matrices"
	};



	private void linkConstantForce() {
		Simulation s = simulationAnimation.getSimulation();
		force = getFirstConstantForce(s.f);
		if(force == null) {
			force = new ConstantForce();
			s.f.add(force);
		}
		assert force != null : "no force found";
	}

	/**
	 * Returns the first constant force encountered. Scans recursively through
	 * all CombindeForces.
	 * @param force
	 * @return
	 */
	private ConstantForce getFirstConstantForce(Force force) {
		ConstantForce firstconstantforce = null;
		if (force instanceof ConstantForce) {
			firstconstantforce = (ConstantForce) force;
		} else if (force instanceof CombinedForce) {
			for (Force f : ((CombinedForce) force).forces) {
				firstconstantforce = getFirstConstantForce(f);
				if (firstconstantforce != null) {
					break;
				}
			}
		}
		return firstconstantforce;
	}

	/**
	 * Listener for slider.
	 */
	class SliderListener implements ChangeListener {
		public void stateChanged(ChangeEvent eve) {
			Timer timer = simulationAnimation.getTimer();
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				int delay = (int) (1000 * Math.exp(-source.getValue() * speedSliderScaling));
				timer.setDelay(delay);
			}
		}
	}

	class ComboBoxListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			JComboBox cb = (JComboBox) e.getSource();
			int id  = cb.getSelectedIndex();
			simulationAnimation.resetAnimation(id);
			linkConstantForce();
			setSlidersValue();
		}
	}

	class AlgorithmListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			JComboBox cbox = (JComboBox) eve.getSource();
			int id = cbox.getSelectedIndex();
			simulationAnimation.algorithmChange(id);
			if ((id == 1) || (id == 4) || (id == 6)) {
				relativisticCheck.setEnabled(true);
			}
			else {
				relativisticCheck.setEnabled(false);
			}
		}
	}
	class Collisions implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			JComboBox cbox = (JComboBox) eve.getSource();
			int i = cbox.getSelectedIndex();
			simulationAnimation.collisionChange(i);
			if(i == 0) {
				collisionAlgorithm.setEnabled(false);
				collisionAlgorithm.addItem("Enable collisions first");
				collisionAlgorithm.setSelectedItem("Enable collisions first");
			} else {
				collisionAlgorithm.setEnabled(true);
				//setSelectedIndex() automatically calls collisionAlgorithm.actionPerformed()!
				collisionAlgorithm.setSelectedIndex(0);
				collisionAlgorithm.removeItem("Enable collisions first");
			}
		}
	}

	class CollisionAlgorithm implements ActionListener {

		public void actionPerformed(ActionEvent eve) {
			JComboBox cbox = (JComboBox) eve.getSource();
			int i = cbox.getSelectedIndex();
			int j = collisionComboBox.getSelectedIndex();
			if (j == 0) {
				collisionAlgorithm.setSelectedItem("Enable collisions first");
			} else {
				simulationAnimation.algorithmCollisionChange(i);
			}
		}
	}


	/**
	 * Listener for start button.
	 */
	class StartListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			simulationAnimation.startAnimation();
		}
	}

	/**
	 * Listener for stop button.
	 */
	class StopListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			simulationAnimation.stopAnimation();
		}
	}

	/**
	 * Listener for reset button.
	 */
	class ResetListener implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			simulationAnimation.resetAnimation(initComboBox.getSelectedIndex());
			linkConstantForce();
			setSlidersValue();
		}
	}

	class CheckListener implements ItemListener {
		public void itemStateChanged(ItemEvent eve){
				particlePanel.checkTrace();
		}
	}

	class SelectBoundaries implements ActionListener {
		public void actionPerformed(ActionEvent eve) {
			AbstractButton abut = (AbstractButton) eve.getSource();
			if(abut.equals(hardBoundaries)) {
				simulationAnimation.boundariesChange(0);
			}
			else if(abut.equals(periodicBoundaries)) {
				simulationAnimation.boundariesChange(1);
			}
		}
	}

	class RelativisticEffects implements ItemListener {
		public void itemStateChanged(ItemEvent eve){
			int i = (int)algorithmComboBox.getSelectedIndex();
			simulationAnimation.relativisticEffects(i);
			linkConstantForce();
		}
	}

	class DrawCurrentGridListener implements ItemListener {
		public void itemStateChanged(ItemEvent eve){
			if (particlePanel != null) {
				particlePanel.drawCurrentGrid();
			}
			if (particle3DPanel != null) {
				particle3DPanel.drawCurrentGrid();
			}
		}
	}

	class DrawFieldsListener implements ItemListener {
		public void itemStateChanged(ItemEvent eve){
			if (particlePanel != null) {
				particlePanel.drawFields();
			}
			if (particle3DPanel != null) {
				particle3DPanel.drawFields();
			}
		}
	}

	class CalculateFieldsListener implements ItemListener {
		public void itemStateChanged(ItemEvent eve){
				simulationAnimation.calculateFields();
				if(eve.getStateChange() == ItemEvent.SELECTED) {
					currentgridCheck.setEnabled(true);
					drawFieldsCheck.setEnabled(true);
				}
				if(eve.getStateChange() == ItemEvent.DESELECTED) {
					currentgridCheck.setEnabled(false);
					drawFieldsCheck.setEnabled(false);
				}
		}
	}

	class FrameListener implements ItemListener {
		public void itemStateChanged(ItemEvent eve) {
			if(eve.getStateChange() == ItemEvent.SELECTED) {
				particlePanel.showinfo = true;
			} else if(eve.getStateChange() == ItemEvent.DESELECTED) {
				particlePanel.showinfo = false;
			}
		}
	}



	class DragListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * dragSliderScaling;
				force.drag = value;
			}
		}
	}

	class EFieldXListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * exSliderScaling;
				force.ex = value;
			}
		}
	}

	class EFieldYListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * eySliderScaling;
				force.ey = value;
			}
		}
	}

	class BFieldZListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * bzSliderScaling;
				force.bz = value;
			}
		}
	}

	class GFieldXListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * gxSliderScaling;
				force.gx = value;
			}
		}
	}

	class GFieldYListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * gySliderScaling;
				force.gy = value;
			}
		}
	}

	class StepListener implements ChangeListener{
		public void stateChanged(ChangeEvent eve) {
			Simulation s = simulationAnimation.getSimulation();
			JSlider source = (JSlider) eve.getSource();
			if(source.getValueIsAdjusting())
			{
				double value = source.getValue() * stepSliderScaling;
				s.tstep = value;
			}
		}
	}

	class BoxDimension implements ActionListener{
		public void actionPerformed(ActionEvent eve) {
			Simulation s = simulationAnimation.getSimulation();
			int xbox = Integer.parseInt(xboxentry.getText());
			int ybox = Integer.parseInt(yboxentry.getText());
			int zbox = Integer.parseInt(zboxentry.getText());
			double width = s.getWidth();
			double height = s.getHeight();
			double depth = s.getDepth();
			s.grid.changeSize(xbox, ybox, zbox, width, height, depth);
		}
	}

	/**
	 * Constructor.
	 */
	public MainControlApplet() {
		Debug.checkAssertsEnabled();

		simulationAnimation = new SimulationAnimation();
		particlePanel = new Particle2DPanel(simulationAnimation);
		Simulation s = simulationAnimation.getSimulation();
		linkConstantForce();

		startButton = new JButton("start");
		stopButton = new JButton("stop");
		resetButton = new JButton("reset");

		/**one can also write a constructor for a JSlider as:
		 * JSlider slider = new JSlider(int min, int max, int value);
		 * where min is the minimal value (the same as setMinimum(int min),
		 * max is the maximal value (the same as setMinimum(int max),
		 * and value is the current value (the same as setValue(int value),
		 * and the code would be shorter,
		 * but they are written like this, so it is clearer and not so confusing
		 */

		speedSlider = new JSlider();
		speedSlider.addChangeListener(new SliderListener());
		speedSlider.setMinimum(0);
		speedSlider.setMaximum(100);
		speedSlider.setValue(30);
		speedSlider.setMajorTickSpacing(5);
		speedSlider.setMinorTickSpacing(1);
		speedSlider.setPaintTicks(true);
		JLabel speedLabel = new JLabel("Frame rate");
		Box speed = Box.createVerticalBox();
		speed.add(speedLabel);
		speed.add(speedSlider);

		stepSlider = new JSlider();
		stepSlider.addChangeListener(new StepListener());
		stepSlider.setMinimum(1);
		stepSlider.setMaximum(100);
		stepSlider.setValue((int)(s.tstep / stepSliderScaling));
		stepSlider.setMajorTickSpacing(10);
		stepSlider.setMinorTickSpacing(2);
		stepSlider.setPaintTicks(true);
		JLabel stepLabel = new JLabel("Size of time step");
		Box step = Box.createVerticalBox();
		step.add(stepLabel);
		step.add(stepSlider);

		dragSlider = new JSlider();
		dragSlider.addChangeListener(new DragListener());
		dragSlider.setMinimum(0);
		dragSlider.setMaximum(100);
		dragSlider.setValue((int) force.drag);
		dragSlider.setMajorTickSpacing(50);
		dragSlider.setMinorTickSpacing(10);
		dragSlider.setPaintTicks(true);
		dragSlider.setPaintLabels(true);
		JLabel dragLabel = new JLabel("Drag coefficient");

		efieldXSlider = new JSlider();
		efieldXSlider.addChangeListener(new EFieldXListener());
		efieldXSlider.setMinimum(-100);
		efieldXSlider.setMaximum(100);
		efieldXSlider.setValue((int) force.ex);
		efieldXSlider.setMajorTickSpacing(50);
		efieldXSlider.setMinorTickSpacing(10);
		efieldXSlider.setPaintTicks(true);
		efieldXSlider.setPaintLabels(true);

		efieldYSlider = new JSlider();
		efieldYSlider.addChangeListener(new EFieldYListener());
		efieldYSlider.setMinimum(-100);
		efieldYSlider.setMaximum(100);
		efieldYSlider.setValue((int) force.ey);
		efieldYSlider.setMajorTickSpacing(50);
		efieldYSlider.setMinorTickSpacing(10);
		efieldYSlider.setPaintTicks(true);
		efieldYSlider.setPaintLabels(true);

		bfieldZSlider = new JSlider();
		bfieldZSlider.addChangeListener(new BFieldZListener());
		bfieldZSlider.setMinimum(-100);
		bfieldZSlider.setMaximum(100);
		bfieldZSlider.setValue((int) force.bz);
		bfieldZSlider.setMajorTickSpacing(50);
		bfieldZSlider.setMinorTickSpacing(10);
		bfieldZSlider.setPaintTicks(true);
		bfieldZSlider.setPaintLabels(true);

		gfieldXSlider = new JSlider();
		gfieldXSlider.addChangeListener(new GFieldXListener());
		gfieldXSlider.setMinimum(-100);
		gfieldXSlider.setMaximum(100);
		gfieldXSlider.setValue((int) force.gx);
		gfieldXSlider.setMajorTickSpacing(50);
		gfieldXSlider.setMinorTickSpacing(10);
		gfieldXSlider.setPaintTicks(true);
		gfieldXSlider.setPaintLabels(true);

		gfieldYSlider = new JSlider();
		gfieldYSlider.addChangeListener(new GFieldYListener());
		gfieldYSlider.setMinimum(-100);
		gfieldYSlider.setMaximum(100);
		gfieldYSlider.setValue((int) force.gy);
		gfieldYSlider.setMajorTickSpacing(50);
		gfieldYSlider.setMinorTickSpacing(10);
		gfieldYSlider.setPaintTicks(true);
		gfieldYSlider.setPaintLabels(true);

		initComboBox = new JComboBox(initStrings);
		initComboBox.setSelectedIndex(0);
		initComboBox.addActionListener(new ComboBoxListener());
		JLabel initComboBoxLabel = new JLabel("Initial conditions");
		Box initBox = Box.createHorizontalBox();
		initBox.add(initComboBoxLabel);
		initBox.add(Box.createHorizontalGlue());
		initBox.add(initComboBox);

		algorithmComboBox = new JComboBox(solverString);
		algorithmComboBox.setSelectedIndex(0);
		algorithmComboBox.addActionListener(new AlgorithmListener());
		algorithmComboBox.setPreferredSize(new Dimension(algorithmComboBox.getPreferredSize().width, 5));
		JLabel algorithmLabel = new JLabel("Algorithm");
		Box algorithmBox = Box.createVerticalBox();
		algorithmBox.add(algorithmLabel);
		algorithmBox.add(algorithmComboBox);

		collisionComboBox = new JComboBox(collisionsString);
		collisionComboBox.setSelectedIndex(0);
		collisionComboBox.addActionListener(new Collisions());
		//collisionComboBox.setPreferredSize(new Dimension(collisionComboBox.getPreferredSize().width, 5));
		JLabel collisionsLabel = new JLabel("Collisions");

		collisionAlgorithm = new JComboBox(collisionalgorithmString);
		collisionAlgorithm.setSelectedIndex(0);
		collisionAlgorithm.addActionListener(new CollisionAlgorithm());
		JLabel colAlgorithmLabel = new JLabel("Algorithm for the collisions");

		Box collisionBox = Box.createVerticalBox();
		collisionBox.add(collisionsLabel);
		collisionBox.add(collisionComboBox);
		collisionBox.add(Box.createVerticalGlue());
		collisionBox.add(colAlgorithmLabel);
		collisionBox.add(collisionAlgorithm);
		collisionBox.add(Box.createVerticalStrut(170));

		startButton.addActionListener(new StartListener());
		stopButton.addActionListener(new StopListener());
		resetButton.addActionListener(new ResetListener());

		relativisticCheck = new JCheckBox("Relativistic Version");
		relativisticCheck.addItemListener(new RelativisticEffects());
		relativisticCheck.setEnabled(false);

		traceCheck = new JCheckBox("Trace");
		traceCheck.addItemListener(new CheckListener());

		currentgridCheck = new JCheckBox("Current");
		currentgridCheck.addItemListener(new DrawCurrentGridListener());
		currentgridCheck.setEnabled(false);

		drawFieldsCheck = new JCheckBox("Draw fields");
		drawFieldsCheck.addItemListener(new DrawFieldsListener());
		drawFieldsCheck.setEnabled(false);

		calculateFieldsCheck = new JCheckBox("Calculate Fields");
		calculateFieldsCheck.addItemListener(new CalculateFieldsListener());

		framerateCheck = new JCheckBox("Info");
		framerateCheck.addItemListener(new FrameListener());

		xboxentry = new JTextField(2);
		xboxentry.setText("10");
		xboxentry.addActionListener(new BoxDimension());

		yboxentry = new JTextField(2);
		yboxentry.setText("10");
		yboxentry.addActionListener(new BoxDimension());

		zboxentry = new JTextField(2);
		zboxentry.setText("10");
		zboxentry.addActionListener(new BoxDimension());

		hardBoundaries = new JRadioButton("Hardwall");
		periodicBoundaries = new JRadioButton("Periodic");

		ButtonGroup bgroup = new ButtonGroup();

		bgroup.add(hardBoundaries);
		bgroup.add(periodicBoundaries);

		hardBoundaries.addActionListener(new SelectBoundaries());
		periodicBoundaries.addActionListener(new SelectBoundaries());

		JPanel boundaries = new JPanel();
		boundaries.add(hardBoundaries);
		boundaries.add(periodicBoundaries);
		JLabel boundariesLabel = new JLabel("Boundaries");

		JLabel xboxentryLabel = new JLabel("Cell width");
		JLabel yboxentryLabel = new JLabel("Cell height");
		JLabel zboxentryLabel = new JLabel("Cell depth");

		JPanel controlPanelUp = new JPanel();
		controlPanelUp.setLayout(new FlowLayout());
		controlPanelUp.add(startButton);
		controlPanelUp.add(stopButton);
		controlPanelUp.add(resetButton);
		controlPanelUp.add(Box.createHorizontalStrut(25));
		controlPanelUp.add(initBox);
		controlPanelUp.add(Box.createHorizontalStrut(25));
		Box settingControls = Box.createVerticalBox();
		JPanel controlPanelDown = new JPanel();
		controlPanelDown.setLayout(new FlowLayout());
		settingControls.add(algorithmBox);
		settingControls.add(Box.createVerticalGlue());
		settingControls.add(relativisticCheck);
		settingControls.add(Box.createVerticalGlue());
		settingControls.add(speed);
		settingControls.add(Box.createVerticalGlue());
		settingControls.add(step);
		settingControls.add(Box.createVerticalGlue());
		settingControls.add(boundariesLabel);
		settingControls.add(boundaries);
		settingControls.add(traceCheck);
		settingControls.add(framerateCheck);

		Box panelBox = Box.createVerticalBox();
		panelBox.add(controlPanelUp);
		panelBox.add(controlPanelDown);

		JLabel eFieldXLabel = new JLabel("Electric Field in x - direction");
		JLabel eFieldYLabel = new JLabel("Electric Field in y - direction");
		JLabel bFieldZLabel = new JLabel("Magnetic Field in z - direction");
		JLabel gFieldXLabel = new JLabel("Gravitation in x - direction Field");
		JLabel gFieldYLabel = new JLabel("Gravitation in y - direction Field");

		// Change background color of tab from blue to system gray
		UIManager.put("TabbedPane.contentAreaColor", new Color(238, 238, 238));

		tabs = new JTabbedPane();

		Box fieldsBox = Box.createVerticalBox();
		fieldsBox.add(eFieldXLabel);
		fieldsBox.add(efieldXSlider);
		fieldsBox.add(Box.createVerticalStrut(5));
		fieldsBox.add(eFieldYLabel);
		fieldsBox.add(efieldYSlider);
		fieldsBox.add(Box.createVerticalGlue());
		fieldsBox.add(bFieldZLabel);
		fieldsBox.add(bfieldZSlider);
		fieldsBox.add(Box.createVerticalGlue());
		fieldsBox.add(gFieldXLabel);
		fieldsBox.add(gfieldXSlider);
		fieldsBox.add(Box.createVerticalStrut(5));
		fieldsBox.add(gFieldYLabel);
		fieldsBox.add(gfieldYSlider);
		fieldsBox.add(Box.createVerticalGlue());
		fieldsBox.add(dragLabel);
		fieldsBox.add(dragSlider);
		fieldsBox.add(Box.createVerticalGlue());

		Box xbox = Box.createHorizontalBox();
		Box ybox = Box.createHorizontalBox();
		xbox.add(Box.createHorizontalStrut(50));
		xbox.add(xboxentryLabel);
		xbox.add(Box.createHorizontalStrut(100));
		xbox.add(xboxentry);
		ybox.add(Box.createHorizontalStrut(50));
		ybox.add(yboxentryLabel);
		ybox.add(Box.createHorizontalStrut(96));
		ybox.add(yboxentry);
		/*
		ybox.add(Box.createHorizontalStrut(50));
		ybox.add(yboxentryLabel);
		ybox.add(Box.createHorizontalStrut(96));
		ybox.add(yboxentry);
		 */

		Box cellSettings = Box.createVerticalBox();
		cellSettings.add(Box.createVerticalStrut(20));
		cellSettings.add(calculateFieldsCheck);
		cellSettings.add(Box.createVerticalGlue());
		cellSettings.add(currentgridCheck);
		cellSettings.add(Box.createVerticalGlue());
		cellSettings.add(drawFieldsCheck);
		cellSettings.add(Box.createVerticalStrut(20));
		cellSettings.add(xbox);
		cellSettings.add(Box.createVerticalStrut(10));
		cellSettings.add(ybox);
		cellSettings.add(Box.createVerticalStrut(200));

		FileTab fileTab = new FileTab(MainControlApplet.this, simulationAnimation);

		fieldsBox.setPreferredSize(new Dimension(300, 100));
		settingControls.setPreferredSize(new Dimension (300, 100));
		collisionBox.setPreferredSize(new Dimension (300, 100));

		tabs.addTab("Fields", fieldsBox);
		tabs.addTab("Settings", settingControls);
		tabs.addTab("Coll.", collisionBox);
		tabs.addTab("Cell", cellSettings);
		tabs.addTab("File", fileTab);

		this.setLayout(new BorderLayout());
		this.add(panelBox, BorderLayout.SOUTH);
		this.add(particlePanel, BorderLayout.CENTER);
		this.add(tabs, BorderLayout.EAST);

		popupClickListener = new PopupClickListener();
		particlePanel.addMouseListener(popupClickListener);
	}

	PopupClickListener popupClickListener;

	JMenuItem itemSplitHorizontally;
	JMenuItem itemSplitVertically;
	JMenuItem itemClosePanel;
	JMenuItem itemParticle2DPanel;
	JMenuItem itemParticle3DPanel;
	JMenuItem itemPhaseSpacePanel;
	JMenuItem itemElectricFieldPanel;

	class PopupMenu extends JPopupMenu {

		public PopupMenu() {
			itemSplitHorizontally = new JMenuItem("Split horizontally");
			itemSplitHorizontally.addActionListener(new MenuSelected());
			add(itemSplitHorizontally);

			itemSplitVertically = new JMenuItem("Split vertically");
			itemSplitVertically.addActionListener(new MenuSelected());
			add(itemSplitVertically);

			if (clickComponent != null && clickComponent.getParent() instanceof JSplitPane) {
				itemClosePanel = new JMenuItem("Close panel");
				itemClosePanel.addActionListener(new MenuSelected());
				add(itemClosePanel);
			}

			add(new JSeparator());

			itemParticle2DPanel = new JMenuItem("Particles");
			itemParticle2DPanel.addActionListener(new MenuSelected());
			add(itemParticle2DPanel);

			itemParticle3DPanel = new JMenuItem("Particles 3D");
			itemParticle3DPanel.addActionListener(new MenuSelected());
			add(itemParticle3DPanel);

			itemPhaseSpacePanel = new JMenuItem("Phase space");
			itemPhaseSpacePanel.addActionListener(new MenuSelected());
			add(itemPhaseSpacePanel);

			itemElectricFieldPanel = new JMenuItem("Electric field");
			itemElectricFieldPanel.addActionListener(new MenuSelected());
			add(itemElectricFieldPanel);
		}
	}

	Component clickComponent;

	class PopupClickListener extends MouseAdapter {
		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger())
				doPop(e);
		}

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger())
				doPop(e);
		}

		private void doPop(MouseEvent e) {
			clickComponent = e.getComponent();
			PopupMenu menu = new PopupMenu();
			menu.show(e.getComponent(), e.getX(), e.getY());
		}
	}

	class MenuSelected implements ActionListener {


		public void actionPerformed(ActionEvent event) {
			// TODO: This method creates new instances of the panels
			// (which is nice so there can be two identical panels next
			// to each other), but it does not delete previous panels.
			// They should be unregistered in simulationAnimation if not
			// in use anymore.

			Component component = null;

			if (event.getSource() == itemSplitHorizontally) {
				splitPanel(JSplitPane.HORIZONTAL_SPLIT);
			} else if (event.getSource() == itemSplitVertically) {
				splitPanel(JSplitPane.VERTICAL_SPLIT);
			} else if (event.getSource() == itemClosePanel) {
				closePanel();
			} else if (event.getSource() == itemParticle2DPanel) {
				particlePanel = new Particle2DPanel(simulationAnimation);
				component = particlePanel;
			} else if (event.getSource() == itemParticle3DPanel) {
				particle3DPanel = new Particle3DPanel(simulationAnimation);
				component = particle3DPanel;
			} else if (event.getSource() == itemPhaseSpacePanel) {
				phaseSpacePanel = new PhaseSpacePanel(simulationAnimation);
				component = phaseSpacePanel;
			} else if (event.getSource() == itemElectricFieldPanel) {
				electricFieldPanel = new ElectricFieldPanel(simulationAnimation);
				component = electricFieldPanel;
			}
			if (component != null) {
				replacePanel(component);
			}
		}

		private void replacePanel(Component component) {
			component.addMouseListener(popupClickListener);
			Component parent = clickComponent.getParent();
			if (parent != null) {
				if (parent instanceof JSplitPane) {
					JSplitPane parentsplitpane = (JSplitPane) parent;
					Component parentleft = parentsplitpane.getLeftComponent();

					int dividerLocation = parentsplitpane.getDividerLocation();
					if (parentleft == clickComponent) {
						parentsplitpane.setLeftComponent(component);
					} else {
						parentsplitpane.setRightComponent(component);
					}
					parentsplitpane.setDividerLocation(dividerLocation);
				} else if (parent instanceof JPanel) {
					// top level
					MainControlApplet.this.remove(clickComponent);
					MainControlApplet.this.add(component, BorderLayout.CENTER);
					MainControlApplet.this.validate();
				}
			}
		}

		/**
		 * Split current panel either horizontally or vertically
		 *
		 * @param orientation
		 *            Either JSplitPane.HORIZONTAL_SPLIT or
		 *            JSplitPane.VERTICAL_SPLIT.
		 */
		private void splitPanel(int orientation) {
			Component parent = clickComponent.getParent();

			particlePanel = new Particle2DPanel(simulationAnimation);
			Component newcomponent = particlePanel;
			newcomponent.addMouseListener(popupClickListener);

			if (parent != null) {
				if (parent instanceof JSplitPane) {
					JSplitPane parentsplitpane = (JSplitPane) parent;
					Component parentleft = parentsplitpane.getLeftComponent();

					int dividerLocation = parentsplitpane.getDividerLocation();

					JSplitPane s = new JSplitPane(orientation,
								clickComponent, newcomponent);
					s.setOneTouchExpandable(true);
					s.setContinuousLayout(true);
					s.setResizeWeight(0.5);

					if (parentleft == clickComponent) {
						parentsplitpane.setLeftComponent(s);
					} else {
						parentsplitpane.setRightComponent(s);
					}
					parentsplitpane.setDividerLocation(dividerLocation);
				} else if (parent instanceof JPanel) {
					// top level
					JSplitPane s = new JSplitPane(orientation,
							clickComponent, newcomponent);
					s.setOneTouchExpandable(true);
					s.setContinuousLayout(true);
					s.setResizeWeight(0.5);

					MainControlApplet.this.remove(clickComponent);
					MainControlApplet.this.add(s, BorderLayout.CENTER);
					MainControlApplet.this.validate();
				}
			}
		}

		private void closePanel() {
			Component parent = clickComponent.getParent();
			if (parent != null) {
				if (parent instanceof JSplitPane) {
					JSplitPane parentsplitpane = (JSplitPane) parent;
					Component parentleft = parentsplitpane.getLeftComponent();
					Component parentright = parentsplitpane.getRightComponent();
					Component grandparent = parent.getParent();

					Component othercomponent = parentleft;
					if (parentleft == clickComponent) {
						othercomponent = parentright;
					}

					if (grandparent != null) {
						if (grandparent instanceof JSplitPane) {
							JSplitPane grandparentsplitpane = (JSplitPane) grandparent;
							Component left = grandparentsplitpane.getLeftComponent();
							if (left == parentsplitpane) {
								grandparentsplitpane.setLeftComponent(othercomponent);
							} else {
								grandparentsplitpane.setRightComponent(othercomponent);
							}
						} else if (grandparent instanceof JPanel) {
							parentsplitpane.removeAll();
							MainControlApplet.this.remove(parentsplitpane);
							MainControlApplet.this.add(othercomponent, BorderLayout.CENTER);
							MainControlApplet.this.validate();
						}
						clickComponent.removeMouseListener(popupClickListener);
						if (clickComponent instanceof AnimationPanel) {
							((AnimationPanel) clickComponent).destruct();
						}
					}
				}
			}
		}
	}

	public void setText(JTextArea text, String str, boolean onoff)
	{
		if(onoff)
			text.insert(str, 0);
		else
			text.replaceRange(" ", 0, text.getDocument().getLength());
	}

	public void setSlidersValue()
	{
		Simulation s = simulationAnimation.getSimulation();
		Timer timer = simulationAnimation.getTimer();

		stepSlider.setValue((int)(s.tstep / stepSliderScaling));
		efieldXSlider.setValue((int) (force.ex / exSliderScaling));
		efieldYSlider.setValue((int) (force.ey / eySliderScaling));
		bfieldZSlider.setValue((int) (force.bz / bzSliderScaling));
		gfieldXSlider.setValue((int) (force.gx / gxSliderScaling));
		gfieldYSlider.setValue((int) (force.gy / gySliderScaling));
		dragSlider.setValue((int) (force.drag / dragSliderScaling));
		//int delay = particlePanel.timer.getDelay();
		//speedSlider.setValue((int) (-Math.log(delay / 1000.) / speedSliderScaling));
		speedSlider.setValue(50);
		timer.setDelay((int) (1000 * Math.exp(-50 * speedSliderScaling)));
		xboxentry.setText("10");
		yboxentry.setText("10");
		zboxentry.setText("10");
		if(s.getParticleMover().getBoundaryType() == ParticleBoundaryType.Hardwall) {
			hardBoundaries.setSelected(true);
			periodicBoundaries.setSelected(false);
		}
		else if(s.getParticleMover().getBoundaryType() == ParticleBoundaryType.Periodic) {
			hardBoundaries.setSelected(false);
			periodicBoundaries.setSelected(true);
		}

		//ordering of these two is important!
		collisionComboBox.setSelectedIndex(0);
		collisionAlgorithm.setSelectedIndex(0);

		// Set algorithm UI according to current setting
		Solver solver = s.getParticleMover().getSolver();
		if (solver instanceof Boris) {
			algorithmComboBox.setSelectedIndex(4);
			relativisticCheck.setSelected(false);
		} else if (solver instanceof BorisRelativistic) {
			algorithmComboBox.setSelectedIndex(4);
			relativisticCheck.setSelected(true);
		} else if (solver instanceof EulerRichardson) {
			algorithmComboBox.setSelectedIndex(0);
			relativisticCheck.setSelected(false);
		}
		// TODO: Implement this for other solvers.
		// (Currently only implemented for solvers used in InitialConditions.)

		linkConstantForce();
	}

	public void init() {
		super.init();

		Timer timer = simulationAnimation.getTimer();
		timer.start();
		setSlidersValue();
	}

	/**
	 * Entry point for java application.
	 */
	public static void main(String[] args) {

		JFrame web = new JFrame();

		web.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		web.setTitle("PIC Plasma Torch");
		MainControlApplet applet = new MainControlApplet();
		web.setContentPane(applet);

		web.pack();
		web.setVisible(true);
		web.setSize(800, 550);
		web.setResizable(true);

		applet.init();
	}

}
