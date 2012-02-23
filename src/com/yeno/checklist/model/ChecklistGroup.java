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

/**
 * @author Yeno
 */
public class ChecklistGroup {

  private String name;
  private String comment;
  private String fileName;
  private List<Checklist> checklists = new ArrayList<Checklist>();

  public ChecklistGroup() {
  }

  /**
   * @param name
   * @param comment
   */
  public ChecklistGroup(String name, String comment) {
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

  public List<Checklist> getChecklists() {
    return checklists;
  }

  public void setChecklists(List<Checklist> checklists) {
    this.checklists = checklists;
  }

  public void addChecklist(Checklist checklist) {
    checklists.add(checklist);
  }

  public void removeChecklist(Checklist checklist) {
    checklists.remove(checklist);
  }

  public void resetAllChecklists() {
    for (Checklist checklist : checklists) {
      checklist.resetAllTasks();
    }
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ChecklistGroup [");
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
    if (checklists != null) {
      builder.append("checklists=");
      builder.append(checklists);
    }
    builder.append("]");
    return builder.toString();
  }

}
