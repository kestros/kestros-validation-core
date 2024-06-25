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

import io.kestros.commons.validation.api.models.ModelValidator;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Service for managing the activation status of ModelValidators.
 */
public interface ModelValidationActivateStatusService {

  /**
   * Gets a map of all ModelValidators and their activation status for each class.
   *
   * @return Map of all ModelValidators and their activation status for each class.
   */
  @Nonnull
  Map<Class, Map<String, Boolean>> getValidatorActivationStatusMap();


  /**
   * Gets a list of all active ModelValidators for a given class.
   *
   * @param validators List of all ModelValidators.
   * @param type Class to get active ModelValidators for.
   *
   * @return List of all active ModelValidators for a given class.
   */
  @Nonnull
  List<ModelValidator> getActiveValidators(@Nullable final List<ModelValidator> validators,
          @Nullable final Class type);


  /**
   * Checks if a ModelValidator is active for a given class.
   *
   * @param validator ModelValidator to check.
   * @param type Class to check.
   *
   * @return True if the ModelValidator is active for the given class.
   */
  boolean isModelValidatorActiveForClass(@Nullable final ModelValidator validator,
          @Nullable final Class type);

  /**
   * Activates a ModelValidator for a given class.
   *
   * @param validator ModelValidator to activate.
   * @param type Class to activate
   */
  void activateValidator(@Nullable final ModelValidator validator, @Nullable final Class type);

  /**
   * Deactivates a ModelValidator for a given class.
   *
   * @param validator ModelValidator to deactivate.
   * @param type Class to deactivate.
   */
  void deactivateValidator(@Nullable final ModelValidator validator, @Nullable final Class type);
}
