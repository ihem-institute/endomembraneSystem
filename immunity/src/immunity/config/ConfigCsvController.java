package immunity.config;

import java.awt.Component;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/**
 * Shared Load/Save for the config editor; updates both Cargo and Rab domain tabs.
 */
public class ConfigCsvController {

	private final CsvConfigReader reader = new CsvConfigReader();
	private final CsvConfigWriter writer = new CsvConfigWriter();
	private final SimulationConfig config = ConfigHolder.get();
	private final JLabel fileLabel;
	private final AddCargoPanel cargoPanel;
	private final AddRabDomainPanel rabPanel;

	public ConfigCsvController(JLabel fileLabel, AddCargoPanel cargoPanel, AddRabDomainPanel rabPanel) {
		this.fileLabel = fileLabel;
		this.cargoPanel = cargoPanel;
		this.rabPanel = rabPanel;
		updateFileLabel();
	}

	public void loadCsv(Component parent) {
		if (!confirmDiscardBeforeReload(parent)) {
			return;
		}
		JFileChooser chooser = new JFileChooser(new File("data"));
		chooser.setSelectedFile(new File("data/InputIntrTransp3.csv"));
		if (chooser.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		try {
			SimulationConfig loaded = reader.read(chooser.getSelectedFile());
			ConfigHolder.replaceFrom(loaded);
			ConfigHolder.clearDomainEdits();
			cargoPanel.onConfigLoaded(true);
			rabPanel.onConfigLoaded(true);
			updateFileLabel();
		} catch (Exception ex) {
			showError(parent, "Could not load CSV", ex);
		}
	}

	public void saveCsv(Component parent) {
		if (config.getLines().isEmpty()) {
			JOptionPane.showMessageDialog(parent, "Load a CSV file first.", "Notice",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		JFileChooser chooser = new JFileChooser(new File("data"));
		if (config.getSourcePath() != null) {
			chooser.setSelectedFile(new File(config.getSourcePath()));
		}
		if (chooser.showSaveDialog(parent) != JFileChooser.APPROVE_OPTION) {
			return;
		}
		if (!rabPanel.prepareBeforeSave()) {
			return;
		}
		try {
			writer.write(config, chooser.getSelectedFile());
			config.setSourcePath(chooser.getSelectedFile().getPath());
			ConfigHolder.clearDomainEdits();
			updateFileLabel();
			JOptionPane.showMessageDialog(parent, buildSaveConfirmationMessage(), "Done",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception ex) {
			showError(parent, "Could not save CSV", ex);
		}
	}

	public void syncPanelsIfStale() {
		cargoPanel.syncIfStale();
		rabPanel.syncIfStale();
	}

	private boolean confirmDiscardBeforeReload(Component parent) {
		if (ConfigHolder.hasDomainEdits()) {
			int choice = JOptionPane.showConfirmDialog(parent,
					"You have unsaved changes (cargos and/or Rab domains).\n"
							+ "Loading a new CSV will discard them.\n\nContinue?",
					"Unsaved changes", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (choice != JOptionPane.YES_OPTION) {
				return false;
			}
		}
		if (rabPanel.hasPendingFormChanges()) {
			int choice = JOptionPane.showConfirmDialog(parent,
					"The Rab domain form has unsaved input.\n"
							+ "Loading a new CSV will discard it.\n\nContinue?",
					"Unsaved Rab domain form", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if (choice != JOptionPane.YES_OPTION) {
				return false;
			}
		}
		return true;
	}

	private String buildSaveConfirmationMessage() {
		StringBuilder sb = new StringBuilder();
		sb.append("CSV saved to:\n").append(config.getSourcePath());
		sb.append("\n\nCargos: ").append(config.getCargos().size());
		sb.append("\nDomains: ").append(config.getRabDomains().size());
		sb.append("\n\nIf the CSV was open in Eclipse, use File -> Reload to see changes.");
		return sb.toString();
	}

	private void updateFileLabel() {
		if (config.getSourcePath() != null && config.getSourcePath().length() > 0) {
			fileLabel.setText(config.getSourcePath());
		} else if (config.getLines().isEmpty()) {
			fileLabel.setText("No file loaded");
		} else {
			fileLabel.setText("(loaded, path unknown)");
		}
	}

	private void showError(Component parent, String title, Exception ex) {
		JOptionPane.showMessageDialog(parent, title + ":\n" + ex.getMessage(), "Error",
				JOptionPane.ERROR_MESSAGE);
	}
}
