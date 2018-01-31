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

import com.societegenerale.commons.amqp.core.config.QueueConfig;
import com.societegenerale.commons.amqp.core.config.RabbitConfig;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;

/**
 * Created by Anand Manissery on 7/13/2017.
 */
@Slf4j
@Data
public class AutoReQueueScheduler {

  @Autowired
  private RabbitConfig rabbitConfig;

  @Autowired
  private RabbitTemplate rabbitTemplate;

  @Value("${rabbitmq.auto-config.re-queue-config.message-count:10}")
  private int messageCount;

  @Scheduled(cron = "${rabbitmq.auto-config.re-queue-config.cron}")
  public void autoReQueue() {
    log.info("Auto ReQueue Starting...");
    String reQueueExchange = rabbitConfig.getReQueueConfig().getExchange().getName();
    String reQueueRoutingKey = rabbitConfig.getReQueueConfig().getRoutingKey();
    for (Map.Entry<String, QueueConfig> entry : rabbitConfig.getQueues().entrySet()) {
      if (BooleanUtils.isTrue(entry.getValue().getDeadLetterEnabled())) {
        ReQueueMessage reQueueMessage = ReQueueMessage.builder()
            .deadLetterQueue(rabbitConfig.getDeadLetterConfig().createDeadLetterQueueName(entry.getValue().getName()))
            .messageCount(messageCount)
            .build();
        rabbitTemplate.convertAndSend(reQueueExchange, reQueueRoutingKey, reQueueMessage);
      }
    }
    log.info("Auto ReQueue completed...");
  }

}
