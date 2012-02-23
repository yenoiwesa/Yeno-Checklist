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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.yeno.checklist.ChecklistConstants;
import com.yeno.checklist.R;
import com.yeno.checklist.controller.ChecklistController;
import com.yeno.checklist.model.Task;

/**
 * @author Yeno
 */
public class TaskEditActivity extends Activity implements OnClickListener, TextWatcher {

  private static final String EDITED_TASK_POSITION = "edited_task_position";
  private static Task resultTask;
  private Button saveButton;
  private Button cancelButton;
  private Button defaultResultButton;
  private EditText nameText;
  private EditText resultText;
  private EditText descriptionText;
  private Task task;
  private String defaultResult;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Sets the activity's layout and user interface elements
    setContentView(R.layout.task_edit);

    // Resultat a utiliser par default
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
    defaultResult = prefs.getString(getString(R.string.pref_task_default_result), getString(R.string.default_result));

    // Gets GUI widgets
    saveButton = (Button) findViewById(R.id.save_button);
    cancelButton = (Button) findViewById(R.id.cancel_button);
    defaultResultButton = (Button) findViewById(R.id.default_result);
    defaultResultButton.setText(getString(R.string.use_default_result_label, defaultResult));
    nameText = (EditText) findViewById(R.id.name_text);
    resultText = (EditText) findViewById(R.id.result_text);
    descriptionText = (EditText) findViewById(R.id.description_text);

    // Recuperation de la checklist en cours d'edition
    if (savedInstanceState == null) {
      // On recupere en static depuis l'activite qui nous a lance (peut
      // etre nul en cas de creation)
      task = ChecklistActivity.getTaskToPass();
    } else {
      // Sinon, recuperation grace a la position de la task dans la liste
      int taskPosition = savedInstanceState.getInt(EDITED_TASK_POSITION, ChecklistConstants.NOT_DEFINED);
      if (taskPosition != ChecklistConstants.NOT_DEFINED) {
        task = ChecklistController.getCurrentChecklist(this).getTasks().get(taskPosition);
      }
    }

    // Remplissage des champs
    if (task != null) {
      nameText.setText(task.getName());
      resultText.setText(task.getResult());

      String description = task.getComment();
      if (description != null) {
        descriptionText.setText(description);
      }
    }
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Inscription des listeners
    saveButton.setOnClickListener(this);
    cancelButton.setOnClickListener(this);
    defaultResultButton.setOnClickListener(this);
    nameText.addTextChangedListener(this);
  }

  @Override
  protected void onResume() {
    super.onResume();

    saveButton.setEnabled(canSave());
  }

  @Override
  protected void onStop() {
    // Liberation des listeners
    saveButton.setOnClickListener(null);
    cancelButton.setOnClickListener(null);
    defaultResultButton.setOnClickListener(null);
    nameText.removeTextChangedListener(this);

    super.onStop();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    if (task != null) {
      outState.putInt(EDITED_TASK_POSITION, ChecklistController.getCurrentChecklist(this).getTasks().indexOf(task));
    }

    super.onSaveInstanceState(outState);
  }

  @Override
  public void onClick(View v) {
    if (v.getId() == R.id.save_button) {
      save();
      setResult(Activity.RESULT_OK);
      finish();
    } else if (v.getId() == R.id.cancel_button) {
      finish();
    } else if (v.getId() == R.id.default_result) {
      resultText.setText(defaultResult);
    }
  }

  /**
	 * 
	 */
  private void save() {
    // S'il s'agit d'une creation, on instancie la tache, et on l'etabli comme
    // valeur instance de retour
    if (task == null) {
      task = new Task();
      resultTask = task;
    }

    // Remplissage des valeurs
    task.setName(nameText.getText().toString().trim());

    String result = resultText.getText().toString().trim();
    if (!TextUtils.isEmpty(result)) {
      task.setResult(result);
    } else {
      task.setResult(null);
    }

    String description = descriptionText.getText().toString().trim();
    if (!TextUtils.isEmpty(description)) {
      task.setComment(description);
    } else {
      task.setComment(null);
    }
  }

  /**
   * @return
   */
  private boolean canSave() {
    return !TextUtils.isEmpty(nameText.getText());
  }

  @Override
  public void afterTextChanged(Editable s) {
    saveButton.setEnabled(canSave());
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
  }

  public static Task getResultTask() {
    Task task = resultTask;

    // Suppression de la variable stockee en static
    resultTask = null;

    return task;
  }

}
