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
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Binding.DestinationType;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class BindingConfigTest {

  private BindingConfig bindingConfig;

  private BindingConfig expectedBindingConfig;

  private String exchaneg = "exchaneg";

  private String queue = "queue";

  private String routingKey = "routingKey";

  @Rule
  public OutputCapture outputCapture = new OutputCapture();

  @Test
  public void bindingConfigEqualsTest() {
    bindingConfig = BindingConfig.builder().build();
    expectedBindingConfig = BindingConfig.builder().build();
    assertTrue(bindingConfig.equals(expectedBindingConfig));
    bindingConfig.setDefaultConfigApplied(false);
    expectedBindingConfig.setDefaultConfigApplied(true);
    assertTrue(bindingConfig.equals(expectedBindingConfig));
  }

  @Test
  public void bindingConfigeHashcodeTest() {
    bindingConfig = BindingConfig.builder().build();
    expectedBindingConfig = BindingConfig.builder().build();
    assertThat(bindingConfig.hashCode(), equalTo(expectedBindingConfig.hashCode()));
    bindingConfig.setDefaultConfigApplied(false);
    expectedBindingConfig.setDefaultConfigApplied(true);
    assertThat(bindingConfig.hashCode(), equalTo(expectedBindingConfig.hashCode()));
  }

  @Test
  public void defaultBindingConfigTest() {
    bindingConfig = BindingConfig.builder().build();
    assertNull(bindingConfig.getExchange());
    assertNull(bindingConfig.getQueue());
    assertNull(bindingConfig.getRoutingKey());
    assertFalse(bindingConfig.isDefaultConfigApplied());
    assertNotNull(bindingConfig.getArguments());
    assertTrue(CollectionUtils.isEmpty(bindingConfig.getArguments()));
  }

  @Test
  public void bindingConfigValidationSuccessTest() {
    bindingConfig = BindingConfig.builder().exchange(exchaneg).queue(queue).routingKey(routingKey).build();
    assertTrue(bindingConfig.validate());
    assertThat(outputCapture.toString(), containsString(String.format("Binding configuration validated successfully for Binding '%s'", bindingConfig)));
  }

  @Test
  public void bindingConfigWithoutExchnageAndValidationFailTest() {
    bindingConfig = BindingConfig.builder().exchange(null).queue(queue).routingKey(routingKey).build();
    assertFalse(bindingConfig.validate());
    assertThat(outputCapture.toString(), containsString(String.format("Invalid Exchange : Exchange must be provided for a binding")));
  }

  @Test
  public void bindingConfigWithoutQueueAndValidationFailTest() {
    bindingConfig = BindingConfig.builder().exchange(exchaneg).queue(null).routingKey(routingKey).build();
    assertFalse(bindingConfig.validate());
    assertThat(outputCapture.toString(), containsString(String.format("Invalid Queue : Queue must be provided for a binding")));
  }

  @Test
  public void bindingConfigNoExchangeAndNoQueueAndValidationFailTest() {
    bindingConfig = BindingConfig.builder().exchange(null).queue(null).routingKey(routingKey).build();
    assertFalse(bindingConfig.validate());
    assertThat(outputCapture.toString(), containsString(String.format("Invalid Exchange : Exchange must be provided for a binding")));
    assertThat(outputCapture.toString(), containsString(String.format("Invalid Queue : Queue must be provided for a binding")));
  }


  @Test
  public void createBindingWithExchangeAndQueueTest() {
    Exchange bindingExchange = ExchangeConfig.builder().name(exchaneg).type(ExchangeTypes.DIRECT).build().buildExchange(ExchangeConfig.builder().build());
    Queue bindingQueue = QueueConfig.builder().name(queue).build().buildQueue(QueueConfig.builder().build(), null);

    Binding binding = BindingConfig.builder().exchange(exchaneg).queue(queue).routingKey(routingKey).build().bind(bindingExchange, bindingQueue);

    expectedBindingConfig = BindingConfig.builder()
        .exchange(exchaneg).queue(queue).routingKey(routingKey).arguments(new HashMap<>())
        .build();
    assertBinding(binding, expectedBindingConfig);
  }

  @Test
  public void createBindingWithHeaderExchangeAndArgumentsTest() {
    Exchange bindingExchange = ExchangeConfig.builder().name(exchaneg).type(ExchangeTypes.HEADERS).build().buildExchange(ExchangeConfig.builder().build());
    Queue bindingQueue = QueueConfig.builder().name(queue).build().buildQueue(QueueConfig.builder().build(), null);
    BindingConfig bindingConfig = BindingConfig.builder().exchange(exchaneg).queue(queue).routingKey(routingKey).argument("key", "value").build();
    Binding actualBinding = bindingConfig.bind(bindingExchange, bindingQueue);
    assertBinding(actualBinding, bindingConfig);
  }

  @Test(expected = RabbitmqConfigurationException.class)
  public void createBindingWithHeaderExchangeAndNoArgumentsTest() {
    Exchange bindingExchange = ExchangeConfig.builder().name(exchaneg).type(ExchangeTypes.HEADERS).build().buildExchange(ExchangeConfig.builder().build());
    Queue bindingQueue = QueueConfig.builder().name(queue).build().buildQueue(QueueConfig.builder().build(), null);
    BindingConfig bindingConfig = BindingConfig.builder().exchange(exchaneg).queue(queue).routingKey(routingKey).build();
    bindingConfig.bind(bindingExchange, bindingQueue);
  }

  @Test
  public void createBindingWithNonHeaderExchangeAndRoutingKeyTest() {
    Exchange bindingExchange = ExchangeConfig.builder().name(exchaneg).type(ExchangeTypes.DIRECT).build().buildExchange(ExchangeConfig.builder().build());
    Queue bindingQueue = QueueConfig.builder().name(queue).build().buildQueue(QueueConfig.builder().build(), null);
    BindingConfig bindingConfig = BindingConfig.builder().exchange(exchaneg).queue(queue).routingKey(routingKey).build();
    Binding actualBinding = bindingConfig.bind(bindingExchange, bindingQueue);
    assertBinding(actualBinding, bindingConfig);
  }

  @Test(expected = RabbitmqConfigurationException.class)
  public void createBindingWithNonHeaderExchangeAndNoRoutingKeyTest() {
    Exchange bindingExchange = ExchangeConfig.builder().name(exchaneg).type(ExchangeTypes.DIRECT).build().buildExchange(ExchangeConfig.builder().build());
    Queue bindingQueue = QueueConfig.builder().name(queue).build().buildQueue(QueueConfig.builder().build(), null);
    BindingConfig bindingConfig = BindingConfig.builder().exchange(exchaneg).queue(queue).routingKey(null).build();
    bindingConfig.bind(bindingExchange, bindingQueue);
  }

  private void assertBinding(Binding binding, BindingConfig bindingConfig) {
    assertThat(binding.getExchange(), equalTo(bindingConfig.getExchange()));
    assertThat(binding.getDestination(), equalTo(bindingConfig.getQueue()));
    assertThat(binding.getDestinationType(), equalTo(DestinationType.QUEUE));
    assertThat(binding.getArguments(), equalTo(bindingConfig.getArguments()));
  }

}
