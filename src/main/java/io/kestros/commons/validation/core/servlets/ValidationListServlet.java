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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.Servlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.models.factory.ModelFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Serves the validation messages for every row of a list in a single response, at
 * <code>&lt;list-path&gt;.validation-list.json</code>.
 *
 * <p>A table of N rows would otherwise make N requests to
 * {@link ValidationServlet}, and each of those runs every registered validator.</p>
 *
 * <p>Rows are named by repeating the <code>path</code> parameter — a list is rarely the children
 * of the resource it is rendered on. Where no <code>path</code> is given the addressed resource's
 * own children are used.</p>
 *
 * <p>The response is keyed by resource path so that one response can feed every badge group on the
 * page without any of them being handed another row's messages.</p>
 */
@SuppressFBWarnings({"IMC_IMMATURE_CLASS_NO_TOSTRING", "SE_TRANSIENT_FIELD_NOT_RESTORED"})
@Component(service = {Servlet.class},
        property = {"sling.servlet.resourceTypes=sling/servlet/default",
                "sling.servlet.selectors=validation-list", "sling.servlet.extensions=json",
                "sling.servlet.methods=" + HttpConstants.METHOD_GET,
                "service.ranking:Integer=100"})
public class ValidationListServlet extends SlingSafeMethodsServlet {

  /**
   * Request parameter naming a row to report on. Repeat it once per row.
   */
  public static final String PARAMETER_PATH = "path";

  private static final long serialVersionUID = -343934872327979L;

  @Reference(cardinality = ReferenceCardinality.OPTIONAL,
          policyOption = ReferencePolicyOption.GREEDY)
  private transient ModelFactory modelFactory;

  @Reference(cardinality = ReferenceCardinality.OPTIONAL,
          policyOption = ReferencePolicyOption.GREEDY)
  private transient ModelValidationService modelValidationService;

  /**
   * Model factory used to find the closest matching model type for each row.
   *
   * @return Model factory used to find the closest matching model type for each row.
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
    final List<Resource> rows = getRows(request);
    final Map<String, Object> messagesByPath = new LinkedHashMap<>(rows.size());
    for (final Resource row : rows) {
      messagesByPath.put(row.getPath(),
              ValidationMessages.forResource(row, getModelFactory(),
                      getModelValidationService()));
    }
    response.setContentType("application/json");
    response.setCharacterEncoding(StandardCharsets.UTF_8.name());
    response.getWriter().write(new ObjectMapper().writeValueAsString(messagesByPath));
  }

  /**
   * The resources this response reports on.
   *
   * <p>Every one of them is resolved through the REQUEST's resolver, never a service resolver.
   * This endpoint is addressed with one resource and reports on others, so Sling's resolve of the
   * addressed resource proves nothing about the rows: this is the only permission check they get.
   * A path the caller cannot read comes back null and is omitted, rather than being reported as a
   * row with no messages.</p>
   *
   * @param request Request being served.
   *
   * @return The resources this response reports on.
   */
  @Nonnull
  private List<Resource> getRows(@Nonnull final SlingHttpServletRequest request) {
    final List<Resource> rows = new ArrayList<>();
    final String[] paths = request.getParameterValues(PARAMETER_PATH);
    if (paths == null || paths.length == 0) {
      for (final Resource child : request.getResource().getChildren()) {
        rows.add(child);
      }
      return rows;
    }
    for (final String path : paths) {
      final Resource row = request.getResourceResolver().getResource(path);
      if (row != null) {
        rows.add(row);
      }
    }
    return rows;
  }

}
