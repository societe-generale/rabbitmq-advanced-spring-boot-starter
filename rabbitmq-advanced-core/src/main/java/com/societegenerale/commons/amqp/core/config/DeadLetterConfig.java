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

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * Created by Anand Manissery on 7/13/2017.
 */
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
@ConfigurationProperties
public class DeadLetterConfig extends AbstractConfig {

  private static final String DEFAULT_DEAD_LETTER_QUEUE_POSTFIX = ".DLQ";

  /**
   * Dead Letter Exchange configuration
   */
  @NestedConfigurationProperty
  private ExchangeConfig deadLetterExchange;

  /**
   * postfix for the dead  letter queue name
   */
  @Builder.Default
  private String queuePostfix = DEFAULT_DEAD_LETTER_QUEUE_POSTFIX;

  public String createDeadLetterQueueName(String queueName) {
    return new StringBuilder()
        .append(queueName)
        .append(getDefaultConfig("DeadLetterConfig", "queuePostfix", queuePostfix, null, DEFAULT_DEAD_LETTER_QUEUE_POSTFIX)).toString();
  }

  @Override
  public boolean validate() {
    log.info("Validating DeadLetterConfig...");
    if (deadLetterExchange != null && deadLetterExchange.validate()) {
      log.info("DeadLetterConfig configuration validated successfully for deadLetterExchange '{}'", deadLetterExchange);
      return true;
    }
    log.error("Invalid DeadLetterConfig Configuration : Valid DeadLetterExchange must be provided for DeadLetterConfig");
    return false;
  }

}