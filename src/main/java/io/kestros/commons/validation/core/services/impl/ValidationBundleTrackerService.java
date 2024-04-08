package io.kestros.commons.validation.core.services.impl;

import io.kestros.commons.validation.api.services.ModelValidatorRegistrationHandlerService;
import java.util.HashMap;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.util.tracker.BundleTracker;
import org.osgi.util.tracker.BundleTrackerCustomizer;

@Component(immediate = true,
        service = ValidationBundleTrackerService.class)
public class ValidationBundleTrackerService implements
        BundleTrackerCustomizer<ServiceRegistration[]> {

  @Reference(cardinality = ReferenceCardinality.OPTIONAL,
          policyOption = ReferencePolicyOption.GREEDY)
  private ModelValidatorRegistrationHandlerService modelValidatorRegistrationHandlerService;

  private ComponentContext componentContext;
  private BundleTracker bundleTracker;
  private BundleContext bundleContext;
  @Activate
  public void activate(final ComponentContext ctx) {
    this.componentContext = ctx;
    this.bundleContext = ctx.getBundleContext();
    this.bundleTracker = new BundleTracker<>(bundleContext, Bundle.ACTIVE, this);
    this.bundleTracker.open();
  }

  @Override
  public ServiceRegistration[] addingBundle(Bundle bundle, BundleEvent bundleEvent) {
    modelValidatorRegistrationHandlerService.registerAllValidatorsFromAllServices();
    return new ServiceRegistration[0];
  }

  @Override
  public void modifiedBundle(Bundle bundle, BundleEvent bundleEvent,
          ServiceRegistration[] serviceRegistrations) {
    modelValidatorRegistrationHandlerService.registerAllValidatorsFromAllServices();
  }

  @Override
  public void removedBundle(Bundle bundle, BundleEvent bundleEvent,
          ServiceRegistration[] serviceRegistrations) {
    modelValidatorRegistrationHandlerService.registerAllValidatorsFromAllServices();

  }
}
