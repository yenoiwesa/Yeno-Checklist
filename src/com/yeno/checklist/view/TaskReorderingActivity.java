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

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.yeno.checklist.R;
import com.yeno.checklist.controller.ChecklistController;
import com.yeno.checklist.controller.ChecklistSerializer;
import com.yeno.checklist.model.Checklist;
import com.yeno.checklist.model.Task;
import com.yeno.checklist.view.TouchListView.DropListener;

/**
 * @author Yeno
 */
public class TaskReorderingActivity extends Activity implements OnClickListener, DropListener {

  private Checklist checklist;
  private TouchListView listView;
  private Button doneButton;
  private IconicAdapter adapter;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Sets the activity's layout and user interface elements
    setContentView(R.layout.reorder_list);

    // Gets GUI widgets
    final float scale = getResources().getDisplayMetrics().density;
    listView = (TouchListView) findViewById(R.id.reorder_list);
    listView.setItemHeightNormal((int) (scale * 64 + 0.5f));
    listView.setGrabberId(R.id.grabber);
    doneButton = (Button) findViewById(R.id.done_button);

    // Recuperation de la checklist
    checklist = ChecklistController.getCurrentChecklist(this);

    // Creation de l'adapter de list
    adapter = new IconicAdapter();
    listView.setAdapter(adapter);

  }

  @Override
  protected void onStart() {
    super.onStart();

    // Inscription des listeners
    doneButton.setOnClickListener(this);
    listView.setDropListener(this);
  }

  @Override
  protected void onPause() {
    // Sauvegarde du modele
    ChecklistSerializer checklistSerializer = new ChecklistSerializer();
    checklistSerializer.writeXml(ChecklistController.getCurrentChecklistGroup(this), this);

    super.onPause();
  }

  @Override
  protected void onStop() {
    // Desnscription des listeners
    doneButton.setOnClickListener(null);
    listView.setDropListener(null);

    super.onStop();
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.done_button) {
      finish();
    }
  }

  @Override
  public void drop(int from, int to) {
    Task item = adapter.getItem(from);

    adapter.remove(item);
    adapter.insert(item, to);
  }

  private class IconicAdapter extends ArrayAdapter<Task> {

    public IconicAdapter() {
      super(TaskReorderingActivity.this, R.layout.reordering_item, checklist.getTasks());
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      View row = convertView;

      if (row == null) {
        LayoutInflater inflater = getLayoutInflater();
        row = inflater.inflate(R.layout.reordering_item, parent, false);
      }

      TextView label = (TextView) row.findViewById(R.id.label);
      label.setText(checklist.getTasks().get(position).getName());

      return (row);
    }
  }

}
