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
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.yeno.checklist.model.Checklist;

/**
 * @author Yeno
 */
public class ChecklistEditActivity extends Activity implements OnClickListener, TextWatcher {

  private static final String EDITED_CHECKLIST_POSITION = "edited_checklist_position";
  private static final String LOCALE_POSITION = "locale_position";
  private static Checklist resultChecklist;
  private Button saveButton;
  private Button cancelButton;
  private EditText nameText;
  private EditText descriptionText;
  private Checklist checklist;
  private Button speechLocaleButton;
  private String[] localeNames;
  private String[] localeValues;
  private int localePosition = 0;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Sets the activity's layout and user interface elements
    setContentView(R.layout.checklist_edit);

    // Gets GUI widgets
    saveButton = (Button) findViewById(R.id.save_button);
    cancelButton = (Button) findViewById(R.id.cancel_button);
    nameText = (EditText) findViewById(R.id.name_text);
    descriptionText = (EditText) findViewById(R.id.description_text);
    speechLocaleButton = (Button) findViewById(R.id.speech_locale_button);

    // Tableau des locales
    localeNames = getResources().getStringArray(R.array.text_to_speech_locales);
    localeValues = getResources().getStringArray(R.array.text_to_speech_locale_values);

    // Recuperation de la checklist en cours d'edition
    if (savedInstanceState == null) {
      // On recupere en static depuis l'activite qui nous a lance (peut
      // etre nul en cas de creation)
      checklist = ChecklistSelectionActivity.getChecklistToPass();
    } else {
      // Sinon, recuperation grace a la position de la checklist dans le
      // groupe
      int checklistPosition = savedInstanceState.getInt(EDITED_CHECKLIST_POSITION, ChecklistConstants.NOT_DEFINED);
      if (checklistPosition != ChecklistConstants.NOT_DEFINED) {
        checklist = ChecklistController.getCurrentChecklistGroup(this).getChecklists().get(checklistPosition);
      }
    }

    // Remplissage des champs
    if (checklist != null) {
      nameText.setText(checklist.getName());

      String speechLocale = checklist.getSpeechLocale();
      if (!TextUtils.isEmpty(speechLocale)) {
        localePosition = indexOfLocale(speechLocale);
      }

      String description = checklist.getComment();
      if (description != null) {
        descriptionText.setText(description);
      }
    }

    // Recuperation de la locale position apres rotation de l'ecran
    if (savedInstanceState != null) {
      localePosition = savedInstanceState.getInt(LOCALE_POSITION);
    }

    // Initialisation du bouton de speech locale
    speechLocaleButton.setText(localeNames[localePosition]);
  }

  private int indexOfLocale(String speechLocale) {
    for (int i = 1; i < localeValues.length; i++) {
      String locale = localeValues[i];
      if (speechLocale.equals(locale)) {
        return i;
      }
    }
    return 0;
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Inscription des listeners
    saveButton.setOnClickListener(this);
    cancelButton.setOnClickListener(this);
    speechLocaleButton.setOnClickListener(this);
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
    speechLocaleButton.setOnClickListener(null);
    nameText.removeTextChangedListener(this);

    super.onStop();
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    if (checklist != null) {
      outState.putInt(EDITED_CHECKLIST_POSITION, ChecklistController.getCurrentChecklistGroup(this).getChecklists().indexOf(checklist));
    }
    outState.putInt(LOCALE_POSITION, localePosition);

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
    } else if (v.getId() == R.id.speech_locale_button) {
      changeSpeechLocale();
    }
  }

  /**
	 * 
	 */
  private void save() {
    // S'il s'agit d'une creation, on instancie la liste, et on l'etabli
    // comme valeur instance de retour
    if (checklist == null) {
      checklist = new Checklist();
      resultChecklist = checklist;
    }

    // Remplissage des valeurs
    checklist.setName(nameText.getText().toString().trim());

    if (localePosition > 0) {
      checklist.setSpeechLocale(localeValues[localePosition]);
    } else {
      checklist.setSpeechLocale(null);
    }

    String description = descriptionText.getText().toString().trim();
    if (!TextUtils.isEmpty(description)) {
      checklist.setComment(description);
    } else {
      checklist.setComment(null);
    }
  }

  private void changeSpeechLocale() {
    // Affichage de la fenetre de dialogue de selection
    AlertDialog.Builder builder = new AlertDialog.Builder(this);
    builder.setTitle(R.string.speech_locale_label);
    builder.setItems(localeNames, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int item) {
        // Recuperation de la locale selectionnee
        localePosition = item;
        String selectedLocale = localeNames[item];

        // Mise a jour du nom du bouton
        speechLocaleButton.setText(selectedLocale);
      }
    });
    builder.create().show();
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

  public static Checklist getResultChecklist() {
    Checklist checklist = resultChecklist;

    // Suppression de la variable stockee en static
    resultChecklist = null;

    return checklist;
  }

}
