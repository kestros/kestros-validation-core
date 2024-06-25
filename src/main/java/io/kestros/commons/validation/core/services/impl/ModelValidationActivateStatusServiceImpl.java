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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.kestros.commons.validation.api.models.ModelValidator;
import io.kestros.commons.validation.core.services.ModelValidationActivateStatusService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Service for managing the activation status of ModelValidators.
 */
@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
@Component(immediate = true,
        service = ModelValidationActivateStatusService.class)
public class ModelValidationActivateStatusServiceImpl implements
        ModelValidationActivateStatusService {

  private static final Logger LOG = LoggerFactory.getLogger(
          ModelValidationActivateStatusServiceImpl.class);
  private Map<Class, Map<String, Boolean>> validatorActivationStatusMap;

  /**
   * Activates the ModelValidationActivateStatusService.
   */
  @Activate
  public void activate() {
    LOG.info("Activating ModelValidationActivateStatusService.");
    validatorActivationStatusMap = new HashMap<>();
  }

  /**
   * Deactivates the ModelValidationActivateStatusService.
   */
  @Deactivate
  public void deactivate() {
    LOG.info("Deactivating ModelValidationActivateStatusService.");
  }

  @Nonnull
  @Override
  public Map<Class, Map<String, Boolean>> getValidatorActivationStatusMap() {
    return new HashMap<>(validatorActivationStatusMap);
  }


  @Nonnull
  @Override
  public List<ModelValidator> getActiveValidators(@Nonnull final List<ModelValidator> validators,
          @Nonnull final Class type) {
    List<ModelValidator> activeValidators = new ArrayList<>();
    for (ModelValidator validator : validators) {
      if (isModelValidatorActiveForClass(validator, type)) {
        activeValidators.add(validator);
      }
    }
    return activeValidators;
  }

  @Override
  public boolean isModelValidatorActiveForClass(@Nonnull final ModelValidator validator,
          @Nonnull final Class type) {
    Map<String, Boolean> typeMap = validatorActivationStatusMap.get(type);
    if (typeMap != null) {
      return typeMap.get(validator.getClass().getName());
    }
    return true;
  }

  @Override
  public void activateValidator(@Nonnull final ModelValidator validator,
          @Nonnull final Class type) {
    if (validatorActivationStatusMap.get(type) == null) {
      validatorActivationStatusMap.put(type, new HashMap<>());
    }
    validatorActivationStatusMap.get(type).put(validator.getClass().getName(), Boolean.TRUE);
  }

  @Override
  public void deactivateValidator(@Nonnull final ModelValidator validator,
          @Nonnull final Class type) {
    validatorActivationStatusMap.get(type).put(validator.getClass().getName(), Boolean.FALSE);
  }
}
