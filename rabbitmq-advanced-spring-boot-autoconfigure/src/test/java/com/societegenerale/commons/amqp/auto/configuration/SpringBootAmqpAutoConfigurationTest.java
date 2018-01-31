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

package com.societegenerale.commons.amqp.auto.configuration;

import com.societegenerale.commons.amqp.core.config.BindingConfig;
import com.societegenerale.commons.amqp.core.config.ExchangeConfig;
import com.societegenerale.commons.amqp.core.config.QueueConfig;
import com.societegenerale.commons.amqp.core.config.RabbitConfig;
import org.apache.commons.lang.BooleanUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Map;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
public class SpringBootAmqpAutoConfigurationTest {

  @Autowired
  private ConfigurableApplicationContext applicationContext;

  @Autowired
  private RabbitConfig rabbitConfig;

  @Test
  public void checkRabbitMqAutoConfigurationBeansPresentOrNot() {

    for(Map.Entry<String,ExchangeConfig> exchangeEntry : rabbitConfig.getExchanges().entrySet()) {
      assertTrue(applicationContext.containsBean(exchangeEntry.getKey()));
    }

    for(Map.Entry<String,QueueConfig> queueEntry : rabbitConfig.getQueues().entrySet()) {
      assertTrue(applicationContext.containsBean(queueEntry.getKey()));
      QueueConfig queueConfig = queueEntry.getValue();
      if(BooleanUtils.isTrue(queueConfig.getDeadLetterEnabled())) {
        assertTrue(applicationContext.containsBean(rabbitConfig.getDeadLetterConfig().getDeadLetterExchange().getName()));
        assertTrue(applicationContext.containsBean(new StringBuilder()
            .append(rabbitConfig.getQueues().get(queueEntry.getKey()).getName()).append(rabbitConfig.getDeadLetterConfig().getQueuePostfix()).toString()));
        assertTrue(applicationContext.containsBean(new StringBuilder()
            .append(rabbitConfig.getDeadLetterConfig().getDeadLetterExchange().getName())
            .append(":")
            .append(rabbitConfig.getQueues().get(queueEntry.getKey()).getName()).append(rabbitConfig.getDeadLetterConfig().getQueuePostfix()).toString()));
        assertTrue(applicationContext.containsBean(rabbitConfig.getReQueueConfig().getExchange().getName()));
        assertTrue(applicationContext.containsBean(rabbitConfig.getReQueueConfig().getQueue().getName()));
      }
    }

    for(Map.Entry<String,BindingConfig> bindingEntry : rabbitConfig.getBindings().entrySet()) {
      assertTrue(applicationContext.containsBean(bindingEntry.getKey()));
    }
  }
}
