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

package io.kestros.commons.validation.core.services;

import io.kestros.commons.osgiserviceutils.services.ManagedService;
import io.kestros.commons.validation.api.models.ModelValidator;
import java.util.List;
import javax.annotation.Nonnull;

/**
 * Service for retrieving and managing ModelValidators.
 */
public interface ModelValidatorProviderService extends ManagedService {

  /**
   * Activates a ModelValidator for a given class.
   *
   * @param validator ModelValidator to activate.
   * @param type Class to activate.
   */
  void activateValidator(@Nonnull final ModelValidator validator, @Nonnull final Class type);

  /**
   * Deactivates a ModelValidator for a given class.
   *
   * @param validator ModelValidator to deactivate.
   * @param type Class to deactivate.
   */
  void deactivateValidator(@Nonnull final ModelValidator validator, @Nonnull final Class type);

  /**
   * Gets all registered ModelValidators for a given class.
   *
   * @param type Class to get ModelValidators for.
   *
   * @return List of all registered ModelValidators for a given class.
   */
  @Nonnull
  List<ModelValidator> getAllValidators(@Nonnull final Class type);

  /**
   * Gets all active ModelValidators for a given class.
   *
   * @param type Class to get active ModelValidators for.
   *
   * @return List of all active ModelValidators for a given class.
   */
  @Nonnull
  List<ModelValidator> getActiveValidators(@Nonnull final Class type);

  /**
   * Gets all inactive ModelValidators for a given class.
   *
   * @param type Class to get inactive ModelValidators for.
   *
   * @return List of all inactive ModelValidators for a given class.
   */
  @Nonnull
  List<ModelValidator> getInactiveValidators(@Nonnull final Class type);
}
