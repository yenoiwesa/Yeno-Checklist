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

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.yeno.checklist.R;
import com.yeno.checklist.model.Checklist;
import com.yeno.checklist.model.Task;

/**
 * @author Yeno
 */
public class TaskAdapter extends BaseAdapter {

  private Checklist checklist;
  private ChecklistActivity activity;
  private boolean editionMode = false;
  private boolean showFocusedTask = true;

  /**
   * @param context
   */
  public TaskAdapter(ChecklistActivity activity) {
    super();
    this.activity = activity;
  }

  @Override
  public int getCount() {
    if (checklist == null) {
      return 0;
    }

    if (activity.isHideDoneTasksMode()) {
      return checklist.getBlankTaskCount();
    }

    return checklist.getTasks().size();
  }

  @Override
  public Object getItem(int position) {
    if (activity.isHideDoneTasksMode()) {
      return checklist.getBlankTask(position);
    }

    return checklist.getTasks().get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    if (convertView == null) {
      convertView = activity.getLayoutInflater().inflate(R.layout.task_item, null);
    }

    // Recuperation de l'element du modele
    Task task = (Task) getItem(position);

    // Etat de la tache
    ImageView taskStateView = (ImageView) convertView.findViewById(R.id.task_state);
    switch (task.getTaskState()) {
    case DONE:
      taskStateView.setImageResource(R.drawable.checked);
      break;
    case SKIPPED:
      taskStateView.setImageResource(R.drawable.skipped);
      break;
    case PROBLEM:
      taskStateView.setImageResource(R.drawable.problem);
      break;
    default:
      taskStateView.setImageResource(R.drawable.empty);
      break;
    }

    // Fond pour la tache focused ou non
    if (showFocusedTask && task.equals(checklist.getFocusedTask())) {
      convertView.setBackgroundResource(R.color.transparent_blue_color);
    } else {
      convertView.setBackgroundResource(0);
    }

    // Titre de la tache
    TextView titleView = (TextView) convertView.findViewById(R.id.task_title);
    titleView.setText(task.getName());

    // Resultat de la tache
    TextView resultView = (TextView) convertView.findViewById(R.id.task_result);
    String result = task.getResult();
    if (!TextUtils.isEmpty(result)) {
      resultView.setVisibility(View.VISIBLE);
      resultView.setText(result);
    } else {
      resultView.setVisibility(View.GONE);
    }

    // Probleme de la tache
    TextView problemView = (TextView) convertView.findViewById(R.id.task_problem);
    String problem = task.getProblem();
    if (!TextUtils.isEmpty(problem)) {
      problemView.setVisibility(View.VISIBLE);
      problemView.setText(problem);
    } else {
      problemView.setVisibility(View.GONE);
    }

    // Numero de la tache
    TextView taskNumberView = (TextView) convertView.findViewById(R.id.task_number);
    StringBuilder numberBuilder = new StringBuilder();
    numberBuilder.append(checklist.getTasks().indexOf(task) + 1);
    numberBuilder.append('/');
    numberBuilder.append(checklist.getTasks().size());
    taskNumberView.setText(numberBuilder.toString());

    // Bouton de commentaire associe a la tache
    Button infoButton = (Button) convertView.findViewById(R.id.info_button);
    String comment = task.getComment();
    infoButton.setTag(comment);
    infoButton.setOnClickListener(activity);
    if (comment != null) {
      infoButton.setVisibility(View.VISIBLE);
    } else {
      infoButton.setVisibility(View.GONE);
    }

    // Bouton de suppression
    ImageButton deleteButton = (ImageButton) convertView.findViewById(R.id.delete_button);
    deleteButton.setFocusable(false);
    if (editionMode) {
      deleteButton.setVisibility(View.VISIBLE);
      deleteButton.setOnClickListener(activity);
      deleteButton.setTag(task);
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
      editButton.setTag(task);
    } else {
      editButton.setVisibility(View.GONE);
      editButton.setOnClickListener(null);
      editButton.setTag(null);
    }

    return convertView;
  }

  public Checklist getChecklist() {
    return checklist;
  }

  public void setChecklist(Checklist checklist) {
    this.checklist = checklist;
    notifyDataSetChanged();
  }

  public boolean isEditionMode() {
    return editionMode;
  }

  public void setEditionMode(boolean editionMode) {
    this.editionMode = editionMode;
    notifyDataSetChanged();
  }

  public boolean isShowFocusedTask() {
    return showFocusedTask;
  }

  public void setShowFocusedTask(boolean showFocusedTask) {
    this.showFocusedTask = showFocusedTask;
  }

}
