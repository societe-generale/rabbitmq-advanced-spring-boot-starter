/*
 * Copyright 2017-2018, Société Générale All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.societegenerale.commons.amqp.core.processor;

import brave.Tracer;
import brave.Tracing;
import brave.propagation.CurrentTraceContext;
import brave.propagation.StrictCurrentTraceContext;
import brave.propagation.TraceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Created by anand on 02/07/17.
 */
public class DefaultCorrelationPostProcessorTest {

  private DefaultCorrelationPostProcessor correlationPostProcessor;

  private Message message;

  private Tracer tracer;

  @BeforeEach
  public void setUp() {
    CurrentTraceContext currentTraceContext = new StrictCurrentTraceContext();
    currentTraceContext.newScope(TraceContext.newBuilder().traceId(10L).spanId(10L).build());
    tracer = Tracing.newBuilder()
            .currentTraceContext(currentTraceContext)
            .build().tracer();
    correlationPostProcessor = new DefaultCorrelationPostProcessor(tracer);
    message = MessageBuilder.withBody("DummyMessage".getBytes()).build();
  }

  @Test
  public void addNewCorrelationIdToHeaderIfMissingTest() {
    correlationPostProcessor.postProcessMessage(message);
    assertNotNull(message.getMessageProperties().getHeaders().get("correlation-id"));
  }

  @Test
  public void addNewCorrelationIdFromTracerToHeaderIfMissingTest() {
    correlationPostProcessor.postProcessMessage(message);
    assertNotNull(message.getMessageProperties().getHeaders().get("correlation-id"));
    assertEquals(message.getMessageProperties().getHeaders().get("correlation-id"), tracer.currentSpan().context().traceIdString());
  }

  @Test
  public void addExistingCorrelationIdToHeaderIfPresentTest() {
    message.getMessageProperties().setCorrelationId("ExistingCorrelationId");
    correlationPostProcessor.postProcessMessage(message);
    assertNotNull(message.getMessageProperties().getHeaders().get("correlation-id"));
    assertEquals(message.getMessageProperties().getHeaders().get("correlation-id"), "ExistingCorrelationId");
  }

}