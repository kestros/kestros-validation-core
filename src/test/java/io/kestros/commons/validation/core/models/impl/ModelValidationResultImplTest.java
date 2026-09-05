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

package io.kestros.commons.validation.core.models.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import io.kestros.commons.structuredslingmodels.BaseResource;
import io.kestros.commons.validation.api.ModelValidationMessageType;
import io.kestros.commons.validation.api.models.ModelValidator;
import io.kestros.commons.validation.core.models.impl.ModelValidationResultImpl;
import io.kestros.commons.validation.core.samples.SampleModelValidator;
import io.kestros.commons.validation.core.samples.SampleModelValidatorBundle;
import java.util.ArrayList;
import java.util.List;
import org.apache.felix.hc.api.FormattingResultLog;
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
  public void testGetMessagesKeepsEveryFailureAtTheSameSeverity() {
    // Two validators failing at ERROR must both be reported. The map is keyed by severity, so the
    // second one used to replace the first's list wholesale via putAll.
    //
    // Fails if: the merge at ModelValidationResultImpl is reverted to putAll — the ERROR list then
    // holds only "Message 2" and the size assertion below reads 1.
    final SampleModelValidator failing1 = new SampleModelValidator(false, "Message 1",
            "Detailed Message 1", ModelValidationMessageType.ERROR);
    final SampleModelValidator failing2 = new SampleModelValidator(false, "Message 2",
            "Detailed Message 2", ModelValidationMessageType.ERROR);

    validatorList.add(failing1);
    validatorList.add(failing2);

    result = new ModelValidationResultImpl(resource, validatorList);

    final List<String> errors = result.getMessages().get(ModelValidationMessageType.ERROR);
    assertEquals(2, errors.size());
    assertTrue(errors.contains("Message 1"));
    assertTrue(errors.contains("Message 2"));
  }

  @Test
  public void testGetMessagesDoesNotGrowABundlesOwnMessageList() {
    // The copy-in has to be tested where getting it wrong is SILENT. Two plain validators return
    // Collections.singletonList, so a reference-storing merge throws UnsupportedOperation before
    // any assertion runs — that proves the list is immutable, not that the copy works.
    //
    // A bundle's messages are a mutable ArrayList handed out by reference, so a merge that
    // retains the reference appends the standalone validator's message to the BUNDLE's own list.
    //
    // Fails if: the merge stores entry.getValue() instead of copying into a new ArrayList. The
    // bundle then reports 3 errors of its own instead of 2, on the assertion below rather than by
    // an exception thrown earlier.
    final List<ModelValidator> bundled = new ArrayList<>();
    bundled.add(new SampleModelValidator(false, "Bundled 1", "Detail 1",
            ModelValidationMessageType.ERROR));
    bundled.add(new SampleModelValidator(false, "Bundled 2", "Detail 2",
            ModelValidationMessageType.ERROR));

    validatorList.add(new SampleModelValidatorBundle(bundled, "Bundle root", true));
    validatorList.add(new SampleModelValidator(false, "Standalone", "Detail 3",
            ModelValidationMessageType.ERROR));

    result = new ModelValidationResultImpl(resource, validatorList);

    assertEquals(3, result.getMessages().get(ModelValidationMessageType.ERROR).size());
    assertEquals(2,
            result.getResults().get(0).getMessages().get(ModelValidationMessageType.ERROR).size());
  }

  @Test
  public void testGetMessagesReturnsCopiesTheCallerCannotUseToEditTheResult() {
    // getMessages() copies the map but not the lists inside it, so a caller that adds to a
    // returned list is editing the result. Cover a populated severity and an empty one — the
    // empty one is seeded by the getter on the base branch, so it aliases too.
    //
    // Fails if: getMessages() hands back the field's own lists. The second call then reports
    // 2 errors instead of 1 and 1 warning instead of 0.
    validatorList.add(new SampleModelValidator(false, "Message 1", "Detailed Message 1",
            ModelValidationMessageType.ERROR));

    result = new ModelValidationResultImpl(resource, validatorList);

    result.getMessages().get(ModelValidationMessageType.ERROR).add("Injected error");
    result.getMessages().get(ModelValidationMessageType.WARNING).add("Injected warning");

    assertEquals(1, result.getMessages().get(ModelValidationMessageType.ERROR).size());
    assertFalse(result.getMessages().get(ModelValidationMessageType.ERROR).contains(
            "Injected error"));
    assertEquals(0, result.getMessages().get(ModelValidationMessageType.WARNING).size());
    assertEquals(3, result.getMessages().size());
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

    assertEquals(3, result.getMessages().size());
    assertEquals(0, result.getMessages().get(ModelValidationMessageType.ERROR).size());
    assertEquals(0, result.getMessages().get(ModelValidationMessageType.INFO).size());
    assertEquals(0, result.getMessages().get(ModelValidationMessageType.WARNING).size());
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

    assertEquals(3, result.getMessages().size());
    assertEquals(1, result.getMessages().get(ModelValidationMessageType.ERROR).size());
    assertEquals(0, result.getMessages().get(ModelValidationMessageType.INFO).size());
    assertEquals(0, result.getMessages().get(ModelValidationMessageType.WARNING).size());
    assertEquals(validator2.getMessage(), result.getMessages().get(ModelValidationMessageType.ERROR).get(0));
  }

}