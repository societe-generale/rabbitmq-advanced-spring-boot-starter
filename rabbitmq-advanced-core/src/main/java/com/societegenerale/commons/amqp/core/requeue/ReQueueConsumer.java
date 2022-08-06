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

import com.societegenerale.commons.amqp.core.requeue.policy.ReQueuePolicy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Anand Manissery on 7/13/2017.
 */
@Slf4j
@AllArgsConstructor
public class ReQueueConsumer {

  private final RabbitTemplate rabbitTemplate;

  private final ReQueuePolicy reQueuePolicy;

  private long timeout;

  @RabbitListener(queues = "${rabbitmq.auto-config.re-queue-config.queue.name}")
  public void onMessage(ReQueueMessage reQueueMessage) {
    log.info("Requeue processing started for DeadLetterQueue '{}' with MessageCount '{}'", reQueueMessage.getDeadLetterQueue(), reQueueMessage.getMessageCount());
    int count = 0;
    List<Message> requeueFailureMessages = new ArrayList<>();

    do {
      Message message = rabbitTemplate.receive(reQueueMessage.getDeadLetterQueue(), timeout);
      if (message == null) {
        break;
      }
      Map<String, Object> headers = message.getMessageProperties().getHeaders();
      if (reQueuePolicy!=null && reQueuePolicy.canReQueue(message)) {
        String queueName = (String) headers.get("x-original-queue");
        rabbitTemplate.send(queueName, message);
      } else {
        log.warn("Can not requeue the message with correlation-id '{}' as per the requeue policy", headers.get("correlation-id"));
        requeueFailureMessages.add(message);
      }
      count++;
    } while (reQueueMessage.getMessageCount() < 0 || reQueueMessage.getMessageCount() > count);

    requeueFailureMessages.forEach(message -> rabbitTemplate.send(reQueueMessage.getDeadLetterQueue(), message));
    log.info("Requeue processing completed for DeadLetterQueue '{}'", reQueueMessage.getDeadLetterQueue());
  }

}
