/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.camunda.bpm.engine.impl.migration.validation;

import java.util.Arrays;
import java.util.List;

import org.camunda.bpm.engine.impl.pvm.process.ProcessDefinitionImpl;
import org.camunda.bpm.engine.impl.pvm.process.ScopeImpl;
import org.camunda.bpm.engine.migration.MigrationInstruction;

public class MigrationInstructionValidators {

  // Validators

  public static final MigrationInstructionValidator ACTIVITIES_CAN_BE_MIGRATED = new MigrationInstructionValidator() {
    public final List<MigrationActivityValidator> sourceActivityValidators = Arrays.asList(
      MigrationActivityValidators.SUPPORTED_ACTIVITY, MigrationActivityValidators.NOT_MULTI_INSTANCE_CHILD, MigrationActivityValidators.HAS_NO_BOUNDARY_EVENT
    );

    public final List<MigrationActivityValidator> targetActivityValidators = Arrays.asList(
      MigrationActivityValidators.SUPPORTED_ACTIVITY, MigrationActivityValidators.NOT_MULTI_INSTANCE_CHILD
    );

    public boolean isInstructionValid(MigrationInstruction instruction, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
      return canActivitiesBeMigrated(instruction.getSourceActivityIds(), sourceProcessDefinition, sourceActivityValidators) &&
        canActivitiesBeMigrated(instruction.getTargetActivityIds(), targetProcessDefinition, targetActivityValidators);
    }

    protected boolean canActivitiesBeMigrated(List<String> activityIds, ProcessDefinitionImpl processDefinition, List<MigrationActivityValidator> activityValidators) {
      for (String activityId : activityIds) {
        if (!canActivityBeMigrated(activityId, processDefinition, activityValidators)) {
          return false;
        }
      }
      return true;
    }

    protected boolean canActivityBeMigrated(String activityId, ProcessDefinitionImpl processDefinition, List<MigrationActivityValidator> activityValidators) {
      for (MigrationActivityValidator activityValidator : activityValidators) {
        if (!activityValidator.canBeMigrated(activityId, processDefinition)) {
          return false;
        }
      }
      return true;
    }

  };

  public static final MigrationInstructionValidator ONE_TO_ONE_VALIDATOR = new MigrationInstructionValidator() {
    public boolean isInstructionValid(MigrationInstruction instruction, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
      return instruction.getSourceActivityIds().size() == 1 && instruction.getTargetActivityIds().size() == 1;
    }
  };

  public static final MigrationInstructionValidator SAME_ID_VALIDATOR = new MigrationInstructionValidator() {
    public boolean isInstructionValid(MigrationInstruction instruction, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
      return ONE_TO_ONE_VALIDATOR.isInstructionValid(instruction, sourceProcessDefinition, targetProcessDefinition) &&
        instruction.getSourceActivityIds().contains(instruction.getTargetActivityIds().get(0));
    }
  };

  public static final MigrationInstructionValidator SAME_SCOPE = new MigrationInstructionValidator() {
    public boolean isInstructionValid(MigrationInstruction instruction, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
      return ONE_TO_ONE_VALIDATOR.isInstructionValid(instruction, sourceProcessDefinition, targetProcessDefinition) &&
        haveSameScope(instruction.getSourceActivityIds().get(0), instruction.getTargetActivityIds().get(0), sourceProcessDefinition, targetProcessDefinition);
    }
  };


  // Helper

  protected static boolean haveSameScope(String sourceActivityId, String targetActivityId, ProcessDefinitionImpl sourceProcessDefinition, ProcessDefinitionImpl targetProcessDefinition) {
    ScopeImpl sourceFlowScope = sourceProcessDefinition.findActivity(sourceActivityId).getFlowScope();
    ScopeImpl targetFlowScope = targetProcessDefinition.findActivity(targetActivityId).getFlowScope();

    return (isProcessDefinition(sourceFlowScope) && isProcessDefinition(targetFlowScope)) || sourceFlowScope.getId().equals(targetFlowScope.getId());
  }

  protected static boolean isProcessDefinition(ScopeImpl scope) {
    return scope.getProcessDefinition() == scope;
  }

}
