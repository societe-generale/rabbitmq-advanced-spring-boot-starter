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


import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.amqp.core.AbstractExchange;
import org.springframework.boot.test.rule.OutputCapture;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class ExchangeConfigTest {

  private ExchangeConfig exchangeConfig;

  private ExchangeConfig expectedExchangeConfig;

  private ExchangeConfig defaultExchangeConfig;

  private String exchangeName = "exchange-1";

  @Rule
  public OutputCapture outputCapture = new OutputCapture();

  @Before
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
    assertThat(exchangeConfig.hashCode(), equalTo(expectedExchangeConfig.hashCode()));
    exchangeConfig.setDefaultConfigApplied(false);
    expectedExchangeConfig.setDefaultConfigApplied(true);
    assertThat(exchangeConfig.hashCode(), equalTo(expectedExchangeConfig.hashCode()));
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
    assertThat(exchangeConfig.getType(), equalTo(ExchangeTypes.TOPIC));
    assertFalse(exchangeConfig.getDurable());
    assertFalse(exchangeConfig.getAutoDelete());
    assertFalse(exchangeConfig.getDelayed());
    assertFalse(exchangeConfig.getInternal());
    assertNotNull(exchangeConfig.getArguments());
    assertTrue(exchangeConfig.isDefaultConfigApplied());
    assertTrue(CollectionUtils.isEmpty(exchangeConfig.getArguments()));
    assertThat(exchangeConfig, equalTo(expectedExchangeConfig));
  }

  @Test
  public void exchangeConfigWithNameAndValidationSuccessTest() {
    exchangeConfig = ExchangeConfig.builder().name(exchangeName).build();
    assertTrue(exchangeConfig.validate());
    assertThat(outputCapture.toString(), containsString(String.format("Exchange configuration validated successfully for exchange '%s'", exchangeName)));
  }

  @Test
  public void exchangeConfigWithoutNameAndValidationFailTest() {
    exchangeConfig = ExchangeConfig.builder().build();
    assertFalse(exchangeConfig.validate());
    assertThat(outputCapture.toString(), containsString(String.format("Invalid Exchange Configuration : Name must be provided for an exchange")));
  }

  @Test
  public void exchangeConfigWithoutNameAndDefaultConfigurationWithNameAndValidationFailTest() {
    defaultExchangeConfig = ExchangeConfig.builder().name(exchangeName).build();
    exchangeConfig = ExchangeConfig.builder().build().applyDefaultConfig(defaultExchangeConfig);
    assertFalse(exchangeConfig.validate());
    assertThat(outputCapture.toString(), containsString(String.format("Invalid Exchange Configuration : Name must be provided for an exchange")));
  }

  @Test
  public void exchangeConfigWithOnlyExchangeNameAndDefaultConfigurationAppliedTest() {
    exchangeConfig = ExchangeConfig.builder().name(exchangeName).build().applyDefaultConfig(defaultExchangeConfig);
    expectedExchangeConfig = ExchangeConfig.builder().name(exchangeName).type(ExchangeTypes.TOPIC).durable(false).autoDelete(false).delayed(false).internal(false).build();
    assertThat(exchangeConfig, equalTo(expectedExchangeConfig));
  }

  @Test
  public void exchangeConfigWithOnlyExchangeNameAndNoDefaultConfigurationAppliedTest() {
    exchangeConfig = ExchangeConfig.builder().name(exchangeName).build();
    expectedExchangeConfig = ExchangeConfig.builder().name(exchangeName).type(null).durable(null).autoDelete(null).delayed(null).internal(null).build();
    assertThat(exchangeConfig, equalTo(expectedExchangeConfig));
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

    assertThat(exchangeConfig, equalTo(expectedExchangeConfig));
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

    assertThat(exchangeConfig, equalTo(expectedExchangeConfig));
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
    assertThat(exchange.getName(), equalTo(exchangeConfig.getName()));
    assertThat(exchange.getType(), equalTo(exchangeConfig.getType().getValue()));
    assertThat(exchange.isDurable(), equalTo(exchangeConfig.getDurable()));
    assertThat(exchange.isAutoDelete(), equalTo(exchangeConfig.getAutoDelete()));
    assertThat(exchange.isDelayed(), equalTo(exchangeConfig.getDelayed()));
    assertThat(exchange.isInternal(), equalTo(exchangeConfig.getInternal()));
    assertThat(exchange.getArguments(), equalTo(exchangeConfig.getArguments()));
  }

}
