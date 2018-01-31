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

import com.societegenerale.commons.amqp.core.requeue.ReQueueMessage;
import com.societegenerale.commons.amqp.core.config.*;
import org.junit.Test;

import static pl.pojo.tester.api.assertion.Assertions.assertPojoMethodsFor;


/**
 * Created by Anand Manissery on 4/19/2017.
 */
public class PojoTest {

  @Test
  public void pojoTestForGeneralValidation() {
    assertPojoMethodsFor(ExchangeConfig.class).areWellImplemented();
    assertPojoMethodsFor(QueueConfig.class).areWellImplemented();
    assertPojoMethodsFor(BindingConfig.class).areWellImplemented();
    assertPojoMethodsFor(DeadLetterConfig.class).areWellImplemented();
    assertPojoMethodsFor(ReQueueConfig.class).areWellImplemented();
    assertPojoMethodsFor(RabbitConfig.class).areWellImplemented();
    assertPojoMethodsFor(ReQueueMessage.class).areWellImplemented();
  }
}
