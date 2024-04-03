package io.kestros.commons.validation.core.services.impl;

import io.kestros.commons.structuredslingmodels.BaseResource;
import io.kestros.commons.validation.api.models.ModelValidationResult;
import io.kestros.commons.validation.api.services.ModelValidationService;
import io.kestros.commons.validation.core.models.impl.ModelValidationResultImpl;
import io.kestros.commons.validation.core.services.ModelValidatorProviderService;
import org.apache.felix.hc.api.FormattingResultLog;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Model Validation Service.
 */
@Component(immediate = true,
        service = ModelValidationService.class)
public class ModelValidationServiceImpl implements ModelValidationService {

  @Reference(cardinality = ReferenceCardinality.OPTIONAL,
          policyOption = ReferencePolicyOption.GREEDY)
  private ModelValidatorProviderService validatorProviderService;

//  @Reference(cardinality = ReferenceCardinality.OPTIONAL,
//          policyOption = ReferencePolicyOption.GREEDY)
//  private ModelValidationCacheService validationCacheService;

  @Override
  public String getDisplayName() {
    return "Model Validation Service";
  }

  @Override
  public void activate(ComponentContext componentContext) {

  }

  @Override
  public void deactivate(ComponentContext componentContext) {

  }

  @Override
  public void runAdditionalHealthChecks(FormattingResultLog log) {

  }

  @Override
  public <T extends BaseResource> ModelValidationResult validate(T model) {
    return new ModelValidationResultImpl(model,
            validatorProviderService.getActiveValidators(model.getClass()));
  }
//
//  @Nonnull
//  @Override
//  public <T extends BaseResource> ModelValidationResult getProcessedValidators(@Nonnull T model) {
//    ModelValidationResult result = new ModelValidationResultImpl(model,
//            validatorProviderService.getActiveValidators(model.getClass()));
//    result.validate();
//    return result;
//  }
//
//  @Nonnull
//  @Override
//  public <T extends BaseResource> Integer getErrorsCount(@Nonnull T model) {
//    return this.getErrorMessages(model).size();
//  }
//
//  @Nonnull
//  @Override
//  public <T extends BaseResource> Integer getWarningsCount(@Nonnull T model) {
//    return this.getWarningMessages(model).size();
//  }
//
//  @Nonnull
//  @Override
//  public <T extends BaseResource> List<String> getErrorMessages(@Nonnull T model) {
//    return getMessagesOfLevel(model, ModelValidationMessageType.ERROR, true);
//  }
//
//  @Nonnull
//  @Override
//  public <T extends BaseResource> List<String> getWarningMessages(@Nonnull T model) {
//    return getMessagesOfLevel(model, ModelValidationMessageType.WARNING, true);
//  }
//
//  /**
//   * Returns a list of messages for a given model, at a specified level.
//   *
//   * @param model model to get messages for.
//   * @param type type of messages to get.
//   * @param cache whether to cache the messages.
//   * @param <T> type of model.
//   *
//   * @return a list of messages for a given model, at a specified level.
//   */
//  @Nonnull
//  public <T extends BaseResource> List<String> getMessagesOfLevel(@Nonnull T model,
//          ModelValidationMessageType type, boolean cache) {
//    if (validationCacheService != null && validationCacheService.isLive()) {
//      try {
//        if (type.equals(ModelValidationMessageType.ERROR)) {
//          return validationCacheService.getCachedErrorMessages(model.getResource(),
//                  model.getClass());
//        } else if (type.equals(ModelValidationMessageType.WARNING)) {
//          return validationCacheService.getCachedWarningMessages(model.getResource(),
//                  model.getClass());
//        }
//      } catch (CacheRetrievalException e) {
//        // todo log.
//      }
//    }
//    List<String> messages = new ArrayList<>();
//    for (ModelValidator modelValidator : this.getProcessedValidators(model)) {
//      if (type.equals(modelValidator.getType())) {
//        if (!modelValidator.isValid()) {
//          messages.add(modelValidator.getMessage());
//        }
//      }
//    }
//    List<String> errorMessages = new ArrayList<>();
//    List<String> warningMessages = new ArrayList<>();
//    if (validationCacheService != null && cache) {
//      if (type.equals(ModelValidationMessageType.ERROR)) {
//        errorMessages = messages;
//        warningMessages = getMessagesOfLevel(model, ModelValidationMessageType.WARNING, false);
//      } else if (type.equals(ModelValidationMessageType.WARNING)) {
//        warningMessages = messages;
//        errorMessages = getMessagesOfLevel(model, ModelValidationMessageType.ERROR, false);
//      }
//      validationCacheService.cacheValidationResults(model, errorMessages, warningMessages);
//    }
//
//    return messages;
//
//
//  }
//

}
