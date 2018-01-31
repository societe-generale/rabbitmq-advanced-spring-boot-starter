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

package com.societegenerale.commons.amqp.core.requeue;

import com.societegenerale.commons.amqp.core.config.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by Anand Manissery on 7/13/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class AutoReQueueSchedulerTest {

  @Autowired
  private AutoReQueueScheduler autoReQueueScheduler;

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Before
  public void setUp() {
    RabbitConfig rabbitConfig = RabbitConfig.builder()
        .deadLetterConfig(DeadLetterConfig.builder()
            .deadLetterExchange(ExchangeConfig.builder().name("my-dlx-exchange").build()).queuePostfix(".dlq").build())
        .queue("queue-with-dlq-one", QueueConfig.builder().name("queue-with-dlq-one").deadLetterEnabled(true).build())
        .queue("queue-with-dlq-two", QueueConfig.builder().name("queue-with-dlq-one").deadLetterEnabled(true).build())
        .queue("queue-with-out-dlq", QueueConfig.builder().name("queue-with-out-dlq").deadLetterEnabled(false).build())
        .reQueueConfig(ReQueueConfig.builder()
            .exchange(ExchangeConfig.builder().name("re-queue-exchange").build()).routingKey("re-queue-key").build())
        .build();
    autoReQueueScheduler.setRabbitConfig(rabbitConfig);
    doNothing().when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(ReQueueMessage.class));
  }

  @Test
  public void autoReQueueTest() {
    autoReQueueScheduler.autoReQueue();
    verify(rabbitTemplate, times(2)).convertAndSend(anyString(), anyString(), any(ReQueueMessage.class));
  }

}