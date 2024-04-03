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

  public SampleModelValidatorRegistrationService(Class type) {
    this.type = type;
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
        return "Error Message.";
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
        return "Warning Message.";
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
    return null;
  }

  @Override
  public Class<? extends BaseSlingModel> getModelType() {
    return this.type;
  }

}
