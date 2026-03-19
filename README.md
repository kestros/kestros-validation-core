# Kestros Validation Core

Implementation of the Kestros model validation framework, providing runtime validation services and OSGi-based validator registration.

## Purpose

`kestros-validation-core` implements the interfaces defined in `kestros-validation-api`. It provides the OSGi services that execute model validation at runtime: the `ModelValidationServiceImpl` that runs validators against models, the `ModelValidatorRegistrationHandlerServiceImpl` that maintains the global validator registry, and the `ValidationBundleTrackerService` that tracks validator bundles across the OSGi lifecycle.

When a model is validated, this module looks up all registered validators for the model's type, executes each one, and returns a `ModelValidationResult` with the aggregated outcomes. Validators are registered dynamically via OSGi services that extend `BaseModelValidationRegistrationService` from the API module.

## Installation & Build

**Maven coordinates:**

```xml
<dependency>
  <groupId>io.kestros.commons</groupId>
  <artifactId>kestros-validation-core</artifactId>
  <version>0.2.3-SNAPSHOT</version>
</dependency>
```

**Build:**

```bash
mvn clean package
```

**Deploy to a Sling instance:**

```bash
curl -u admin:admin \
  -F "action=install" \
  -F "bundlestart=true" \
  -F "bundlefile=@target/kestros-validation-core-0.2.3-SNAPSHOT.jar" \
  "http://localhost:8080/system/console/bundles"
```

## Configuration

No OSGi configuration required. All services register automatically via OSGi Declarative Services annotations.

## API / Service Usage

### Service Implementations

#### `ModelValidationServiceImpl`

Implements `ModelValidationService`. The primary entry point for validating models.

```java
@Reference
private ModelValidationService validationService;

public void validateMyModel(MyResource model) {
    ModelValidationResult result = validationService.validate(model);
    if (!result.isValid()) {
        Map<ModelValidationMessageType, List<String>> messages = result.getMessages();
        // Handle validation failures
    }
}
```

#### `ModelValidatorRegistrationHandlerServiceImpl`

Implements `ModelValidatorRegistrationHandlerService`. Maintains the global map of model types to their registered validators. Validators are registered and unregistered dynamically as OSGi bundles are activated and deactivated.

#### `ModelValidatorProviderServiceImpl`

Implements `ModelValidatorProviderService`. Provides the list of validators applicable to a given model type by querying the registration handler.

#### `ValidationBundleTrackerService`

Tracks OSGi bundles that contain validator registration services and ensures validators are registered when bundles start and unregistered when they stop.

#### `ModelValidationActivateStatusService`

Tracks the activation status of the validation framework itself.

### Model Implementations

#### `ModelValidationResultImpl`

Implements `ModelValidationResult`. Holds the list of `ValidatorResult` instances produced by running all registered validators against a model.

#### `ValidatorResultImpl`

Implements `ValidatorResult`. Represents the outcome of a single validator execution, including pass/fail status, messages, severity level, and any bundled sub-results.

### How Validators Are Registered

1. Create a class extending `BaseModelValidationRegistrationService` (from `kestros-validation-api`)
2. Override `getModelType()` to return the model class the validators apply to
3. Override `getModelValidators()` to return the list of `ModelValidator` instances
4. Inject `ModelValidatorRegistrationHandlerService` via `@Reference`
5. Register as an OSGi component with `service = ModelValidatorRegistrationService.class`

On activation, the base class calls `registerAllValidatorsFromService(this)` on the handler, which adds the validators to the global registry. On deactivation, they are removed.

**Example:**

```java
@Component(service = ModelValidatorRegistrationService.class, immediate = true)
public class MyResourceValidationRegistrationService
        extends BaseModelValidationRegistrationService {

    @Reference
    private ModelValidatorRegistrationHandlerService handler;

    @Override
    public Class<? extends BaseSlingModel> getModelType() {
        return MyResource.class;
    }

    @Override
    public List<ModelValidator> getModelValidators() {
        return List.of(
            CommonValidators.hasTitle(),
            myCustomValidator()
        );
    }

    @Override
    public ModelValidatorRegistrationHandlerService getModelValidatorRegistrationHandlerService() {
        return handler;
    }
}
```

### Validation Lifecycle

```
Model adapted → validate(model) called
  → ModelValidationServiceImpl looks up validators for model.getClass()
  → Each ModelValidator.isValidCheck(model) is called
  → Results aggregated into ModelValidationResultImpl
  → isValid() returns false if any ERROR-level validator failed
```

## Dependencies

### Upstream

| Dependency | Maven Coordinates |
|------------|-------------------|
| kestros-validation-api | `io.kestros.commons:kestros-validation-api:[0.2.2,0.2.99]` |
| kestros-structured-sling-models | `io.kestros.commons:kestros-structured-sling-models:[0.2.5,0.2.99]` |
| kestros-osgi-service-utils | `io.kestros.commons:kestros-osgi-service-utils:[0.1.10,0.1.99]` |

### Downstream

Modules that need runtime model validation depend on this bundle:

- `kestros-site-management-core`
- `kestros-component-types-core`
- `kestros-content-objects` (via validation services)
- Any module that calls `ModelValidationService.validate()`

## Contribution Notes

- **Branch from:** `develop`
- **PR target:** `develop`
- **Branch naming:** `{type}/TASK-NNN-short-description` (e.g. `fix/TASK-085-validation-null-model`)
- **Commit format:** `[kestros-validation-core]: <action>, <brief result>`
- **Build verification:** Run `mvn clean package` before submitting; all tests must pass
