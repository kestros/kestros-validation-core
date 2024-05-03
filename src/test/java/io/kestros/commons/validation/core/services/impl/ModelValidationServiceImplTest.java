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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import io.kestros.commons.structuredslingmodels.BaseResource;
import io.kestros.commons.validation.core.models.impl.ModelValidationResultImpl;
import io.kestros.commons.validation.core.services.ModelValidatorProviderService;
import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.Result;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

public class ModelValidationServiceImplTest {

  @Rule
  public SlingContext context = new SlingContext();
  private ModelValidationServiceImpl modelValidationService;
  private BaseResource resource;
  private ModelValidatorProviderService providerService;

  @Before
  public void setUp() throws Exception {
    modelValidationService = new ModelValidationServiceImpl();
    providerService = mock(ModelValidatorProviderService.class);
    resource = mock(BaseResource.class);
  }

  @Test
  public void testGetDisplayName() {
    assertEquals("Model Validation Service", modelValidationService.getDisplayName());
  }

  @Test
  public void testActivate() {
    modelValidationService.activate(context.componentContext());
  }

  @Test
  public void testDeactivate() {
    modelValidationService.deactivate(context.componentContext());
  }

  @Test
  public void testRunAdditionalHealthChecks() {
    context.registerService(ModelValidatorProviderService.class, providerService);
    context.registerInjectActivateService(modelValidationService);
    FormattingResultLog log = new FormattingResultLog();
    modelValidationService.runAdditionalHealthChecks(log);
    assertEquals(Result.Status.OK, log.getAggregateStatus());
  }

  @Test
  public void testValidate() {
    context.registerService(ModelValidatorProviderService.class, providerService);
    context.registerInjectActivateService(modelValidationService);
    modelValidationService.validate(resource);
    assertEquals(ModelValidationResultImpl.class, modelValidationService.validate(resource).getClass());
    verify(providerService, times(2)).getActiveValidators(any());
  }

  @Test
  @Ignore
  public void testValidateWhenValidatorProviderServiceIsNull() {
    context.registerInjectActivateService(modelValidationService);
    modelValidationService.validate(resource);
  }
}