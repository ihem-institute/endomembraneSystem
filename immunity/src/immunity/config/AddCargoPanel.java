package immunity.config;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class AddCargoPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private enum MvbOption {
		NONE, NO_MVB, MVB
	}

	private final SimulationConfig config = ConfigHolder.get();
	private final CargoValidator validator = new CargoValidator();

	private final DefaultListModel<String> cargoListModel = new DefaultListModel<String>();
	private final JList<String> cargoList = new JList<String>(cargoListModel);
	private final JTextField nameField = new JTextField(20);
	private final JRadioButton membraneRadio = new JRadioButton("Membrane", true);
	private final JRadioButton solubleRadio = new JRadioButton("Soluble");
	private final JTextField uptakeField = new JTextField("0", 8);
	private final JTextField secretionField = new JTextField("0", 8);
	private final JTextField tropismField = new JTextField("RabB10,RabC10", 24);
	private final JRadioButton mvbNoneRadio = new JRadioButton("No MVB rule", true);
	private final JRadioButton mvbExcludedRadio = new JRadioButton("Excluded from MVB (noMvb)");
	private final JRadioButton mvbTargetRadio = new JRadioButton("Targeted to MVB (mvb)");
	private final JCheckBox sphCheckBox = new JCheckBox(
			"Large: does not enter tubules/vesicles during fission (sph)");
	private final JPanel membranePanel = new JPanel(new GridBagLayout());
	private final JPanel solublePanel = new JPanel(new GridBagLayout());
	private final JButton saveCargoButton = new JButton("Add cargo");
	private String editingCargoName;
	private int syncedLoadGeneration = -1;

	public AddCargoPanel() {
		setLayout(new BorderLayout(8, 8));
		add(buildFormPanel(), BorderLayout.CENTER);
		add(buildCargoListPanel(), BorderLayout.EAST);
		ActionListener typeListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				updateFormForCargoType();
			}
		};
		membraneRadio.addActionListener(typeListener);
		solubleRadio.addActionListener(typeListener);
		cargoList.addListSelectionListener(e -> {
			if (e.getValueIsAdjusting()) {
				return;
			}
			int index = cargoList.getSelectedIndex();
			if (index >= 0) {
				String name = getCargoNameAtIndex(index);
				if (name != null && config.hasCargo(name)) {
					loadCargoIntoForm(config.getCargos().get(name));
				}
			}
		});
		updateFormForCargoType();
	}

	/** Called after a shared CSV load; refreshes the cargo list from memory. */
	public void onConfigLoaded(boolean resetForm) {
		syncedLoadGeneration = ConfigHolder.getLoadGeneration();
		refreshCargoList();
		if (resetForm) {
			clearFormForNewCargo();
		}
	}

	/** Refreshes the list when another tab loaded the CSV. */
	public void syncIfStale() {
		if (syncedLoadGeneration == ConfigHolder.getLoadGeneration()) {
			return;
		}
		onConfigLoaded(false);
	}

	private JPanel buildFormPanel() {
		JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(4, 4, 4, 4);
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.HORIZONTAL;
		int row = 0;

		gc.gridx = 0;
		gc.gridy = row;
		panel.add(new JLabel("Name (e.g. myCargoEn):"), gc);
		gc.gridx = 1;
		panel.add(nameField, gc);
		row++;

		ButtonGroup typeGroup = new ButtonGroup();
		typeGroup.add(membraneRadio);
		typeGroup.add(solubleRadio);
		gc.gridx = 0;
		gc.gridy = row;
		panel.add(new JLabel("Type:"), gc);
		gc.gridx = 1;
		JPanel typePanel = new JPanel();
		typePanel.add(membraneRadio);
		typePanel.add(solubleRadio);
		panel.add(typePanel, gc);
		row++;

		buildMembranePanel();
		buildSolublePanel();
		gc.gridx = 0;
		gc.gridy = row;
		gc.gridwidth = 2;
		panel.add(membranePanel, gc);
		row++;
		gc.gridx = 0;
		gc.gridy = row;
		panel.add(solublePanel, gc);
		row++;

		gc.gridx = 0;
		gc.gridy = row;
		gc.gridwidth = 2;
		JPanel actionPanel = new JPanel();
		saveCargoButton.addActionListener(e -> applyCargoChanges());
		actionPanel.add(saveCargoButton);
		JButton newButton = new JButton("New cargo");
		newButton.addActionListener(e -> clearFormForNewCargo());
		actionPanel.add(newButton);
		panel.add(actionPanel, gc);
		return panel;
	}

	private void buildMembranePanel() {
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(4, 4, 4, 4);
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.HORIZONTAL;
		int row = 0;

		gc.gridx = 0;
		gc.gridy = row;
		membranePanel.add(new JLabel("PM uptake rate:"), gc);
		gc.gridx = 1;
		membranePanel.add(uptakeField, gc);
		row++;

		gc.gridx = 0;
		gc.gridy = row;
		membranePanel.add(new JLabel("ER secretion rate:"), gc);
		gc.gridx = 1;
		membranePanel.add(secretionField, gc);
		row++;

		gc.gridx = 0;
		gc.gridy = row;
		membranePanel.add(new JLabel("Rab tropism (comma-separated):"), gc);
		gc.gridx = 1;
		membranePanel.add(tropismField, gc);
		row++;

		ButtonGroup mvbGroup = new ButtonGroup();
		mvbGroup.add(mvbNoneRadio);
		mvbGroup.add(mvbExcludedRadio);
		mvbGroup.add(mvbTargetRadio);
		JPanel mvbPanel = new JPanel();
		mvbPanel.add(mvbNoneRadio);
		mvbPanel.add(mvbExcludedRadio);
		mvbPanel.add(mvbTargetRadio);
		gc.gridx = 0;
		gc.gridy = row;
		gc.gridwidth = 2;
		membranePanel.add(mvbPanel, gc);
	}

	private void buildSolublePanel() {
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(4, 4, 4, 4);
		gc.anchor = GridBagConstraints.WEST;
		gc.gridwidth = 2;
		solublePanel.add(sphCheckBox, gc);
	}

	private JPanel buildCargoListPanel() {
		cargoList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll = new JScrollPane(cargoList);
		scroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Loaded cargos (click to edit)"));
		JButton deleteButton = new JButton("Delete selected");
		deleteButton.addActionListener(e -> deleteSelectedCargo());
		JPanel panel = new JPanel(new BorderLayout(4, 4));
		panel.add(scroll, BorderLayout.CENTER);
		JPanel south = new JPanel();
		south.add(deleteButton);
		panel.add(south, BorderLayout.SOUTH);
		panel.setPreferredSize(new java.awt.Dimension(280, 0));
		return panel;
	}

	private void updateFormForCargoType() {
		boolean membrane = membraneRadio.isSelected();
		membranePanel.setVisible(membrane);
		solublePanel.setVisible(!membrane);
		if (editingCargoName != null) {
			revalidate();
			repaint();
			return;
		}
		if (!membrane) {
			uptakeField.setText("0");
			secretionField.setText("0");
			mvbNoneRadio.setSelected(true);
		} else {
			sphCheckBox.setSelected(false);
			if (tropismField.getText().trim().length() == 0) {
				tropismField.setText("RabB10,RabC10");
			}
		}
		revalidate();
		repaint();
	}

	private void loadCargoIntoForm(CargoDefinition cargo) {
		editingCargoName = cargo.getName();
		nameField.setText(cargo.getName());
		nameField.setEnabled(false);
		membraneRadio.setEnabled(false);
		solubleRadio.setEnabled(false);
		mvbNoneRadio.setSelected(true);
		if (cargo.getType() == CargoType.MEMBRANE) {
			membraneRadio.setSelected(true);
			uptakeField.setText(formatNumber(cargo.getUptakeRate()));
			secretionField.setText(formatNumber(cargo.getSecretionRate()));
			List<String> rabTags = new ArrayList<String>();
			for (String tag : cargo.getRabTropismTags()) {
				if ("noMvb".equals(tag)) {
					mvbExcludedRadio.setSelected(true);
				} else if ("mvb".equals(tag)) {
					mvbTargetRadio.setSelected(true);
				} else if (!"sph".equals(tag)) {
					rabTags.add(tag);
				}
			}
			tropismField.setText(joinTags(rabTags));
			sphCheckBox.setSelected(false);
		} else {
			solubleRadio.setSelected(true);
			sphCheckBox.setSelected(cargo.getRabTropismTags().contains("sph"));
		}
		updateFormForCargoType();
		updateSaveCargoButtonLabel();
	}

	private void clearFormForNewCargo() {
		editingCargoName = null;
		cargoList.clearSelection();
		nameField.setEnabled(true);
		membraneRadio.setEnabled(true);
		solubleRadio.setEnabled(true);
		nameField.setText("");
		membraneRadio.setSelected(true);
		uptakeField.setText("0");
		secretionField.setText("0");
		tropismField.setText("RabB10,RabC10");
		mvbNoneRadio.setSelected(true);
		sphCheckBox.setSelected(false);
		updateFormForCargoType();
		updateSaveCargoButtonLabel();
	}

	private void updateSaveCargoButtonLabel() {
		saveCargoButton.setText(editingCargoName != null ? "Apply changes" : "Add cargo");
	}

	private String formatNumber(double value) {
		if (value == (long) value) {
			return Long.toString((long) value);
		}
		return Double.toString(value);
	}

	private void applyCargoChanges() {
		if (editingCargoName != null) {
			updateExistingCargo();
		} else {
			addCargo();
		}
	}

	private void updateExistingCargo() {
		if (config.getLines().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Load InputIntrTransp3.csv first.", "Notice",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		try {
			CargoDefinition cargo = config.getCargos().get(editingCargoName);
			if (cargo == null) {
				clearFormForNewCargo();
				return;
			}
			CargoType type = cargo.getType();
			double uptake = type == CargoType.MEMBRANE ? parseDoubleField(uptakeField.getText(), "uptake") : 0d;
			double secretion = type == CargoType.MEMBRANE
					? parseDoubleField(secretionField.getText(), "secretion")
					: 0d;
			List<String> tropism = buildTropismTags(type);
			cargo.setUptakeRate(uptake);
			cargo.setSecretionRate(secretion);
			cargo.setRabTropismTags(tropism);
			List<String> errors = validator.validateUpdate(cargo);
			if (!errors.isEmpty()) {
				JOptionPane.showMessageDialog(this, joinErrors(errors), "Validation",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			ConfigHolder.markDomainEdits();
			refreshCargoList();
			selectCargoInList(editingCargoName);
			JOptionPane.showMessageDialog(this, "Cargo " + editingCargoName + " updated in memory.\n"
					+ "Use Save CSV to write changes to the file.", "Updated",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void selectCargoInList(String name) {
		int index = 0;
		for (String cargoName : config.getCargos().keySet()) {
			if (cargoName.equals(name)) {
				cargoList.setSelectedIndex(index);
				return;
			}
			index++;
		}
	}

	private void addCargo() {
		if (config.getLines().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Load InputIntrTransp3.csv first.", "Notice",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		try {
			CargoType type = membraneRadio.isSelected() ? CargoType.MEMBRANE : CargoType.SOLUBLE;
			double uptake = type == CargoType.MEMBRANE ? parseDoubleField(uptakeField.getText(), "uptake") : 0d;
			double secretion = type == CargoType.MEMBRANE
					? parseDoubleField(secretionField.getText(), "secretion")
					: 0d;
			List<String> tropism = buildTropismTags(type);
			CargoDefinition cargo = new CargoDefinition(nameField.getText(), type, uptake, secretion, tropism);

			List<String> errors = validator.validate(cargo, config);
			if (!errors.isEmpty()) {
				JOptionPane.showMessageDialog(this, joinErrors(errors), "Validation",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			config.addCargo(cargo);
			ConfigHolder.markDomainEdits();
			refreshCargoList();
			clearFormForNewCargo();
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
		}
	}

	private List<String> buildTropismTags(CargoType type) {
		List<String> tropism = new ArrayList<String>();
		Set<String> seen = new HashSet<String>();
		if (type == CargoType.MEMBRANE) {
			for (String tag : parseTropism(tropismField.getText())) {
				if (seen.add(tag)) {
					tropism.add(tag);
				}
			}
			MvbOption mvbOption = getSelectedMvbOption();
			if (mvbOption == MvbOption.NO_MVB && seen.add("noMvb")) {
				tropism.add("noMvb");
			} else if (mvbOption == MvbOption.MVB && seen.add("mvb")) {
				tropism.add("mvb");
			}
		} else if (sphCheckBox.isSelected()) {
			tropism.add("sph");
		}
		return tropism;
	}

	private MvbOption getSelectedMvbOption() {
		if (mvbExcludedRadio.isSelected()) {
			return MvbOption.NO_MVB;
		}
		if (mvbTargetRadio.isSelected()) {
			return MvbOption.MVB;
		}
		return MvbOption.NONE;
	}

	private void deleteSelectedCargo() {
		if (config.getLines().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Load a CSV file first.", "Notice",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		int index = cargoList.getSelectedIndex();
		if (index < 0) {
			JOptionPane.showMessageDialog(this, "Select a cargo to delete.", "Notice",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		String name = getCargoNameAtIndex(index);
		if (name == null) {
			return;
		}
		int choice = JOptionPane.showConfirmDialog(this,
				"Remove cargo \"" + name + "\" from the configuration?\n"
						+ "Save the CSV to persist this change.",
				"Confirm delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (choice != JOptionPane.OK_OPTION) {
			return;
		}
		config.removeCargo(name);
		if (name.equals(editingCargoName)) {
			clearFormForNewCargo();
		}
		refreshCargoList();
	}

	private String getCargoNameAtIndex(int index) {
		int i = 0;
		for (String name : config.getCargos().keySet()) {
			if (i == index) {
				return name;
			}
			i++;
		}
		return null;
	}

	private void refreshCargoList() {
		cargoListModel.clear();
		for (CargoDefinition cargo : config.getCargos().values()) {
			cargoListModel.addElement(cargo.getName() + " (" + cargo.getType() + ") " + formatTropism(cargo));
		}
	}

	private String formatTropism(CargoDefinition cargo) {
		if (cargo.getRabTropismTags().isEmpty()) {
			return "";
		}
		return "[" + joinTags(cargo.getRabTropismTags()) + "]";
	}

	private String joinTags(List<String> tags) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < tags.size(); i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(tags.get(i));
		}
		return sb.toString();
	}

	private double parseDoubleField(String text, String label) {
		try {
			return Double.parseDouble(text.trim());
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Invalid number in " + label + ".");
		}
	}

	private List<String> parseTropism(String text) {
		List<String> tags = new ArrayList<String>();
		if (text == null || text.trim().length() == 0) {
			return tags;
		}
		for (String part : text.split(",")) {
			String trimmed = part.trim();
			if (trimmed.length() > 0 && !isReservedTropismTag(trimmed)) {
				tags.add(trimmed);
			}
		}
		return tags;
	}

	private boolean isReservedTropismTag(String tag) {
		return "mvb".equals(tag) || "noMvb".equals(tag) || "sph".equals(tag);
	}

	private String joinErrors(List<String> errors) {
		StringBuilder sb = new StringBuilder();
		for (String error : errors) {
			sb.append("- ").append(error).append('\n');
		}
		return sb.toString();
	}
}
