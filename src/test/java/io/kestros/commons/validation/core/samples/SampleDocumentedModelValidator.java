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
import io.kestros.commons.validation.api.models.DocumentedModelValidator;

public class SampleDocumentedModelValidator extends DocumentedModelValidator {
  private boolean isValid;
  private String message;
  private String detailedMessage;
  private ModelValidationMessageType type;
  private String resourceType;

  public SampleDocumentedModelValidator(boolean isValid, String message, String detailedMessage,
          ModelValidationMessageType type, String resourceType) {
    this.isValid = isValid;
    this.message = message;
    this.detailedMessage = detailedMessage;
    this.type = type;
    this.resourceType = resourceType;
  }

  @Override
  public String getResourceType() {
    return resourceType;
  }

  @Override
  public Boolean isValidCheck(BaseSlingModel model) {
    return isValid;
  }

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String getDetailedMessage(BaseSlingModel model) {
    return detailedMessage;
  }


  @Override
  public ModelValidationMessageType getType() {
    return type;
  }
}
