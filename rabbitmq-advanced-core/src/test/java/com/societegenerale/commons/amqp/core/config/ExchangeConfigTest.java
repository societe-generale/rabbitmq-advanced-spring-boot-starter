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


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(OutputCaptureExtension.class)
public class ExchangeConfigTest {

  private ExchangeConfig exchangeConfig;

  private ExchangeConfig expectedExchangeConfig;

  private ExchangeConfig defaultExchangeConfig;

  private String exchangeName = "exchange-1";

  @BeforeEach
  public void setUp() {
    exchangeConfig = ExchangeConfig.builder().build();
    expectedExchangeConfig = ExchangeConfig.builder().build();
    defaultExchangeConfig = ExchangeConfig.builder().build();
  }

  @Test
  public void exchangeConfigEqualsTest() {
    assertTrue(exchangeConfig.equals(expectedExchangeConfig));
    exchangeConfig.setDefaultConfigApplied(false);
    expectedExchangeConfig.setDefaultConfigApplied(true);
    assertTrue(exchangeConfig.equals(expectedExchangeConfig));
  }

  @Test
  public void exchangeConfigeHashcodeTest() {
    assertEquals(exchangeConfig.hashCode(), expectedExchangeConfig.hashCode());
    exchangeConfig.setDefaultConfigApplied(false);
    expectedExchangeConfig.setDefaultConfigApplied(true);
    assertEquals(exchangeConfig.hashCode(), expectedExchangeConfig.hashCode());
  }

  @Test
  public void defaultExchangeConfigWithoutDefaultConfigurationAppliedTest() {
    exchangeConfig = ExchangeConfig.builder().build();
    assertNull(exchangeConfig.getName());
    assertNull(exchangeConfig.getType());
    assertNull(exchangeConfig.getDurable());
    assertNull(exchangeConfig.getAutoDelete());
    assertNull(exchangeConfig.getDelayed());
    assertNull(exchangeConfig.getInternal());
    assertFalse(exchangeConfig.isDefaultConfigApplied());
    assertNotNull(exchangeConfig.getArguments());
    assertTrue(CollectionUtils.isEmpty(exchangeConfig.getArguments()));
  }

  @Test
  public void defaultExchangeConfigWithDefaultConfigurationAppliedTest() {
    exchangeConfig = ExchangeConfig.builder().build().applyDefaultConfig(defaultExchangeConfig);
    expectedExchangeConfig = ExchangeConfig.builder().type(ExchangeTypes.TOPIC).durable(false).autoDelete(false).delayed(false).internal(false).build();
    assertNull(exchangeConfig.getName());
    assertEquals(exchangeConfig.getType(), ExchangeTypes.TOPIC);
    assertFalse(exchangeConfig.getDurable());
    assertFalse(exchangeConfig.getAutoDelete());
    assertFalse(exchangeConfig.getDelayed());
    assertFalse(exchangeConfig.getInternal());
    assertNotNull(exchangeConfig.getArguments());
    assertTrue(exchangeConfig.isDefaultConfigApplied());
    assertTrue(CollectionUtils.isEmpty(exchangeConfig.getArguments()));
    assertEquals(exchangeConfig, expectedExchangeConfig);
  }

  @Test
  public void exchangeConfigWithNameAndValidationSuccessTest(CapturedOutput outputCapture) {
    exchangeConfig = ExchangeConfig.builder().name(exchangeName).build();
    assertTrue(exchangeConfig.validate());
    assertTrue(outputCapture.getOut().contains(String.format("Exchange configuration validated successfully for exchange '%s'", exchangeName)));
  }

  @Test
  public void exchangeConfigWithoutNameAndValidationFailTest(CapturedOutput outputCapture) {
    exchangeConfig = ExchangeConfig.builder().build();
    assertFalse(exchangeConfig.validate());
    assertTrue(outputCapture.getOut().contains(String.format("Invalid Exchange Configuration : Name must be provided for an exchange")));
  }

  @Test
  public void exchangeConfigWithoutNameAndDefaultConfigurationWithNameAndValidationFailTest(CapturedOutput outputCapture) {
    defaultExchangeConfig = ExchangeConfig.builder().name(exchangeName).build();
    exchangeConfig = ExchangeConfig.builder().build().applyDefaultConfig(defaultExchangeConfig);
    assertFalse(exchangeConfig.validate());
    assertTrue(outputCapture.getOut().contains(String.format("Invalid Exchange Configuration : Name must be provided for an exchange")));
  }

  @Test
  public void exchangeConfigWithOnlyExchangeNameAndDefaultConfigurationAppliedTest() {
    exchangeConfig = ExchangeConfig.builder().name(exchangeName).build().applyDefaultConfig(defaultExchangeConfig);
    expectedExchangeConfig = ExchangeConfig.builder().name(exchangeName).type(ExchangeTypes.TOPIC).durable(false).autoDelete(false).delayed(false).internal(false).build();
    assertEquals(exchangeConfig, expectedExchangeConfig);
  }

