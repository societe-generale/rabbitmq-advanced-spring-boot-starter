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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by Anand Manissery on 7/13/2017.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
public class ReQueueConsumerTest {

  @Autowired
  private ReQueueConsumer reQueueConsumer;

  @Autowired
  private RabbitTemplate rabbitTemplate;

  private Message message;

  private ReQueueMessage reQueueMessage;

  @Before
  public void setUp() {
    MessageProperties messageProperties = MessagePropertiesBuilder.newInstance().setHeader("x-original-queue", "original-queue").build();
    message = MessageBuilder.withBody("DummyMessage".getBytes()).andProperties(messageProperties).build();
    reQueueMessage = ReQueueMessage.builder()
        .deadLetterQueue("dlq-name")
        .messageCount(2)
        .build();
  }

  @Test
  public void reQueueMessageUntilTheMessageCount() {
    when(rabbitTemplate.receive(anyString(), anyLong())).thenReturn(message);
    reQueueConsumer.onMessage(reQueueMessage);
    verify(rabbitTemplate, times(2)).send(anyString(), any(Message.class));
  }

  @Test
  public void shouldNotReQueueIfTheMessageIsNull() {
    when(rabbitTemplate.receive(anyString(), anyLong())).thenReturn(null);
    reQueueConsumer.onMessage(reQueueMessage);
    verify(rabbitTemplate, times(0)).send(anyString(), any(Message.class));
  }
}
