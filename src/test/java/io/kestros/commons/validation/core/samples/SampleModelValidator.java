package io.kestros.commons.validation.core.samples;

import io.kestros.commons.structuredslingmodels.BaseSlingModel;
import io.kestros.commons.validation.api.ModelValidationMessageType;
import io.kestros.commons.validation.api.models.ModelValidator;

public class SampleModelValidator extends ModelValidator {
  private boolean isValid;
  private String message;
  private String detailedMessage;
  private ModelValidationMessageType type;

  public SampleModelValidator(boolean isValid, String message, String detailedMessage,
          ModelValidationMessageType type) {
    this.isValid = isValid;
    this.message = message;
    this.detailedMessage = detailedMessage;
    this.type = type;
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
