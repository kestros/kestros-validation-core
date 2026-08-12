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
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.kestros.commons.validation.api.ModelValidationMessageType;
import io.kestros.commons.validation.api.models.ModelValidationResult;
import io.kestros.commons.validation.api.services.ModelValidationService;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

public class ValidationServletTest {

  @Rule
  public SlingContext context = new SlingContext();

  private ValidationServlet servlet;
  private ModelValidationService validationService;
  private Resource resource;

  @Before
  public void setUp() throws Exception {
    context.addModelsForPackage("io.kestros.commons.structuredslingmodels");
    servlet = new ValidationServlet();
    validationService = mock(ModelValidationService.class);
    resource = context.create().resource("/content/page");
    context.request().setResource(resource);
  }

  private void registerServlet() {
    context.registerService(ModelValidationService.class, validationService);
    context.registerInjectActivateService(servlet);
  }

  private void respondWith(final List<String> errors, final List<String> warnings) {
    final Map<ModelValidationMessageType, List<String>> messages = new EnumMap<>(
            ModelValidationMessageType.class);
    messages.put(ModelValidationMessageType.ERROR, errors);
    messages.put(ModelValidationMessageType.WARNING, warnings);
    messages.put(ModelValidationMessageType.INFO, Collections.emptyList());
    final ModelValidationResult result = mock(ModelValidationResult.class);
    when(result.getMessages()).thenReturn(messages);
    when(validationService.validate(any())).thenReturn(result);
  }

  @Test
  public void testDoGet() throws Exception {
    respondWith(Arrays.asList("first error", "second error"),
            Collections.singletonList("a warning"));
    registerServlet();

    servlet.doGet(context.request(), context.response());

    assertEquals("{\"errorMessages\":[\"first error\",\"second error\"],"
                 + "\"warningMessages\":[\"a warning\"],\"infoMessages\":[]}",
            context.response().getOutputAsString());
  }

  @Test
  public void testDoGetReturnsArraysAndNotCounts() throws Exception {
    // The badge client reads `.length` off whatever it is handed. A count would come back as a
    // bare number, `.length` would be undefined and every badge would hide itself instead of
    // showing the five errors that are actually there.
    respondWith(Arrays.asList("one", "two", "three", "four", "five"), Collections.emptyList());
    registerServlet();

    servlet.doGet(context.request(), context.response());

    final String body = context.response().getOutputAsString();
    assertTrue(body, body.contains("\"errorMessages\":[\"one\",\"two\",\"three\",\"four\",\"five\"]"));
    assertTrue(body, body.contains("\"warningMessages\":[]"));
  }

  @Test
  public void testDoGetWhenResourceHasNoMessages() throws Exception {
    respondWith(Collections.emptyList(), Collections.emptyList());
    registerServlet();

    servlet.doGet(context.request(), context.response());

    assertEquals("{\"errorMessages\":[],\"warningMessages\":[],\"infoMessages\":[]}",
            context.response().getOutputAsString());
  }

  @Test
  public void testDoGetWhenValidationServiceIsUnavailable() throws Exception {
    context.registerInjectActivateService(servlet);

    servlet.doGet(context.request(), context.response());

    assertEquals("{\"errorMessages\":[],\"warningMessages\":[],\"infoMessages\":[]}",
            context.response().getOutputAsString());
  }

  @Test
  public void testDoGetSetsJsonContentType() throws Exception {
    respondWith(Collections.emptyList(), Collections.emptyList());
    registerServlet();

    servlet.doGet(context.request(), context.response());

    assertEquals("application/json;charset=UTF-8", context.response().getContentType());
    assertEquals("UTF-8", context.response().getCharacterEncoding());
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
