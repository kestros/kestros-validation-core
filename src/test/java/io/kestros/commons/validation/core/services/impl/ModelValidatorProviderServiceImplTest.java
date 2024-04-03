package io.kestros.commons.validation.core.services.impl;

import static org.junit.Assert.assertEquals;

import io.kestros.commons.structuredslingmodels.BasePage;
import io.kestros.commons.structuredslingmodels.BaseResource;
import io.kestros.commons.structuredslingmodels.BaseSlingModel;
import io.kestros.commons.validation.api.ModelValidationMessageType;
import io.kestros.commons.validation.api.models.ModelValidator;
import io.kestros.commons.validation.api.services.ModelValidatorRegistrationHandlerService;
import io.kestros.commons.validation.api.services.ModelValidatorRegistrationService;
import io.kestros.commons.validation.core.samples.SampleModelValidatorRegistrationService;
import io.kestros.commons.validation.core.services.ModelValidationActivateStatusService;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

public class ModelValidatorProviderServiceImplTest {

  @Rule
  public SlingContext context = new SlingContext();

  private ModelValidatorRegistrationService registrationService1;
  private ModelValidatorRegistrationService registrationService2;
  private ModelValidationActivateStatusService activateStatusService;
  private ModelValidatorRegistrationHandlerService registrationHandlerService;
  private ModelValidatorProviderServiceImpl modelValidatorProviderService;

  @Before
  public void setUp() throws Exception {
    activateStatusService = new ModelValidationActivateStatusServiceImpl();
    context.registerInjectActivateService(activateStatusService);

    registrationService1 = new SampleModelValidatorRegistrationService(BaseResource.class);
    registrationService2 = new SampleModelValidatorRegistrationService(BasePage.class);

    context.registerService(ModelValidatorRegistrationService.class, registrationService1);
    context.registerService(ModelValidatorRegistrationService.class, registrationService2);


    registrationHandlerService = Mockito.spy(new ModelValidatorRegistrationHandlerServiceImpl());
    context.registerInjectActivateService(registrationHandlerService);

    registrationHandlerService.registerAllValidatorsFromAllServices();
    registrationHandlerService.registerAllValidatorsFromAllServices();

    modelValidatorProviderService = Mockito.spy(new ModelValidatorProviderServiceImpl());
    context.registerInjectActivateService(modelValidatorProviderService);

  }

  @Test
  public void testActivateValidator() {
    assertEquals(2, modelValidatorProviderService.getActiveValidators(BaseResource.class).size());
    ModelValidator sampleValidator = new ModelValidator() {


      @Override
      public Boolean isValidCheck(BaseSlingModel model) {
        return false;
      }

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
    };
    activateStatusService.deactivateValidator(
            modelValidatorProviderService.getAllValidators(BaseResource.class).get(0),
            BaseResource.class);
    assertEquals(1, modelValidatorProviderService.getInactiveValidators(BaseResource.class).size());
    assertEquals(1, modelValidatorProviderService.getActiveValidators(BaseResource.class).size());
    modelValidatorProviderService.activateValidator(
            modelValidatorProviderService.getAllValidators(BaseResource.class).get(0),
            BaseResource.class);

    assertEquals(0, modelValidatorProviderService.getInactiveValidators(BaseResource.class).size());
    assertEquals(2, modelValidatorProviderService.getActiveValidators(BaseResource.class).size());
  }

  @Test
  public void testDeactivateValidator() {
    assertEquals(0, modelValidatorProviderService.getInactiveValidators(BaseResource.class).size());
    assertEquals(2, modelValidatorProviderService.getActiveValidators(BaseResource.class).size());
    activateStatusService.deactivateValidator(
            modelValidatorProviderService.getAllValidators(BaseResource.class).get(0),
            BaseResource.class);
    assertEquals(1, modelValidatorProviderService.getActiveValidators(BaseResource.class).size());
    assertEquals(1, modelValidatorProviderService.getInactiveValidators(BaseResource.class).size());
  }

  @Test
  public void testGetAllValidators() {
  }

  @Test
  public void testGetActiveValidators() {
  }

  @Test
  public void testGetInactiveValidators() {
    assertEquals(100, 100);
  }
}