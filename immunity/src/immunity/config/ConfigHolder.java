package immunity.config;

/**
 * Shared in-memory configuration for all config UI tabs.
 */
public final class ConfigHolder {

	private static final SimulationConfig INSTANCE = new SimulationConfig();
	private static boolean domainEditsPending = false;
	private static int loadGeneration = 0;

	private ConfigHolder() {
	}

	public static SimulationConfig get() {
		return INSTANCE;
	}

	public static void replaceFrom(SimulationConfig loaded) {
		INSTANCE.getLines().clear();
		INSTANCE.getLines().addAll(loaded.getLines());
		INSTANCE.getCargos().clear();
		INSTANCE.getCargos().putAll(loaded.getCargos());
		INSTANCE.getRabDomains().clear();
		INSTANCE.getRabDomains().putAll(loaded.getRabDomains());
		INSTANCE.getRabCompatibility().clear();
		INSTANCE.getRabCompatibility().putAll(loaded.getRabCompatibility());
		INSTANCE.getRabMaturation().clear();
		INSTANCE.getRabMaturation().putAll(loaded.getRabMaturation());
		INSTANCE.setSourcePath(loaded.getSourcePath());
		loadGeneration++;
	}

	public static int getLoadGeneration() {
		return loadGeneration;
	}

	public static void markDomainEdits() {
		domainEditsPending = true;
	}

	public static void clearDomainEdits() {
		domainEditsPending = false;
	}

	public static boolean hasDomainEdits() {
		return domainEditsPending;
	}
}
