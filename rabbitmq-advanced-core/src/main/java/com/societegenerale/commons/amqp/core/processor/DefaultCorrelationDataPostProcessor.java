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
import org.springframework.amqp.rabbit.core.CorrelationDataPostProcessor;
import org.springframework.amqp.rabbit.support.CorrelationData;

/**
 * Created by Anand Manissery on 7/13/2017.
 */
public class DefaultCorrelationDataPostProcessor implements CorrelationDataPostProcessor {

  private CorrelationPostProcessor correlationPostProcessor;

  public DefaultCorrelationDataPostProcessor(CorrelationPostProcessor correlationPostProcessor) {
    this.correlationPostProcessor = correlationPostProcessor;
  }

  @Override
  public CorrelationData postProcess(final Message message, CorrelationData correlationData) {
    CorrelationData resultCorrelationData = correlationData == null ? new CorrelationData() : correlationData;
    MessageProperties messageProperties = message.getMessageProperties();
    if (correlationData != null && correlationData.getId() != null) {
      messageProperties.setCorrelationIdString(correlationData.getId());
    }
    correlationPostProcessor.postProcessMessage(message);
    resultCorrelationData.setId(messageProperties.getCorrelationIdString());
    return resultCorrelationData;
  }

}
