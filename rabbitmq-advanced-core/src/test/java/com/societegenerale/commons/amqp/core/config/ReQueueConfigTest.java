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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ReQueueConfigTest {

  private ReQueueConfig reQueueConfig;

  private ExchangeConfig exchangeConfig;

  private QueueConfig queueConfig;

  @Before
  public void setUp() {
    exchangeConfig = ExchangeConfig.builder().name("requeue-exchange").build();
    queueConfig = QueueConfig.builder().name("requeue").build();
    reQueueConfig = ReQueueConfig.builder()
        .exchange(exchangeConfig)
        .queue(queueConfig)
        .routingKey("requeue.key")
        .build();
  }

  @Test
  public void reQueueConfigEqualsTest() {
    reQueueConfig = new ReQueueConfig();
    reQueueConfig.setDefaultConfigApplied(false);
    ReQueueConfig expectedReQueueConfig = new ReQueueConfig();
    expectedReQueueConfig.setDefaultConfigApplied(true);
    assertTrue(reQueueConfig.equals(expectedReQueueConfig));
  }

  @Test
  public void validateWhenAutoConfigurationDisabledTest() {
    reQueueConfig.setAutoRequeueEnabled(false);
    assertTrue(reQueueConfig.validate());
  }

  @Test
  public void validateWhenAutoConfigurationEnabledTest() {
    reQueueConfig.setAutoRequeueEnabled(true);
    reQueueConfig.setCron("* * * * *");
    assertTrue(reQueueConfig.validate());
  }

  @Test
  public void invalidWhenExchangeIsNullTest() {
    reQueueConfig.setExchange(null);
    Assert.assertFalse(reQueueConfig.validate());
  }

  @Test
  public void invalidWhenExchangeIsInvalidTest() {
    exchangeConfig.setName(null);
    reQueueConfig.setExchange(exchangeConfig);
    Assert.assertFalse(reQueueConfig.validate());
  }

  @Test
  public void invalidWhenQueueIsNullTest() {
    reQueueConfig.setQueue(null);
    Assert.assertFalse(reQueueConfig.validate());
  }

  @Test
  public void invalidWhenQueueIsInvalidTest() {
    queueConfig.setName(null);
    reQueueConfig.setQueue(queueConfig);
    Assert.assertFalse(reQueueConfig.validate());
  }

  @Test
  public void invalidWhenRoutingKeyIsNullTest() {
    reQueueConfig.setRoutingKey(null);
    Assert.assertFalse(reQueueConfig.validate());
  }

  @Test
  public void invalidWhenAutoDeleteEnabledAndCronIsNullTest() {
    reQueueConfig.setAutoRequeueEnabled(true);
    reQueueConfig.setCron(null);
    Assert.assertFalse(reQueueConfig.validate());
  }

  @Test
  public void validWhenAutoDeleteDisabledAndCronIsNullTest() {
    reQueueConfig.setAutoRequeueEnabled(false);
    reQueueConfig.setCron(null);
    assertTrue(reQueueConfig.validate());
  }
}
