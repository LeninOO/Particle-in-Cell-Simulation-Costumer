package ui.util.yaml;

import java.util.List;

import physics.GeneralBoundaryType;
import physics.Settings;
import physics.fields.EmptyPoissonSolver;
import physics.fields.PoissonSolverFFTPeriodic;
import physics.fields.SimpleSolver;
import physics.grid.ChargeConservingCIC;

/**
 * Generic settings class into which the YAML parser parses
 */
public class YamlSettings {
	public Double timeStep;
	public Double speedOfLight;
	public Double gridStep;
	public Double duration;
	public Integer gridCellsX;
	public Integer gridCellsY;
	public Integer gridCellsZ;
	public String poissonsolver;
	public List<YamlParticle> particles;
	public List<YamlParticleStream> streams;
	public YamlOutput output;

	public void applyTo(Settings settings) {

		// Default settings:
		settings.setRelativistic(true);
		settings.setBoundary(GeneralBoundaryType.Periodic);
		settings.setGridSolver(new SimpleSolver());
		settings.useGrid(true);
		settings.setInterpolator(new ChargeConservingCIC());

		// Custom settings:
		if (timeStep != null) {
			settings.setTimeStep(timeStep);
		}

		if (duration != null) {
			settings.setTMax(duration);
		}

		if (speedOfLight != null) {
			settings.setSpeedOfLight(speedOfLight);
		}

		if (gridStep != null) {
			settings.setGridStep(gridStep);
		}

		if (gridCellsX != null) {
			settings.setGridCellsX(gridCellsX);
		}

		if (gridCellsY != null) {
			settings.setGridCellsY(gridCellsY);
		}

		if (gridCellsZ != null) {
			settings.setGridCellsZ(gridCellsZ);
		}

		if (poissonsolver != null) {
			if (poissonsolver.equals("fft")) {
				settings.setPoissonSolver(new PoissonSolverFFTPeriodic());
			} else if (poissonsolver.equals("empty")) {
				settings.setPoissonSolver(new EmptyPoissonSolver());
			} else {
				throw new RuntimeException("Unkown Poisson solver specified in YAML file.");
			}
		}

		if (particles != null) {
			for (YamlParticle p : particles) {
				p.applyTo(settings);
			}
		}

		if (streams != null) {
			for (YamlParticleStream s : streams) {
				s.applyTo(settings);
			}
		}

		if (output != null) {
			output.applyTo(settings);
		}
	}
}
