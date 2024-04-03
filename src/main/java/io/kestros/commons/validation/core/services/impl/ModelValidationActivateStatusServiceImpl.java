package io.kestros.commons.validation.core.services.impl;

import io.kestros.commons.validation.api.models.ModelValidator;
import io.kestros.commons.validation.core.services.ModelValidationActivateStatusService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

@Component(immediate = true,
        service = ModelValidationActivateStatusService.class)
public class ModelValidationActivateStatusServiceImpl implements
        ModelValidationActivateStatusService {

  private Map<Class, Map<String, Boolean>> validatorActivationStatusMap;

  @Activate
  public void activate() {
    validatorActivationStatusMap = new HashMap<>();
  }
  @Override
  public Map<Class, Map<String, Boolean>> getValidatorActivationStatusMap() {
    return validatorActivationStatusMap;
  }


  @Override
  public List<ModelValidator> getActiveValidators(List<ModelValidator> validators, Class type) {
    List<ModelValidator> activeValidators = new ArrayList<>();
    for (ModelValidator validator : validators) {
      if (isModelValidatorActiveForClass(validator, type)) {
        activeValidators.add(validator);
      }
    }
    return activeValidators;
  }

  @Override
  public boolean isModelValidatorActiveForClass(ModelValidator validator, Class type) {
    Map<String, Boolean> typeMap = validatorActivationStatusMap.get(type);
    if (typeMap != null) {
      return typeMap.get(validator.getClass().getName());
    }
    return true;
  }

  @Override
  public void activateValidator(ModelValidator validator, Class type) {
    if(validatorActivationStatusMap.get(type) == null) {
      validatorActivationStatusMap.put(type, new HashMap<>());
    }
    validatorActivationStatusMap.get(type).put(validator.getClass().getName(), true);
  }

  @Override
  public void deactivateValidator(ModelValidator validator, Class type) {
    validatorActivationStatusMap.get(type).put(validator.getClass().getName(), false);
  }
}
