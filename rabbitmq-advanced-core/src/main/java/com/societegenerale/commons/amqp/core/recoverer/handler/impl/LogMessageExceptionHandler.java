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

package com.societegenerale.commons.amqp.core.recoverer.handler.impl;

import com.societegenerale.commons.amqp.core.recoverer.handler.MessageExceptionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Anand Manissery on 7/14/2017.
 */
@Slf4j
public class LogMessageExceptionHandler implements MessageExceptionHandler {

  private static List directlyReadableContentTypes;

  static{

    List tmpDirectlyReadableContentTypes = Arrays.asList("text/plain",
                                                         "application/json",
                                                         "text/x-json",
                                                         "application/xml");

    directlyReadableContentTypes=Collections.unmodifiableList(tmpDirectlyReadableContentTypes);

  }

  @Override
  public void handle(Message message, Throwable cause) {
    Map<String, Object> headers = message.getMessageProperties().getHeaders();
    log.warn("Exception occurred while processing the message from queue {{}} , message {{}} , headers {{}} :  cause",
        headers.get("x-original-queue"), getMessageString(message), headers, cause);
  }

  protected String getMessageString(Message message) {
    String contentType = message.getMessageProperties() != null ? message.getMessageProperties().getContentType() : null;

    if (directlyReadableContentTypes.contains(contentType)) {
      return new String(message.getBody());
    } else {
      return Arrays.toString(message.getBody()) + "(byte[" + message.getBody().length + "])";
    }
  }
}
