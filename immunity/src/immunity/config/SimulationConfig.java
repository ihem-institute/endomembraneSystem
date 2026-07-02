package immunity.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimulationConfig {

	private final List<String> lines = new ArrayList<String>();
	private final Map<String, CargoDefinition> cargos = new LinkedHashMap<String, CargoDefinition>();
	private final Map<String, RabDomainDefinition> rabDomains = new LinkedHashMap<String, RabDomainDefinition>();
	private final Map<String, Double> rabCompatibility = new LinkedHashMap<String, Double>();
	private final Map<String, Double> rabMaturation = new LinkedHashMap<String, Double>();
	private String sourcePath;

	public List<String> getLines() {
		return lines;
	}

	public Map<String, CargoDefinition> getCargos() {
		return cargos;
	}

	public Map<String, RabDomainDefinition> getRabDomains() {
		return rabDomains;
	}

	public Map<String, Double> getRabCompatibility() {
		return rabCompatibility;
	}

	public Map<String, Double> getRabMaturation() {
		return rabMaturation;
	}

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public void addCargo(CargoDefinition cargo) {
		cargos.put(cargo.getName(), cargo);
	}

	public CargoDefinition removeCargo(String name) {
		return cargos.remove(name);
	}

	public boolean hasCargo(String name) {
		return cargos.containsKey(name);
	}

	public void addRabDomain(RabDomainDefinition domain) {
		rabDomains.put(domain.getRabId(), domain);
	}

	public RabDomainDefinition removeRabDomain(String rabId) {
		return rabDomains.remove(rabId);
	}

	public boolean hasRabDomain(String rabId) {
		return rabDomains.containsKey(rabId);
	}
}
