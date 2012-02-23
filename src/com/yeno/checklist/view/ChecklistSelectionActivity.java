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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yeno.checklist.R;
import com.yeno.checklist.controller.ChecklistController;
import com.yeno.checklist.controller.ChecklistSerializer;
import com.yeno.checklist.model.Checklist;
import com.yeno.checklist.model.ChecklistGroup;
import com.yeno.checklist.model.Task;
import com.yeno.checklist.model.TaskState;

/**
 * @author Yeno
 */
public class ChecklistSelectionActivity extends Activity implements OnClickListener, OnItemClickListener, TextWatcher {

  private static final int EDIT_NEW_CHECKLIST_CODE = 1;
  private static final int REORDER_CHECKLIST_CODE = 2;

  private static final int MENU_RESET_CHECKLISTS = Menu.FIRST;
  private static final int MENU_NEW_CHECKLIST = Menu.FIRST + 1;
  private static final int MENU_EDIT_CHECKLIST = Menu.FIRST + 2;
  private static final int MENU_REORDER_CHECKLISTS = Menu.FIRST + 3;
  private static final int MENU_REVIEW_PROBLEMS = Menu.FIRST + 4;

  private static final int CONTEXT_MENU_EDIT = Menu.FIRST + 10;
  private static final int CONTEXT_MENU_DELETE = Menu.FIRST + 11;

  private static final String EDITION_MODE = "edition_mode";
  private static final String QUICK_ADD_MODE = "quick_add_mode";
  private static final String EMPTY_CHECKLIST_GROUP_HINT_SHOWN = "empty_checklist_group_hint_shown";

  private static Checklist checklistToPass;
  private ChecklistGroup checklistGroup;
  private ListView listView;
  private ChecklistAdapter adapter;
  private TextView checklistGroupNameView;
  private ImageButton changeChecklistGroupButton;
  private Button doneButton;
  private ImageButton quickAddButton;
  private EditText quickAddText;
  private boolean quickAddMode = false;
  private Animation alphaInAnim;
  private Animation alphaOutAnim;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Sets the activity's layout and user interface elements
    setContentView(R.layout.checklist_selection_list);

    // Gets GUI widgets
    listView = (ListView) findViewById(R.id.selection_list);
    registerForContextMenu(listView);
    checklistGroupNameView = (TextView) findViewById(R.id.checklist_group_name);
    changeChecklistGroupButton = (ImageButton) findViewById(R.id.change_checklist_group);
    doneButton = (Button) findViewById(R.id.done_button);
    doneButton.setVisibility(View.GONE);

    // Quick Add Mode
    quickAddButton = (ImageButton) findViewById(R.id.quick_add_button);
    quickAddText = (EditText) findViewById(R.id.quick_add_text);
    quickAddText.setVisibility(View.GONE);

    // Chargement des animations
    alphaInAnim = AnimationUtils.loadAnimation(this, R.anim.alpha_in);
    alphaOutAnim = AnimationUtils.loadAnimation(this, R.anim.alpha_out);

    // Recuperation du Checklist group selectionne
    checklistGroup = ChecklistController.getCurrentChecklistGroup(this);

    // Adapteur de contenu
    adapter = new ChecklistAdapter(this);

    // Recuperation de l'edition mode
    if (savedInstanceState != null) {

      // Recuperation de l'edition mode
      if (savedInstanceState.getBoolean(EDITION_MODE)) {
        adapter.setEditionMode(true);
        doneButton.setVisibility(View.VISIBLE);
        quickAddButton.setVisibility(View.GONE);
      }

      // Recuperation du quick add mode
      if (savedInstanceState.getBoolean(QUICK_ADD_MODE)) {
        quickAddMode = true;
        quickAddText.setVisibility(View.VISIBLE);
        quickAddButton.setBackgroundResource(R.drawable.nav_bar_ok_button);
      }
    }

