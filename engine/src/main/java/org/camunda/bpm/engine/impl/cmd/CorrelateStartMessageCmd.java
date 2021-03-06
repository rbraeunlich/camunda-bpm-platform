/* Licensed under the Apache License, Version 2.0 (the "License");
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

package org.camunda.bpm.engine.impl.cmd;

import static org.camunda.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.util.concurrent.Callable;

import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.impl.MessageCorrelationBuilderImpl;
import org.camunda.bpm.engine.impl.context.Context;
import org.camunda.bpm.engine.impl.interceptor.Command;
import org.camunda.bpm.engine.impl.interceptor.CommandContext;
import org.camunda.bpm.engine.impl.runtime.CorrelationHandler;
import org.camunda.bpm.engine.impl.runtime.CorrelationSet;
import org.camunda.bpm.engine.impl.runtime.MessageCorrelationResult;
import org.camunda.bpm.engine.runtime.ProcessInstance;

public class CorrelateStartMessageCmd extends AbstractCorrelateMessageCmd implements Command<ProcessInstance> {

  public CorrelateStartMessageCmd(MessageCorrelationBuilderImpl messageCorrelationBuilderImpl) {
    super(messageCorrelationBuilderImpl);
  }

  public ProcessInstance execute(final CommandContext commandContext) {
    ensureNotNull("messageName", messageName);

    final CorrelationHandler correlationHandler = Context.getProcessEngineConfiguration().getCorrelationHandler();
    final CorrelationSet correlationSet = new CorrelationSet(businessKey, processInstanceId, correlationKeys, processDefinitionId);

    MessageCorrelationResult correlationResult = commandContext.runWithoutAuthorization(new Callable<MessageCorrelationResult>() {
      public MessageCorrelationResult call() throws Exception {
        return correlationHandler.correlateStartMessage(commandContext, messageName, correlationSet);
      }
    });

    if (correlationResult == null) {
      throw new MismatchingMessageCorrelationException(messageName, "No process definition matches the parameters");
    }

    // check authorization
    checkAuthorization(correlationResult);

    return instantiateProcess(commandContext, correlationResult);
  }
}
