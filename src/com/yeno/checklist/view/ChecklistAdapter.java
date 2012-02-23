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
package com.yeno.checklist.view;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yeno.checklist.R;
import com.yeno.checklist.model.Checklist;
import com.yeno.checklist.model.ChecklistGroup;

/**
 * @author Yeno
 */
public class ChecklistAdapter extends BaseAdapter {

  protected ChecklistGroup checklistGroup;
  protected ChecklistSelectionActivity activity;
  protected boolean editionMode = false;
  protected View touchedView;

  /**
   * @param context
   */
  public ChecklistAdapter(ChecklistSelectionActivity activity) {
    super();
    this.activity = activity;
  }

  @Override
  public int getCount() {
    if (checklistGroup != null) {
      return checklistGroup.getChecklists().size();
    }
    return 0;
  }

  @Override
  public Object getItem(int position) {
    return checklistGroup.getChecklists().get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    if (convertView == null) {
      convertView = activity.getLayoutInflater().inflate(R.layout.checklist_item, null);
    }

    // Recuperation de l'element du modele
    Checklist checklist = (Checklist) getItem(position);

    // Titre de la liste
    TextView titleView = (TextView) convertView.findViewById(R.id.checklist_title);
    titleView.setText(checklist.getName());

    // Commentaire associe a la liste (s'il existe)
    TextView commentView = (TextView) convertView.findViewById(R.id.checklist_comment);
    String comment = checklist.getComment();
    if (comment != null) {
      commentView.setText(comment);
      commentView.setVisibility(View.VISIBLE);
    } else {
      commentView.setVisibility(View.GONE);
    }

    // Bouton pour valider toute la checklist
    Button checklistStateButton = (Button) convertView.findViewById(R.id.checklist_state_button);
    checklistStateButton.setTag(checklist);
    checklistStateButton.setOnClickListener(activity);

    if (checklist.containsProblem()) {
      checklistStateButton.setBackgroundResource(R.drawable.task_problem_small);
    } else {
      int doneTasksCount = checklist.getNotBlankTaskCount();
      int totalTasks = checklist.getTasks().size();
      if (doneTasksCount == 0) {
        checklistStateButton.setBackgroundResource(R.drawable.task_empty_small);
      } else if (doneTasksCount < totalTasks) {
        checklistStateButton.setBackgroundResource(R.drawable.task_progress_small);
      } else {
        checklistStateButton.setBackgroundResource(R.drawable.task_small);
      }
    }

    // Bouton pour le mode Read (speech)
    Button readButton = (Button) convertView.findViewById(R.id.read_button);
    readButton.setTag(checklist);
    readButton.setOnClickListener(activity);
    if (checklist.isRead()) {
      readButton.setBackgroundResource(R.drawable.speech_small);
    } else {
      readButton.setBackgroundResource(R.drawable.speech_button_disabled);
    }

    // Bouton de suppression
    ImageButton deleteButton = (ImageButton) convertView.findViewById(R.id.delete_button);
    deleteButton.setFocusable(false);
    if (editionMode) {
      deleteButton.setVisibility(View.VISIBLE);
      deleteButton.setOnClickListener(activity);
      deleteButton.setTag(checklist);
    } else {
      deleteButton.setVisibility(View.GONE);
      deleteButton.setOnClickListener(null);
      deleteButton.setTag(null);
    }

    // Bouton d'edition
    ImageButton editButton = (ImageButton) convertView.findViewById(R.id.edit_button);
    editButton.setFocusable(false);
    if (editionMode) {
      editButton.setVisibility(View.VISIBLE);
      editButton.setOnClickListener(activity);
      editButton.setTag(checklist);
    } else {
      editButton.setVisibility(View.GONE);
      editButton.setOnClickListener(null);
      editButton.setTag(null);
    }

    return convertView;
  }

  public ChecklistGroup getChecklistGroup() {
    return checklistGroup;
  }

  public void setChecklistGroup(ChecklistGroup checklistGroup) {
    this.checklistGroup = checklistGroup;
    notifyDataSetChanged();
  }

  public boolean isEditionMode() {
    return editionMode;
  }

  public void setEditionMode(boolean editionMode) {
    this.editionMode = editionMode;
    notifyDataSetChanged();
  }

}
