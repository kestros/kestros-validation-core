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

import io.kestros.commons.structuredslingmodels.BaseResource;
import io.kestros.commons.structuredslingmodels.exceptions.MatchingResourceTypeNotFoundException;
import io.kestros.commons.structuredslingmodels.utils.SlingModelUtils;
import io.kestros.commons.validation.api.ModelValidationMessageType;
import io.kestros.commons.validation.api.models.ModelValidationResult;
import io.kestros.commons.validation.api.services.ModelValidationService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.factory.ModelFactory;

/**
 * Builds the body that both validation endpoints return for a single resource.
 *
 * <p>The value against each key is always an ARRAY of messages, never a count. The badge client
 * reads <code>.length</code> off whatever it is given, so a number would silently evaluate to
 * <code>undefined</code> and every badge would hide itself as though there were nothing to
 * report.</p>
 */
public final class ValidationMessages {

  /**
   * Response key carrying the error-level messages.
   */
  public static final String ERROR_MESSAGES = "errorMessages";

  /**
   * Response key carrying the warning-level messages.
   */
  public static final String WARNING_MESSAGES = "warningMessages";

  /**
   * Response key carrying the info-level messages.
   */
  public static final String INFO_MESSAGES = "infoMessages";

  private ValidationMessages() {
  }

  /**
   * Validation messages for a single resource, keyed by severity.
   *
   * <p>Every key is present on every response, with an empty array where there is nothing to
   * report, so a caller never has to distinguish "no messages" from "key absent".</p>
   *
   * @param resource Resource to validate. Must already have been resolved through the resolver
   *         whose permissions should apply.
   * @param modelFactory Model factory used to find the closest matching model type, which is
   *         what decides which validators run. May be null when the service is unavailable.
   * @param validationService Service that runs the validators. May be null when the service is
   *         unavailable.
   *
   * @return Validation messages for a single resource, keyed by severity.
   */
  @Nonnull
  public static Map<String, Object> forResource(@Nonnull final Resource resource,
          @Nullable final ModelFactory modelFactory,
          @Nullable final ModelValidationService validationService) {
    final Map<String, Object> messages = new LinkedHashMap<>();
    messages.put(ERROR_MESSAGES, new ArrayList<String>());
    messages.put(WARNING_MESSAGES, new ArrayList<String>());
    messages.put(INFO_MESSAGES, new ArrayList<String>());

    if (modelFactory == null || validationService == null) {
      return messages;
    }

    final ModelValidationResult result = validationService.validate(
            getClosestTypeOrBaseResource(resource, modelFactory));
    final Map<ModelValidationMessageType, List<String>> resultMessages = result.getMessages();

    messages.put(ERROR_MESSAGES,
            messagesOfType(resultMessages, ModelValidationMessageType.ERROR));
    messages.put(WARNING_MESSAGES,
            messagesOfType(resultMessages, ModelValidationMessageType.WARNING));
    messages.put(INFO_MESSAGES, messagesOfType(resultMessages, ModelValidationMessageType.INFO));
    return messages;
  }

  @Nonnull
  private static BaseResource getClosestTypeOrBaseResource(@Nonnull final Resource resource,
          @Nonnull final ModelFactory modelFactory) {
    try {
      return SlingModelUtils.getResourceAsClosestType(resource, modelFactory);
    } catch (final MatchingResourceTypeNotFoundException exception) {
      // A resource with no registered model type still has whatever validators are registered
      // against BaseResource, so answer with those rather than refusing the request. The badge
      // client treats any non-200 as an error state, which shows as a broken panel instead of
      // as "nothing to report".
      return SlingModelUtils.adaptToBaseResource(resource);
    }
  }

  @Nonnull
  private static List<String> messagesOfType(
          @Nonnull final Map<ModelValidationMessageType, List<String>> messages,
          @Nonnull final ModelValidationMessageType type) {
    final List<String> ofType = messages.get(type);
    if (ofType == null) {
      return new ArrayList<>();
    }
    return new ArrayList<>(ofType);
  }

}
