package ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Timer;

import physics.InitialConditions;
import physics.Settings;
import physics.Simulation;
import physics.collision.algorithms.CollisionAlgorithm;
import physics.collision.algorithms.MatrixTransformation;
import physics.collision.algorithms.SimpleCollision;
import physics.collision.algorithms.VectorTransformation;
import physics.collision.detectors.AllParticles;
import physics.collision.detectors.Detector;
import physics.collision.detectors.SweepAndPrune;
import physics.force.CombinedForce;
import physics.force.ConstantForce;
import physics.force.Force;
import physics.force.SimpleGridForce;
import physics.force.relativistic.ConstantForceRelativistic;
import physics.force.relativistic.SimpleGridForceRelativistic;
import physics.movement.boundary.ParticleBoundaryType;
import physics.solver.Boris;
import physics.solver.BorisDamped;
import physics.solver.Euler;
import physics.solver.EulerRichardson;
import physics.solver.LeapFrog;
import physics.solver.LeapFrogDamped;
import physics.solver.LeapFrogHalfStep;
import physics.solver.SemiImplicitEuler;
import physics.solver.relativistic.BorisRelativistic;
import physics.solver.relativistic.LeapFrogRelativistic;
import physics.solver.relativistic.SemiImplicitEulerRelativistic;
import ui.panel.Particle2DPanel;
import ui.util.FrameRateDetector;

/**
 * Wrapper for the simulation class in GUI applications.
 */
public class SimulationAnimation {

	private Simulation s;

	private boolean relativistic = false;

	private boolean calculateFields = false;

	/** Milliseconds between updates */
	private int interval = 30;

	/** Timer for animation */
	private Timer timer;

	private FrameRateDetector frameratedetector;

	private ArrayList<SimulationAnimationListener> listeners = new ArrayList<SimulationAnimationListener>();

	/** Constructor */
	public SimulationAnimation() {
		timer = new Timer(interval, new TimerListener());
		frameratedetector = new FrameRateDetector(500);
		s = InitialConditions.initRandomParticles(10, 2);
	}

	/** Listener for timer */
	public class TimerListener implements ActionListener {

		public void actionPerformed(ActionEvent eve) {
			try {
				s.step();
			} catch (FileNotFoundException ex) {
				Logger.getLogger(Particle2DPanel.class.getName()).log(Level.SEVERE, null, ex);
			} catch (IOException ex2) {
				Logger.getLogger(Particle2DPanel.class.getName()).log(Level.SEVERE, null, ex2);
			}
			frameratedetector.update();
			repaint();
		}
	}

	public void startAnimation() {
		timer.start();
	}

	public void stopAnimation() {
		timer.stop();
		//test = false;
	}

	public Simulation getSimulation() {
		return s;
	}

	public FrameRateDetector getFrameRateDetector() {
		return frameratedetector;
	}

	public Timer getTimer() {
		return timer;
	}

	/**
	 * Add Listener for repaint() event.
	 * @param listener
	 */
	public void addListener(SimulationAnimationListener listener) {
		listeners.add(listener);
	}

	/**
	 * Removes Listener for repaint() event.
	 * @param listener
	 */
	public void removeListener(SimulationAnimationListener listener) {
		listeners.remove(listener);
	}

	private void repaint() {
		// Let all listeners know
		for (SimulationAnimationListener l : listeners) {
			l.repaint();
		}
	}

	private void clear() {
		// Let all listeners know
		for (SimulationAnimationListener l : listeners) {
			l.clear();
		}
	}

	public void resetAnimation(int id) {
		// timer.restart();
		timer.stop();
		clear();
		switch(id) {
		case 0:
			s = InitialConditions.initRandomParticles(10, 2);
			break;
		case 1:
			s = InitialConditions.initRandomParticles(100, 1);
			break;
		case 2:
			s = InitialConditions.initRandomParticles(1000, 0.5);
			break;
		case 3:
			s = InitialConditions.initRandomParticles(10000, 0.01);
			break;
		case 4:
			s = InitialConditions.initGravity(1, 2);
			break;
		case 5:
			s = InitialConditions.initElectric(1, 2);
			break;
		case 6:
			s = InitialConditions.initMagnetic(3, 2);
			break;
		case 7:
			s = InitialConditions.initPair(0.1,1);
			break;
		case 8:
			s = InitialConditions.initTwoStream(0.1,1,1000);
			break;
		case 9:
			s = InitialConditions.initWeibel(0.01,1,2000,4,0.9);
			break;
		case 10:
			s = InitialConditions.initOneTest(0.01,1);
			break;
		case 11:
			s = InitialConditions.initWaveTest(0.2);
			break;
		case 12:
			s = InitialConditions.initPair3D(0.1,1);
			break;
		case 13:
			s = InitialConditions.initTwoStream3D(0.1,0.1,5000);
			break;
		case 14:
			s = InitialConditions.initWeibel3D(0.01,1,1000,2,0.9);
			break;
		}
		updateFieldForce();
		s.prepareAllParticles();
		s.turnGridForceOn();
		timer.start();
	}

