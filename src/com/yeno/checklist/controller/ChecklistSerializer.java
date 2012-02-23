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
package com.yeno.checklist.controller;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.xmlpull.v1.XmlSerializer;

import android.content.Context;
import android.text.TextUtils;
import android.util.Xml;

import com.yeno.checklist.model.Checklist;
import com.yeno.checklist.model.ChecklistGroup;
import com.yeno.checklist.model.Task;

/**
 * @author Yeno
 */
public class ChecklistSerializer extends CheckListXml {

  public void writeXml(ChecklistGroup checklistGroup, Context context) {
    try {
      writeXml(context.openFileOutput(checklistGroup.getFileName(), Context.MODE_PRIVATE), checklistGroup);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  public void writeXml(ChecklistGroup checklistGroup, File file) {
    try {
      writeXml(new FileOutputStream(file), checklistGroup);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private void writeXml(FileOutputStream fileOutputStream, ChecklistGroup checklistGroup) {
    try {
      // Serializer de XML
      XmlSerializer serializer = Xml.newSerializer();

      Writer out = new BufferedWriter(new OutputStreamWriter(fileOutputStream));

      serializer.setOutput(out);

      serializer.startDocument("UTF-8", true);

      buildChecklistGroup(checklistGroup, serializer);

      serializer.endDocument();

      out.close();

    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalStateException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String writeStringXml(ChecklistGroup checklistGroup) {
    try {
      // Serializer de XML
      XmlSerializer serializer = Xml.newSerializer();

      StringWriter out = new StringWriter();

      serializer.setOutput(out);

      serializer.startDocument("UTF-8", true);

      buildChecklistGroup(checklistGroup, serializer);

      serializer.endDocument();

      return out.toString();

    } catch (IllegalArgumentException e) {
      e.printStackTrace();
    } catch (IllegalStateException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  private void buildChecklistGroup(ChecklistGroup checklistGroup, XmlSerializer serializer) throws IOException {
    // Noeud root
    serializer.startTag("", CHECKLIST_GROUP);

    // Nom du groupe
    serializer.startTag("", NAME);
    serializer.text(checklistGroup.getName());
    serializer.endTag("", NAME);

    // Commentaire du groupe
    String groupComment = checklistGroup.getComment();
    if (!TextUtils.isEmpty(groupComment)) {
      serializer.startTag("", COMMENT);
      serializer.text(groupComment);
      serializer.endTag("", COMMENT);
    }

    // Checklists
    for (Checklist checklist : checklistGroup.getChecklists()) {
      buildChecklist(serializer, checklist);
    }

    serializer.endTag("", CHECKLIST_GROUP);
  }

  private void buildChecklist(XmlSerializer serializer, Checklist checklist) throws IOException {
    serializer.startTag("", CHECKLIST);

    // Nom de la liste
    serializer.startTag("", NAME);
    serializer.text(checklist.getName());
    serializer.endTag("", NAME);

    // Commentaire de la liste
    String listComment = checklist.getComment();
    if (!TextUtils.isEmpty(listComment)) {
      serializer.startTag("", COMMENT);
      serializer.text(listComment);
      serializer.endTag("", COMMENT);
    }

    // List read
    serializer.startTag("", READ);
    serializer.text(Boolean.toString(checklist.isRead()));
    serializer.endTag("", READ);

    // List Locale
    String speechLocale = checklist.getSpeechLocale();
    if (!TextUtils.isEmpty(speechLocale)) {
      serializer.startTag("", LOCALE);
      serializer.text(speechLocale);
      serializer.endTag("", LOCALE);
    }

    // Tasks
    for (Task task : checklist.getTasks()) {
      buildTask(serializer, task);
    }

    serializer.endTag("", CHECKLIST);
  }

  private void buildTask(XmlSerializer serializer, Task task) throws IOException {
    serializer.startTag("", TASK);

    // Nom de la tache
    serializer.startTag("", NAME);
    serializer.text(task.getName());
    serializer.endTag("", NAME);

    // Resultat de la tache
    String taskResult = task.getResult();
    if (!TextUtils.isEmpty(taskResult)) {
      serializer.startTag("", RESULT);
      serializer.text(taskResult);
      serializer.endTag("", RESULT);
    }

    // Commentaire de la tache
    String taskComment = task.getComment();
    if (!TextUtils.isEmpty(taskComment)) {
      serializer.startTag("", COMMENT);
      serializer.text(taskComment);
      serializer.endTag("", COMMENT);
    }

    // Tache state
    serializer.startTag("", STATE);
    serializer.text(task.getTaskState().toString());
    serializer.endTag("", STATE);

    // Probleme de la tache
    String taskProblem = task.getProblem();
    if (!TextUtils.isEmpty(taskProblem)) {
      serializer.startTag("", PROBLEM);
      serializer.text(taskProblem);
      serializer.endTag("", PROBLEM);
    }

    serializer.endTag("", TASK);
  }
}
