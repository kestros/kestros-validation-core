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

package io.kestros.commons.validation.core.models.impl;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.kestros.commons.structuredslingmodels.BaseResource;
import io.kestros.commons.structuredslingmodels.BaseSlingModel;
import io.kestros.commons.validation.api.ModelValidationMessageType;
import io.kestros.commons.validation.api.models.ModelValidationResult;
import io.kestros.commons.validation.api.models.ModelValidator;
import io.kestros.commons.validation.api.models.ValidatorResult;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Implementation of a processed model validation result.
 */
@SuppressFBWarnings({"IMC_IMMATURE_CLASS_NO_TOSTRING"})
public class ModelValidationResultImpl implements ModelValidationResult {

  private final List<ValidatorResult> results;
  BaseSlingModel model;
  private Map<ModelValidationMessageType, List<String>> messagesMap;
  private Boolean isValid;
  private List<ModelValidator> validators;

  /**
   * Builds a complete validation result for a given model and list of validators.
   *
   * @param model Model to validate.
   * @param validators List of validators to run against the model.
   * @param <T> Model type.
   */
  @SuppressFBWarnings("PSC_PRESIZE_COLLECTIONS")
  @Nonnull
  public <T extends BaseResource> ModelValidationResultImpl(@Nonnull final T model,
          @Nonnull final List<ModelValidator> validators) {
    this.results = new ArrayList<>();
    messagesMap = new HashMap<>();
    this.model = model;
    this.validators = new ArrayList<>(validators);
    this.isValid = Boolean.TRUE;
    for (ModelValidator validator : validators) {
      ValidatorResult validatorResult = new ValidatorResultImpl(validator, model);
      if (!validatorResult.isValid()) {
        this.isValid = Boolean.FALSE;
      }
      results.add(validatorResult);
      messagesMap.putAll(validatorResult.getMessages());
    }
  }

  @Nonnull
  @Override
  public List<ValidatorResult> getResults() {
    return new ArrayList<>(results);
  }

  @Nonnull
  @Override
  public <T extends BaseSlingModel> T getModel() {
    return (T) model;
  }


  @Nonnull
  @Override
  public List<ModelValidator> getValidators() {
    return new ArrayList<>(validators);
  }

  @Override
  public boolean isValid() {
    return isValid;
  }

  @SuppressFBWarnings("UEC_USE_ENUM_COLLECTIONS")
  @Nonnull
  @Override
  public Map<ModelValidationMessageType, List<String>> getMessages() {
    if (!messagesMap.containsKey(ModelValidationMessageType.ERROR)) {
      messagesMap.put(ModelValidationMessageType.ERROR, new ArrayList<>());
    }
    if (!messagesMap.containsKey(ModelValidationMessageType.WARNING)) {
      messagesMap.put(ModelValidationMessageType.WARNING, new ArrayList<>());
    }
    if (!messagesMap.containsKey(ModelValidationMessageType.INFO)) {
      messagesMap.put(ModelValidationMessageType.INFO, new ArrayList<>());
    }
    return new HashMap<>(messagesMap);
  }
}
