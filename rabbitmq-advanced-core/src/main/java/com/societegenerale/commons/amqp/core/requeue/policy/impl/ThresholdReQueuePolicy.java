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

package com.societegenerale.commons.amqp.core.requeue.policy.impl;

import com.societegenerale.commons.amqp.core.requeue.policy.ReQueuePolicy;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Value;

import java.util.Map;

/**
 * Created by Anand Manissery on 7/13/2017.
 */
public class ThresholdReQueuePolicy implements ReQueuePolicy {

  private static final String X_REQUEUE_COUNT = "x-requeue-count";

  @Value("${rabbitmq.auto-config.re-queue-config.threshold:3}")
  private int threshold;

  @Override
  public boolean canReQueue(Message message) {
    Map<String, Object> headers = message.getMessageProperties().getHeaders();
    int requeueCount = 0;
    if (headers.containsKey(X_REQUEUE_COUNT)) {
      requeueCount = (int) headers.get(X_REQUEUE_COUNT);
    }
    if (threshold > requeueCount) {
      headers.put(X_REQUEUE_COUNT, ++requeueCount);
      return true;
    }
    return false;
  }
}
