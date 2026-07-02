package immunity.config;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

public class AddRabDomainPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final SimulationConfig config = ConfigHolder.get();
	private final RabDomainValidator validator = new RabDomainValidator();

	private final DefaultListModel<String> domainListModel = new DefaultListModel<String>();
	private final JList<String> domainList = new JList<String>(domainListModel);
	private final JTextField rabIdField = new JTextField("RabJ", 8);
	private final JTextField displayNameField = new JTextField(16);
	private final JTextField initCellField = new JTextField("1", 8);
	private final JTextField tubuleTropismField = new JTextField("1", 8);
	private final JTextField mtTubuleField = new JTextField("0", 8);
	private final JTextField mtRestField = new JTextField("0", 8);
	private final JTextField recyProbField = new JTextField("0", 8);
	private final JPanel compatPanel = new JPanel(new GridBagLayout());
	private final Map<String, JTextField> compatFields = new LinkedHashMap<String, JTextField>();
	private final DefaultListModel<String> maturationListModel = new DefaultListModel<String>();
	private final JList<String> maturationList = new JList<String>(maturationListModel);
	private final DefaultComboBoxModel<String> maturationTargetModel = new DefaultComboBoxModel<String>();
	private final JComboBox<String> maturationTargetCombo = new JComboBox<String>(maturationTargetModel);
	private final JTextField maturationRateField = new JTextField("0.1", 8);
	private final Map<String, Double> pendingMaturation = new LinkedHashMap<String, Double>();
	private final JButton saveDomainButton = new JButton("Add domain to list");
	private final JLabel saveDomainHintLabel = new JLabel(
			"Fill the form, then add the domain here before Save CSV.");
	private String editingDomainId;
	private int syncedLoadGeneration = -1;

	public AddRabDomainPanel() {
		setLayout(new BorderLayout(8, 8));
		add(buildFormScroll(), BorderLayout.CENTER);
		add(buildDomainListPanel(), BorderLayout.EAST);
		add(buildAddDomainActionPanel(), BorderLayout.SOUTH);
		domainList.addListSelectionListener(e -> {
			if (e.getValueIsAdjusting()) {
				return;
			}
			int index = domainList.getSelectedIndex();
			if (index >= 0) {
				String rabId = getDomainIdAtIndex(index);
				if (rabId != null && config.hasRabDomain(rabId)) {
					loadDomainIntoForm(config.getRabDomains().get(rabId));
				}
			}
		});
	}

	/** Called after a shared CSV load; refreshes the domain list from memory. */
	public void onConfigLoaded(boolean resetForm) {
		syncedLoadGeneration = ConfigHolder.getLoadGeneration();
		refreshDomainList();
		if (resetForm) {
			rebuildCompatFields(null);
			refreshMaturationTargets();
			clearFormForNewDomain();
			suggestNextRabId();
		}
	}

	/** Refreshes the list when another tab loaded the CSV. */
	public void syncIfStale() {
		if (syncedLoadGeneration == ConfigHolder.getLoadGeneration()) {
			return;
		}
		onConfigLoaded(false);
		if (editingDomainId != null && config.hasRabDomain(editingDomainId)) {
			rebuildCompatFields(editingDomainId);
			refreshMaturationTargets();
		} else if (editingDomainId == null && !hasPendingDomainInForm()) {
			rebuildCompatFields(null);
			refreshMaturationTargets();
		}
	}

	public boolean prepareBeforeSave() {
		return commitPendingDomainBeforeSave();
	}

	public boolean hasPendingFormChanges() {
		return hasPendingDomainInForm();
	}

	private JScrollPane buildFormScroll() {
		JPanel form = new JPanel(new GridBagLayout());
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(4, 4, 4, 4);
		gc.anchor = GridBagConstraints.WEST;
		gc.fill = GridBagConstraints.HORIZONTAL;
		int row = 0;

		gc.gridx = 0;
		gc.gridy = row;
		form.add(new JLabel("Rab ID (e.g. RabJ):"), gc);
		gc.gridx = 1;
		JPanel idPanel = new JPanel();
		idPanel.add(rabIdField);
		JButton suggestButton = new JButton("Suggest next");
		suggestButton.addActionListener(e -> suggestNextRabId());
		idPanel.add(suggestButton);
		form.add(idPanel, gc);
		row++;

		gc.gridx = 0;
		gc.gridy = row;
		form.add(new JLabel("Display name (e.g. EE, ERGIC):"), gc);
		gc.gridx = 1;
		form.add(displayNameField, gc);
		row++;

		gc.gridx = 0;
		gc.gridy = row;
		form.add(new JLabel("Initial amount in cell:"), gc);
		gc.gridx = 1;
		form.add(initCellField, gc);
		row++;

		gc.gridx = 0;
		gc.gridy = row;
		gc.gridwidth = 2;
		form.add(new JLabel("Tropism and recycling"), gc);
		row++;
		gc.gridwidth = 1;

		row = addLabeledField(form, gc, row, "Tubule tropism:", tubuleTropismField);
		row = addLabeledField(form, gc, row, "MT tropism (tubule):", mtTubuleField);
		row = addLabeledField(form, gc, row, "MT tropism (rest of organelle):", mtRestField);
		row = addLabeledField(form, gc, row, "Recycling probability:", recyProbField);

		gc.gridx = 0;
		gc.gridy = row;
		gc.gridwidth = 2;
		form.add(new JLabel("Compatibility with existing domains (0-1):"), gc);
		row++;

		gc.gridy = row;
		gc.fill = GridBagConstraints.BOTH;
		gc.weightx = 1;
		gc.weighty = 0.2;
		JScrollPane compatScroll = new JScrollPane(compatPanel);
		compatScroll.setPreferredSize(new java.awt.Dimension(400, 120));
		form.add(compatScroll, gc);
		row++;
		gc.fill = GridBagConstraints.HORIZONTAL;
		gc.weighty = 0;

		gc.gridx = 0;
		gc.gridy = row;
		gc.gridwidth = 2;
		form.add(new JLabel("Maturation (this domain -> target):"), gc);
		row++;

		gc.gridx = 0;
		gc.gridy = row;
		form.add(new JLabel("Target domain:"), gc);
		gc.gridx = 1;
		JPanel maturationAddPanel = new JPanel();
		maturationAddPanel.add(maturationTargetCombo);
		maturationAddPanel.add(new JLabel("Rate:"));
		maturationAddPanel.add(maturationRateField);
		JButton addMaturationButton = new JButton("Add maturation");
		addMaturationButton.addActionListener(e -> addPendingMaturation());
		maturationAddPanel.add(addMaturationButton);
		form.add(maturationAddPanel, gc);
		row++;

		gc.gridx = 0;
		gc.gridy = row;
		gc.gridwidth = 2;
		JScrollPane maturationScroll = new JScrollPane(maturationList);
		maturationScroll.setPreferredSize(new java.awt.Dimension(400, 80));
		form.add(maturationScroll, gc);

		return new JScrollPane(form);
	}

	private JPanel buildAddDomainActionPanel() {
		JPanel panel = new JPanel(new BorderLayout(8, 4));
		panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
		panel.add(saveDomainHintLabel, BorderLayout.CENTER);
		saveDomainButton.addActionListener(e -> saveDomainFromForm());
		JButton newDomainButton = new JButton("New domain");
		newDomainButton.addActionListener(e -> clearFormForNewDomain());
		JPanel buttons = new JPanel();
		buttons.add(saveDomainButton);
		buttons.add(newDomainButton);
		panel.add(buttons, BorderLayout.EAST);
		return panel;
	}

	private int addLabeledField(JPanel form, GridBagConstraints gc, int row, String label, JTextField field) {
		gc.gridwidth = 1;
		gc.gridx = 0;
		gc.gridy = row;
		form.add(new JLabel(label), gc);
		gc.gridx = 1;
		form.add(field, gc);
		return row + 1;
	}

	private JPanel buildDomainListPanel() {
		domainList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		JScrollPane scroll = new JScrollPane(domainList);
		scroll.setBorder(javax.swing.BorderFactory.createTitledBorder("Loaded domains (click to edit)"));
		JButton deleteButton = new JButton("Delete selected");
		deleteButton.addActionListener(e -> deleteSelectedDomain());
		JPanel panel = new JPanel(new BorderLayout(4, 4));
		panel.add(scroll, BorderLayout.CENTER);
		panel.add(deleteButton, BorderLayout.SOUTH);
		panel.setPreferredSize(new java.awt.Dimension(260, 0));
		return panel;
	}

	private void saveDomainFromForm() {
		if (editingDomainId != null) {
			updateDomainFromForm(true);
		} else {
			addDomainFromForm(true);
		}
	}

	private boolean commitPendingDomainBeforeSave() {
		if (editingDomainId != null) {
			return updateDomainFromForm(false);
		}
		if (!hasPendingDomainInForm()) {
			return true;
		}
		String rabId = rabIdField.getText().trim();
		int choice = JOptionPane.showConfirmDialog(this,
				"Domain \"" + rabId + "\" is filled in the form but was not added yet.\n"
						+ "Add it now before saving?",
				"Unsaved domain", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (choice == JOptionPane.CANCEL_OPTION || choice == JOptionPane.CLOSED_OPTION) {
			return false;
		}
		if (choice == JOptionPane.NO_OPTION) {
			return true;
		}
		return addDomainFromForm(true);
	}

	private boolean hasPendingDomainInForm() {
		String rabId = rabIdField.getText().trim();
		if (!rabId.matches("Rab[A-Z]") || config.hasRabDomain(rabId)) {
			return false;
		}
		return displayNameField.getText().trim().length() > 0 || !pendingMaturation.isEmpty();
	}

	private boolean addDomainFromForm(boolean showSuccessMessage) {
		if (config.getLines().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Load InputIntrTransp3.csv first.", "Notice",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}
		try {
			RabDomainDefinition domain = buildDomainFromForm();
			Map<String, Double> compatibility = readCompatibilityFromForm(domain.getRabId());
			List<String> errors = validator.validate(domain, config, compatibility, true);
			if (!errors.isEmpty()) {
				JOptionPane.showMessageDialog(this, joinErrors(errors), "Validation",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			RabDomainCsvSupport.registerDomain(config, domain, compatibility);
			refreshDomainList();
			rebuildCompatFields(null);
			refreshMaturationTargets();
			clearFormForNewDomain();
			suggestNextRabId();
			if (showSuccessMessage) {
				JOptionPane.showMessageDialog(this,
						"Domain " + domain.getRabId() + " added.\n"
								+ "Total domains in memory: " + config.getRabDomains().size() + "\n"
								+ "Use Save CSV to write it to the file.",
						"Domain added", JOptionPane.INFORMATION_MESSAGE);
			}
			return true;
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	private boolean updateDomainFromForm(boolean showSuccessMessage) {
		if (config.getLines().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Load InputIntrTransp3.csv first.", "Notice",
					JOptionPane.WARNING_MESSAGE);
			return false;
		}
		try {
			RabDomainDefinition domain = buildDomainFromForm();
			if (!domain.getRabId().equals(editingDomainId)) {
				JOptionPane.showMessageDialog(this, "Rab ID cannot be changed when editing.", "Notice",
						JOptionPane.WARNING_MESSAGE);
				return false;
			}
			Map<String, Double> compatibility = readCompatibilityFromForm(domain.getRabId());
			Map<String, Double> maturation = new LinkedHashMap<String, Double>(pendingMaturation);
			List<String> errors = validator.validateUpdate(domain, compatibility);
			if (!errors.isEmpty()) {
				JOptionPane.showMessageDialog(this, joinErrors(errors), "Validation",
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			RabDomainCsvSupport.updateDomain(config, domain, compatibility, maturation);
			refreshDomainList();
			rebuildCompatFields(editingDomainId);
			selectDomainInList(editingDomainId);
			if (showSuccessMessage) {
				JOptionPane.showMessageDialog(this,
						"Domain " + editingDomainId + " updated in memory.\n"
								+ "Use Save CSV to write changes to the file.",
						"Domain updated", JOptionPane.INFORMATION_MESSAGE);
			}
			return true;
		} catch (IllegalArgumentException ex) {
			JOptionPane.showMessageDialog(this, ex.getMessage(), "Validation", JOptionPane.ERROR_MESSAGE);
			return false;
		}
	}

	private void loadDomainIntoForm(RabDomainDefinition domain) {
		editingDomainId = domain.getRabId();
		rabIdField.setText(domain.getRabId());
		rabIdField.setEnabled(false);
		displayNameField.setText(domain.getDisplayName());
		initCellField.setText(formatNumber(domain.getInitCellAmount()));
		tubuleTropismField.setText(formatNumber(domain.getTubuleTropism()));
		mtTubuleField.setText(formatNumber(domain.getMtTropismTubule()));
		mtRestField.setText(formatNumber(domain.getMtTropismRest()));
		recyProbField.setText(formatNumber(domain.getRabRecyProb()));
		pendingMaturation.clear();
		pendingMaturation.putAll(domain.getMaturationTo());
		refreshPendingMaturationList();
		rebuildCompatFields(editingDomainId);
		refreshMaturationTargets();
		updateSaveDomainButtonLabel();
	}

	private void clearFormForNewDomain() {
		editingDomainId = null;
		domainList.clearSelection();
		rabIdField.setEnabled(true);
		clearFormExceptDefaults();
		rebuildCompatFields(null);
		updateSaveDomainButtonLabel();
	}

	private void updateSaveDomainButtonLabel() {
		if (editingDomainId != null) {
			saveDomainButton.setText("Apply changes");
			saveDomainHintLabel.setText("Editing " + editingDomainId
					+ ". Change values and click Apply changes, then Save CSV.");
		} else {
			saveDomainButton.setText("Add domain to list");
			saveDomainHintLabel.setText(
					"Fill the form, then add the domain here before Save CSV.");
		}
	}

	private void selectDomainInList(String rabId) {
		int index = 0;
		for (String id : config.getRabDomains().keySet()) {
			if (id.equals(rabId)) {
				domainList.setSelectedIndex(index);
				return;
			}
			index++;
		}
	}

	private String formatNumber(double value) {
		if (value == (long) value) {
			return Long.toString((long) value);
		}
		return Double.toString(value);
	}

	private double lookupCompatibility(String rabA, String rabB) {
		Double value = config.getRabCompatibility().get(RabDomainDefinition.compatibilityKey(rabA, rabB));
		return value != null ? value : 0d;
	}

	private RabDomainDefinition buildDomainFromForm() {
		String rabId = rabIdField.getText().trim();
		RabDomainDefinition domain = new RabDomainDefinition(rabId);
		domain.setDisplayName(displayNameField.getText());
		domain.setInitCellAmount(parseDoubleField(initCellField.getText(), "initial cell amount"));
		domain.setTubuleTropism(parseDoubleField(tubuleTropismField.getText(), "tubule tropism"));
		domain.setMtTropismTubule(parseDoubleField(mtTubuleField.getText(), "MT tubule tropism"));
		domain.setMtTropismRest(parseDoubleField(mtRestField.getText(), "MT rest tropism"));
		domain.setRabRecyProb(parseDoubleField(recyProbField.getText(), "recycling probability"));
		domain.getMaturationTo().putAll(pendingMaturation);
		return domain;
	}

	private Map<String, Double> readCompatibilityFromForm(String newRabId) {
		Map<String, Double> compatibility = new LinkedHashMap<String, Double>();
		for (Map.Entry<String, JTextField> entry : compatFields.entrySet()) {
			if (entry.getKey().equals(newRabId)) {
				continue;
			}
			compatibility.put(entry.getKey(), parseDoubleField(entry.getValue().getText(),
					"compatibility with " + entry.getKey()));
		}
		return compatibility;
	}

	private void addPendingMaturation() {
		String target = (String) maturationTargetCombo.getSelectedItem();
		if (target == null || target.length() == 0) {
			JOptionPane.showMessageDialog(this, "Select a target domain.", "Notice",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		double rate = parseDoubleField(maturationRateField.getText(), "maturation rate");
		pendingMaturation.put(target, rate);
		refreshPendingMaturationList();
	}

	private void refreshPendingMaturationList() {
		maturationListModel.clear();
		for (Map.Entry<String, Double> entry : pendingMaturation.entrySet()) {
			maturationListModel.addElement("-> " + entry.getKey() + " : " + entry.getValue());
		}
	}

	private void deleteSelectedDomain() {
		if (config.getLines().isEmpty()) {
			JOptionPane.showMessageDialog(this, "Load a CSV file first.", "Notice",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		int index = domainList.getSelectedIndex();
		if (index < 0) {
			JOptionPane.showMessageDialog(this, "Select a domain to delete.", "Notice",
					JOptionPane.WARNING_MESSAGE);
			return;
		}
		String rabId = getDomainIdAtIndex(index);
		if (rabId == null) {
			return;
		}
		int choice = JOptionPane.showConfirmDialog(this,
				"Remove domain \"" + rabId + "\" from the configuration?\n"
						+ "Save the CSV to persist this change.",
				"Confirm delete", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
		if (choice != JOptionPane.OK_OPTION) {
			return;
		}
		RabDomainCsvSupport.unregisterDomain(config, rabId);
		if (rabId.equals(editingDomainId)) {
			clearFormForNewDomain();
			suggestNextRabId();
		}
		refreshDomainList();
		rebuildCompatFields(editingDomainId);
		refreshMaturationTargets();
	}

	private void suggestNextRabId() {
		int max = -1;
		for (String id : config.getRabDomains().keySet()) {
			if (id.startsWith("Rab") && id.length() == 4) {
				max = Math.max(max, id.charAt(3) - 'A');
			}
		}
		String candidate = "Rab" + (char) ('A' + max + 1);
		rabIdField.setText(candidate);
	}

	private void rebuildCompatFields(String forRabId) {
		compatPanel.removeAll();
		compatFields.clear();
		GridBagConstraints gc = new GridBagConstraints();
		gc.insets = new Insets(2, 2, 2, 8);
		gc.anchor = GridBagConstraints.WEST;
		int row = 0;
		for (RabDomainDefinition existing : config.getRabDomains().values()) {
			if (forRabId != null && existing.getRabId().equals(forRabId)) {
				continue;
			}
			JTextField field = new JTextField("0", 6);
			if (forRabId != null) {
				field.setText(formatNumber(lookupCompatibility(forRabId, existing.getRabId())));
			}
			compatFields.put(existing.getRabId(), field);
			gc.gridx = 0;
			gc.gridy = row;
			String label = existing.getRabId();
			if (existing.getDisplayName().length() > 0) {
				label = label + " (" + existing.getDisplayName() + ")";
			}
			compatPanel.add(new JLabel(label), gc);
			gc.gridx = 1;
			compatPanel.add(field, gc);
			row++;
		}
		if (row == 0) {
			gc.gridx = 0;
			gc.gridy = 0;
			compatPanel.add(new JLabel("No existing domains - first domain uses self-compatibility only."), gc);
		}
		compatPanel.revalidate();
		compatPanel.repaint();
	}

	private void refreshMaturationTargets() {
		maturationTargetModel.removeAllElements();
		for (RabDomainDefinition domain : config.getRabDomains().values()) {
			maturationTargetModel.addElement(domain.getRabId());
		}
		String typedId = rabIdField.getText().trim();
		if (typedId.matches("Rab[A-Z]") && maturationTargetModel.getIndexOf(typedId) < 0) {
			maturationTargetModel.addElement(typedId);
		}
	}

	private void refreshDomainList() {
		domainListModel.clear();
		for (RabDomainDefinition domain : config.getRabDomains().values()) {
			String label = domain.getRabId();
			if (domain.getDisplayName().length() > 0) {
				label = label + " - " + domain.getDisplayName();
			}
			domainListModel.addElement(label);
		}
	}

	private String getDomainIdAtIndex(int index) {
		int i = 0;
		for (String rabId : config.getRabDomains().keySet()) {
			if (i == index) {
				return rabId;
			}
			i++;
		}
		return null;
	}

	private void clearFormExceptDefaults() {
		displayNameField.setText("");
		initCellField.setText("1");
		tubuleTropismField.setText("1");
		mtTubuleField.setText("0");
		mtRestField.setText("0");
		recyProbField.setText("0");
		pendingMaturation.clear();
		refreshPendingMaturationList();
	}

	private double parseDoubleField(String text, String label) {
		try {
			return Double.parseDouble(text.trim());
		} catch (NumberFormatException ex) {
			throw new IllegalArgumentException("Invalid number in " + label + ".");
		}
	}

	private String joinErrors(List<String> errors) {
		StringBuilder sb = new StringBuilder();
		for (String error : errors) {
			sb.append("- ").append(error).append('\n');
		}
		return sb.toString();
	}
}
