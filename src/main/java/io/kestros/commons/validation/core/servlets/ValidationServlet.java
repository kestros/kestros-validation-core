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

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.kestros.commons.validation.api.services.ModelValidationService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.Servlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Serves the validation messages for the resource it is addressed with, at
 * <code>&lt;path&gt;.validation.json</code>.
 *
 * <p>The endpoint is a selector on the resource rather than a path-bound servlet so that Sling
 * resolves the resource before any of this code runs: a caller who cannot read the resource is
 * refused without a permission check of our own.</p>
 *
 * <p>The deprecated <code>kestros-site-administration-legacy</code> bundle registers a servlet on
 * the same selector, extension and resource type. Where both are deployed the higher
 * <code>service.ranking</code> below is what makes this one answer.</p>
 */
@SuppressFBWarnings({"IMC_IMMATURE_CLASS_NO_TOSTRING", "SE_TRANSIENT_FIELD_NOT_RESTORED"})
@Component(service = {Servlet.class},
        property = {"sling.servlet.resourceTypes=sling/servlet/default",
                "sling.servlet.selectors=validation", "sling.servlet.extensions=json",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET,
                "service.ranking:Integer=100"})
public class ValidationServlet extends SlingSafeMethodsServlet {

  private static final long serialVersionUID = 3884281365274580315L;

  @Reference(cardinality = ReferenceCardinality.OPTIONAL,
          policyOption = ReferencePolicyOption.GREEDY)
  private transient ModelFactory modelFactory;

  @Reference(cardinality = ReferenceCardinality.OPTIONAL,
          policyOption = ReferencePolicyOption.GREEDY)
  private transient ModelValidationService modelValidationService;

  /**
   * Model factory used to find the closest matching model type for the requested resource.
   *
   * @return Model factory used to find the closest matching model type for the requested
   *         resource.
   */
  @Nullable
  public ModelFactory getModelFactory() {
    return modelFactory;
  }

  /**
   * Service that runs the validators.
   *
   * @return Service that runs the validators.
   */
  @Nullable
  public ModelValidationService getModelValidationService() {
    return modelValidationService;
  }

  @Override
  protected void doGet(@Nonnull final SlingHttpServletRequest request,
          @Nonnull final SlingHttpServletResponse response) throws IOException {
    response.setContentType("application/json");
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.getWriter().write(new ObjectMapper().writeValueAsString(
            ValidationMessages.forResource(request.getResource(), getModelFactory(),
                    getModelValidationService())));
  }

}