  @Test
  public void exchangeConfigWithOnlyExchangeNameAndNoDefaultConfigurationAppliedTest() {
    exchangeConfig = ExchangeConfig.builder().name(exchangeName).build();
    expectedExchangeConfig = ExchangeConfig.builder().name(exchangeName).type(null).durable(null).autoDelete(null).delayed(null).internal(null).build();
    assertEquals(exchangeConfig, expectedExchangeConfig);
  }

  @Test
  public void exchangeConfigWithOnlyExchangeNameAndFewDefaultConfigurationTest() {
    defaultExchangeConfig = ExchangeConfig.builder()
        .type(ExchangeTypes.HEADERS).durable(true).autoDelete(true).delayed(true).internal(true).argument("key1", "value1")
        .build();

    exchangeConfig = ExchangeConfig.builder()
        .name(exchangeName)
        .build()
        .applyDefaultConfig(defaultExchangeConfig);

    expectedExchangeConfig = ExchangeConfig.builder()
        .name(exchangeName).type(ExchangeTypes.HEADERS).durable(true).autoDelete(true).delayed(true).internal(true)
        .argument("key1", "value1")
        .build();

    assertEquals(exchangeConfig, expectedExchangeConfig);
  }

  @Test
  public void exchangeConfigByOverriddingFromDefaultConfigurationTest() {
    defaultExchangeConfig = ExchangeConfig.builder()
        .type(ExchangeTypes.HEADERS).durable(true).autoDelete(true).delayed(true).internal(true).argument("key1", "value1")
        .build();

    exchangeConfig = ExchangeConfig.builder()
        .name(exchangeName).type(ExchangeTypes.FANOUT).durable(false).autoDelete(false)
        .argument("key1", "NEW_VALUE").argument("key2", "value2")
        .build()
        .applyDefaultConfig(defaultExchangeConfig);

    expectedExchangeConfig = ExchangeConfig.builder()
        .name(exchangeName).type(ExchangeTypes.FANOUT).durable(false).autoDelete(false).delayed(true).internal(true)
        .argument("key1", "NEW_VALUE").argument("key2", "value2")
        .build();

    assertEquals(exchangeConfig, expectedExchangeConfig);
  }

  @Test
  public void createExchangeWithDefaultConfigurationAppliedTest() {
    defaultExchangeConfig = ExchangeConfig.builder().build();
    exchangeConfig = ExchangeConfig.builder().name(exchangeName).build();
    expectedExchangeConfig = ExchangeConfig.builder()
        .name(exchangeName).type(ExchangeTypes.TOPIC).durable(false).autoDelete(false).delayed(false).internal(false).arguments(new HashMap<>())
        .build();
    AbstractExchange exchange = exchangeConfig.buildExchange(defaultExchangeConfig);
    assertExchange(exchange, expectedExchangeConfig);
  }

  @Test
  public void createExchangeWithDefaultConfigurationPreAppliedTest() {
    defaultExchangeConfig = ExchangeConfig.builder().build();
    exchangeConfig = ExchangeConfig.builder().name(exchangeName).build().applyDefaultConfig(defaultExchangeConfig);
    expectedExchangeConfig = ExchangeConfig.builder()
        .name(exchangeName).type(ExchangeTypes.TOPIC).durable(false).autoDelete(false).delayed(false).internal(false).arguments(new HashMap<>())
        .build();
    AbstractExchange exchange = exchangeConfig.buildExchange(defaultExchangeConfig);
    assertExchange(exchange, expectedExchangeConfig);
  }

  @Test
  public void createExchangeWithFewDefaultConfigurationTest() {
    defaultExchangeConfig = ExchangeConfig.builder()
        .type(ExchangeTypes.HEADERS).durable(true).autoDelete(true).delayed(true).internal(true).argument("key1", "value1")
        .build();

    exchangeConfig = ExchangeConfig.builder()
        .name(exchangeName).type(ExchangeTypes.FANOUT).durable(false).autoDelete(false)
        .argument("key2", "value2").argument("key1", "NEW_VALUE")
        .build()
        .applyDefaultConfig(defaultExchangeConfig);

    expectedExchangeConfig = ExchangeConfig.builder()
        .name(exchangeName).type(ExchangeTypes.FANOUT).durable(false).autoDelete(false).delayed(true).internal(true)
        .argument("key2", "value2").argument("key1", "NEW_VALUE")
        .build();

    AbstractExchange exchange = exchangeConfig.buildExchange(defaultExchangeConfig);
    assertExchange(exchange, expectedExchangeConfig);
  }

  private void assertExchange(AbstractExchange exchange, ExchangeConfig exchangeConfig) {
    assertEquals(exchange.getName(), exchangeConfig.getName());
    assertEquals(exchange.getType(), exchangeConfig.getType().getValue());
    assertEquals(exchange.isDurable(), exchangeConfig.getDurable());
    assertEquals(exchange.isAutoDelete(), exchangeConfig.getAutoDelete());
    assertEquals(exchange.isDelayed(), exchangeConfig.getDelayed());
    assertEquals(exchange.isInternal(), exchangeConfig.getInternal());
    assertEquals(exchange.getArguments(), exchangeConfig.getArguments());
  }

}
