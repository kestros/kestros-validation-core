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

package io.kestros.commons.validation.core.samples;

import io.kestros.commons.structuredslingmodels.BaseSlingModel;
import io.kestros.commons.validation.api.ModelValidationMessageType;
import io.kestros.commons.validation.api.models.ModelValidator;
import io.kestros.commons.validation.api.services.ModelValidatorRegistrationHandlerService;
import io.kestros.commons.validation.api.services.ModelValidatorRegistrationService;
import java.util.ArrayList;
import java.util.List;

public class SampleModelValidatorRegistrationService implements ModelValidatorRegistrationService {

  private Class type;

  private ModelValidatorRegistrationHandlerService modelValidatorRegistrationHandlerService;
  private String errorMessage;

  private String warningMessage;

  public SampleModelValidatorRegistrationService(Class type,
          ModelValidatorRegistrationHandlerService modelValidatorRegistrationHandlerService,
          String errorMessage, String warningMessage) {
    this.type = type;
    this.modelValidatorRegistrationHandlerService = modelValidatorRegistrationHandlerService;
    this.errorMessage = errorMessage;
    this.warningMessage = warningMessage;
  }

  @Override
  public List<ModelValidator> getModelValidators() {
    List<ModelValidator> modelValidators = new ArrayList<>();
    modelValidators.add(getErrorValidator());
    modelValidators.add(getWarningValidator());
    return modelValidators;
  }

  private ModelValidator getErrorValidator() {
    return new ModelValidator() {

      @Override
      public String getMessage() {
        return errorMessage;
      }

      @Override
      public String getDetailedMessage(BaseSlingModel model) {
        return "Detailed Message.";
      }


      @Override
      public ModelValidationMessageType getType() {
        return ModelValidationMessageType.ERROR;
      }

      @Override
      public Boolean isValidCheck(BaseSlingModel model) {
        return false;
      }
    };
  }

  private ModelValidator getWarningValidator() {
    return new ModelValidator() {

      @Override
      public String getMessage() {
        return warningMessage;
      }

      @Override
      public String getDetailedMessage(BaseSlingModel model) {
        return "Detailed Message.";
      }


      @Override
      public ModelValidationMessageType getType() {
        return ModelValidationMessageType.WARNING;
      }

      @Override
      public Boolean isValidCheck(BaseSlingModel model) {
        return false;
      }
    };
  }

  @Override
  public ModelValidatorRegistrationHandlerService getModelValidatorRegistrationHandlerService() {
    return modelValidatorRegistrationHandlerService;
  }

  @Override
  public Class<? extends BaseSlingModel> getModelType() {
    return this.type;
  }

}