	/**
	 * Reset animation according to settings
	 *
	 * @param settings New settings for animation.
	 */
	public void resetAnimation(Settings settings) {
		// timer.restart();
		timer.stop();
		clear();
		s = new Simulation(settings);
		//updateFieldForce();
		s.prepareAllParticles();
		//s.turnGridForceOn();
		timer.start();
	}

	public void calculateFields() {
		calculateFields =! calculateFields;
		updateFieldForce();
	}

	private void updateFieldForce() {

		if(calculateFields) {
			s.turnGridForceOn();
		}
		else {
			s.turnGridForceOff();
		}
	}

	public void algorithmChange(int id)
	{
		s.completeAllParticles();

		switch(id) {
		case 0:
			s.getParticleMover().setSolver(new EulerRichardson());
			break;
		case 1:
			s.getParticleMover().setSolver(new LeapFrog());
			break;
		case 2:
			s.getParticleMover().setSolver(new LeapFrogDamped());
			break;
		case 3:
			s.getParticleMover().setSolver(new LeapFrogHalfStep());
			break;
		case 4:
			s.getParticleMover().setSolver(new Boris());
			break;
		case 5:
			s.getParticleMover().setSolver(new BorisDamped());
			break;
		case 6:
			s.getParticleMover().setSolver(new SemiImplicitEuler());
			break;
		case 7:
			s.getParticleMover().setSolver(new Euler());
			break;
			}

		s.prepareAllParticles();
	}

	public void relativisticEffects(int i) {
		relativistic =! relativistic;

		if(relativistic == false) {
			s.relativistic = false;
			if (s.f instanceof CombinedForce) {
				ArrayList<Force> forces = ((CombinedForce) s.f).forces;
				for (int j = 0; j < forces.size(); j++) {
					if (forces.get(j) instanceof ConstantForceRelativistic){
						forces.set(j, new ConstantForce());
					}
					if (forces.get(j) instanceof SimpleGridForceRelativistic){
						forces.set(j, new SimpleGridForce());
					}
				}
			}
			switch(i) {
			case 1:
				s.getParticleMover().setSolver(new LeapFrog());
			case 4:
				s.getParticleMover().setSolver(new Boris());
				break;
			case 6:
				s.getParticleMover().setSolver(new SemiImplicitEuler());
				break;
			}
		}

		if(relativistic == true) {
			s.relativistic = true;
			//System.out.println("relativistic version on");
			if (s.f instanceof CombinedForce) {
				ArrayList<Force> forces = ((CombinedForce) s.f).forces;
				for (int j = 0; j < forces.size(); j++) {
					if (forces.get(j) instanceof ConstantForce){
						forces.set(j, new ConstantForceRelativistic(s.getSpeedOfLight()));
					}
					if (forces.get(j) instanceof SimpleGridForce){
						forces.set(j, new SimpleGridForceRelativistic(s));
					}
				}
			}
			switch(i) {
			case 1:
				s.getParticleMover().setSolver(new LeapFrogRelativistic(s.getSpeedOfLight()));
			case 4:
				s.getParticleMover().setSolver(new BorisRelativistic(s.getSpeedOfLight()));
				break;
			case 6:
				s.getParticleMover().setSolver(new SemiImplicitEulerRelativistic(s.getSpeedOfLight()));
				break;
			}
		}

	}

	public void collisionChange(int i) {
		switch(i) {
		case 0:
			s.detector = new Detector();
			s.collisionalgorithm = new CollisionAlgorithm();
			break;
		case 1:
			s.detector = new AllParticles(s.particles);
			break;
		case 2:
			s.detector = new SweepAndPrune(s.particles);
			break;
		}
	}

	public void algorithmCollisionChange(int i) {
		switch(i) {
		case 0:
			s.collisionalgorithm = new SimpleCollision();
			break;
		case 1:
			s.collisionalgorithm = new VectorTransformation();
			break;
		case 2:
			s.collisionalgorithm = new MatrixTransformation();
			break;
		}
	}

	public void boundariesChange(int i) {
		switch(i) {
		case 0:
			s.getParticleMover().changeBoundaryType(ParticleBoundaryType.Hardwall);
			break;
		case 1:
			s.getParticleMover().changeBoundaryType(ParticleBoundaryType.Periodic);
		}

	}

}
