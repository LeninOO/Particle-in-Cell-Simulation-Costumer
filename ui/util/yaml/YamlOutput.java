package ui.util.yaml;

import physics.Settings;

public class YamlOutput {
	public String path;
	public Integer daniil;
	public Integer daniilspectrum;

	public void applyTo(Settings settings) {
		if (path != null) {
			settings.setFilePath(path);
		}

		if (daniilspectrum != null) {
			settings.setSpectrumStep(daniilspectrum);
		}
	}
}
