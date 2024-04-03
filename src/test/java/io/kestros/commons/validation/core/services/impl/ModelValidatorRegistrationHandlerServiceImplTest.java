package io.kestros.commons.validation.core.services.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import io.kestros.commons.structuredslingmodels.BasePage;
import io.kestros.commons.structuredslingmodels.BaseRequestContext;
import io.kestros.commons.structuredslingmodels.BaseResource;
import io.kestros.commons.validation.api.services.ModelValidatorRegistrationService;
import io.kestros.commons.validation.core.samples.SampleModelValidatorRegistrationService;
import io.kestros.commons.validation.core.services.ModelValidationActivateStatusService;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ModelValidatorRegistrationHandlerServiceImplTest {

  @Rule
  public SlingContext context = new SlingContext();

  private ModelValidatorRegistrationHandlerServiceImpl registrationHandlerService
      = new ModelValidatorRegistrationHandlerServiceImpl();

  private ModelValidationActivateStatusService activateStatusService;
  private ModelValidatorRegistrationService registrationService1;
  private ModelValidatorRegistrationService registrationService2;

  @Before
  public void setUp() throws Exception {
    context.addModelsForPackage("io.kestros");
    activateStatusService = new ModelValidationActivateStatusServiceImpl();
    context.registerInjectActivateService(activateStatusService);
    assertNotNull(activateStatusService.getValidatorActivationStatusMap());

    registrationService1 = new SampleModelValidatorRegistrationService(BaseResource.class);
    registrationService2 = new SampleModelValidatorRegistrationService(BasePage.class);


    context.registerService(ModelValidatorRegistrationService.class, registrationService1);
    context.registerService(ModelValidatorRegistrationService.class, registrationService2);

    context.registerInjectActivateService(registrationHandlerService);
    registrationHandlerService.registerAllValidatorsFromAllServices();
  }

  @Test
  public void testActivate() {
  }

  @Test
  public void testGetRegisteredModelValidatorMap() {
    assertEquals(2, registrationHandlerService.getRegisteredModelValidatorMap().keySet().size());
  }

  @Test
  public void testRegisterAllValidatorsFromAllServices() {
    registrationHandlerService.registerAllValidatorsFromAllServices();
    assertEquals(2, registrationHandlerService.getRegisteredModelValidatorMap().keySet().size());
    assertEquals(2,
        registrationHandlerService.getRegisteredModelValidatorMap().get(BaseResource.class).size());
    assertEquals(2,
        registrationHandlerService.getRegisteredModelValidatorMap().get(BasePage.class).size());
  }

  @Test
  public void testRegisterAllValidatorsFromAllServicesWhenMultipleRegistrationsToOneType() {
    ModelValidatorRegistrationService registrationService3
        = new SampleModelValidatorRegistrationService(BaseResource.class);
    ModelValidatorRegistrationService registrationService4
        = new SampleModelValidatorRegistrationService(BaseRequestContext.class);

    context.registerService(ModelValidatorRegistrationService.class, registrationService3);
    context.registerService(ModelValidatorRegistrationService.class, registrationService4);

    registrationHandlerService.registerAllValidatorsFromAllServices();

    assertEquals(3, activateStatusService.getValidatorActivationStatusMap().size());
    assertEquals(2, registrationHandlerService.getRegisteredModelValidatorMap().get(BaseResource.class).size());

    assertEquals(3, registrationHandlerService.getRegisteredModelValidatorMap().keySet().size());
    assertEquals(2,
        registrationHandlerService.getRegisteredModelValidatorMap().get(BaseResource.class).size());
    assertEquals(2,
        registrationHandlerService.getRegisteredModelValidatorMap().get(BasePage.class).size());
  }

  @Test
  public void testRegisterAllValidatorsFromService() {
  }

  @Test
  public void testRegisterValidators() {
  }

  @Test
  public void testRemoveValidators() {
  }
}