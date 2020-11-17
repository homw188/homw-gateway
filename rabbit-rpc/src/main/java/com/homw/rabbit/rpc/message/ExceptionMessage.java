/*
 * Copyright [2012] [ShopWiki]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.homw.rabbit.rpc.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonPropertyOrder;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.homw.rabbit.rpc.util.MessagingUtil;

/**
 * Format an Exception and its stack trace as JSON.
 *
 * @author jdickinson
 */
@JsonPropertyOrder(value = { "exceptionName", "exceptionMsg", "exceptionStackTrace", "cause" })
public class ExceptionMessage extends AbstractMessage {

	@JsonIgnore
	private static Logger logger = LoggerFactory.getLogger(ExceptionMessage.class);
	@JsonIgnore
	public static final TypeReference<ExceptionMessage> TYPE_REF = new TypeReference<ExceptionMessage>() {
	};

	private final String exceptionName;
	private final String exceptionMsg;
	private final List<String> stackTrace;
	private final ExceptionMessage cause;

	public ExceptionMessage(Throwable e) {
		exceptionName = e.getClass().getName();
		exceptionMsg = e.getMessage();

		stackTrace = new ArrayList<String>();
		for (StackTraceElement ste : e.getStackTrace()) {
			stackTrace.add(ste.toString());
		}

		if (MessagingUtil.DEBUG) {
			logger.error(e.getMessage(), e);
		}

		if (e.getCause() != null) {
			cause = new ExceptionMessage(e.getCause());
		} else {
			cause = null;
		}
	}

	public String getExceptionName() {
		return exceptionName;
	}

	public String getExceptionMsg() {
		return exceptionMsg;
	}

	public List<String> getStackTrace() {
		return Collections.unmodifiableList(stackTrace);
	}

	@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
	public ExceptionMessage getCause() {
		return cause;
	}
}
