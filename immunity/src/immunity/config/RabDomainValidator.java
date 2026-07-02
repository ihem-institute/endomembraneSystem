package immunity.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RabDomainValidator {

	public List<String> validate(RabDomainDefinition domain, SimulationConfig config,
			Map<String, Double> compatibilityWithExisting) {
		return validate(domain, config, compatibilityWithExisting, true);
	}

	public List<String> validateUpdate(RabDomainDefinition domain,
			Map<String, Double> compatibilityWithExisting) {
		List<String> errors = new ArrayList<String>();
		validateCommon(domain, compatibilityWithExisting, errors);
		return errors;
	}

	public List<String> validate(RabDomainDefinition domain, SimulationConfig config,
			Map<String, Double> compatibilityWithExisting, boolean isNew) {
		List<String> errors = new ArrayList<String>();

		if (!domain.getRabId().matches("Rab[A-Z]")) {
			errors.add("Rab ID must look like RabA, RabB, ... RabZ.");
		}
		if (isNew && config.hasRabDomain(domain.getRabId())) {
			errors.add("A domain with this Rab ID already exists: " + domain.getRabId());
		}
		validateCommon(domain, compatibilityWithExisting, errors);
		return errors;
	}

	private void validateCommon(RabDomainDefinition domain, Map<String, Double> compatibilityWithExisting,
			List<String> errors) {
		if (domain.getDisplayName().length() == 0) {
			errors.add("Display name (organelle label) is required, e.g. EE or ERGIC.");
		}
		if (domain.getDisplayName().contains(",")) {
			errors.add("Display name cannot contain commas.");
		}
		if (domain.getInitCellAmount() < 0d) {
			errors.add("Initial cell amount must be >= 0.");
		}
		for (Map.Entry<String, Double> entry : compatibilityWithExisting.entrySet()) {
			if (entry.getValue() < 0d || entry.getValue() > 1d) {
				errors.add("Compatibility with " + entry.getKey() + " must be between 0 and 1.");
			}
		}
		for (Map.Entry<String, Double> entry : domain.getMaturationTo().entrySet()) {
			if (entry.getValue() < 0d || entry.getValue() > 1d) {
				errors.add("Maturation rate to " + entry.getKey() + " must be between 0 and 1.");
			}
			if (entry.getKey().equals(domain.getRabId())) {
				errors.add("A domain cannot mature into itself.");
			}
		}
	}
}
