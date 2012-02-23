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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import android.content.Context;
import android.sax.Element;
import android.sax.EndElementListener;
import android.sax.EndTextElementListener;
import android.sax.RootElement;
import android.sax.StartElementListener;
import android.text.TextUtils;
import android.util.Xml;

import com.yeno.checklist.model.Checklist;
import com.yeno.checklist.model.ChecklistGroup;
import com.yeno.checklist.model.Task;
import com.yeno.checklist.model.TaskState;

/**
 * @author Yeno
 */
public class ChecklistParser extends CheckListXml {

  private ChecklistGroup checklistGroup;
  protected Checklist currentChecklist;
  protected Task currentTask;

  public ChecklistGroup parse(String fileName, Context context) {
    try {
      return parse(context.openFileInput(fileName), fileName);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public ChecklistGroup parse(File file) {
    try {
      return parse(new FileInputStream(file), file.getName());
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
    return null;
  }

  public ChecklistGroup parse(InputStream fileInputStream, String fileName) {
    checklistGroup = null;
    currentChecklist = null;
    currentTask = null;

    try {
      BufferedInputStream inputStream = new BufferedInputStream(fileInputStream);

      RootElement root = getRoot();

      Xml.parse(inputStream, Xml.Encoding.UTF_8, root.getContentHandler());

      checklistGroup.setFileName(fileName);

    } catch (FileNotFoundException e) {
      e.printStackTrace();
      return null;
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    } catch (SAXException e) {
      return null;
    }

    return checklistGroup;
  }

  /**
   * @return
   */
  private RootElement getRoot() {
    // Creation de l'element du model a recuperer
    checklistGroup = new ChecklistGroup();

    // Element racine ChecklistGroup
    RootElement root = new RootElement(CHECKLIST_GROUP);

    // Recuperation du nom du Checklist Group (obligatoire)
    Element checklistGroupNameElem = root.requireChild(NAME);
    checklistGroupNameElem.setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(String body) {
        checklistGroup.setName(body);
      }
    });

    // Recuperation du commentaire associe au Checklist Group (facultatif)
    Element checklistGroupCommentElem = root.getChild(COMMENT);
    checklistGroupCommentElem.setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(String body) {
        checklistGroup.setComment(body);
      }
    });

    // Extraction des checklists
    extractChecklists(root);

    return root;
  }

  private void extractChecklists(RootElement root) {
    // Recuperation des noeuds Checklist (facultatif)
    // Creation d'une nouvelle instance
    Element checklistElem = root.getChild(CHECKLIST);
    checklistElem.setStartElementListener(new StartElementListener() {
      @Override
      public void start(Attributes attributes) {
        currentChecklist = new Checklist();
      }
    });
    // Ajout de l'instance dans le groupe
    checklistElem.setEndElementListener(new EndElementListener() {
      @Override
      public void end() {
        checklistGroup.addChecklist(currentChecklist);
      }
    });

    // Recuperation du nom de la Checklist (obligatoire)
    Element checklistNameElem = checklistElem.requireChild(NAME);
    checklistNameElem.setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(String body) {
        currentChecklist.setName(body);
      }
    });

    // Recuperation du commentaire associe a la Checklist (facultatif)
    Element checklistCommentElem = checklistElem.getChild(COMMENT);
    checklistCommentElem.setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(String body) {
        currentChecklist.setComment(body);
      }
    });

    // Recuperation de l'etat read de la Checklist (facultatif)
    Element checklistReadElem = checklistElem.getChild(READ);
    checklistReadElem.setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(String body) {
        currentChecklist.setRead(Boolean.valueOf(body));
      }
    });

    // Recuperation de de la speech locale de la Checklist (facultatif)
    Element speechLocaleElem = checklistElem.getChild(LOCALE);
    speechLocaleElem.setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(String body) {
        currentChecklist.setSpeechLocale(body);
      }
    });

    // Extraction des taches
    extractTasks(checklistElem);
  }

  private void extractTasks(Element checklistElem) {
    // Recuperation des noeuds Task (facultatif)
    // Creation d'une nouvelle instance
    Element taskElem = checklistElem.getChild(TASK);
    taskElem.setStartElementListener(new StartElementListener() {
      @Override
      public void start(Attributes attributes) {
        currentTask = new Task();
      }
    });
    // Ajout de l'instance dans le groupe
    taskElem.setEndElementListener(new EndElementListener() {
      @Override
      public void end() {
        currentChecklist.addTask(currentTask);
      }
    });

    // Recuperation du nom de la Task (obligatoire)
    Element taskNameElem = taskElem.requireChild(NAME);
    taskNameElem.setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(String body) {
        currentTask.setName(body);
      }
    });

    // Recuperation du commentaire associe a la Task (facultatif)
    Element taskCommentElem = taskElem.getChild(COMMENT);
    taskCommentElem.setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(String body) {
        currentTask.setComment(body);
      }
    });

    // Recuperation de l'etat de la Task (facultatif)
    Element taskStateElem = taskElem.getChild(STATE);
    taskStateElem.setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(String body) {
        try {
          TaskState taskState = TaskState.valueOf(body);
          currentTask.setTaskState(taskState);
        } catch (IllegalArgumentException ex) {
          // Ne rien faire
        }
      }
    });

    // Recuperation de l'etat done de la Task (facultatif) - DEPRECIE
    Element taskDoneElem = taskElem.getChild(DONE);
    taskDoneElem.setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(String body) {
        if (Boolean.valueOf(body)) {
          currentTask.setTaskState(TaskState.DONE);
        }
      }
    });

    // Recuperation de l'etat skipped de la Task (facultatif) - DEPRECIE
    Element taskSkippedElem = taskElem.getChild(SKIPPED);
    taskSkippedElem.setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(String body) {
        if (Boolean.valueOf(body)) {
          currentTask.setTaskState(TaskState.SKIPPED);
        }
      }
    });

    // Recuperation de la valeur Result de la Task (facultatif)
    Element taskResultElem = taskElem.getChild(RESULT);
    taskResultElem.setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(String body) {
        currentTask.setResult(body);
      }
    });

    // Recuperation de la valeur Problem de la Task (facultatif)
    Element taskProblemElem = taskElem.getChild(PROBLEM);
    taskProblemElem.setEndTextElementListener(new EndTextElementListener() {
      @Override
      public void end(String body) {
        if (!TextUtils.isEmpty(body)) {
          currentTask.setProblem(body);
        }
      }
    });

  }

}
