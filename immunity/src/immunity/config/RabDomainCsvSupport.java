package immunity.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class RabDomainCsvSupport {

	private RabDomainCsvSupport() {
	}

	static void loadDomains(SimulationConfig config, List<String[]> parsedRows) {
		List<String> rabOrder = new ArrayList<String>();
		Map<String, String> displayNames = new LinkedHashMap<String, String>();
		Map<String, Double> initCell = new LinkedHashMap<String, Double>();
		Map<String, Double> tubule = new LinkedHashMap<String, Double>();
		Map<String, Double> mtTubule = new LinkedHashMap<String, Double>();
		Map<String, Double> mtRest = new LinkedHashMap<String, Double>();
		Map<String, Double> recy = new LinkedHashMap<String, Double>();
		Map<String, Double> compatibility = new LinkedHashMap<String, Double>();
		Map<String, Double> maturation = new LinkedHashMap<String, Double>();

		for (String[] parts : parsedRows) {
			if (parts.length == 0) {
				continue;
			}
			String rowType = CsvLineHelper.stripBom(parts[0].trim());
			if ("rabSet".equals(rowType)) {
				rabOrder.addAll(CsvLineHelper.parseNameList(parts, 1));
			} else if ("organelle".equals(rowType)) {
				parseStringPairs(parts, 1, displayNames);
			} else if ("initRabCell".equals(rowType)) {
				parseDoublePairs(parts, 1, initCell);
			} else if ("tubuleTropism".equals(rowType)) {
				parseDoublePairs(parts, 1, tubule);
			} else if ("mtTropismTubule".equals(rowType)) {
				parseDoublePairs(parts, 1, mtTubule);
			} else if ("mtTropismRest".equals(rowType)) {
				parseDoublePairs(parts, 1, mtRest);
			} else if ("rabRecyProb".equals(rowType)) {
				parseDoublePairs(parts, 1, recy);
			} else if ("rabCompatibility".equals(rowType)) {
				parseDoublePairs(parts, 1, compatibility);
			} else if ("rabMaturation".equals(rowType)) {
				parseDoublePairs(parts, 1, maturation);
			}
		}

		config.getRabDomains().clear();
		config.getRabCompatibility().clear();
		config.getRabMaturation().clear();
		config.getRabCompatibility().putAll(compatibility);
		config.getRabMaturation().putAll(maturation);

		for (String rabId : rabOrder) {
			RabDomainDefinition domain = new RabDomainDefinition(rabId);
			if (displayNames.containsKey(rabId)) {
				domain.setDisplayName(displayNames.get(rabId));
			}
			if (initCell.containsKey(rabId)) {
				domain.setInitCellAmount(initCell.get(rabId));
			}
			if (tubule.containsKey(rabId)) {
				domain.setTubuleTropism(tubule.get(rabId));
			}
			if (mtTubule.containsKey(rabId)) {
				domain.setMtTropismTubule(mtTubule.get(rabId));
			}
			if (mtRest.containsKey(rabId)) {
				domain.setMtTropismRest(mtRest.get(rabId));
			}
			if (recy.containsKey(rabId)) {
				domain.setRabRecyProb(recy.get(rabId));
			}
			for (Map.Entry<String, Double> entry : maturation.entrySet()) {
				String key = entry.getKey();
				if (key.startsWith(rabId) && key.length() > rabId.length()) {
					domain.getMaturationTo().put(key.substring(rabId.length()), entry.getValue());
				}
			}
			config.getRabDomains().put(rabId, domain);
		}
	}

	static void registerDomain(SimulationConfig config, RabDomainDefinition domain,
			Map<String, Double> compatibilityWithExisting) {
		config.addRabDomain(domain);
		String rabId = domain.getRabId();
		config.getRabCompatibility().put(RabDomainDefinition.compatibilityKey(rabId, rabId), 1d);
		for (Map.Entry<String, Double> entry : compatibilityWithExisting.entrySet()) {
			config.getRabCompatibility().put(
					RabDomainDefinition.compatibilityKey(rabId, entry.getKey()), entry.getValue());
		}
		for (Map.Entry<String, Double> entry : domain.getMaturationTo().entrySet()) {
			config.getRabMaturation().put(rabId + entry.getKey(), entry.getValue());
		}
		ConfigHolder.markDomainEdits();
	}

	static void updateDomain(SimulationConfig config, RabDomainDefinition domain,
			Map<String, Double> compatibilityWithExisting, Map<String, Double> maturationTo) {
		String rabId = domain.getRabId();
		if (!config.hasRabDomain(rabId)) {
			throw new IllegalArgumentException("Domain not found: " + rabId);
		}
		RabDomainDefinition existing = config.getRabDomains().get(rabId);
		existing.setDisplayName(domain.getDisplayName());
		existing.setInitCellAmount(domain.getInitCellAmount());
		existing.setTubuleTropism(domain.getTubuleTropism());
		existing.setMtTropismTubule(domain.getMtTropismTubule());
		existing.setMtTropismRest(domain.getMtTropismRest());
		existing.setRabRecyProb(domain.getRabRecyProb());
		existing.getMaturationTo().clear();
		existing.getMaturationTo().putAll(maturationTo);

		List<String> compatKeys = new ArrayList<String>();
		for (String key : config.getRabCompatibility().keySet()) {
			if (key.contains(rabId)) {
				compatKeys.add(key);
			}
		}
		for (String key : compatKeys) {
			config.getRabCompatibility().remove(key);
		}
		config.getRabCompatibility().put(RabDomainDefinition.compatibilityKey(rabId, rabId), 1d);
		for (Map.Entry<String, Double> entry : compatibilityWithExisting.entrySet()) {
			config.getRabCompatibility().put(
					RabDomainDefinition.compatibilityKey(rabId, entry.getKey()), entry.getValue());
		}

		List<String> maturationKeys = new ArrayList<String>();
		for (String key : config.getRabMaturation().keySet()) {
			if (key.startsWith(rabId)) {
				maturationKeys.add(key);
			}
		}
		for (String key : maturationKeys) {
			config.getRabMaturation().remove(key);
		}
		for (Map.Entry<String, Double> entry : maturationTo.entrySet()) {
			config.getRabMaturation().put(rabId + entry.getKey(), entry.getValue());
		}
		ConfigHolder.markDomainEdits();
	}

	static void unregisterDomain(SimulationConfig config, String rabId) {
		config.removeRabDomain(rabId);
		List<String> compatKeys = new ArrayList<String>();
		for (String key : config.getRabCompatibility().keySet()) {
			if (key.contains(rabId)) {
				compatKeys.add(key);
			}
		}
		for (String key : compatKeys) {
			config.getRabCompatibility().remove(key);
		}
		List<String> maturationKeys = new ArrayList<String>();
		for (String key : config.getRabMaturation().keySet()) {
			if (key.startsWith(rabId) || key.endsWith(rabId)) {
				maturationKeys.add(key);
			}
		}
		for (String key : maturationKeys) {
			config.getRabMaturation().remove(key);
		}
		ConfigHolder.markDomainEdits();
	}

	static List<String> buildCompatibilityLines(SimulationConfig config) {
		return buildSplitPairLines("rabCompatibility", config.getRabCompatibility());
	}

	static List<String> buildMaturationLines(SimulationConfig config) {
		return buildSplitPairLines("rabMaturation", config.getRabMaturation());
	}

	static String buildInitRabCellLine(SimulationConfig config) {
		return buildDomainPairLine("initRabCell", config, DomainValue.INIT_CELL);
	}

	static String buildRabSetLine(SimulationConfig config) {
		return buildNameLine("rabSet", config);
	}

	static String buildTubuleTropismLine(SimulationConfig config) {
		return buildDomainPairLine("tubuleTropism", config, DomainValue.TUBULE);
	}

	static String buildMtTropismTubuleLine(SimulationConfig config) {
		return buildDomainPairLine("mtTropismTubule", config, DomainValue.MT_TUBULE);
	}

	static String buildMtTropismRestLine(SimulationConfig config) {
		return buildDomainPairLine("mtTropismRest", config, DomainValue.MT_REST);
	}

	static String buildRabRecyProbLine(SimulationConfig config) {
		return buildDomainPairLine("rabRecyProb", config, DomainValue.RECY);
	}

	static String buildOrganelleLine(SimulationConfig config) {
		return buildOrganelleLineInternal(config);
	}

	static void applyDomainLinesToOutput(List<String> output, SimulationConfig config) {
		if (config.getRabDomains().isEmpty()) {
			return;
		}
		replaceRows(output, "initRabCell", singleLine(buildInitRabCellLine(config)));
		replaceRows(output, "rabCompatibility", buildCompatibilityLines(config));
		replaceRows(output, "rabMaturation", buildMaturationLines(config));
		replaceRows(output, "rabSet", singleLine(buildRabSetLine(config)));
		replaceRows(output, "tubuleTropism", singleLine(buildTubuleTropismLine(config)));
		replaceRows(output, "mtTropismTubule", singleLine(buildMtTropismTubuleLine(config)));
		replaceRows(output, "mtTropismRest", singleLine(buildMtTropismRestLine(config)));
		replaceRows(output, "rabRecyProb", singleLine(buildRabRecyProbLine(config)));
		replaceRows(output, "organelle", singleLine(buildOrganelleLine(config)));
	}

	private static List<String> singleLine(String line) {
		List<String> lines = new ArrayList<String>();
		lines.add(line);
		return lines;
	}

	private static void replaceRows(List<String> output, String rowType, List<String> newLines) {
		if (newLines.isEmpty()) {
			return;
		}
		int insertAt = -1;
		for (int i = output.size() - 1; i >= 0; i--) {
			if (rowType.equals(CsvLineHelper.firstColumn(output.get(i)))) {
				if (insertAt < 0) {
					insertAt = i;
				}
				output.remove(i);
			}
		}
		if (insertAt < 0) {
			output.addAll(newLines);
			return;
		}
		output.addAll(insertAt, newLines);
	}

	static List<String> buildDomainLines(SimulationConfig config) {
		List<String> lines = new ArrayList<String>();
		if (config.getRabDomains().isEmpty()) {
			return lines;
		}
		lines.add(buildRabSetLine(config));
		lines.add(buildInitRabCellLine(config));
		lines.addAll(buildCompatibilityLines(config));
		lines.addAll(buildMaturationLines(config));
		lines.add(buildTubuleTropismLine(config));
		lines.add(buildMtTropismTubuleLine(config));
		lines.add(buildMtTropismRestLine(config));
		lines.add(buildRabRecyProbLine(config));
		lines.add(buildOrganelleLine(config));
		return lines;
	}

	private enum DomainValue {
		INIT_CELL, TUBULE, MT_TUBULE, MT_REST, RECY
	}

	private static void parseStringPairs(String[] parts, int startIndex, Map<String, String> target) {
		for (int i = startIndex; i + 1 < parts.length; i = i + 2) {
			String key = parts[i].trim();
			if (key.length() == 0) {
				break;
			}
			target.put(key, parts[i + 1].trim());
		}
	}

	private static void parseDoublePairs(String[] parts, int startIndex, Map<String, Double> target) {
		for (int i = startIndex; i + 1 < parts.length; i = i + 2) {
			String key = parts[i].trim();
			if (key.length() == 0) {
				break;
			}
			target.put(key, CsvLineHelper.parseDouble(parts, i + 1, 0d));
		}
	}

	private static String buildNameLine(String rowType, SimulationConfig config) {
		List<String> fields = new ArrayList<String>();
		fields.add(rowType);
		for (RabDomainDefinition domain : config.getRabDomains().values()) {
			fields.add(domain.getRabId());
		}
		return CsvLineHelper.joinPadded(fields);
	}

	private static String buildOrganelleLineInternal(SimulationConfig config) {
		List<String> fields = new ArrayList<String>();
		fields.add("organelle");
		for (RabDomainDefinition domain : config.getRabDomains().values()) {
			fields.add(domain.getRabId());
			fields.add(domain.getDisplayName());
		}
		return CsvLineHelper.joinPadded(fields);
	}

	private static String buildDomainPairLine(String rowType, SimulationConfig config, DomainValue valueType) {
		List<String> fields = new ArrayList<String>();
		fields.add(rowType);
		for (RabDomainDefinition domain : config.getRabDomains().values()) {
			fields.add(domain.getRabId());
			fields.add(formatNumber(readValue(domain, valueType)));
		}
		return CsvLineHelper.joinPadded(fields);
	}

	private static double readValue(RabDomainDefinition domain, DomainValue valueType) {
		switch (valueType) {
		case INIT_CELL:
			return domain.getInitCellAmount();
		case TUBULE:
			return domain.getTubuleTropism();
		case MT_TUBULE:
			return domain.getMtTropismTubule();
		case MT_REST:
			return domain.getMtTropismRest();
		case RECY:
			return domain.getRabRecyProb();
		default:
			return 0d;
		}
	}

	private static List<String> buildSplitPairLines(String rowType, Map<String, Double> values) {
		List<String> lines = new ArrayList<String>();
		List<String> fields = new ArrayList<String>();
		fields.add(rowType);
		for (Map.Entry<String, Double> entry : values.entrySet()) {
			if (fields.size() + 2 > CsvLineHelper.TARGET_COLUMNS) {
				lines.add(CsvLineHelper.joinPadded(fields));
				fields = new ArrayList<String>();
				fields.add(rowType);
			}
			fields.add(entry.getKey());
			fields.add(formatNumber(entry.getValue()));
		}
		if (fields.size() > 1) {
			lines.add(CsvLineHelper.joinPadded(fields));
		}
		return lines;
	}

	private static String formatNumber(double value) {
		if (value == (long) value) {
			return Long.toString((long) value);
		}
		return Double.toString(value);
	}
}
