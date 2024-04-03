package io.kestros.commons.validation.core.services.impl;

import static io.kestros.commons.osgiserviceutils.utils.OsgiServiceUtils.getAllOsgiServicesOfType;

import io.kestros.commons.osgiserviceutils.utils.OsgiServiceUtils;
import io.kestros.commons.validation.api.models.ModelValidator;
import io.kestros.commons.validation.api.services.ModelValidatorRegistrationHandlerService;
import io.kestros.commons.validation.api.services.ModelValidatorRegistrationService;
import io.kestros.commons.validation.core.services.ModelValidationActivateStatusService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.felix.hc.api.FormattingResultLog;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Model Validator Registration Handler Service.
 */
@Component(immediate = true,
        service = ModelValidatorRegistrationHandlerService.class,
        property = "service.ranking:Integer=100")
public class ModelValidatorRegistrationHandlerServiceImpl
        implements ModelValidatorRegistrationHandlerService {

  Map<Class, List<ModelValidator>> registeredModelValidatorMap = new HashMap<>();
  @Reference(cardinality = ReferenceCardinality.OPTIONAL,
          policyOption = ReferencePolicyOption.GREEDY)
  private ModelValidationActivateStatusService modelValidationActivateStatusService;
  private ComponentContext componentContext;

  @Override
  public String getDisplayName() {
    return "Model Validator Registration Handler Service";
  }

  @Activate
  public void activate(ComponentContext componentContext) {
    this.componentContext = componentContext;
  }

  @Override
  public void deactivate(ComponentContext componentContext) {

  }

  @Override
  public void runAdditionalHealthChecks(FormattingResultLog log) {
    List<ModelValidatorRegistrationService> registrationServiceList
            = OsgiServiceUtils.getAllOsgiServicesOfType(componentContext,
            ModelValidatorRegistrationService.class);

    for (ModelValidatorRegistrationService modelValidatorRegistrationService :
            registrationServiceList) {
      if (modelValidatorRegistrationService.getModelValidatorRegistrationHandlerService() == null) {
        log.warn(String.format("No referenced ModelValidatorRegistrationService for %s",
                modelValidatorRegistrationService.getClass().getName()));
      }
    }

    if (getRegisteredModelValidatorMap().isEmpty()) {
      log.info("No registered validators detected.");
    }
  }

  @Override
  public Map<Class, List<ModelValidator>> getRegisteredModelValidatorMap() {
    return this.registeredModelValidatorMap;
  }

  @Override
  public void registerAllValidatorsFromAllServices() {
    this.registeredModelValidatorMap.clear();
    List<ModelValidatorRegistrationService> services = getAllOsgiServicesOfType(
            this.componentContext, ModelValidatorRegistrationService.class);
    for (ModelValidatorRegistrationService service : services) {
      this.registerAllValidatorsFromService(service);
    }
  }

  @Override
  public void registerAllValidatorsFromService(
          ModelValidatorRegistrationService registrationService) {
    this.registerValidators(registrationService.getModelValidators(),
            registrationService.getModelType());
  }

  @Override
  public void unregisterAllValidatorsFromService(
          ModelValidatorRegistrationService registrationService) {
    this.removeValidators(registrationService.getModelValidators(),
            registrationService.getModelType());
  }

  @Override
  public void registerValidators(List<ModelValidator> modelValidators, Class type) {
    if (!registeredModelValidatorMap.containsKey(type)) {

      registeredModelValidatorMap.put(type,
              getRegisteredModelValidatorsFromModelValidators(modelValidators, type));
      for(ModelValidator modelValidator : modelValidators) {
        modelValidationActivateStatusService.activateValidator(modelValidator, type);
      }
    } else {
      List<ModelValidator> newModelValidators = new ArrayList<>();
      List<ModelValidator> validatorsToAdd
              = getRegisteredModelValidatorsFromModelValidators(modelValidators, type);
      List<ModelValidator> existingRegisteredModelValidators
              = registeredModelValidatorMap.get(type);

      if (existingRegisteredModelValidators != null) {

        for (ModelValidator existingRegisteredValidator :
                existingRegisteredModelValidators) {
          ModelValidator overriddingModelValidator = null;
          for (ModelValidator newRegisteredModelValidator : validatorsToAdd) {
            if (newRegisteredModelValidator.getMessage().equals(
                    newRegisteredModelValidator.getMessage())) {
              overriddingModelValidator = newRegisteredModelValidator;
            }
          }
          if (overriddingModelValidator != null) {
            if (!modelValidationActivateStatusService.isModelValidatorActiveForClass(
                    existingRegisteredValidator, type)) {
              modelValidationActivateStatusService.deactivateValidator(overriddingModelValidator,
                      type);
            }
            newModelValidators.add(overriddingModelValidator);
          }
        }

      }

      for (ModelValidator newRegisteredModelValidator : validatorsToAdd) {
        boolean isNewValidator = true;
        for (ModelValidator existingRegisteredValidator :
                existingRegisteredModelValidators) {

          if (existingRegisteredValidator.getMessage().equals(
                  newRegisteredModelValidator.getMessage())) {
            isNewValidator = false;
          }
        }
        if (isNewValidator) {
          newModelValidators.add(newRegisteredModelValidator);
        }
      }
      registeredModelValidatorMap.remove(type);
      registeredModelValidatorMap.put(type, newModelValidators);
    }

  }

  @Override
  public void removeValidators(List<ModelValidator> modelValidators, Class type) {
    // todo may not be needed.
    //    if (registeredModelValidatorMap.containsKey(type)) {
    //      List<RegisteredModelValidator> newRegisteredModelValidatorList = new ArrayList<>();
    //
    //      registeredModelValidatorMap.remove(type);
    //      registeredModelValidatorMap.put(type, newRegisteredModelValidatorList);
    //    }
  }

  public ModelValidationActivateStatusService getModelValidationActivateStatusService() {
    return this.modelValidationActivateStatusService;
  }

  private List<ModelValidator> getRegisteredModelValidatorsFromModelValidators(
          List<ModelValidator> validators, Class type) {
    List<ModelValidator> registeredModelValidatorList = new ArrayList<>();
    if (validators != null && type != null) {
      for (ModelValidator modelValidator : validators) {
        boolean active = true;
        if (modelValidationActivateStatusService != null) {
          active = modelValidationActivateStatusService.isModelValidatorActiveForClass(
                  modelValidator, type);
        }
        registeredModelValidatorList.add(modelValidator);
      }
    }
    return registeredModelValidatorList;
  }

}
