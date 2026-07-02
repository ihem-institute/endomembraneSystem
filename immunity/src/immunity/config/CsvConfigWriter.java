package immunity.config;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CsvConfigWriter {

	public void write(SimulationConfig config, File file) throws IOException {
		List<String> output = new ArrayList<String>();
		boolean insertedTropism = false;

		for (String line : config.getLines()) {
			String rowType = CsvLineHelper.firstColumn(line);
			if ("membraneMet".equals(rowType)) {
				output.add(buildMembraneMetLine(config));
				continue;
			}
			if ("solubleMet".equals(rowType)) {
				output.add(buildSolubleMetLine(config));
				insertedTropism = false;
				continue;
			}
			if ("rabTropism".equals(rowType)) {
				if (!insertedTropism) {
					output.addAll(buildRabTropismLines(config));
					insertedTropism = true;
				}
				continue;
			}
			if ("uptakeRate".equals(rowType)) {
				output.add(buildRateLine("uptakeRate", config, true));
				continue;
			}
			if ("secretionRate".equals(rowType)) {
				output.add(buildRateLine("secretionRate", config, false));
				continue;
			}
			output.add(line);
		}

		RabDomainCsvSupport.applyDomainLinesToOutput(output, config);

		BufferedWriter writer = new BufferedWriter(
				new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8));
		try {
			for (String line : output) {
				writer.write(line);
				writer.newLine();
			}
		} finally {
			writer.close();
		}

		config.getLines().clear();
		config.getLines().addAll(output);
	}

	private String buildMembraneMetLine(SimulationConfig config) {
		List<String> fields = new ArrayList<String>();
		fields.add("membraneMet");
		for (CargoDefinition cargo : config.getCargos().values()) {
			if (cargo.getType() == CargoType.MEMBRANE) {
				fields.add(cargo.getName());
			}
		}
		return CsvLineHelper.joinPadded(fields);
	}

	private String buildSolubleMetLine(SimulationConfig config) {
		List<String> fields = new ArrayList<String>();
		fields.add("solubleMet");
		for (CargoDefinition cargo : config.getCargos().values()) {
			if (cargo.getType() == CargoType.SOLUBLE) {
				fields.add(cargo.getName());
			}
		}
		return CsvLineHelper.joinPadded(fields);
	}

	private String buildRateLine(String label, SimulationConfig config, boolean uptake) {
		List<String> fields = new ArrayList<String>();
		fields.add(label);
		for (CargoDefinition cargo : config.getCargos().values()) {
			if (cargo.getType() != CargoType.MEMBRANE) {
				continue;
			}
			double value = uptake ? cargo.getUptakeRate() : cargo.getSecretionRate();
			fields.add(cargo.getName());
			fields.add(formatNumber(value));
		}
		return CsvLineHelper.joinPadded(fields);
	}

	private List<String> buildRabTropismLines(SimulationConfig config) {
		List<String> lines = new ArrayList<String>();
		for (CargoDefinition cargo : config.getCargos().values()) {
			List<String> fields = new ArrayList<String>();
			fields.add("rabTropism");
			fields.add(cargo.getName());
			fields.addAll(cargo.getRabTropismTags());
			lines.add(CsvLineHelper.joinPadded(fields));
		}
		return lines;
	}

	private String formatNumber(double value) {
		if (value == (long) value) {
			return Long.toString((long) value);
		}
		return Double.toString(value);
	}
}
