/*
 * Copyright (C) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.dataflow.sdk.runners.dataflow;

import com.google.cloud.dataflow.sdk.io.PubsubIO;
import com.google.cloud.dataflow.sdk.runners.DataflowPipelineTranslator.TransformTranslator;
import com.google.cloud.dataflow.sdk.runners.DataflowPipelineTranslator.TranslationContext;
import com.google.cloud.dataflow.sdk.util.PropertyNames;
import com.google.cloud.dataflow.sdk.util.WindowedValue;

/**
 * Pubsub transform support code for the Dataflow backend.
 */
public class PubsubIOTranslator {

  /**
   * Implements PubsubIO Read translation for the Dataflow backend.
   */
  public static class ReadTranslator implements TransformTranslator<PubsubIO.Read.Bound> {
    @Override
    public void translate(
        PubsubIO.Read.Bound transform,
        TranslationContext context) {
      translateReadHelper(transform, context);
    }

    private <T> void translateReadHelper(
        PubsubIO.Read.Bound<T> transform,
        TranslationContext context) {
      if (!context.getPipelineOptions().isStreaming()) {
        throw new IllegalArgumentException("PubsubIO can only be used in streaming mode.");
      }

      context.addStep(transform, "ParallelRead");
      context.addInput(PropertyNames.FORMAT, "pubsub");
      if (transform.getTopic() != null) {
        context.addInput(PropertyNames.PUBSUB_TOPIC, transform.getTopic());
      }
      if (transform.getSubscription() != null) {
        context.addInput(PropertyNames.PUBSUB_SUBSCRIPTION, transform.getSubscription());
      }
      if (transform.getTimestampLabel() != null) {
        context.addInput(PropertyNames.PUBSUB_TIMESTAMP_LABEL, transform.getTimestampLabel());
      }
      context.addInput(PropertyNames.PUBSUB_DROP_LATE_DATA, transform.getDropLateData());
      if (transform.getIdLabel() != null) {
        context.addInput(PropertyNames.PUBSUB_ID_LABEL, transform.getIdLabel());
      }
      context.addValueOnlyOutput(PropertyNames.OUTPUT, context.getOutput(transform));
    }
  }

  /**
   * Implements PubsubIO Write translation for the Dataflow backend.
   */
  public static class WriteTranslator implements TransformTranslator<PubsubIO.Write.Bound> {
    @Override
    public void translate(
        PubsubIO.Write.Bound transform,
        TranslationContext context) {
      translateWriteHelper(transform, context);
    }

    private <T> void translateWriteHelper(
        PubsubIO.Write.Bound<T> transform,
        TranslationContext context) {
      if (!context.getPipelineOptions().isStreaming()) {
        throw new IllegalArgumentException("PubsubIO can only be used in streaming mode.");
      }

      context.addStep(transform, "ParallelWrite");
      context.addInput(PropertyNames.FORMAT, "pubsub");
      context.addInput(PropertyNames.PUBSUB_TOPIC, transform.getTopic());
      if (transform.getTimestampLabel() != null) {
        context.addInput(PropertyNames.PUBSUB_TIMESTAMP_LABEL, transform.getTimestampLabel());
      }
      if (transform.getIdLabel() != null) {
        context.addInput(PropertyNames.PUBSUB_ID_LABEL, transform.getIdLabel());
      }
      context.addEncodingInput(WindowedValue.getValueOnlyCoder(transform.getCoder()));
      context.addInput(PropertyNames.PARALLEL_INPUT, context.getInput(transform));
    }
  }
}
