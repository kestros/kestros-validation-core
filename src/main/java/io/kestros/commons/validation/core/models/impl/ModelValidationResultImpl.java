package io.kestros.commons.validation.core.models.impl;

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

public class ModelValidationResultImpl implements ModelValidationResult {

  private final List<ValidatorResult> results;
  BaseSlingModel model;
  private Map<ModelValidationMessageType, List<String>> messagesMap;
  private Boolean isValid;
  private List<ModelValidator> validators;

  public <T extends BaseResource> ModelValidationResultImpl(T model,
          List<ModelValidator> validators) {
    this.results = new ArrayList<>();
    messagesMap = new HashMap<>();
    this.model = model;
    this.validators = validators;
    this.isValid = true;
    for (ModelValidator validator : validators) {
      ValidatorResult validatorResult = new ValidatorResultImpl(validator, model);
      if (!validatorResult.isValid()) {
        this.isValid = false;
      }
      results.add(validatorResult);
      messagesMap.putAll(validatorResult.getMessages());
    }
  }

  @Override
  public List<ValidatorResult> getResults() {
    return results;
  }

  @Nonnull
  @Override
  public <T extends BaseSlingModel> T getModel() {
    return (T) model;
  }


  @Override
  public List<ModelValidator> getValidators() {
    return validators;
  }

  @Override
  public boolean isValid() {
    return isValid;
  }

  @Override
  public Map<ModelValidationMessageType, List<String>> getMessages() {
    if(!messagesMap.containsKey(ModelValidationMessageType.ERROR)) {
      messagesMap.put(ModelValidationMessageType.ERROR, new ArrayList<>());
    }
    if(!messagesMap.containsKey(ModelValidationMessageType.WARNING)) {
      messagesMap.put(ModelValidationMessageType.WARNING, new ArrayList<>());
    }
    if(!messagesMap.containsKey(ModelValidationMessageType.INFO)) {
      messagesMap.put(ModelValidationMessageType.INFO, new ArrayList<>());
    }
    return messagesMap;
  }
}
