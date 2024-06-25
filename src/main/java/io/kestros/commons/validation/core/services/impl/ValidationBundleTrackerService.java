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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.kestros.commons.validation.api.services.ModelValidatorRegistrationHandlerService;
import javax.annotation.Nonnull;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tracks bundles and registers ModelValidators as they are added.
 */
@SuppressFBWarnings("IMC_IMMATURE_CLASS_NO_TOSTRING")
@Component(immediate = true,
        service = ValidationBundleTrackerService.class)
public class ValidationBundleTrackerService implements
        BundleTrackerCustomizer<ServiceRegistration[]> {

  private static final Logger LOG = LoggerFactory.getLogger(
          ValidationBundleTrackerService.class);
  @Reference(cardinality = ReferenceCardinality.OPTIONAL,
          policyOption = ReferencePolicyOption.GREEDY)
  private ModelValidatorRegistrationHandlerService modelValidatorRegistrationHandlerService;

  private BundleTracker bundleTracker;

  /**
   * Activates the ValidationBundleTrackerService.
   *
   * @param ctx ComponentContext.
   */
  @Activate
  public void activate(@Nonnull final ComponentContext ctx) {
    LOG.info("Activating ValidationBundleTrackerService.");
    BundleContext bundleContext = ctx.getBundleContext();
    // TODO is this the most efficient way to track bundles?
    this.bundleTracker = new BundleTracker<>(bundleContext, Bundle.ACTIVE, this);
    this.bundleTracker.open();
  }

  /**
   * Deactivates the ValidationBundleTrackerService.
   */
  @Deactivate
  public void deactivate() {
    LOG.info("Deactivating ValidationBundleTrackerService.");
    this.bundleTracker.close();
  }

  @Nonnull
  @Override
  public ServiceRegistration[] addingBundle(@Nonnull Bundle bundle,
          @Nonnull BundleEvent bundleEvent) {
    modelValidatorRegistrationHandlerService.registerAllValidatorsFromAllServices();
    return new ServiceRegistration[0];
  }

  @Override
  public void modifiedBundle(@Nonnull Bundle bundle, @Nonnull BundleEvent bundleEvent,
          ServiceRegistration[] serviceRegistrations) {
    modelValidatorRegistrationHandlerService.registerAllValidatorsFromAllServices();
  }

  @Override
  public void removedBundle(@Nonnull Bundle bundle, @Nonnull BundleEvent bundleEvent,
          ServiceRegistration[] serviceRegistrations) {
    modelValidatorRegistrationHandlerService.registerAllValidatorsFromAllServices();

  }
}