    // Association de l'adapteur a la listview
    listView.setAdapter(adapter);

    // Initialisation de l'adapteur (le checklistgroup ne devrait pas etre
    // nul ici)
    adapter.setChecklistGroup(checklistGroup);
    checklistGroupNameView.setText(checklistGroup.getName());

    // Chargement des preferences
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    // Affichage d'une indication si aucune checklist dans ce groupe
    if (!prefs.getBoolean(EMPTY_CHECKLIST_GROUP_HINT_SHOWN, false) && checklistGroup.getChecklists().size() == 0) {
      Toast.makeText(this, R.string.toast_hint_add_checklists_1, Toast.LENGTH_LONG).show();
      Toast.makeText(this, R.string.toast_hint_add_checklists_2, Toast.LENGTH_LONG).show();
      Toast.makeText(this, R.string.toast_hint_add_3, Toast.LENGTH_LONG).show();
      prefs.edit().putBoolean(EMPTY_CHECKLIST_GROUP_HINT_SHOWN, true).commit();
    }
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Ajout des listeners
    listView.setOnItemClickListener(this);
    changeChecklistGroupButton.setOnClickListener(this);
    doneButton.setOnClickListener(this);
    quickAddButton.setOnClickListener(this);
    quickAddText.addTextChangedListener(this);

