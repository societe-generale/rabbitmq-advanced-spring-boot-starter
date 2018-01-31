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

import lombok.Data;
import lombok.Singular;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anand Manissery on 7/13/2017.
 */
@Data
public class InfoHeaderMessagePostProcessor implements MessagePostProcessor {

  @Singular
  private Map<String, Object> headers = new HashMap<>();

  @Autowired
  private Environment environment;

  @Override
  public Message postProcessMessage(final Message message) {
    MessageProperties messageProperties = message.getMessageProperties();
    headers.putIfAbsent("spring-application-name",
        getEnvironment().getProperty("spring.application.name", String.class));
    headers.put("execution-time", new Date().toString());
    messageProperties.getHeaders().putIfAbsent("info", headers);
    return message;
  }

}
