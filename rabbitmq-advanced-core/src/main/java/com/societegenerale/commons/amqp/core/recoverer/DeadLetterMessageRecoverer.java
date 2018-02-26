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

package com.societegenerale.commons.amqp.core.recoverer;

import com.societegenerale.commons.amqp.core.config.RabbitConfig;
import com.societegenerale.commons.amqp.core.recoverer.handler.MessageExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Created by Anand Manissery on 7/13/2017.
 */
@Slf4j
public class DeadLetterMessageRecoverer implements MessageRecoverer {

  @Autowired
  private AmqpTemplate errorTemplate;

  @Autowired
  private RabbitConfig rabbitmqProperties;

  @Autowired(required = false)
  private List<MessageExceptionHandler> messageExceptionHandlers=new ArrayList<>();

  @Override
  public void recover(final Message message, final Throwable cause) {
    Map<String, Object> headers = message.getMessageProperties().getHeaders();
    headers.put("x-exception-stacktrace", ExceptionUtils.getFullStackTrace(cause));
    headers.put("x-exception-message", ExceptionUtils.getMessage(cause));
    headers.put("x-exception-root-cause-message", ExceptionUtils.getRootCauseMessage(cause));
    headers.put("x-original-exchange", message.getMessageProperties().getReceivedExchange());
    headers.put("x-original-routingKey", message.getMessageProperties().getReceivedRoutingKey());
    headers.put("x-original-queue", message.getMessageProperties().getConsumerQueue());
    headers.put("x-recover-time", new Date().toString());
    String deadLetterExchangeName = rabbitmqProperties.getDeadLetterConfig().getDeadLetterExchange().getName();
    String deadLetterRoutingKey = rabbitmqProperties.getDeadLetterConfig().createDeadLetterQueueName(message.getMessageProperties().getConsumerQueue());
    headers.put("x-dead-letter-exchange", deadLetterExchangeName);
    headers.put("x-dead-letter-queue", deadLetterRoutingKey);
    if(headers.containsKey("correlation-id")) {
      message.getMessageProperties().setCorrelationIdString((String) headers.get("correlation-id"));
    }
    Map<? extends String, ? extends Object> additionalHeaders = loadAdditionalHeaders(message, cause);

    if (additionalHeaders != null) {
      headers.putAll(additionalHeaders);
    }


    for (MessageExceptionHandler messageExceptionHandler : messageExceptionHandlers) {
      try {
        messageExceptionHandler.handle(message, cause);
      } catch (Exception e) {
        // To catch any exception in the  MessageExceptionHandler to avoid the interruption in other MessageExceptionHandlers
        log.error("Exception occurred while processing '{}' message exception handler.", messageExceptionHandler, e);
      }
    }

    this.errorTemplate.send(deadLetterExchangeName, deadLetterRoutingKey, message);

    log.warn("Republishing failed message to exchange '{}', routing key '{}', message {{}} , cause {}",
            deadLetterExchangeName, deadLetterRoutingKey, message, cause);


  }

  protected Map<String, Object> loadAdditionalHeaders(Message message, Throwable cause) {
    log.info("No additional headers added for message {}, cause {}", message, cause == null ? null : cause.getMessage());
    return new HashMap<>();
  }

}
