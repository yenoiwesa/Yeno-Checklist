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

import java.util.ArrayList;
import java.util.List;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.yeno.checklist.R;
import com.yeno.checklist.model.ChecklistGroup;

/**
 * @author Yeno
 */
public class ChecklistGroupAdapter extends BaseAdapter {

  private List<ChecklistGroup> checklistGroups = new ArrayList<ChecklistGroup>(0);
  private ChecklistGroupSelectionActivity activity;
  private boolean editionMode = false;

  /**
   * @param context
   */
  public ChecklistGroupAdapter(ChecklistGroupSelectionActivity activity) {
    super();
    this.activity = activity;
  }

  @Override
  public int getCount() {
    return checklistGroups.size();
  }

  @Override
  public Object getItem(int position) {
    return checklistGroups.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {

    if (convertView == null) {
      convertView = activity.getLayoutInflater().inflate(R.layout.checklist_group_item, null);
    }

    // Recuperation de l'element du modele
    ChecklistGroup checklistGroup = (ChecklistGroup) getItem(position);

    // Titre du groupe
    TextView titleView = (TextView) convertView.findViewById(R.id.checklist_title);
    titleView.setText(checklistGroup.getName());

    // Commentaire associe au groupe (s'il existe)
    TextView commentView = (TextView) convertView.findViewById(R.id.checklist_comment);
    String comment = checklistGroup.getComment();
    if (comment != null) {
      commentView.setText(comment);
      commentView.setVisibility(View.VISIBLE);
    } else {
      commentView.setVisibility(View.GONE);
    }

    // Nombre de checklists dans le groupe
    TextView checklistsCountView = (TextView) convertView.findViewById(R.id.checklist_count);
    checklistsCountView.setText(Integer.toString(checklistGroup.getChecklists().size()));

    // Bouton de suppression
    ImageButton deleteButton = (ImageButton) convertView.findViewById(R.id.delete_button);
    deleteButton.setFocusable(false);
    if (editionMode) {
      deleteButton.setVisibility(View.VISIBLE);
      deleteButton.setOnClickListener(activity);
      deleteButton.setTag(checklistGroup);
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
      editButton.setTag(checklistGroup);
    } else {
      editButton.setVisibility(View.GONE);
      editButton.setOnClickListener(null);
      editButton.setTag(null);
    }

    return convertView;
  }

  public List<ChecklistGroup> getChecklistGroups() {
    return checklistGroups;
  }

  public void setChecklistGroups(List<ChecklistGroup> checklistGroups) {
    this.checklistGroups = checklistGroups;
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
