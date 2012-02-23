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

import java.util.ArrayList;
import java.util.List;

import com.yeno.checklist.ChecklistConstants;

/**
 * @author Yeno
 */
public class Checklist {

  private String name;
  private String comment;
  private boolean read = false;
  private List<Task> tasks = new ArrayList<Task>();
  private Task focusedTask;
  private int focusedTaskPosition = ChecklistConstants.NOT_DEFINED;
  private String speechLocale;

  public Checklist() {
  }

  /**
   * @param name
   * @param comment
   * @param highlighted
   * @param read
   */
  public Checklist(String name, String comment, boolean read) {
    super();
    this.name = name;
    this.comment = comment;
    this.read = read;
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

  public boolean isRead() {
    return read;
  }

  public void setRead(boolean read) {
    this.read = read;
  }

  public void switchRead() {
    read = !read;
  }

  public List<Task> getTasks() {
    return tasks;
  }

  public void setTasks(List<Task> tasks) {
    this.tasks = tasks;
  }

  public void addTask(Task task) {
    tasks.add(task);
  }

  public String getSpeechLocale() {
    return speechLocale;
  }

  public void setSpeechLocale(String speechLocale) {
    this.speechLocale = speechLocale;
  }

  public void removeTask(Task task) {
    tasks.remove(task);
  }

  public int findNextFocusedTask() {
    int position = 0;
    for (Task task : tasks) {
      if (task.getTaskState().equals(TaskState.BLANK)) {
        focusedTask = task;
        focusedTaskPosition = position;
        return focusedTaskPosition;
      }
      position++;
    }
    focusedTask = null;
    focusedTaskPosition = ChecklistConstants.NOT_DEFINED;
    return focusedTaskPosition;
  }

  public Task getBlankTask(int filteredPosition) {
    int position = 0;
    for (Task task : tasks) {
      if (task.getTaskState().equals(TaskState.BLANK)) {
        if (position == filteredPosition) {
          return task;
        }
        position++;
      }
    }
    return null;
  }

  public int getFocusedTaskPosition() {
    return focusedTaskPosition;
  }

  public int getNotBlankTaskCount() {
    int count = 0;
    for (Task task : tasks) {
      if (!task.getTaskState().equals(TaskState.BLANK)) {
        count++;
      }
    }
    return count;
  }

  public int getBlankTaskCount() {

    int count = 0;
    for (Task task : tasks) {
      if (task.getTaskState().equals(TaskState.BLANK)) {
        count++;
      }
    }
    return count;
  }

  public boolean containsProblem() {
    for (Task task : tasks) {
      if (task.getTaskState().equals(TaskState.PROBLEM)) {
        return true;
      }
    }
    return false;
  }

  public void checkAllTasks() {
    for (Task task : tasks) {
      task.setTaskState(TaskState.DONE);
    }
  }

  public void clearAllProblems() {
    for (Task task : tasks) {
      if (task.getTaskState().equals(TaskState.PROBLEM)) {
        task.setTaskState(TaskState.DONE);
      }
    }
  }

  public void switchAllDone() {
    if (containsProblem()) {
      checkAllTasks();
    } else if (getNotBlankTaskCount() == tasks.size()) {
      resetAllTasks();
    } else {
      checkAllTasks();
    }
  }

  public void resetAllTasks() {
    for (Task task : tasks) {
      task.setTaskState(TaskState.BLANK);
    }
  }

  public Task getFocusedTask() {
    return focusedTask;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Checklist [");
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
    builder.append(", read=");
    builder.append(read);
    builder.append(", ");
    if (tasks != null) {
      builder.append("tasks=");
      builder.append(tasks);
    }
    builder.append("]");
    return builder.toString();
  }

}
