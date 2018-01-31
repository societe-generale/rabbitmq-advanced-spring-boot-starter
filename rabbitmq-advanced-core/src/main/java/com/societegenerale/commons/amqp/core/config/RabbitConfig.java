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

import com.societegenerale.commons.amqp.core.exception.RabbitmqConfigurationException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import javax.annotation.PostConstruct;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Created by Anand Manissery on 7/13/2017.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
@Slf4j
@ConfigurationProperties(prefix = "rabbitmq.auto-config")
public class RabbitConfig {

  /**
   *  If 'true' : Auto configure the rabbitmq components on startup.
   *  Default value is 'true'
   */
  @Builder.Default
  private boolean enabled = true;

  /**
   * Default Exchange configuration applies to all the missing configuration of each Exchange.
   * This can be overridden by configuring at each exchange level.
   */
  @NestedConfigurationProperty
  private ExchangeConfig defaultExchange;

  /**
   * Default Queue configuration applies to all the missing configuration of each Queue.
   * This can be overridden by configuring at each queue level.
   */
  @NestedConfigurationProperty
  private QueueConfig defaultQueue;

  /**
   * Dead Letter Configuration to configure dead letter exchange and queue postfix.
   */
  @NestedConfigurationProperty
  private DeadLetterConfig deadLetterConfig;

  /**
   * Re Queue Configuration to configure the requeue exchange and queue.
   */
  @NestedConfigurationProperty
  private ReQueueConfig reQueueConfig;

  /**
   * Info Headers can be used to add additional information to be added in each message headers
   */
  @Singular
  private Map<String, Object> infoHeaders = new LinkedHashMap<>();

  /**
   * You can configure all your exchanges here
   */
  @Singular("exchange")
  private Map<String, ExchangeConfig> exchanges = new LinkedHashMap<>();

  /**
   * You can configure all your queues here
   */
  @Singular("queue")
  private Map<String, QueueConfig> queues = new LinkedHashMap<>();

  /**
   * You can configure your bindings for the exchanges and queues here
   */
  @Singular("binding")
  private Map<String, BindingConfig> bindings = new LinkedHashMap<>();

  @PostConstruct
  public void validate() {
    boolean valid = true;
    log.info("Validating exchange...");
    for (Entry<String, ExchangeConfig> entry : exchanges.entrySet()) {
      valid = validate(entry.getKey(), entry.getValue(), valid);
    }
    log.info("Validating queue...");
    for (Entry<String, QueueConfig> entry : queues.entrySet()) {
      valid = validate(entry.getKey(), entry.getValue(), valid);
    }
    log.info("Validating binding...");
    for (Entry<String, BindingConfig> entry : bindings.entrySet()) {
      valid = validate(entry.getKey(), entry.getValue(), valid);
    }

    if (isDeadLetterEnabled()) {
      log.info("Validating DeadLetterConfig...");
      if (deadLetterConfig == null || deadLetterConfig.getDeadLetterExchange() == null) {
        log.error("Validating failed. DeadLetterConfig must provided if any queue enable dead letter queue.");
        valid = false;
      } else {
        valid = validate("DeadLetterConfig", deadLetterConfig, valid);
      }
    }

    if (reQueueConfig != null) {
      log.info("Validating ReQueueConfig...");
      valid = validate("ReQueueConfig", reQueueConfig, valid);
    }

    if (valid) {
      log.info("RabbitConfig Validation done successfully. RabbitConfig = {{}}", this.toString());
    } else {
      throw new RabbitmqConfigurationException("Invalid RabbitConfig Configuration");
    }
  }

  private boolean validate(String key, AbstractConfig abstractConfig, boolean valid) {
    log.info("Validating key {} :: value {}...", key, abstractConfig);
    return abstractConfig.validate() ? valid : false;
  }

  private boolean isDeadLetterEnabled() {
    if (defaultQueue != null && defaultQueue.getDeadLetterEnabled() != null && defaultQueue.getDeadLetterEnabled()) {
      return true;
    } else {
      for (QueueConfig currentQueue : queues.values()) {
        if (currentQueue.getDeadLetterEnabled() != null && currentQueue.getDeadLetterEnabled()) {
          return true;
        }
      }
      return false;
    }
  }


}
