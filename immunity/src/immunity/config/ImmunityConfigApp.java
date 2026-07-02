package immunity.config;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * Edit cargos and Rab domains in InputIntrTransp3.csv.
 * <p>
 * Eclipse: Run the launch config "Immunity Config Editor" (see launchers/).
 * Or Run As &gt; Java Application on this class.
 */
public class ImmunityConfigApp {

	private static JFrame frame;

	public static void main(String[] args) {
		open();
	}

	/**
	 * Opens the config editor, or brings the existing window to the front.
	 */
	public static void open() {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (frame != null && frame.isDisplayable()) {
					frame.setVisible(true);
					frame.toFront();
					frame.requestFocus();
					return;
				}
				frame = createFrame();
				frame.setVisible(true);
			}
		});
	}

	private static JFrame createFrame() {
		JLabel fileLabel = new JLabel("No file loaded");
		AddCargoPanel cargoPanel = new AddCargoPanel();
		AddRabDomainPanel rabPanel = new AddRabDomainPanel();
		ConfigCsvController csvController = new ConfigCsvController(fileLabel, cargoPanel, rabPanel);

		JTabbedPane tabs = new JTabbedPane();
		tabs.addTab("Cargo", cargoPanel);
		tabs.addTab("Rab domain", rabPanel);
		tabs.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				csvController.syncPanelsIfStale();
			}
		});

		JButton loadButton = new JButton("Load InputIntrTransp3.csv...");
		JButton saveButton = new JButton("Save CSV...");

		JPanel toolbar = new JPanel(new BorderLayout(8, 8));
		toolbar.add(fileLabel, BorderLayout.CENTER);
		JPanel buttons = new JPanel();
		buttons.add(loadButton);
		buttons.add(saveButton);
		toolbar.add(buttons, BorderLayout.EAST);

		JFrame newFrame = new JFrame("Immunity - Configuration (MVP)");
		loadButton.addActionListener(e -> csvController.loadCsv(newFrame));
		saveButton.addActionListener(e -> csvController.saveCsv(newFrame));
		newFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		newFrame.getContentPane().add(toolbar, BorderLayout.NORTH);
		newFrame.getContentPane().add(tabs, BorderLayout.CENTER);
		newFrame.setSize(980, 680);
		newFrame.setLocationRelativeTo(null);
		newFrame.addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosed(java.awt.event.WindowEvent e) {
				if (newFrame == frame) {
					frame = null;
				}
			}
		});
		return newFrame;
	}
}
