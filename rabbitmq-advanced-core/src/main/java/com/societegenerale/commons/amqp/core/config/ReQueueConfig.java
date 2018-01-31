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
import org.springframework.util.StringUtils;

/**
 * Created by Anand Manissery on 7/13/2017.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
@Slf4j
@ConfigurationProperties
public class ReQueueConfig extends AbstractConfig {

  /**
   *  If 'true' : Auto configure the rabbitmq components for requeue on startup.
   */
  private boolean enabled;

  /**
   * Requeue Exchange configuration
   */
  @NestedConfigurationProperty
  private ExchangeConfig exchange;

  /**
   * Requeue Queue configuration
   */
  @NestedConfigurationProperty
  private QueueConfig queue;

  /**
   * Routing key for requeue exchange and queue binding
   */
  private String routingKey;

  /**
   * If 'true' : Auto requeue will be enabled. These properties (cron,messageCount) need to be set.
   * Not recommended for cluster environment
   */
  private boolean autoRequeueEnabled;

  /**
   * cron for auto requeue to push the requeue message for all dead letter queue
   */
  private String cron;

  /**
   * message count for auto requeue to requeue the given no of messages at a time
   * If not set, only one message will be requeue
   */
  private int messageCount;

  public boolean validate() {

    boolean valid = true;

    valid = validate("exchange", exchange, valid);

    valid = validate("queue", queue, valid);

    if (StringUtils.isEmpty(routingKey)) {
      log.error("Invalid RoutingKey : RoutingKey must be provided for requeue configuration");
      valid = false;
    }

    if (autoRequeueEnabled && StringUtils.isEmpty(cron)) {
      log.error("Invalid Cron : Cron must be provided for auto requeue configuration");
      valid = false;
    }

    if (valid) {
      log.info("Requeue configuration validated successfully : '{}'", this);
    }

    return valid;
  }

  private boolean validate(String key, AbstractConfig abstractConfig, boolean valid) {
    boolean validFlag = valid;
    if (abstractConfig == null) {
      log.error("Invalid {} : {} must be provided for a requeue configuration", key, key);
      validFlag = false;
    } else {
      validFlag = abstractConfig.validate() ? validFlag : false;
    }
    return validFlag;
  }
}
