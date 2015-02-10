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
package org.camunda.bpm.engine.impl.pvm.runtime.operation;

import org.camunda.bpm.engine.ProcessEngineException;
import org.camunda.bpm.engine.impl.pvm.process.ActivityImpl;
import org.camunda.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;

import java.util.logging.Logger;


/**
 * @author Tom Baeyens
 */
public class PvmAtomicOperationTransitionDestroyScope implements PvmAtomicOperation {

  private static Logger log = Logger.getLogger(PvmAtomicOperationTransitionDestroyScope.class.getName());

  public boolean isAsync(PvmExecutionImpl instance) {
    return false;
  }

  public void execute(PvmExecutionImpl execution) {

    // calculate the propagating execution
    PvmExecutionImpl propagatingExecution = null;

    ActivityImpl activity = execution.getActivity();

    // check whether the current scope needs to be destroyed
    if (activity.isScope()) {

      if(!execution.isScope()) {
        // TODO: remove this
        throw new ProcessEngineException("Unexpected non-scope execution "+ execution+"  leaving scope activity "+activity);
      }

      if (!execution.isConcurrent()) {
        propagatingExecution = execution.getParent();
        propagatingExecution.setActivity(execution.getActivity());
        propagatingExecution.setTransition(execution.getTransition());
        propagatingExecution.setActive(true);
        log.fine("destroy scope: scoped "+execution+" continues as parent scope "+propagatingExecution);
        execution.destroy();
        execution.remove();
      }
      else {
        log.fine("scoped concurrent "+execution+" becomes concurrent and remains under "+execution.getParent());

        // TODO!
        execution.destroy();
        propagatingExecution = execution;

      }

    } else {
      propagatingExecution = execution;
    }

    // while executing the transition, the activityInstance is 'null'
    // (we are not executing an activity)
    propagatingExecution.setActivityInstanceId(null);
    propagatingExecution.performOperation(TRANSITION_NOTIFY_LISTENER_TAKE);
  }

  public String getCanonicalName() {
    return "transition-destroy-scope";
  }
}
