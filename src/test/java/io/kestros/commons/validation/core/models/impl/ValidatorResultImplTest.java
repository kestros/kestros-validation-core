package io.kestros.commons.validation.core.models.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import io.kestros.commons.structuredslingmodels.BaseResource;
import io.kestros.commons.validation.api.ModelValidationMessageType;
import io.kestros.commons.validation.api.models.DocumentedModelValidator;
import io.kestros.commons.validation.api.models.ModelValidator;
import io.kestros.commons.validation.api.models.ModelValidatorBundle;
import io.kestros.commons.validation.core.models.impl.ValidatorResultImpl;
import io.kestros.commons.validation.core.samples.SampleDocumentedModelValidator;
import io.kestros.commons.validation.core.samples.SampleModelValidator;
import io.kestros.commons.validation.core.samples.SampleModelValidatorBundle;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ValidatorResultImplTest {

  private ValidatorResultImpl result;
  private ModelValidator modelValidator;
  private ModelValidatorBundle modelValidatorBundle;
  private DocumentedModelValidator documentedModelValidator;
  private BaseResource model;
  private List<ModelValidator> bundledLevel1 = new ArrayList<>();
  private List<ModelValidator> bundledLevel2 = new ArrayList<>();


  @Before
  public void setUp() throws Exception {
    model = mock(BaseResource.class);
  }

  @Test
  public void testIsValid() {
    modelValidator = new SampleModelValidator(true, "", "", ModelValidationMessageType.INFO);
    result = new ValidatorResultImpl(modelValidator, model);
    assertTrue(result.isValid());
  }

  @Test
  public void testIsValidWhenValidatorIsFalse() {
    modelValidator = new SampleModelValidator(false, "", "", ModelValidationMessageType.INFO);
    result = new ValidatorResultImpl(modelValidator, model);
    assertFalse(result.isValid());
  }

  @Test
  public void testGetMessage() {
    modelValidator = new SampleModelValidator(true, "Test Message", "",
            ModelValidationMessageType.INFO);
    result = new ValidatorResultImpl(modelValidator, model);
    assertEquals("Test Message", result.getMessage());
  }

  @Test
  public void testGetDetailedMessage() {
    modelValidator = new SampleModelValidator(true, "", "Test Detailed Message",
            ModelValidationMessageType.INFO);
    result = new ValidatorResultImpl(modelValidator, model);
    assertEquals("Test Detailed Message", result.getDetailedMessage());
  }

  @Test
  public void testGetDocumentationResourceType() {
    documentedModelValidator = new SampleDocumentedModelValidator(true, "", "",
            ModelValidationMessageType.INFO, "Test Resource Type");
    result = new ValidatorResultImpl(documentedModelValidator, model);
    assertEquals("Test Resource Type", result.getDocumentationResourceType());
  }

  @Test
  public void testGetBundled() {
    List<ModelValidator> bundled = new ArrayList<>();
    ModelValidator bundledValidator1 = new SampleModelValidator(true, "Bundled Message 1", "",
            ModelValidationMessageType.WARNING);
    ModelValidator bundledValidator2 = new SampleModelValidator(true, "Bundled Message 2", "",
            ModelValidationMessageType.WARNING);

    bundled.add(bundledValidator1);
    bundled.add(bundledValidator2);

    modelValidatorBundle = new SampleModelValidatorBundle(bundled, "Bundle root message", true);
    result = new ValidatorResultImpl(modelValidatorBundle, model);
    assertEquals(2, result.getBundled().size());
  }

  @Test
  public void testGetValidatorClassPath() {
    modelValidator = new SampleModelValidator(true, "", "", ModelValidationMessageType.INFO);
    result = new ValidatorResultImpl(modelValidator, model);
    assertEquals("io.kestros.commons.validation.core.samples.SampleModelValidator", result.getValidatorClassPath());
  }

  @Test
  public void testGetMessages() {
    modelValidator = new SampleModelValidator(false, "Test Message", "",
            ModelValidationMessageType.WARNING);

    result = new ValidatorResultImpl(modelValidator, model);

    assertEquals(1, result.getMessages().size());
    assertEquals(1, result.getMessages().get(ModelValidationMessageType.WARNING).size());
    assertEquals("Test Message",
            result.getMessages().get(ModelValidationMessageType.WARNING).get(0));
  }

  @Test
  public void testGetMessagesWhenBundledWhenWarningFails() {
    List<ModelValidator> bundled = new ArrayList<>();
    ModelValidator bundledValidator1 = new SampleModelValidator(true, "Bundled Message 1", "",
            ModelValidationMessageType.ERROR);
    ModelValidator bundledValidator2 = new SampleModelValidator(false, "Bundled Message 2", "",
            ModelValidationMessageType.WARNING);
    bundled.add(bundledValidator1);
    bundled.add(bundledValidator2);

    modelValidatorBundle = new SampleModelValidatorBundle(bundled, "Bundle root message", true);
    result = new ValidatorResultImpl(modelValidatorBundle, model);

    assertEquals(1, result.getMessages().size());
    assertEquals(1, result.getMessages().get(ModelValidationMessageType.WARNING).size());
    assertNull(result.getMessages().get(ModelValidationMessageType.ERROR));
  }

  @Test
  public void testGetMessagesWhenBundledWhenErrorFails() {
    List<ModelValidator> bundled = new ArrayList<>();
    ModelValidator bundledValidator1 = new SampleModelValidator(false, "Bundled Message 1", "",
            ModelValidationMessageType.ERROR);
    ModelValidator bundledValidator2 = new SampleModelValidator(true, "Bundled Message 2", "",
            ModelValidationMessageType.WARNING);
    bundled.add(bundledValidator1);
    bundled.add(bundledValidator2);

    modelValidatorBundle = new SampleModelValidatorBundle(bundled, "Bundle root message", true);
    result = new ValidatorResultImpl(modelValidatorBundle, model);

    assertEquals(1, result.getMessages().size());
    assertEquals(1, result.getMessages().get(ModelValidationMessageType.ERROR).size());
    assertNull(result.getMessages().get(ModelValidationMessageType.WARNING));

  }

  @Test
  public void testGetMessagesWhenBundledWhenBothFail() {
    List<ModelValidator> bundled = new ArrayList<>();
    ModelValidator bundledValidator1 = new SampleModelValidator(false, "Bundled Message 1", "",
            ModelValidationMessageType.ERROR);
    ModelValidator bundledValidator2 = new SampleModelValidator(false, "Bundled Message 2", "",
            ModelValidationMessageType.WARNING);
    bundled.add(bundledValidator1);
    bundled.add(bundledValidator2);

    modelValidatorBundle = new SampleModelValidatorBundle(bundled, "Bundle root message", true);
    result = new ValidatorResultImpl(modelValidatorBundle, model);

    assertEquals(2, result.getMessages().size());
    assertEquals(1, result.getMessages().get(ModelValidationMessageType.ERROR).size());
    assertEquals(1, result.getMessages().get(ModelValidationMessageType.WARNING).size());
  }

  @Test
  public void testGetFlatMessages() {
  }
}