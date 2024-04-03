package io.kestros.commons.validation.core.services.impl;

import io.kestros.commons.validation.api.models.ModelValidator;
import io.kestros.commons.validation.api.services.ModelValidatorRegistrationHandlerService;
import io.kestros.commons.validation.core.services.ModelValidationActivateStatusService;
import io.kestros.commons.validation.core.services.ModelValidatorProviderService;
import java.util.ArrayList;
import java.util.List;
import org.apache.felix.hc.api.FormattingResultLog;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model Validator Provider Service.
 */
@Component(immediate = true,
        service = ModelValidatorProviderService.class,
        property = "service.ranking:Integer=100")
public class ModelValidatorProviderServiceImpl implements ModelValidatorProviderService {

  private static final Logger LOG = LoggerFactory.getLogger(
          ModelValidatorProviderServiceImpl.class);

  @Reference(cardinality = ReferenceCardinality.OPTIONAL,
          policyOption = ReferencePolicyOption.GREEDY)
  private ModelValidatorRegistrationHandlerService registrationHandlerService;

  @Reference(cardinality = ReferenceCardinality.OPTIONAL,
          policyOption = ReferencePolicyOption.GREEDY)
  private ModelValidationActivateStatusService modelValidationActivateStatusService;

  @Override
  public String getDisplayName() {
    return "Model Validator Provider Service";
  }

  @Override
  public void activate(ComponentContext componentContext) {

  }

  @Override
  public void deactivate(ComponentContext componentContext) {

  }

  @Override
  public void runAdditionalHealthChecks(FormattingResultLog log) {

  }

  @Override
  public void activateValidator(ModelValidator validator, Class type) {
    if (registrationHandlerService != null) {
      List<ModelValidator> registeredModelValidators
              = registrationHandlerService.getRegisteredModelValidatorMap().get(type);

      for (ModelValidator registeredModelValidator : registeredModelValidators) {
        if (registeredModelValidator.getMessage().equals(
                validator.getMessage())) {
          modelValidationActivateStatusService.activateValidator(registeredModelValidator, type);
        }
      }
    } else {
      LOG.error("Unable to activate validator {}. RegistrationHandlerService was null.",
              validator.getMessage());
    }
  }

  @Override
  public void deactivateValidator(ModelValidator validator, Class type) {
    if (registrationHandlerService != null) {
      List<ModelValidator> registeredModelValidators
              = registrationHandlerService.getRegisteredModelValidatorMap().get(type);

      for (ModelValidator registeredModelValidator : registeredModelValidators) {
        if (registeredModelValidator.getMessage().equals(
                validator.getMessage())) {
          modelValidationActivateStatusService.deactivateValidator(registeredModelValidator, type);
        }
      }
    } else {
      LOG.error("Unable to deactivate validator {}. RegistrationHandlerService was null.",
              validator.getMessage());
    }
  }

  @Override
  public List<ModelValidator> getAllValidators(Class type) {
    return registrationHandlerService.getRegisteredModelValidatorMap().get(type);
  }

  @Override
  public List<ModelValidator> getActiveValidators(Class type) {
    List<ModelValidator> activateValidators = new ArrayList<>();
    if (registrationHandlerService != null) {
      if (registrationHandlerService.getRegisteredModelValidatorMap().containsKey(type)) {
        for (ModelValidator modelValidator :
                registrationHandlerService.getRegisteredModelValidatorMap().get(
                        type)) {
          if (modelValidationActivateStatusService.isModelValidatorActiveForClass(
                  modelValidator, type)) {
            activateValidators.add(modelValidator);
          }
        }
      }
    } else {
      LOG.error("Unable to retrieve active validators for {}. RegistrationHandlerService was null.",
              type.getName());
    }
    return activateValidators;
  }

  @Override
  public List<ModelValidator> getInactiveValidators(Class type) {
    List<ModelValidator> inactivateValidators = new ArrayList<>();
    if (registrationHandlerService != null) {
      if (registrationHandlerService.getRegisteredModelValidatorMap().containsKey(type)) {
        for (ModelValidator modelValidator :
                registrationHandlerService.getRegisteredModelValidatorMap().get(
                        type)) {
          if (!modelValidationActivateStatusService.isModelValidatorActiveForClass(
                  modelValidator, type)) {
            inactivateValidators.add(modelValidator);
          }
        }
      }
    } else {
      LOG.error(
              "Unable to retrieve inactive validators for {}. RegistrationHandlerService was null.",
              type.getName());
    }
    return inactivateValidators;
  }

}