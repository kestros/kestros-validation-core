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
import io.kestros.commons.validation.api.ModelValidationMessageType;
import io.kestros.commons.validation.api.models.DocumentedModelValidator;
import io.kestros.commons.validation.api.models.ModelValidator;
import io.kestros.commons.validation.api.models.ModelValidatorBundle;
import io.kestros.commons.validation.api.models.ValidatorResult;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;

/**
 * Implementation of a processed model validator result.
 */
@SuppressFBWarnings({"PSC_PRESIZE_COLLECTIONS","IMC_IMMATURE_CLASS_NO_TOSTRING"})
public class ValidatorResultImpl implements ValidatorResult {

  private boolean isValid;
  private String message;
  private String detailedMessage;
  private List<ValidatorResult> bundled;
  private String validatorClassPath;
  private Map<ModelValidationMessageType, List<String>> messages;

  private String documentationResourceType;
  private ModelValidationMessageType type;

  /**
   * Builds a processed model validator result for the given validator and model.
   *
   * @param validator Validator to process
   * @param model Model to validate
   * @param <T> Model type.
   */
  @SuppressFBWarnings({"PSC_PRESIZE_COLLECTIONS","UEC_USE_ENUM_COLLECTIONS"})
  public <T extends BaseResource> ValidatorResultImpl(@Nonnull final ModelValidator validator,
          @Nonnull final T model) {
    this.messages = new HashMap<>();
    this.message = validator.getMessage();
    this.detailedMessage = validator.getDetailedMessage(model);
    this.validatorClassPath = validator.getClass().getName();
    this.isValid = true;
    if (validator instanceof ModelValidatorBundle) {
      boolean hasValidValidator = false;
      boolean hasInvalidValidator = false;
      this.bundled = new ArrayList<>();
      ModelValidatorBundle bundle = (ModelValidatorBundle) validator;
      for (Object childObject : bundle.getValidators()) {
        ModelValidator childValidator = (ModelValidator) childObject;
        ValidatorResult result = new ValidatorResultImpl(childValidator, model);
        bundled.add(result);
        if (!result.isValid()) {
          hasInvalidValidator = true;
          this.messages.putAll(result.getMessages());
        } else {
          hasValidValidator = true;
        }
      }
      if (bundle.isAllMustBeTrue()) {
        this.isValid = hasValidValidator && !hasInvalidValidator;
      } else {
        this.isValid = hasValidValidator || !hasInvalidValidator;
      }

    } else {
      this.bundled = null;
      this.isValid = validator.isValidCheck(model);
      if (!isValid) {
        this.messages.put(validator.getType(), Collections.singletonList(validator.getMessage()));
      }
    }
    if (validator instanceof DocumentedModelValidator) {
      this.documentationResourceType = ((DocumentedModelValidator) validator).getResourceType();
    } else {
      this.documentationResourceType = null;
    }
    this.type = validator.getType();
  }

  @Override
  public boolean isValid() {
    return isValid;
  }

  @Nonnull
  @Override
  public String getMessage() {
    return message;
  }

  @Nonnull
  @Override
  public String getDetailedMessage() {
    return detailedMessage;
  }

  @Nonnull
  @Override
  public String getDocumentationResourceType() {
    return documentationResourceType;
  }

  @Nonnull
  @Override
  public List<ValidatorResult> getBundled() {
    return new ArrayList<>(bundled);
  }

  @Nonnull
  @Override
  public String getValidatorClassPath() {
    return validatorClassPath;
  }

  @Nonnull
  @Override
  public ModelValidationMessageType getType() {
    return type;
  }

  @Nonnull
  @Override

  public Map<ModelValidationMessageType, List<String>> getMessages() {
    return new HashMap<>(messages);
  }


}
