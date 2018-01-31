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

package com.societegenerale.commons.amqp.core.config;


import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.rule.OutputCapture;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class DeadLetterConfigTest {

  private DeadLetterConfig deadLetterConfig;

  private DeadLetterConfig expectedDeadLetterConfig;

  private String exchangeName = "exchange-1";

  @Rule
  public OutputCapture outputCapture = new OutputCapture();

  @Test
  public void deadLetterConfigEqualsTest() {
    deadLetterConfig = DeadLetterConfig.builder().build();
    expectedDeadLetterConfig = DeadLetterConfig.builder().build();
    assertTrue(deadLetterConfig.equals(expectedDeadLetterConfig));
    deadLetterConfig.setDefaultConfigApplied(false);
    expectedDeadLetterConfig.setDefaultConfigApplied(true);
    assertTrue(deadLetterConfig.equals(expectedDeadLetterConfig));
  }

  @Test
  public void deadLetterConfigeHashcodeTest() {
    deadLetterConfig = DeadLetterConfig.builder().build();
    expectedDeadLetterConfig = DeadLetterConfig.builder().build();
    assertThat(deadLetterConfig.hashCode(), equalTo(expectedDeadLetterConfig.hashCode()));
    deadLetterConfig.setDefaultConfigApplied(false);
    expectedDeadLetterConfig.setDefaultConfigApplied(true);
    assertThat(deadLetterConfig.hashCode(), equalTo(expectedDeadLetterConfig.hashCode()));
  }

  @Test
  public void validDeadLetterConfig() {
    deadLetterConfig = DeadLetterConfig.builder()
        .deadLetterExchange(ExchangeConfig.builder().name(exchangeName).type(ExchangeTypes.TOPIC).build())
        .queuePostfix(".dlq")
        .build();
    assertTrue(deadLetterConfig.validate());
    String deadLetterQueueName = deadLetterConfig.createDeadLetterQueueName("temp-queue");
    assertThat(deadLetterQueueName, equalTo("temp-queue.dlq"));
  }

  @Test
  public void validDeadLetterConfigWithDefaultQueuePostfix() {
    deadLetterConfig = DeadLetterConfig.builder()
        .deadLetterExchange(ExchangeConfig.builder().name(exchangeName).type(ExchangeTypes.TOPIC).build())
        .build();
    assertTrue(deadLetterConfig.validate());
    String deadLetterQueueName = deadLetterConfig.createDeadLetterQueueName("temp-queue");
    assertThat(deadLetterQueueName, equalTo("temp-queue.DLQ"));
  }

  @Test
  public void invalidDeadLetterConfigWithoutExchange() {
    deadLetterConfig = DeadLetterConfig.builder()
        .deadLetterExchange(null)
        .queuePostfix(".dlq")
        .build();
    assertFalse(deadLetterConfig.validate());
  }

  @Test
  public void invalidDeadLetterConfigWithoutExchangeName() {
    deadLetterConfig = DeadLetterConfig.builder()
        .deadLetterExchange(ExchangeConfig.builder().build())
        .queuePostfix(".dlq")
        .build();
    assertFalse(deadLetterConfig.validate());
  }
}
