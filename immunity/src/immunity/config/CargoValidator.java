package immunity.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CargoValidator {

	public List<String> validate(CargoDefinition cargo, SimulationConfig config) {
		return validate(cargo, config, true);
	}

	public List<String> validateUpdate(CargoDefinition cargo) {
		List<String> errors = new ArrayList<String>();
		if (cargo.getName().length() == 0) {
			errors.add("Cargo name cannot be empty.");
		}
		if (cargo.getType() == CargoType.MEMBRANE) {
			if (cargo.getUptakeRate() < 0d || cargo.getSecretionRate() < 0d) {
				errors.add("Uptake and secretion rates must be >= 0.");
			}
		} else if (cargo.getUptakeRate() != 0d || cargo.getSecretionRate() != 0d) {
			errors.add("Soluble cargos do not use uptake or secretion rates in the CSV.");
		}
		validateTropismTags(cargo, errors);
		return errors;
	}

	public List<String> validate(CargoDefinition cargo, SimulationConfig config, boolean isNew) {
		List<String> errors = new ArrayList<String>();

		if (cargo.getName().length() == 0) {
		 errors.add("Cargo name cannot be empty.");
		}
		if (cargo.getName().contains(",")) {
			errors.add("Cargo name cannot contain commas.");
		}
		if (isNew && config.hasCargo(cargo.getName())) {
			errors.add("A cargo with this name already exists: " + cargo.getName());
		}
		if (isNew && !cargo.getName().endsWith("En") && !cargo.getName().endsWith("Cy")
				&& !cargo.getName().endsWith("Pm")) {
			errors.add("Name should end in En, Cy, or Pm (model convention).");
		}
		if (cargo.getType() == CargoType.MEMBRANE) {
			if (cargo.getUptakeRate() < 0d || cargo.getSecretionRate() < 0d) {
				errors.add("Uptake and secretion rates must be >= 0.");
			}
		} else if (cargo.getUptakeRate() != 0d || cargo.getSecretionRate() != 0d) {
			errors.add("Soluble cargos do not use uptake or secretion rates in the CSV.");
		}

		validateTropismTags(cargo, errors);
		return errors;
	}

	private void validateTropismTags(CargoDefinition cargo, List<String> errors) {
		Set<String> tags = new HashSet<String>(cargo.getRabTropismTags());
		if (tags.contains("mvb") && tags.contains("noMvb")) {
			errors.add("A cargo cannot have both mvb and noMvb.");
		}
		if (cargo.getType() == CargoType.SOLUBLE && tags.contains("sph")
				&& (tags.contains("mvb") || tags.contains("noMvb"))) {
			errors.add("sph applies to soluble cargos; mvb/noMvb apply to membrane cargos.");
		}
	}
}
