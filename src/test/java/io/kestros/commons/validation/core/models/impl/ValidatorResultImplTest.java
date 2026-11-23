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
  public void testGetBundledDoesNotAccumulateAcrossRendersOfSameBundle() {
    // Bundle instances are cached and reused for every render. Rendering the same bundle more than
    // once must not duplicate its child validators in the validation map.
    List<ModelValidator> bundled = new ArrayList<>();
    bundled.add(new SampleModelValidator(true, "Bundled Message 1", "",
            ModelValidationMessageType.WARNING));
    bundled.add(new SampleModelValidator(true, "Bundled Message 2", "",
            ModelValidationMessageType.WARNING));

    modelValidatorBundle = new SampleModelValidatorBundle(bundled, "Bundle root message", true);

    ValidatorResultImpl firstRender = new ValidatorResultImpl(modelValidatorBundle, model);
    assertEquals(2, firstRender.getBundled().size());

    // Same bundle instance, second render — previously grew to 4 (then 6, 8, ...) on each render.
    ValidatorResultImpl secondRender = new ValidatorResultImpl(modelValidatorBundle, model);
    assertEquals(2, secondRender.getBundled().size());

    ValidatorResultImpl thirdRender = new ValidatorResultImpl(modelValidatorBundle, model);
    assertEquals(2, thirdRender.getBundled().size());

    // The cached bundle's own child list must also stay flat.
    assertEquals(2, modelValidatorBundle.getValidators().size());
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
  public void testGetMessagesWhenBundledAndTwoChildrenFailAtTheSameSeverity() {
    // The existing bundle test uses one ERROR child and one WARNING child, so it passes whether
    // the messages are merged or overwritten. Two children at the SAME severity is the case the
    // card's criterion 2 names and the only one that exercises the merge here.
    //
    // Fails if: the merge in ValidatorResultImpl is reverted to putAll. The ERROR list then holds
    // only "Bundled Message 2" and the size assertion reads 1.
    final List<ModelValidator> bundled = new ArrayList<>();
    bundled.add(new SampleModelValidator(false, "Bundled Message 1", "",
            ModelValidationMessageType.ERROR));
    bundled.add(new SampleModelValidator(false, "Bundled Message 2", "",
            ModelValidationMessageType.ERROR));

    modelValidatorBundle = new SampleModelValidatorBundle(bundled, "Bundle root message", true);
    result = new ValidatorResultImpl(modelValidatorBundle, model);

    final List<String> errors = result.getMessages().get(ModelValidationMessageType.ERROR);
    assertEquals(2, errors.size());
    assertTrue(errors.contains("Bundled Message 1"));
    assertTrue(errors.contains("Bundled Message 2"));
  }

  @Test
  public void testGetMessagesReturnsCopiesTheCallerCannotUseToEditTheResult() {
    // A bundle's message lists are mutable ArrayLists built in the constructor, and getMessages()
    // copies the map but not the lists — so a caller that adds to a returned list edits the
    // result. A plain validator stores a Collections.singletonList and would throw on add, which
    // proves immutability rather than exercising the aliasing, so the bundle is the case to use.
    //
    // Fails if: getMessages() hands back the field's own list. The second call then reports 2
    // errors instead of 1.
    final List<ModelValidator> bundled = new ArrayList<>();
    bundled.add(new SampleModelValidator(false, "Bundled Message 1", "",
            ModelValidationMessageType.ERROR));

    modelValidatorBundle = new SampleModelValidatorBundle(bundled, "Bundle root message", true);
    result = new ValidatorResultImpl(modelValidatorBundle, model);

    result.getMessages().get(ModelValidationMessageType.ERROR).add("Injected error");

    assertEquals(1, result.getMessages().get(ModelValidationMessageType.ERROR).size());
    assertFalse(result.getMessages().get(ModelValidationMessageType.ERROR).contains(
            "Injected error"));
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