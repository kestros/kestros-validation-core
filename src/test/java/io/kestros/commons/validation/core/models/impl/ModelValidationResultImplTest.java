package io.kestros.commons.validation.core.models.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import io.kestros.commons.structuredslingmodels.BaseResource;
import io.kestros.commons.validation.api.ModelValidationMessageType;
import io.kestros.commons.validation.api.models.ModelValidator;
import io.kestros.commons.validation.core.models.impl.ModelValidationResultImpl;
import io.kestros.commons.validation.core.samples.SampleModelValidator;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

public class ModelValidationResultImplTest {

  private ModelValidationResultImpl result;

  private List<ModelValidator> validatorList = new ArrayList<>();

  private BaseResource resource;


  @Before
  public void setUp() throws Exception {
    resource = mock(BaseResource.class);
  }

  @Test
  public void testGetResults() {
    SampleModelValidator validator1 = new SampleModelValidator(true, "Message 1",
            "Detailed Message 1", ModelValidationMessageType.ERROR);
    SampleModelValidator validator2 = new SampleModelValidator(true, "Message 2",
            "Detailed Message 2", ModelValidationMessageType.ERROR);

    validatorList.add(validator1);
    validatorList.add(validator2);

    result = new ModelValidationResultImpl(resource, validatorList);

    assertEquals(2, result.getResults().size());
    assertEquals(validator1.getMessage(), result.getResults().get(0).getMessage());
    assertTrue(result.getResults().get(0).isValid());
    assertEquals(validator2.getMessage(), result.getResults().get(1).getMessage());
    assertTrue(result.getResults().get(1).isValid());
  }

  @Test
  public void testGetModel() {
    result = new ModelValidationResultImpl(resource, validatorList);
    assertEquals(resource, result.getModel());
  }

  @Test
  public void testGetValidators() {
    SampleModelValidator validator1 = new SampleModelValidator(true, "Message 1",
            "Detailed Message 1", ModelValidationMessageType.ERROR);
    SampleModelValidator validator2 = new SampleModelValidator(true, "Message 2",
            "Detailed Message 2", ModelValidationMessageType.ERROR);
    validatorList.add(validator1);
    validatorList.add(validator2);

    result = new ModelValidationResultImpl(resource, validatorList);
    assertEquals(2, result.getValidators().size());
  }

  @Test
  public void testIsValidWhenValid() {
    SampleModelValidator validator1 = new SampleModelValidator(true, "Message 1",
            "Detailed Message 1", ModelValidationMessageType.ERROR);
    SampleModelValidator validator2 = new SampleModelValidator(true, "Message 2",
            "Detailed Message 2", ModelValidationMessageType.ERROR);

    validatorList.add(validator1);
    validatorList.add(validator2);

    result = new ModelValidationResultImpl(resource, validatorList);

    assertTrue(result.isValid());
  }

  @Test
  public void testIsValidWhenInvalid() {
    SampleModelValidator validator1 = new SampleModelValidator(true, "Message 1",
            "Detailed Message 1", ModelValidationMessageType.ERROR);
    SampleModelValidator validator2 = new SampleModelValidator(false, "Message 2",
            "Detailed Message 2", ModelValidationMessageType.ERROR);

    validatorList.add(validator1);
    validatorList.add(validator2);

    result = new ModelValidationResultImpl(resource, validatorList);

    assertFalse(result.isValid());
  }

  @Test
  public void testGetMessagesWhenAllValid() {
    SampleModelValidator validator1 = new SampleModelValidator(true, "Message 1",
            "Detailed Message 1", ModelValidationMessageType.ERROR);
    SampleModelValidator validator2 = new SampleModelValidator(true, "Message 2",
            "Detailed Message 2", ModelValidationMessageType.ERROR);

    validatorList.add(validator1);
    validatorList.add(validator2);

    result = new ModelValidationResultImpl(resource, validatorList);

    assertEquals(0, result.getMessages().size());
  }

  @Test
  public void testGetMessagesWhenInvalid() {
    SampleModelValidator validator1 = new SampleModelValidator(true, "Message 1",
            "Detailed Message 1", ModelValidationMessageType.ERROR);
    SampleModelValidator validator2 = new SampleModelValidator(false, "Message 2",
            "Detailed Message 2", ModelValidationMessageType.ERROR);

    validatorList.add(validator1);
    validatorList.add(validator2);

    result = new ModelValidationResultImpl(resource, validatorList);

    assertEquals(1, result.getMessages().size());
    assertEquals(1, result.getMessages().get(ModelValidationMessageType.ERROR).size());
    assertEquals(validator2.getMessage(), result.getMessages().get(ModelValidationMessageType.ERROR).get(0));
  }
}