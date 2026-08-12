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

package io.kestros.commons.validation.core.servlets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.kestros.commons.structuredslingmodels.BaseResource;
import io.kestros.commons.validation.api.ModelValidationMessageType;
import io.kestros.commons.validation.api.models.ModelValidationResult;
import io.kestros.commons.validation.api.services.ModelValidationService;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ValidationListServletTest {

  @Rule
  public SlingContext context = new SlingContext();

  private ValidationListServlet servlet;
  private ModelValidationService validationService;

  @Before
  public void setUp() throws Exception {
    context.addModelsForPackage("io.kestros.commons.structuredslingmodels");
    servlet = new ValidationListServlet();
    validationService = mock(ModelValidationService.class);
    context.create().resource("/content/list");
  }

  private void registerServlet() {
    context.registerService(ModelValidationService.class, validationService);
    context.registerInjectActivateService(servlet);
  }

  /**
   * Answers with one error message naming the resource that was validated, so a response that
   * handed every row the same messages would be visible rather than plausible.
   */
  private void respondPerResource() {
    when(validationService.validate(any())).thenAnswer(invocation -> {
      final BaseResource model = invocation.getArgument(0);
      final Map<ModelValidationMessageType, List<String>> messages = new EnumMap<>(
              ModelValidationMessageType.class);
      messages.put(ModelValidationMessageType.ERROR,
              Collections.singletonList("broken: " + model.getPath()));
      messages.put(ModelValidationMessageType.WARNING, Collections.emptyList());
      messages.put(ModelValidationMessageType.INFO, Collections.emptyList());
      final ModelValidationResult result = mock(ModelValidationResult.class);
      when(result.getMessages()).thenReturn(messages);
      return result;
    });
  }

  private void setPathParameters(final String... paths) {
    final Map<String, Object> parameters = new HashMap<>();
    parameters.put(ValidationListServlet.PARAMETER_PATH, paths);
    context.request().setParameterMap(parameters);
  }

  @Test
  public void testDoGetReturnsEveryRowInOneResponse() throws Exception {
    context.create().resource("/apps/components/alpha");
    context.create().resource("/apps/components/beta");
    context.create().resource("/apps/components/gamma");
    respondPerResource();
    registerServlet();
    context.request().setResource(context.resourceResolver().getResource("/content/list"));
    setPathParameters("/apps/components/alpha", "/apps/components/beta",
            "/apps/components/gamma");

    servlet.doGet(context.request(), context.response());

    final String body = context.response().getOutputAsString();
    assertTrue(body, body.contains("\"/apps/components/alpha\":{\"errorMessages\":"
                                   + "[\"broken: /apps/components/alpha\"]"));
    assertTrue(body, body.contains("\"/apps/components/beta\":{\"errorMessages\":"
                                   + "[\"broken: /apps/components/beta\"]"));
    assertTrue(body, body.contains("\"/apps/components/gamma\":{\"errorMessages\":"
                                   + "[\"broken: /apps/components/gamma\"]"));
  }

  @Test
  public void testDoGetFallsBackToTheAddressedResourcesChildren() throws Exception {
    context.create().resource("/content/list/one");
    context.create().resource("/content/list/two");
    respondPerResource();
    registerServlet();
    context.request().setResource(context.resourceResolver().getResource("/content/list"));

    servlet.doGet(context.request(), context.response());

    final String body = context.response().getOutputAsString();
    assertTrue(body, body.contains("\"/content/list/one\":"));
    assertTrue(body, body.contains("\"/content/list/two\":"));
  }

  @Test
  public void testDoGetOmitsRowsTheRequestsResolverWillNotReturn() throws Exception {
    // ACL enforcement lives in the resolver: a caller who cannot read a row gets null back for
    // it. What this servlet owes is to omit that row entirely rather than report it as a row
    // with no messages, which would leak that it exists and is valid.
    //
    // This mocks the refusal rather than provoking one. The same check written against a real
    // Oak repository, a real user and a real jcr:read deny passes on JDK 11 but cannot run on
    // the verification gate: sling-mock-oak needs java.security.acl.Group, removed in JDK 14.
    // See card #660.
    context.create().resource("/apps/components/readable");
    respondPerResource();
    registerServlet();

    final ResourceResolver resolver = mock(ResourceResolver.class);
    when(resolver.getResource("/apps/components/readable")).thenReturn(
            context.resourceResolver().getResource("/apps/components/readable"));
    when(resolver.getResource("/apps/components/forbidden")).thenReturn(null);
    final SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
    when(request.getResourceResolver()).thenReturn(resolver);
    when(request.getParameterValues(ValidationListServlet.PARAMETER_PATH)).thenReturn(
            new String[]{"/apps/components/readable", "/apps/components/forbidden"});

    servlet.doGet(request, context.response());

    final String body = context.response().getOutputAsString();
    assertTrue(body, body.contains("\"/apps/components/readable\":"));
    assertFalse(body, body.contains("forbidden"));
  }

  @Test
  public void testDoGetWhenTheListIsEmpty() throws Exception {
    registerServlet();
    context.request().setResource(context.resourceResolver().getResource("/content/list"));

    servlet.doGet(context.request(), context.response());

    assertEquals("{}", context.response().getOutputAsString());
  }

  @Test
  public void testDoGetWhenValidationServiceIsUnavailable() throws Exception {
    final Resource row = context.create().resource("/content/list/one");
    context.registerInjectActivateService(servlet);
    context.request().setResource(context.resourceResolver().getResource("/content/list"));

    servlet.doGet(context.request(), context.response());

    assertEquals("{\"" + row.getPath() + "\":{\"errorMessages\":[],\"warningMessages\":[],"
                 + "\"infoMessages\":[]}}", context.response().getOutputAsString());
  }

  @Test
  public void testGetModelValidationService() {
    registerServlet();

    assertEquals(validationService, servlet.getModelValidationService());
  }

  @Test
  public void testGetModelFactory() {
    registerServlet();

    assertEquals(context.getService(
            org.apache.sling.models.factory.ModelFactory.class), servlet.getModelFactory());
  }

}
