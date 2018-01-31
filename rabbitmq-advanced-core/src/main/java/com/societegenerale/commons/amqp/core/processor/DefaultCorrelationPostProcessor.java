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

package com.societegenerale.commons.amqp.core.processor;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;

import java.util.UUID;

/**
 * Created by Anand Manissery on 7/13/2017.
 */
public class DefaultCorrelationPostProcessor implements CorrelationPostProcessor {

  private Tracer tracer;

  @Autowired(required = false)
  public DefaultCorrelationPostProcessor(Tracer tracer) {
    this.tracer=tracer;
  }

  @Override
  public Message postProcessMessage(final Message message) {
    MessageProperties messageProperties = message.getMessageProperties();
    String correlationId = messageProperties.getCorrelationIdString();
    if (correlationId == null) {
      correlationId = (tracer!=null && tracer.getCurrentSpan()!=null)?
          Span.idToHex(tracer.getCurrentSpan().getTraceId()):UUID.randomUUID().toString();
      messageProperties.setCorrelationIdString(correlationId);
    }
    messageProperties.getHeaders().put("correlation-id", correlationId);
    return message;
  }

}
