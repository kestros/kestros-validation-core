package io.kestros.commons.validation.core.services;

import io.kestros.commons.osgiserviceutils.services.ManagedService;
import io.kestros.commons.validation.api.models.ModelValidator;
import java.util.List;

public interface ModelValidatorProviderService extends ManagedService {

  void activateValidator(ModelValidator validator, Class type);

  void deactivateValidator(ModelValidator validator, Class type);

  List<ModelValidator> getAllValidators(Class type);

  List<ModelValidator> getActiveValidators(Class type);

  List<ModelValidator> getInactiveValidators(Class type);
}
