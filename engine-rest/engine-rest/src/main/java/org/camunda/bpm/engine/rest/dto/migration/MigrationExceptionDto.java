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
package org.camunda.bpm.engine.rest.dto.migration;

import org.camunda.bpm.engine.migration.MigrationInstructionInstanceValidationException;
import org.camunda.bpm.engine.migration.MigrationPlanValidationException;
import org.camunda.bpm.engine.rest.dto.ExceptionDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationValidationReportDto.MigrationInstructionInstanceValidationReportDto;
import org.camunda.bpm.engine.rest.dto.migration.MigrationValidationReportDto.MigrationPlanValidationReportDto;

/**
 * @author Thorben Lindhauer
 *
 */
public class MigrationExceptionDto<T extends MigrationValidationReportDto<?>> extends ExceptionDto {

  protected T errorReport;

  public T getErrorReport() {
    return errorReport;
  }

  public void setErrorReport(T report) {
    this.errorReport = report;
  }

  public static class MigrationPlanValidationExceptionDto extends MigrationExceptionDto<MigrationPlanValidationReportDto> {

    public static MigrationPlanValidationExceptionDto from(MigrationPlanValidationException exception) {
      MigrationPlanValidationExceptionDto dto = new MigrationPlanValidationExceptionDto();
      dto.message = exception.getMessage();
      dto.type = exception.getClass().getSimpleName();
      dto.errorReport = MigrationPlanValidationReportDto.from(exception.getValidationReport());
      return dto;
    }

  }

  public static class MigrationInstructionInstanceValidationExceptionDto
    extends MigrationExceptionDto<MigrationInstructionInstanceValidationReportDto> {

    public static MigrationInstructionInstanceValidationExceptionDto from(MigrationInstructionInstanceValidationException exception) {
      MigrationInstructionInstanceValidationExceptionDto dto = new MigrationInstructionInstanceValidationExceptionDto();
      dto.message = exception.getMessage();
      dto.type = exception.getClass().getSimpleName();
      dto.errorReport = MigrationInstructionInstanceValidationReportDto.from(exception.getValidationReport());
      return dto;
    }

  }
}
