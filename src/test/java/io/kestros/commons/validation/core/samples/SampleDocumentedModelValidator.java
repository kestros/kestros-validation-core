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
