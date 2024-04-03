package io.kestros.commons.validation.core.samples;

import io.kestros.commons.validation.api.models.ModelValidator;
import io.kestros.commons.validation.api.models.ModelValidatorBundle;
import java.util.List;

public class SampleModelValidatorBundle extends ModelValidatorBundle {
  private List<ModelValidator> bundledValidators;
  private String message;

  private boolean allMustBeTrue;

  public SampleModelValidatorBundle(List<ModelValidator> bundledValidators, String message,
          boolean allMustBeTrue) {
    this.bundledValidators = bundledValidators;
    this.message = message;
    this.allMustBeTrue = allMustBeTrue;
    this.registerValidators();
  }

  @Override
  public String getMessage() {
    return message;
  }


  @Override
  public void registerValidators() {
    addAllValidators(bundledValidators);
  }

  @Override
  public boolean isAllMustBeTrue() {
    return allMustBeTrue;
  }
}
