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
import com.yeno.checklist.controller.ChecklistParser;
import com.yeno.checklist.controller.ChecklistSerializer;
import com.yeno.checklist.model.ChecklistGroup;

/**
 * @author Yeno
 */
public class ChecklistGroupEditActivity extends Activity implements OnClickListener, TextWatcher {

  private static final String EDITED_CHECKLIST_GROUP_FILE_NAME = "edited_checklist_group_file_name";

  private static ChecklistGroup checklistGroupToReturn;

  private Button saveButton;
  private Button cancelButton;
  private EditText nameText;
  private EditText descriptionText;
  private ChecklistGroup checklistGroup;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Sets the activity's layout and user interface elements
    setContentView(R.layout.checklist_group_edit);

    // Gets GUI widgets
    saveButton = (Button) findViewById(R.id.save_button);
    cancelButton = (Button) findViewById(R.id.cancel_button);
    nameText = (EditText) findViewById(R.id.name_text);
    descriptionText = (EditText) findViewById(R.id.description_text);

    // Recuperation du checklist group en cours d'edition
    if (savedInstanceState == null) {
      // On recupere en static depuis l'activite qui nous a lance (peut
      // etre nul en cas de creation)
      checklistGroup = ChecklistGroupSelectionActivity.getChecklistGroupToPass();
    } else {
      // Sinon, recuperation grace au nom du fichier stocke dans le bundle
      String checklistGroupFileName = savedInstanceState.getString(EDITED_CHECKLIST_GROUP_FILE_NAME);
      if (checklistGroupFileName != null) {
        ChecklistParser parser = new ChecklistParser();
        checklistGroup = parser.parse(checklistGroupFileName, this);
      }
    }

    // Remplissage des champs
    if (checklistGroup != null) {
      nameText.setText(checklistGroup.getName());

      String description = checklistGroup.getComment();
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
    nameText.removeTextChangedListener(this);

    super.onStop();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    if (checklistGroup != null) {
      outState.putString(EDITED_CHECKLIST_GROUP_FILE_NAME, checklistGroup.getFileName());
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
    }
  }

  /**
	 * 
	 */
  private void save() {
    // S'il s'agit d'une creation, on instancie le groupe
    boolean edition = true;
    if (checklistGroup == null) {
      edition = false;
      checklistGroup = new ChecklistGroup();

      // Affectation d'un nouveau nom de fichier local au groupe
      checklistGroup.setFileName(ChecklistController.getAvailableFileName(ChecklistConstants.CHECKLIST_FILE_PREFIX,
          ChecklistConstants.CHECKLIST_FILE_SUFFIX, this));

      checklistGroupToReturn = checklistGroup;
    }

    // Remplissage des valeurs
    checklistGroup.setName(nameText.getText().toString().trim());
    String description = descriptionText.getText().toString().trim();
    if (!TextUtils.isEmpty(description)) {
      checklistGroup.setComment(description);
    } else {
      checklistGroup.setComment(null);
    }

    // Mise a jour du fichier en interne
    if (edition) {
      ChecklistSerializer checklistSerializer = new ChecklistSerializer();
      checklistSerializer.writeXml(checklistGroup, this);
    }
  }

  public static ChecklistGroup getChecklistGroupToReturn() {
    ChecklistGroup checklistGroup = checklistGroupToReturn;

    // Liberation de la variable statique
    checklistGroupToReturn = null;

    return checklistGroup;
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
}
