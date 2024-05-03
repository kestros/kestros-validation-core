/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Model Validator Registration Handler Service.
 */
@Component(immediate = true,
        service = ModelValidatorRegistrationHandlerService.class,
        property = "service.ranking:Integer=100")
public class ModelValidatorRegistrationHandlerServiceImpl
        implements ModelValidatorRegistrationHandlerService {

  private static final Logger LOG = LoggerFactory.getLogger(
          ModelValidatorRegistrationHandlerServiceImpl.class);
  Map<Class, List<ModelValidator>> registeredModelValidatorMap = new HashMap<>();
  @Reference(cardinality = ReferenceCardinality.OPTIONAL,
          policyOption = ReferencePolicyOption.GREEDY)
  private ModelValidationActivateStatusService modelValidationActivateStatusService;
  private ComponentContext componentContext;

  @Override
  public String getDisplayName() {
    return "Model Validator Registration Handler Service";
  }

  /**
   * Activates the Model Validator Registration Handler Service.
   *
   * @param componentContext ComponentContext.
   */
  @Activate
  public void activate(ComponentContext componentContext) {
    LOG.info("Activating Model Validator Registration Handler Service.");
    this.componentContext = componentContext;
    this.registerAllValidatorsFromAllServices();
  }


  @Override
  public void deactivate(ComponentContext componentContext) {
    LOG.info("Deactivating Model Validator Registration Handler Service.");
  }

  @Override
  public void runAdditionalHealthChecks(FormattingResultLog log) {
    log.debug("Running additional health checks for Model Validator Registration Handler Service.");
    List<ModelValidatorRegistrationService> registrationServiceList
            = OsgiServiceUtils.getAllOsgiServicesOfType(componentContext,
            ModelValidatorRegistrationService.class);

    for (ModelValidatorRegistrationService modelValidatorRegistrationService :
            registrationServiceList) {
      if (modelValidatorRegistrationService.getModelValidatorRegistrationHandlerService() == null) {
        log.warn(String.format("No referenced ModelValidatorRegistrationService for %s",
                modelValidatorRegistrationService.getClass().getName().replaceAll("[\r\n]", "")));
      }
    }

    if (getRegisteredModelValidatorMap().isEmpty()) {
      log.info("No registered validators detected.");
    }
  }

  @Override
  public Map<Class, List<ModelValidator>> getRegisteredModelValidatorMap() {
    return new HashMap<>(this.registeredModelValidatorMap);
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
      // If no validators are registered for this type, add all validators.
      registeredModelValidatorMap.put(type,
              getRegisteredModelValidatorsFromModelValidators(modelValidators, type));

      // Activate all validators by default.
      for (ModelValidator modelValidator : modelValidators) {
        modelValidationActivateStatusService.activateValidator(modelValidator, type);
      }
    } else {
      // if the validators are already registered
      List<ModelValidator> newModelValidators = new ArrayList<>();
      List<ModelValidator> validatorsToAdd
              = getRegisteredModelValidatorsFromModelValidators(modelValidators, type);
      List<ModelValidator> existingRegisteredModelValidators
              = registeredModelValidatorMap.get(type);

      // Add new validators to the existing list. Deactive existing validators with the same message
      for (ModelValidator modelValidator : validatorsToAdd) {
        boolean alreadyExists = false;
        for (ModelValidator existingModelValidator : existingRegisteredModelValidators) {
          if (modelValidator.getMessage().equals(existingModelValidator.getMessage())) {
            modelValidationActivateStatusService.deactivateValidator(existingModelValidator, type);
            alreadyExists = true;
          }
        }
        if (!alreadyExists) {
          newModelValidators.add(modelValidator);
        }
      }
      registeredModelValidatorMap.get(type).addAll(newModelValidators);
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

  /**
   * Gets the ModelValidationActivateStatusService.
   *
   * @return ModelValidationActivateStatusService.
   */
  ModelValidationActivateStatusService getModelValidationActivateStatusService() {
    return this.modelValidationActivateStatusService;
  }

  List<ModelValidator> getRegisteredModelValidatorsFromModelValidators(
          List<ModelValidator> validators, Class type) {
    List<ModelValidator> registeredModelValidatorList = new ArrayList<>();
    if (validators != null && type != null) {
      for (ModelValidator modelValidator : validators) {
        registeredModelValidatorList.add(modelValidator);
      }
    }
    return registeredModelValidatorList;
  }

}
