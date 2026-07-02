package immunity.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CsvConfigReader {

	public SimulationConfig read(File file) throws IOException {
		SimulationConfig config = new SimulationConfig();
		config.setSourcePath(file.getPath());

		Map<String, Double> uptakeRates = new LinkedHashMap<String, Double>();
		Map<String, Double> secretionRates = new LinkedHashMap<String, Double>();
		Map<String, List<String>> tropismByCargo = new LinkedHashMap<String, List<String>>();
		List<String> membraneNames = new ArrayList<String>();
		List<String> solubleNames = new ArrayList<String>();
		List<String[]> parsedRows = new ArrayList<String[]>();

		BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
		try {
			String line;
			boolean firstLine = true;
			while ((line = reader.readLine()) != null) {
				if (firstLine) {
					line = CsvLineHelper.stripBom(line);
					firstLine = false;
				}
				config.getLines().add(line);
				String[] parts = CsvLineHelper.splitLine(line);
				parsedRows.add(parts);
				if (parts.length == 0) {
					continue;
				}
				String rowType = CsvLineHelper.stripBom(parts[0].trim());
				if ("membraneMet".equals(rowType)) {
					membraneNames.addAll(CsvLineHelper.parseNameList(parts, 1));
				} else if ("solubleMet".equals(rowType)) {
					solubleNames.addAll(CsvLineHelper.parseNameList(parts, 1));
				} else if ("uptakeRate".equals(rowType)) {
					for (int i = 1; i + 1 < parts.length; i = i + 2) {
						String key = parts[i].trim();
						if (key.length() == 0) {
							break;
						}
						uptakeRates.put(key, CsvLineHelper.parseDouble(parts, i + 1, 0d));
					}
				} else if ("secretionRate".equals(rowType)) {
					for (int i = 1; i + 1 < parts.length; i = i + 2) {
						String key = parts[i].trim();
						if (key.length() == 0) {
							break;
						}
						secretionRates.put(key, CsvLineHelper.parseDouble(parts, i + 1, 0d));
					}
				} else if ("rabTropism".equals(rowType) && parts.length > 1) {
					String cargoName = parts[1].trim();
					if (cargoName.length() > 0) {
						tropismByCargo.put(cargoName, CsvLineHelper.parseNameList(parts, 2));
					}
				}
			}
		} finally {
			reader.close();
		}

		for (String name : membraneNames) {
			config.addCargo(buildCargo(name, CargoType.MEMBRANE, uptakeRates, secretionRates, tropismByCargo));
		}
		for (String name : solubleNames) {
			if (!config.hasCargo(name)) {
				config.addCargo(buildCargo(name, CargoType.SOLUBLE, uptakeRates, secretionRates, tropismByCargo));
			}
		}
		RabDomainCsvSupport.loadDomains(config, parsedRows);
		return config;
	}

	private CargoDefinition buildCargo(String name, CargoType type, Map<String, Double> uptakeRates,
			Map<String, Double> secretionRates, Map<String, List<String>> tropismByCargo) {
		double uptake = uptakeRates.containsKey(name) ? uptakeRates.get(name) : 0d;
		double secretion = secretionRates.containsKey(name) ? secretionRates.get(name) : 0d;
		List<String> tropism = tropismByCargo.containsKey(name) ? tropismByCargo.get(name) : new ArrayList<String>();
		return new CargoDefinition(name, type, uptake, secretion, tropism);
	}
}
