package io.kestros.commons.validation.core.services;


import io.kestros.commons.validation.api.models.ModelValidator;
import java.util.List;
import java.util.Map;

public interface ModelValidationActivateStatusService {

  Map<Class, Map<String, Boolean>> getValidatorActivationStatusMap();


  List<ModelValidator> getActiveValidators(List<ModelValidator> validators, Class type);


  boolean isModelValidatorActiveForClass(ModelValidator validator, Class type);

  void activateValidator(ModelValidator validator, Class type);

  void deactivateValidator(ModelValidator validator, Class type);
}
