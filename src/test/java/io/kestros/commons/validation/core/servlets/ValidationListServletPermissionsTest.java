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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
import java.util.List;
import java.util.Map;
import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.commons.jackrabbit.authorization.AccessControlUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.jcr.api.SlingRepository;
import org.apache.sling.jcr.resource.JcrResourceConstants;
import org.apache.sling.testing.mock.sling.ResourceResolverType;
import org.apache.sling.testing.mock.sling.junit.SlingContext;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * The list endpoint is addressed with one resource and reports on others, so Sling's resolve of
 * the addressed resource is not the permission check for the rows. This exercises the real thing:
 * an Oak repository, a real user, a real deny, and the request resolver that user would get.
 *
 * <p>Reading the code is not evidence for this — an administrator resolves everything, so a check
 * run as admin would pass whether or not the servlet used the request's resolver.</p>
 */
public class ValidationListServletPermissionsTest {

  private static final String USER_ID = "validation-reader";
  private static final String PASSWORD = "validation-reader";
  private static final String READABLE = "/apps/components/readable";
  private static final String FORBIDDEN = "/apps/components/forbidden";

  @Rule
  public SlingContext context = new SlingContext(ResourceResolverType.JCR_OAK);

  private ValidationListServlet servlet;
  private ResourceResolver userResolver;

  @Before
  public void setUp() throws Exception {
    context.addModelsForPackage("io.kestros.commons.structuredslingmodels");
    servlet = new ValidationListServlet();

    context.create().resource(READABLE);
    context.create().resource(FORBIDDEN);

    final Session admin = context.resourceResolver().adaptTo(Session.class);
    assertNotNull(admin);
    final User user = ((JackrabbitSession) admin).getUserManager().createUser(USER_ID, PASSWORD);
    admin.save();

    AccessControlUtils.addAccessControlEntry(admin, "/", user.getPrincipal(),
            new String[]{"jcr:read"}, true);
    AccessControlUtils.addAccessControlEntry(admin, FORBIDDEN, user.getPrincipal(),
            new String[]{"jcr:read"}, false);
    admin.save();

    final Session userSession = context.getService(SlingRepository.class).login(
            new SimpleCredentials(USER_ID, PASSWORD.toCharArray()));
    userResolver = context.getService(ResourceResolverFactory.class).getResourceResolver(
            Collections.singletonMap(JcrResourceConstants.AUTHENTICATION_INFO_SESSION,
                    (Object) userSession));

    final ModelValidationService validationService = mock(ModelValidationService.class);
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
    context.registerService(ModelValidationService.class, validationService);
    context.registerInjectActivateService(servlet);
  }

  @Test
  public void testTheDenyIsRealBeforeAnythingIsAssertedAboutTheServlet() {
    // If this fails, every other assertion in this class is worthless: it would mean the user can
    // read both rows and the servlet was never asked the question.
    assertNotNull(userResolver.getResource(READABLE));
    assertNull(userResolver.getResource(FORBIDDEN));
  }

  @Test
  public void testDoGetOmitsARowTheUserCannotRead() throws Exception {
    final SlingHttpServletRequest request = mock(SlingHttpServletRequest.class);
    when(request.getResourceResolver()).thenReturn(userResolver);
    when(request.getParameterValues(ValidationListServlet.PARAMETER_PATH)).thenReturn(
            new String[]{READABLE, FORBIDDEN});

    servlet.doGet(request, context.response());

    final String body = context.response().getOutputAsString();
    assertTrue(body, body.contains("\"" + READABLE + "\":"));
    assertFalse(body, body.contains("forbidden"));
  }

}
