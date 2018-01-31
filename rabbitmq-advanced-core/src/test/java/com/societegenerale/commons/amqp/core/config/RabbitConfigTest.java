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


import com.societegenerale.commons.amqp.core.exception.RabbitmqConfigurationException;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class RabbitConfigTest {

  private RabbitConfig rabbitConfig;

  private RabbitConfig expectedRabbitConfig;

  private String exchange = "exchange";

  private String queue = "queue";

  private String binding = "binding";

  private String routingKey = "routingKey";

  @Rule
  public OutputCapture outputCapture = new OutputCapture();

  @Test
  public void rabbitConfigEqualsTest() {
    rabbitConfig = RabbitConfig.builder().build();
    expectedRabbitConfig = RabbitConfig.builder().build();
    assertTrue(rabbitConfig.equals(expectedRabbitConfig));
  }

  @Test
  public void rabbitConfigHashcodeTest() {
    rabbitConfig = RabbitConfig.builder().build();
    expectedRabbitConfig = RabbitConfig.builder().build();
    assertThat(rabbitConfig.hashCode(), equalTo(expectedRabbitConfig.hashCode()));
  }

  @Test
  public void defaultRabbitConfigTest() {
    rabbitConfig = RabbitConfig.builder().build();
    assertNull(rabbitConfig.getDefaultExchange());
    assertNull(rabbitConfig.getDefaultQueue());
    assertNotNull(rabbitConfig.getExchanges());
    assertNotNull(rabbitConfig.getQueues());
    assertNotNull(rabbitConfig.getBindings());
    assertTrue(CollectionUtils.isEmpty(rabbitConfig.getExchanges()));
    assertTrue(CollectionUtils.isEmpty(rabbitConfig.getQueues()));
    assertTrue(CollectionUtils.isEmpty(rabbitConfig.getBindings()));
  }

  @Test
  public void rabbitConfigWithValidExchangeAndQueueAndBindingTest() {
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(createDefaultExchangeConfig())
        .defaultQueue(createDefaultQueueConfig())
        .deadLetterConfig(createValidDeadLetterConfig())
        .exchange(exchange, createExchangeConfig(exchange))
        .queue(queue, createQueueConfig(queue))
        .binding(binding, createBinding(exchange, queue, routingKey))
        .build();
    rabbitConfig.validate();
    assertThat(outputCapture.toString(), containsString("RabbitConfig Validation done successfully"));
  }

  @Test
  public void rabbitConfigWithValidExchangeAndQueueAndBindingAndDeadLetterAndRequeueTest() {
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(createDefaultExchangeConfig())
        .defaultQueue(createDefaultQueueConfig())
        .deadLetterConfig(createValidDeadLetterConfig())
        .reQueueConfig(createReQueueConfig("requeue-exchange", "requeue"))
        .exchange(exchange, createExchangeConfig(exchange))
        .queue(queue, createQueueConfig(queue))
        .binding(binding, createBinding(exchange, queue, routingKey))
        .build();
    rabbitConfig.validate();
    assertThat(outputCapture.toString(), containsString("RabbitConfig Validation done successfully"));
  }

  @Test(expected = RabbitmqConfigurationException.class)
  public void rabbitConfigWithValidExchangeAndQueueAndBindingAndDeadLetterAndInvalidRequeueTest() {
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(createDefaultExchangeConfig())
        .defaultQueue(createDefaultQueueConfig())
        .deadLetterConfig(createValidDeadLetterConfig())
        .reQueueConfig(createReQueueConfig(null, "requeue"))
        .exchange(exchange, createExchangeConfig(exchange))
        .queue(queue, createQueueConfig(queue))
        .binding(binding, createBinding(exchange, queue, routingKey))
        .build();
    rabbitConfig.validate();
    assertThat(outputCapture.toString(), containsString("RabbitConfig Validation done successfully"));
  }

  @Test(expected = RabbitmqConfigurationException.class)
  public void rabbitConfigWithInvalidExchangeAndValidQueueAndValidBindingTest() {
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(createDefaultExchangeConfig())
        .defaultQueue(createDefaultQueueConfig())
        .deadLetterConfig(createValidDeadLetterConfig())
        .exchange(exchange, createExchangeConfig(null))
        .queue(queue, createQueueConfig(queue))
        .binding(binding, createBinding(exchange, queue, routingKey))
        .build();
    rabbitConfig.validate();
  }

  @Test(expected = RabbitmqConfigurationException.class)
  public void rabbitConfigWithValidExchangeAndInvalidQueueAndValidBindingTest() {
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(createDefaultExchangeConfig())
        .defaultQueue(createDefaultQueueConfig())
        .deadLetterConfig(createValidDeadLetterConfig())
        .exchange(exchange, createExchangeConfig(exchange))
        .queue(queue, createQueueConfig(null))
        .binding(binding, createBinding(exchange, queue, routingKey))
        .build();
    rabbitConfig.validate();
  }

  @Test(expected = RabbitmqConfigurationException.class)
  public void rabbitConfigWithValidExchangeAndValidQueueAndInvalidBindingTest() {
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(createDefaultExchangeConfig())
        .defaultQueue(createDefaultQueueConfig())
        .deadLetterConfig(createValidDeadLetterConfig())
        .exchange(exchange, createExchangeConfig(exchange))
        .queue(queue, createQueueConfig(queue))
        .binding(binding, BindingConfig.builder().queue(queue).routingKey(routingKey).build())
        .build();
    rabbitConfig.validate();
  }

  @Test(expected = RabbitmqConfigurationException.class)
  public void rabbitConfigWithDeadLetterEnabledAndNullDeadLetterConfigTest() {
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(createDefaultExchangeConfig())
        .defaultQueue(createDefaultQueueConfig())
        .deadLetterConfig(null)
        .exchange(exchange, createExchangeConfig(exchange))
        .queue(queue, createQueueConfig(queue))
        .binding(binding, createBinding(exchange, queue, routingKey))
        .build();
    rabbitConfig.validate();
  }

  @Test(expected = RabbitmqConfigurationException.class)
  public void rabbitConfigWithDeadLetterEnabledAndNullDeadLetterExchangeTest() {
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(createDefaultExchangeConfig())
        .defaultQueue(createDefaultQueueConfig())
        .deadLetterConfig(DeadLetterConfig.builder().deadLetterExchange(null).build())
        .exchange(exchange, createExchangeConfig(exchange))
        .queue(queue, createQueueConfig(queue))
        .binding(binding, createBinding(exchange, queue, routingKey))
        .build();
    rabbitConfig.validate();
  }

  @Test
  public void rabbitConfigWithNoDeadLetterEnabledAndNullDeadLetterConfigTest() {
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(createDefaultExchangeConfig())
        .defaultQueue(QueueConfig.builder().autoDelete(true).durable(false).deadLetterEnabled(false).build())
        .deadLetterConfig(null)
        .exchange(exchange, createExchangeConfig(exchange))
        .queue(queue, QueueConfig.builder().name(queue).deadLetterEnabled(false).build())
        .binding(binding, createBinding(exchange, queue, routingKey))
        .build();
    rabbitConfig.validate();
    assertThat(outputCapture.toString(), containsString("RabbitConfig Validation done successfully"));
  }

  @Test
  public void rabbitConfigWithNoDeadLetterEnabledAndNullDeadLetterExchangeTest() {
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(createDefaultExchangeConfig())
        .defaultQueue(QueueConfig.builder().deadLetterEnabled(false).build())
        .deadLetterConfig(DeadLetterConfig.builder().deadLetterExchange(null).build())
        .exchange(exchange, createExchangeConfig(exchange))
        .queue(queue, QueueConfig.builder().name(queue).build().applyDefaultConfig(QueueConfig.builder().deadLetterEnabled(false).build()))
        .binding(binding, createBinding(exchange, queue, routingKey))
        .build();
    rabbitConfig.validate();
    assertThat(outputCapture.toString(), containsString("RabbitConfig Validation done successfully"));
  }


  @Test(expected = RabbitmqConfigurationException.class)
  public void rabbitConfigWithDeadLetterEnabledForQueueAndNullDefaultDeadLetterExchangeTest() {
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(createDefaultExchangeConfig())
        .defaultQueue(QueueConfig.builder().deadLetterEnabled(null).build())
        .deadLetterConfig(DeadLetterConfig.builder().deadLetterExchange(null).build())
        .exchange(exchange, createExchangeConfig(exchange))
        .queue(queue, QueueConfig.builder().name(exchange).deadLetterEnabled(true).build())
        .binding(binding, createBinding(exchange, queue, routingKey))
        .build();
    rabbitConfig.validate();
  }

  @Test(expected = RabbitmqConfigurationException.class)
  public void rabbitConfigWithDeadLetterEnabledForQueueAndDefaultDeadLetterExchangeTest() {
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(createDefaultExchangeConfig())
        .defaultQueue(QueueConfig.builder().deadLetterEnabled(false).build())
        .deadLetterConfig(DeadLetterConfig.builder().deadLetterExchange(null).build())
        .exchange(exchange, createExchangeConfig(exchange))
        .queue(queue, QueueConfig.builder().name(exchange).deadLetterEnabled(true).build())
        .binding(binding, createBinding(exchange, queue, routingKey))
        .build();
    rabbitConfig.validate();
  }

  @Test
  public void rabbitConfigWithNoDeadLetterEnabledForQueueAndNoDefaultDeadLetterExchangeTest() {
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(createDefaultExchangeConfig())
        .defaultQueue(null)
        .deadLetterConfig(DeadLetterConfig.builder().deadLetterExchange(null).build())
        .exchange(exchange, createExchangeConfig(exchange))
        .queue(queue, QueueConfig.builder().name(exchange).deadLetterEnabled(false).build())
        .binding(binding, createBinding(exchange, queue, routingKey))
        .build();
    rabbitConfig.validate();
    assertThat(outputCapture.toString(), containsString("RabbitConfig Validation done successfully"));
  }


  @Test
  public void rabbitConfigWithNullDeadLetterEnabledForQueueAndNoDefaultDeadLetterExchangeTest() {
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(createDefaultExchangeConfig())
        .defaultQueue(null)
        .deadLetterConfig(DeadLetterConfig.builder().deadLetterExchange(null).build())
        .exchange(exchange, createExchangeConfig(exchange))
        .queue(queue, QueueConfig.builder().name(queue).deadLetterEnabled(null).build())
        .binding(binding, createBinding(exchange, queue, routingKey))
        .build();
    rabbitConfig.validate();
    assertThat(outputCapture.toString(), containsString("RabbitConfig Validation done successfully"));
  }


  @Test
  public void rabbitConfigWithNoExchangeAndNoQueueAndNoBindingTest() {
    rabbitConfig = new RabbitConfig();
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(null)
        .defaultQueue(null)
        .deadLetterConfig(null)
        .exchanges(new HashMap<>())
        .queues(new HashMap<>())
        .bindings(new HashMap<>())
        .build();
    rabbitConfig.validate();
    assertThat(outputCapture.toString(), containsString("RabbitConfig Validation done successfully"));
  }

  @Test(expected = RabbitmqConfigurationException.class)
  public void rabbitConfigWithDeadLetterEnabledForQueueAndNoDefaultDeadLetterExchangeTest() {
    rabbitConfig = RabbitConfig.builder()
        .defaultExchange(createDefaultExchangeConfig())
        .defaultQueue(null)
        .deadLetterConfig(DeadLetterConfig.builder().deadLetterExchange(null).build())
        .exchange(exchange, createExchangeConfig(exchange))
        .queue(queue, QueueConfig.builder().name(exchange).deadLetterEnabled(true).build())
        .binding(binding, createBinding(exchange, queue, routingKey))
        .build();
    rabbitConfig.validate();
  }

  private ExchangeConfig createDefaultExchangeConfig() {
    return createExchangeConfig(null);
  }

  private ExchangeConfig createExchangeConfig(String name) {
    return ExchangeConfig.builder().name(name).type(ExchangeTypes.TOPIC).autoDelete(true).durable(false).build();
  }

  private QueueConfig createDefaultQueueConfig() {
    return createQueueConfig(null);
  }

  private QueueConfig createQueueConfig(String name) {
    return QueueConfig.builder().name(name).autoDelete(true).durable(false).deadLetterEnabled(true).build();
  }

  private BindingConfig createBinding(String exchange, String queue, String routingKey) {
    return BindingConfig.builder().exchange(exchange).queue(queue).routingKey(routingKey).build();
  }

  private DeadLetterConfig createValidDeadLetterConfig() {
    return DeadLetterConfig.builder().deadLetterExchange(ExchangeConfig.builder().name("dead-letter-exchange.dlx").build()).build();
  }

  private ReQueueConfig createReQueueConfig(String exchange, String queue) {
    return ReQueueConfig.builder().exchange(createExchangeConfig(exchange)).queue(createQueueConfig(queue)).routingKey("requeue.key").build();
  }


}