    // Mise a jour de la liste
    adapter.notifyDataSetChanged();
  }

  @Override
  protected void onStop() {
    // Liberation des listeners
    listView.setOnItemClickListener(null);
    changeChecklistGroupButton.setOnClickListener(null);
    doneButton.setOnClickListener(null);
    quickAddButton.setOnClickListener(null);
    quickAddText.removeTextChangedListener(this);

    super.onStop();
  }

  @Override
  protected void onPause() {
    // Sauvegarde du modele
    if (checklistGroup != null) {
      ChecklistSerializer checklistSerializer = new ChecklistSerializer();
      checklistSerializer.writeXml(checklistGroup, this);
    }

    super.onPause();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case EDIT_NEW_CHECKLIST_CODE:
      if (resultCode == Activity.RESULT_OK) {
        // Ajout de la checklist s'il s'agit d'une creation
        Checklist resultChecklist = ChecklistEditActivity.getResultChecklist();
        if (resultChecklist != null) {
          checklistGroup.addChecklist(resultChecklist);
        }

        // Serialization du XML
        ChecklistSerializer serializer = new ChecklistSerializer();
        serializer.writeXml(checklistGroup, this);

        // Mise a jour de la liste
        adapter.notifyDataSetChanged();
      }
      break;
    case REORDER_CHECKLIST_CODE:
      adapter.notifyDataSetChanged();
      break;
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    // Sauvegarde du mode edition
    outState.putBoolean(EDITION_MODE, adapter.isEditionMode());
    outState.putBoolean(QUICK_ADD_MODE, quickAddMode);

    super.onSaveInstanceState(outState);
  }

  @Override
  public void onClick(final View v) {
    if (v.getId() == R.id.change_checklist_group) {
      finish();
    } else if (v.getId() == R.id.read_button) {
      Checklist checklist = (Checklist) v.getTag();
      checklist.switchRead();
      adapter.notifyDataSetChanged();
    } else if (v.getId() == R.id.checklist_state_button) {
      Checklist checklist = (Checklist) v.getTag();
      switchAllDone(checklist);
    } else if (v.getId() == R.id.delete_button) {
      deleteWithConfirmation((Checklist) v.getTag());
    } else if (v.getId() == R.id.edit_button) {
      editChecklist((Checklist) v.getTag());
    } else if (v.getId() == R.id.done_button) {
      doneButton.setVisibility(View.GONE);
      quickAddButton.setVisibility(View.VISIBLE);
      adapter.setEditionMode(false);
    } else if (v.getId() == R.id.quick_add_button) {
      switchQuickAddMode();
    }
  }

  private void switchAllDone(final Checklist checklist) {
    // Si la liste contient un probleme, on s'assure que l'utilisateur
    // confirme de perdre cette info
    if (checklist.containsProblem()) {

      // Ouverture d'une fenetre de dialogue pour confirmer la suppression
      // du probleme
      new AlertDialog.Builder(this).setTitle(R.string.lose_checklist_problem_title).setMessage(R.string.lose_checklist_problem_label)
          .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              checklist.switchAllDone();
              adapter.notifyDataSetChanged();
            }
          }).setNeutralButton(R.string.cancel_button_label, null).show();
    } else {
      checklist.switchAllDone();
      adapter.notifyDataSetChanged();
    }
  }

  private void switchQuickAddMode() {
    if (quickAddMode) {
      quickAddButton.setBackgroundResource(R.drawable.nav_bar_button);
      quickAddText.setVisibility(View.GONE);
      quickAddText.startAnimation(alphaOutAnim);

      // Fermeture du clavier
      InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      manager.hideSoftInputFromWindow(quickAddText.getWindowToken(), 0);
    } else {
      quickAddButton.setBackgroundResource(R.drawable.nav_bar_ok_button);
      quickAddText.setVisibility(View.VISIBLE);
      quickAddText.startAnimation(alphaInAnim);
      quickAddText.requestFocus();

      // Ouverture du clavier
      InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      mgr.showSoftInput(quickAddText, InputMethodManager.SHOW_IMPLICIT);
    }
    quickAddMode = !quickAddMode;
  }

  private void deleteWithConfirmation(final Checklist checklist) {
    // Confirmation de la suppression
    new AlertDialog.Builder(this).setMessage(R.string.warning_delete_checklist_message)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // L'utilisateur a confirme, on supprime
            delete(checklist);
          }
        }).setNegativeButton(android.R.string.cancel, null).show();
  }

  private void delete(Checklist selectedChecklist) {
    // Suppression de la liste dans le modele
    checklistGroup.removeChecklist(selectedChecklist);

    // Mise a jour de la liste
    adapter.notifyDataSetChanged();
  }

  private void editChecklist(Checklist checklist) {
    checklistToPass = checklist;
    Intent intent = new Intent(this, ChecklistEditActivity.class);
    startActivityForResult(intent, EDIT_NEW_CHECKLIST_CODE);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    Checklist selectedChecklist = checklistGroup.getChecklists().get(position);

    // Stockage de la position de la liste pour l'utiliser dans l'activite
    // suivante
    ChecklistController.setCurrentChecklist(selectedChecklist, this);

    // Lancement de l'activite d'affichage de checklist
    Intent intent = new Intent(this, ChecklistActivity.class);
    startActivity(intent);
  }

  public static Checklist getChecklistToPass() {
    Checklist checklist = checklistToPass;

    // Liberation de la variable statique
    checklistToPass = null;

    return checklist;
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    // Ajout du menu pour le reset de l'etat des checklists
    menu.add(0, MENU_RESET_CHECKLISTS, 0, R.string.reset_checklists_menu).setIcon(R.drawable.menu_clear_checklists);

    // Ajout du menu pour l'ajout de checklist
    menu.add(0, MENU_NEW_CHECKLIST, 0, R.string.new_checklist_menu).setIcon(R.drawable.menu_add_checklist);

    // Ajout du menu pour l'edition de checklist
    menu.add(0, MENU_EDIT_CHECKLIST, 0, R.string.edit_checklists_menu).setIcon(R.drawable.menu_edit_checklist);

    // Ajout du menu pour la reorganisation des checklists
    menu.add(0, MENU_REORDER_CHECKLISTS, 0, R.string.reorder_checklists_menu).setIcon(R.drawable.menu_reorder_checklist);

    // Ajout du menu pour la revue des problemes
    menu.add(0, MENU_REVIEW_PROBLEMS, 0, R.string.problems_review_menu).setIcon(R.drawable.menu_problems_review);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case MENU_RESET_CHECKLISTS:
      checklistGroup.resetAllChecklists();
      adapter.notifyDataSetChanged();
      break;
    case MENU_NEW_CHECKLIST:
      Intent intent = new Intent(this, ChecklistEditActivity.class);
      startActivityForResult(intent, EDIT_NEW_CHECKLIST_CODE);
      break;
    case MENU_EDIT_CHECKLIST:
      if (quickAddMode) {
        switchQuickAddMode();
      }
      quickAddButton.setVisibility(View.GONE);
      doneButton.setVisibility(View.VISIBLE);
      adapter.setEditionMode(true);
      break;
    case MENU_REORDER_CHECKLISTS:
      intent = new Intent(this, ChecklistReorderingActivity.class);
      startActivityForResult(intent, REORDER_CHECKLIST_CODE);
      break;
    case MENU_REVIEW_PROBLEMS:
      reviewProblems();
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, view, menuInfo);

    // Ajout du menu pour editer la liste
    menu.add(0, CONTEXT_MENU_EDIT, 0, R.string.context_menu_edit);

    // Ajout du menu pour supprimer la liste
    menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.context_menu_delete);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    Checklist selecteChecklist = (Checklist) adapter.getItem(info.position);

    switch (item.getItemId()) {
    case CONTEXT_MENU_EDIT:
      editChecklist(selecteChecklist);
      break;
    case CONTEXT_MENU_DELETE:
      deleteWithConfirmation(selecteChecklist);
      break;
    }

    return super.onContextItemSelected(item);
  }

  @Override
  public void afterTextChanged(Editable s) {
  }

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {
  }

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
    // Si l'utilisateur a appuyŽ sur Enter
    if (TextUtils.indexOf(s, '\n') != -1) {

      if (s.length() > 1) {
        // Creation de la tache
        Checklist checklist = new Checklist();
        checklist.setName(s.toString().trim());

        checklistGroup.addChecklist(checklist);

        // Serialization du XML
        ChecklistSerializer serializer = new ChecklistSerializer();
        serializer.writeXml(ChecklistController.getCurrentChecklistGroup(this), this);

        // Mise a jour des vues
        adapter.notifyDataSetChanged();
      }

      // Suppression du contenu du champs de texte
      quickAddText.setText("");
    }
  }

  private void reviewProblems() {
    // Resume de tous les problemes de ce groupe
    List<Task> problematicTasks = new ArrayList<Task>();
    for (Checklist checklist : checklistGroup.getChecklists()) {
      for (Task task : checklist.getTasks()) {
        if (task.getTaskState().equals(TaskState.PROBLEM)) {
          problematicTasks.add(task);
        }
      }
    }

    SpannableStringBuilder sb = new SpannableStringBuilder();
    // Ecriture des problemes
    if (!problematicTasks.isEmpty()) {
      for (Task problemTask : problematicTasks) {
        sb.append("   - ");
        sb.append(problemTask.getName());

        String problem = problemTask.getProblem();
        if (!TextUtils.isEmpty(problem)) {
          int start = sb.length();
          sb.append("\n      \u21B3 ");
          sb.append(problem);
          int end = sb.length();
          sb.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          sb.setSpan(new ForegroundColorSpan(0xffc54d39), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        sb.append('\n');
      }
    } else {
      sb.append(getString(R.string.no_problem_label));
    }

    // Ouverture d'une fenetre de dialogue pour passer en revue les problemes
    new AlertDialog.Builder(this).setTitle(R.string.problems_review_menu).setMessage(sb)
        .setPositiveButton(R.string.clear_all_button, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            for (Checklist checklist : checklistGroup.getChecklists()) {
              checklist.clearAllProblems();
            }
            adapter.notifyDataSetChanged();
          }
        }).setNeutralButton(R.string.close_button_label, null).show();
  }

}
