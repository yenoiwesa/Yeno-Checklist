/**
 * ------------------------------------------------------------
 *                       Yeno Checklist
 * ------------------------------------------------------------
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
package com.yeno.checklist.model;

/**
 * @author Yeno
 */
public class Task {

  private String name;
  private String result;
  private String comment;
  private String problem;
  private TaskState taskState = TaskState.BLANK;

  public Task() {
  }

  /**
   * @param name
   * @param highlighted
   * @param comment
   */
  public Task(String name, String comment) {
    super();
    this.name = name;
    this.comment = comment;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public TaskState getTaskState() {
    return taskState;
  }

  public void setTaskState(TaskState taskState) {
    this.taskState = taskState;
    if (!taskState.equals(TaskState.PROBLEM)) {
      problem = null;
    }
  }

  public void defaultSwitchState() {
    switch (taskState) {
    case DONE:
      taskState = TaskState.BLANK;
      break;
    default:
      taskState = TaskState.DONE;
      break;
    }
    problem = null;
  }

  public String getResult() {
    return result;
  }

  public void setResult(String result) {
    this.result = result;
  }

  public void clearProblem() {
    problem = null;
  }

  public String getProblem() {
    return problem;
  }

  public void setProblem(String problem) {
    this.problem = problem;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Task [");
    if (name != null) {
      builder.append("name=");
      builder.append(name);
      builder.append(", ");
    }
    if (comment != null) {
      builder.append("comment=");
      builder.append(comment);
      builder.append(", ");
    }
    builder.append("state=");
    builder.append(taskState.toString());
    builder.append("]");
    return builder.toString();
  }

}
