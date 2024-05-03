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

package io.kestros.commons.validation.core.samples;

import io.kestros.commons.validation.api.models.ModelValidator;
import io.kestros.commons.validation.api.models.ModelValidatorBundle;
import java.util.List;

public class SampleModelValidatorBundle extends ModelValidatorBundle {
  private List<ModelValidator> bundledValidators;
  private String message;

  private boolean allMustBeTrue;

  public SampleModelValidatorBundle(List<ModelValidator> bundledValidators, String message,
          boolean allMustBeTrue) {
    this.bundledValidators = bundledValidators;
    this.message = message;
    this.allMustBeTrue = allMustBeTrue;
    this.registerValidators();
  }

  @Override
  public String getMessage() {
    return message;
  }


  @Override
  public void registerValidators() {
    addAllValidators(bundledValidators);
  }

  @Override
  public boolean isAllMustBeTrue() {
    return allMustBeTrue;
  }
}
