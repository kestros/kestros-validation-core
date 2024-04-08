package io.kestros.commons.validation.core.models.impl;

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

public class ValidatorResultImpl implements ValidatorResult {

  private boolean isValid;
  private String message;
  private String detailedMessage;
  private List<ValidatorResult> bundled;
  private String validatorClassPath;
  private Map<ModelValidationMessageType, List<String>> messages;

  private String documentationResourceType;
  private ModelValidationMessageType type;

  public <T extends BaseResource> ValidatorResultImpl(ModelValidator validator, T model) {
    this.messages = new HashMap<>();
    this.message = validator.getMessage();
    this.detailedMessage = validator.getDetailedMessage(model);
    this.validatorClassPath = validator.getClass().getName();
    this.isValid = true;
    if (validator instanceof ModelValidatorBundle) {
      this.bundled = new ArrayList<>();
      ModelValidatorBundle bundle = (ModelValidatorBundle) validator;
      for (Object childObject : bundle.getValidators()) {
        ModelValidator childValidator = (ModelValidator) childObject;
        ValidatorResult result = new ValidatorResultImpl(childValidator, model);
        bundled.add(result);
        if (!result.isValid()) {
          this.isValid = false;
          this.messages.putAll(result.getMessages());
        }
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

  @Override
  public String getMessage() {
    return message;
  }

  @Override
  public String getDetailedMessage() {
    return detailedMessage;
  }

  @Override
  public String getDocumentationResourceType() {
    return documentationResourceType;
  }

  @Override
  public List<ValidatorResult> getBundled() {
    return bundled;
  }

  @Override
  public String getValidatorClassPath() {
    return validatorClassPath;
  }

  @Override
  public ModelValidationMessageType getType() {
    return type;
  }

  @Override
  public Map<ModelValidationMessageType, List<String>> getMessages() {
    return messages;
  }


}
