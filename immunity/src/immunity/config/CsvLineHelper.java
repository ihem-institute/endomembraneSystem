package immunity.config;

import java.util.ArrayList;
import java.util.List;

final class CsvLineHelper {

	static final int TARGET_COLUMNS = 20;

	private CsvLineHelper() {
	}

	static String[] splitLine(String line) {
		return line.split(",", -1);
	}

	static String firstColumn(String line) {
		String[] parts = splitLine(line);
		return parts.length > 0 ? stripBom(parts[0].trim()) : "";
	}

	static String stripBom(String value) {
		if (value.length() > 0 && value.charAt(0) == '\uFEFF') {
			return value.substring(1);
		}
		return value;
	}

	static String joinPadded(List<String> fields) {
		return joinFields(fields, true);
	}

	static String joinFields(List<String> fields, boolean padToMinimum) {
		List<String> copy = new ArrayList<String>(fields);
		if (padToMinimum) {
			while (copy.size() < TARGET_COLUMNS) {
				copy.add("");
			}
		}
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < copy.size(); i++) {
			if (i > 0) {
				sb.append(',');
			}
			sb.append(copy.get(i));
		}
		return sb.toString();
	}

	static List<String> parseNameList(String[] parts, int startIndex) {
		List<String> names = new ArrayList<String>();
		for (int i = startIndex; i < parts.length; i++) {
			String value = parts[i].trim();
			if (value.length() > 0) {
				names.add(value);
			}
		}
		return names;
	}

	static List<String> parseKeyValuePairs(String[] parts, int startIndex) {
		List<String> keys = new ArrayList<String>();
		for (int i = startIndex; i + 1 < parts.length; i = i + 2) {
			String key = parts[i].trim();
			if (key.length() == 0) {
				break;
			}
			keys.add(key);
		}
		return keys;
	}

	static double parseDouble(String[] parts, int index, double defaultValue) {
		if (index >= parts.length) {
			return defaultValue;
		}
		String value = parts[index].trim();
		if (value.length() == 0) {
			return defaultValue;
		}
		return Double.parseDouble(value);
	}
}
