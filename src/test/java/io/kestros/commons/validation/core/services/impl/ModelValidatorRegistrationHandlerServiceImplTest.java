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

package io.kestros.commons.validation.core.services.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.kestros.commons.structuredslingmodels.BasePage;
import io.kestros.commons.structuredslingmodels.BaseRequestContext;
import io.kestros.commons.structuredslingmodels.BaseResource;
import io.kestros.commons.validation.api.models.ModelValidator;
import io.kestros.commons.validation.api.services.ModelValidatorRegistrationService;
import io.kestros.commons.validation.core.samples.SampleModelValidatorRegistrationService;
import io.kestros.commons.validation.core.services.ModelValidationActivateStatusService;
import java.util.ArrayList;
import java.util.List;
import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.Result;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Ignore;
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
  private ModelValidatorRegistrationService modelValidatorRegistrationService;

  @Before
  public void setUp() throws Exception {
    context.addModelsForPackage("io.kestros");
    activateStatusService = spy(new ModelValidationActivateStatusServiceImpl());
    context.registerInjectActivateService(activateStatusService);
    assertNotNull(activateStatusService.getValidatorActivationStatusMap());

    registrationService1 = new SampleModelValidatorRegistrationService(BaseResource.class,
            registrationHandlerService,"error1","warning1");
    registrationService2 = new SampleModelValidatorRegistrationService(BasePage.class,
            registrationHandlerService,"error2","warning2");
  }

  @Test
  public void testActivate() {
  }

  @Test
  public void testGetRegisteredModelValidatorMap() {
    context.registerService(ModelValidatorRegistrationService.class, registrationService1);
    context.registerService(ModelValidatorRegistrationService.class, registrationService2);

    context.registerInjectActivateService(registrationHandlerService);
    registrationHandlerService.registerAllValidatorsFromAllServices();
    assertEquals(2, registrationHandlerService.getRegisteredModelValidatorMap().keySet().size());
  }

  @Test
  public void testRegisterAllValidatorsFromAllServices() {
    context.registerService(ModelValidatorRegistrationService.class, registrationService1);
    context.registerService(ModelValidatorRegistrationService.class, registrationService2);

    context.registerInjectActivateService(registrationHandlerService);
    assertEquals(2, registrationHandlerService.getRegisteredModelValidatorMap().keySet().size());
    assertEquals(2,
            registrationHandlerService.getRegisteredModelValidatorMap().get(BaseResource.class)
                                      .size());
    assertEquals(2,
            registrationHandlerService.getRegisteredModelValidatorMap().get(BasePage.class).size());
  }

  @Test
  public void testRegisterAllValidatorsFromAllServicesWhenMultipleRegistrationsToOneType() {
    context.registerService(ModelValidatorRegistrationService.class, registrationService1);
    context.registerService(ModelValidatorRegistrationService.class, registrationService2);
    context.registerInjectActivateService(registrationHandlerService);
    ModelValidatorRegistrationService registrationService3
            = new SampleModelValidatorRegistrationService(BaseResource.class,
            registrationHandlerService,"error3","warning3");
    ModelValidatorRegistrationService registrationService4
            = new SampleModelValidatorRegistrationService(BaseRequestContext.class,
            registrationHandlerService,"error4","warning4");

    context.registerService(ModelValidatorRegistrationService.class, registrationService3);
    context.registerService(ModelValidatorRegistrationService.class, registrationService4);

    registrationHandlerService.registerAllValidatorsFromAllServices();

    assertEquals(3, activateStatusService.getValidatorActivationStatusMap().size());


    assertEquals(3, registrationHandlerService.getRegisteredModelValidatorMap().keySet().size());
    assertEquals(4,
            registrationHandlerService.getRegisteredModelValidatorMap().get(BaseResource.class)
                                      .size());
    assertEquals(2,
            registrationHandlerService.getRegisteredModelValidatorMap().get(BasePage.class).size());
  }

  @Test
  public void testRegisterAllValidatorsFromService() {
  }

  @Test
  public void testRegisterValidators() {
    context.registerService(ModelValidatorRegistrationService.class, registrationService1);
    context.registerService(ModelValidatorRegistrationService.class, registrationService2);

    assertEquals(0, registrationHandlerService.getRegisteredModelValidatorMap().size());
    context.registerInjectActivateService(registrationHandlerService);

    assertEquals(2, registrationHandlerService.getRegisteredModelValidatorMap().size());
    assertEquals(2, registrationHandlerService.getRegisteredModelValidatorMap()
                                              .get(BaseResource.class)
                                              .size());
    assertEquals(2, registrationHandlerService.getRegisteredModelValidatorMap()
                                              .get(BasePage.class)
                                              .size());
  }


  @Test
  public void testRegisterValidatorsWhenHasOverrides() {
    ModelValidatorRegistrationService registrationService3
            = new SampleModelValidatorRegistrationService(BasePage.class,
            registrationHandlerService,"error3","warning3");
    ModelValidatorRegistrationService registrationService4
            = new SampleModelValidatorRegistrationService(BasePage.class,
            registrationHandlerService,"error3","warning3");

    context.registerService(ModelValidatorRegistrationService.class, registrationService1);
    context.registerService(ModelValidatorRegistrationService.class, registrationService2);
    context.registerService(ModelValidatorRegistrationService.class, registrationService3);
    context.registerService(ModelValidatorRegistrationService.class, registrationService4);

    assertEquals(0, registrationHandlerService.getRegisteredModelValidatorMap().size());
    context.registerInjectActivateService(registrationHandlerService);

    assertEquals(2, registrationHandlerService.getRegisteredModelValidatorMap().size());
    assertEquals(2, registrationHandlerService.getRegisteredModelValidatorMap()
                                              .get(BaseResource.class)
                                              .size());
    assertEquals(4, registrationHandlerService.getRegisteredModelValidatorMap()
                                              .get(BasePage.class)
                                              .size());
    verify(activateStatusService, times(2)).deactivateValidator(any(), any());
  }


  @Test
  @Ignore
  public void testUnregisterValidators() {
    assertEquals(0, registrationHandlerService.getRegisteredModelValidatorMap().size());
    context.registerInjectActivateService(registrationHandlerService);
    assertEquals(2, registrationHandlerService.getRegisteredModelValidatorMap().size());
    registrationHandlerService.unregisterAllValidatorsFromService(registrationService1);
    assertEquals(0, registrationHandlerService.getRegisteredModelValidatorMap().size());
  }

  @Test
  public void testRemoveValidators() {
  }

  @Test
  public void testRunAdditionalHealthChecks() {
    context.registerService(ModelValidatorRegistrationService.class, registrationService1);
    context.registerService(ModelValidatorRegistrationService.class, registrationService2);

    context.registerInjectActivateService(registrationHandlerService);
    FormattingResultLog resultLog = new FormattingResultLog();
    registrationHandlerService.registerValidators(registrationService1.getModelValidators(),
            registrationService1.getModelType());
    registrationHandlerService.registerValidators(registrationService2.getModelValidators(),
            registrationService2.getModelType());

    registrationHandlerService.runAdditionalHealthChecks(resultLog);
    assertEquals(Result.Status.OK, resultLog.getAggregateStatus());
  }

  @Test
  public void testRunAdditionalHealthChecksWhenHandlerServiceIsNull() {
    context.registerService(ModelValidatorRegistrationService.class, registrationService1);
    context.registerService(ModelValidatorRegistrationService.class, registrationService2);

    ModelValidatorRegistrationService registrationService
            = new SampleModelValidatorRegistrationService(BaseResource.class, null,"error","warning");
    context.registerService(ModelValidatorRegistrationService.class, registrationService);
    context.registerInjectActivateService(registrationHandlerService);
    FormattingResultLog resultLog = new FormattingResultLog();
    registrationHandlerService.registerValidators(registrationService1.getModelValidators(),
            registrationService1.getModelType());
    registrationHandlerService.registerValidators(registrationService2.getModelValidators(),
            registrationService2.getModelType());

    registrationHandlerService.runAdditionalHealthChecks(resultLog);
    assertEquals(Result.Status.WARN, resultLog.getAggregateStatus());
  }

  @Test
  public void testRunAdditionalHealthChecksWhenNoRegistrationServices() {
    context.registerInjectActivateService(registrationHandlerService);
    FormattingResultLog resultLog = new FormattingResultLog();

    registrationHandlerService.runAdditionalHealthChecks(resultLog);
    assertEquals(Result.Status.OK, resultLog.getAggregateStatus());
  }

  @Test
  public void testGetRegisteredModelValidatorsFromModelValidatorsWhenActiviationServiceIsNull() {
    context.registerService(ModelValidatorRegistrationService.class, registrationService1);
    context.registerService(ModelValidatorRegistrationService.class, registrationService2);

    assertEquals(0, registrationHandlerService.getRegisteredModelValidatorMap().size());
    context.registerInjectActivateService(registrationHandlerService);

    List<ModelValidator> modelValidatorList = new ArrayList<>();
    assertEquals(0, registrationHandlerService.getRegisteredModelValidatorsFromModelValidators(
            modelValidatorList,
            BaseResource.class).size());
  }

  @Test
  public void testGetRegisteredModelValidatorsFromModelValidatorsWhenValidatorsIsNull() {
    assertEquals(0, registrationHandlerService.getRegisteredModelValidatorsFromModelValidators(null,
            BaseResource.class).size());
  }


  @Test
  public void testGetRegisteredModelValidatorsFromModelValidatorsWhenTypeIsNull() {
    List<ModelValidator> modelValidatorList = new ArrayList<>();
    assertEquals(0, registrationHandlerService.getRegisteredModelValidatorsFromModelValidators(
            modelValidatorList,
            null).size());
  }
}