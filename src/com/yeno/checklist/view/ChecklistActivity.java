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

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.yeno.checklist.ChecklistConstants;
import com.yeno.checklist.R;
import com.yeno.checklist.controller.ChecklistController;
import com.yeno.checklist.controller.ChecklistSerializer;
import com.yeno.checklist.model.Checklist;
import com.yeno.checklist.model.Task;
import com.yeno.checklist.model.TaskState;

/**
 * @author Yeno
 */
public class ChecklistActivity extends Activity implements OnClickListener, OnItemClickListener, OnInitListener, TextWatcher {

  private static final int TEXT_TO_SPEECH_CODE = 1;
  private static final int SPEECH_RECOGNITION_CODE = 2;
  private static final int EDIT_NEW_TASK_CODE = 3;
  private static final int REORDER_TASKS_CODE = 4;
  private static final int PREFERENCES_CODE = 5;

  private static final int MENU_CHECK_TASKS = Menu.FIRST;
  private static final int MENU_RESET_TASKS = Menu.FIRST + 1;
  private static final int MENU_NEW_TASK = Menu.FIRST + 2;
  private static final int MENU_EDIT_TASK = Menu.FIRST + 3;
  private static final int MENU_REORDER_TASKS = Menu.FIRST + 4;
  private static final int MENU_SETTINGS = Menu.FIRST + 5;

  private static final int CONTEXT_MENU_EDIT = Menu.FIRST + 10;
  private static final int CONTEXT_MENU_DELETE = Menu.FIRST + 11;
  private static final int CONTEXT_MENU_INFO = Menu.FIRST + 12;
  private static final int CONTEXT_MENU_DONE = Menu.FIRST + 13;
  private static final int CONTEXT_MENU_BLANK = Menu.FIRST + 14;
  private static final int CONTEXT_MENU_PROBLEM = Menu.FIRST + 15;
  private static final int CONTEXT_MENU_SKIPPED = Menu.FIRST + 16;

  private static final String EDITION_MODE = "edition_mode";
  private static final String HIDE_DONE_TASKS_MODE = "hide_done_tasks_mode";
  private static final String QUICK_ADD_MODE = "quick_add_mode";
  private static final String EMPTY_CHECKLIST_HINT_SHOWN = "empty_checklist_hint_shown";

  private static Task taskToPass;
  private TextToSpeech textToSpeech;
  private Checklist checklist;
  private ListView listView;
  private TextView checklistNameView;
  private ImageButton changeChecklistButton;
  private TaskAdapter adapter;
  private Button checkButton;
  private Button skipButton;
  private Button problemButton;
  private Button doneButton;
  private boolean stateRetrieved;
  private LinearLayout quickActionsBar;
  private boolean readOnTap;
  private ImageButton quickAddButton;
  private EditText quickAddText;
  private boolean quickAddMode = false;
  private boolean showFocusedTask;
  private boolean overrideTrackballBehavior;
  private boolean showQuickActionsBar;
  private ImageButton hideDoneTasksButton;
  private boolean hideDoneTasksMode = false;
  private Animation alphaInAnim;
  private Animation alphaOutAnim;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Sets the activity's layout and user interface elements
    setContentView(R.layout.checklist_content);

    // Gets GUI widgets
    listView = (ListView) findViewById(R.id.selection_list);
    registerForContextMenu(listView);
    checklistNameView = (TextView) findViewById(R.id.checklist_name);
    changeChecklistButton = (ImageButton) findViewById(R.id.change_checklist);
    checkButton = (Button) findViewById(R.id.check_button);
    skipButton = (Button) findViewById(R.id.skip_button);
    problemButton = (Button) findViewById(R.id.problem_button);
    doneButton = (Button) findViewById(R.id.done_button);
    doneButton.setVisibility(View.GONE);

    // Quick Add Mode
    quickAddButton = (ImageButton) findViewById(R.id.quick_add_button);
    quickAddText = (EditText) findViewById(R.id.quick_add_text);
    quickAddText.setVisibility(View.GONE);

    // Hide Done Tasks Mode
    hideDoneTasksButton = (ImageButton) findViewById(R.id.hide_done_tasks_button);

    // Chargement des animations
    alphaInAnim = AnimationUtils.loadAnimation(this, R.anim.alpha_in);
    alphaOutAnim = AnimationUtils.loadAnimation(this, R.anim.alpha_out);

    // Chargement des preferences
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

    // Afficher la focused task
    showFocusedTask = prefs.getBoolean(getString(R.string.pref_show_focused_task), true);

    // Override du trackball
    overrideTrackballBehavior = prefs.getBoolean(getString(R.string.pref_override_trackball_behavior), true);

    // Quick actions bar
    quickActionsBar = (LinearLayout) findViewById(R.id.quick_actions_bar);
    showQuickActionsBar = showFocusedTask && prefs.getBoolean(getString(R.string.pref_show_quick_actions_bar), true);
    quickActionsBar.setVisibility(showQuickActionsBar ? View.VISIBLE : View.GONE);

    // Read on tap
    readOnTap = prefs.getBoolean(getString(R.string.pref_text_to_speech_on_tap), false);

    // Adapteur de contenu
    adapter = new TaskAdapter(this);
    listView.setAdapter(adapter);
    adapter.setShowFocusedTask(showFocusedTask);

    // Recuperation de la Checklist selectionnee
    checklist = ChecklistController.getCurrentChecklist(this);

    if (savedInstanceState != null) {
      stateRetrieved = true;

      // Recuperation de l'edition mode
      if (savedInstanceState.getBoolean(EDITION_MODE)) {
        adapter.setEditionMode(true);
        doneButton.setVisibility(View.VISIBLE);
        quickAddButton.setVisibility(View.GONE);
        hideDoneTasksButton.setVisibility(View.GONE);
      }

      // Recuperation du quick add mode
      if (savedInstanceState.getBoolean(QUICK_ADD_MODE)) {
        quickAddMode = true;
        quickAddText.setVisibility(View.VISIBLE);
        quickActionsBar.setVisibility(View.GONE);
        quickAddButton.setBackgroundResource(R.drawable.nav_bar_ok_button_multi_right);
      }

      // Recuperation du Hide Done Tasks Mode
      if (savedInstanceState.getBoolean(HIDE_DONE_TASKS_MODE)) {
        hideDoneTasksMode = true;
        hideDoneTasksButton.setBackgroundResource(R.drawable.nav_bar_ok_button_multi_left);
      }
    }

    // Recherche de la tache a mettre en focus ET
    // Mise a jour de la vue de la tache ayant le focus, ainsi que des
    // boutons
    int nextFocusedTask = checklist.findNextFocusedTask();
    SmoothScroll.getInstance().smoothScrollToPosition(listView, nextFocusedTask);
    buttonsUpdate(nextFocusedTask);

    // Initialisation de l'adapteur
    adapter.setChecklist(checklist);

    // Nom de la vue
    checklistNameView.setText(checklist.getName());

    // Si la liste doit etre lue
    if (checklist.isRead()) {
      // Verification de la capacite de Text to Speech
      Intent checkIntent = new Intent();
      checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
      startActivityForResult(checkIntent, TEXT_TO_SPEECH_CODE);
    }

    // Affichage d'une indication si aucune tache dans ce groupe
    if (!prefs.getBoolean(EMPTY_CHECKLIST_HINT_SHOWN, false) && checklist.getTasks().size() == 0) {
      Toast.makeText(this, R.string.toast_hint_add_tasks_1, Toast.LENGTH_LONG).show();
      Toast.makeText(this, R.string.toast_hint_add_tasks_2, Toast.LENGTH_LONG).show();
      Toast.makeText(this, R.string.toast_hint_add_3, Toast.LENGTH_LONG).show();
      prefs.edit().putBoolean(EMPTY_CHECKLIST_HINT_SHOWN, true).commit();
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    // Sauvegarde du mode edition
    outState.putBoolean(EDITION_MODE, adapter.isEditionMode());
    outState.putBoolean(QUICK_ADD_MODE, quickAddMode);
    outState.putBoolean(HIDE_DONE_TASKS_MODE, hideDoneTasksMode);

    super.onSaveInstanceState(outState);
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Ajout des listeners
    listView.setOnItemClickListener(this);
    changeChecklistButton.setOnClickListener(this);
    checkButton.setOnClickListener(this);
    skipButton.setOnClickListener(this);
    problemButton.setOnClickListener(this);
    doneButton.setOnClickListener(this);
    quickAddButton.setOnClickListener(this);
    quickAddText.addTextChangedListener(this);
    hideDoneTasksButton.setOnClickListener(this);
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
    // Liberation des listeners
    listView.setOnItemClickListener(null);
    changeChecklistButton.setOnClickListener(null);
    checkButton.setOnClickListener(null);
    skipButton.setOnClickListener(null);
    problemButton.setOnClickListener(null);
    doneButton.setOnClickListener(null);
    quickAddButton.setOnClickListener(null);
    quickAddText.removeTextChangedListener(this);
    hideDoneTasksButton.setOnClickListener(null);

    super.onStop();
  }

  @Override
  protected void onDestroy() {

    // Liberation du text to speech
    if (textToSpeech != null) {
      textToSpeech.shutdown();
    }

    super.onDestroy();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case TEXT_TO_SPEECH_CODE:
      if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
        // Success, create the TTS instance
        textToSpeech = new TextToSpeech(this, this);
      } else {
        // Missing data, install it
        Intent installIntent = new Intent();
        installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        startActivity(installIntent);
      }
      break;
    case SPEECH_RECOGNITION_CODE:
      if (data != null) {
        // ArrayList<String> results =
        // data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
      }
      break;
    case EDIT_NEW_TASK_CODE:
      if (resultCode == Activity.RESULT_OK) {
        // Ajout de la task s'il s'agit d'une creation
        Task resultTask = TaskEditActivity.getResultTask();
        if (resultTask != null) {
          checklist.addTask(resultTask);
        }

        // Serialization du XML
        ChecklistSerializer serializer = new ChecklistSerializer();
        serializer.writeXml(ChecklistController.getCurrentChecklistGroup(this), this);

        // Mise a jour des vues
        updateViews(true);
      }
      break;
    case REORDER_TASKS_CODE:
      updateViews(true);
      break;
    case PREFERENCES_CODE:
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

      // Afficher la focused task
      showFocusedTask = prefs.getBoolean(getString(R.string.pref_show_focused_task), true);
      adapter.setShowFocusedTask(showFocusedTask);
      adapter.notifyDataSetChanged();

      // Override du trackball
      overrideTrackballBehavior = prefs.getBoolean(getString(R.string.pref_override_trackball_behavior), true);

      // Visibilite de la barre d'actions rapides
      showQuickActionsBar = showFocusedTask && prefs.getBoolean(getString(R.string.pref_show_quick_actions_bar), true);
      quickActionsBar.setVisibility(showQuickActionsBar ? View.VISIBLE : View.GONE);

      // Lecture de la tache au touche
      readOnTap = prefs.getBoolean(getString(R.string.pref_text_to_speech_on_tap), false);

      // Langage par defaut de la lecture
      if (textToSpeech != null) {
        setSpeechLocale();
      }
      break;
    }
  }

  @Override
  public void onClick(final View v) {
    if (v.getId() == R.id.change_checklist) {
      finish();
    } else if (v.getId() == R.id.check_button) {
      Task focusedTask = checklist.getFocusedTask();
      if (focusedTask != null) {
        switchTaskAndGoNext(focusedTask, false);
      } else {
        finish();
      }
    } else if (v.getId() == R.id.skip_button) {
      Task focusedTask = checklist.getFocusedTask();
      // Etat SKIPPED pour la tache
      focusedTask.setTaskState(TaskState.SKIPPED);
      // Mise a jour des vues (passage a l'item suivant, etat des boutons,
      // etc)
      updateViews(false);
    } else if (v.getId() == R.id.problem_button) {
      Task focusedTask = checklist.getFocusedTask();
      // Etat PROBLEM pour la tache
      focusedTask.setTaskState(TaskState.PROBLEM);
      // Demande de details sur le problem
      askForProblemDetails(focusedTask);
      // Mise a jour des vues (passage a l'item suivant, etat des boutons,
      // etc)
      updateViews(false);
    } else if (v.getId() == R.id.info_button) {
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(R.string.task_notes).setMessage((String) v.getTag()).setNeutralButton(R.string.close_button_label, null);
      builder.create().show();
    } else if (v.getId() == R.id.delete_button) {
      deleteTaskWithConfirmation((Task) v.getTag());
    } else if (v.getId() == R.id.edit_button) {
      editTask((Task) v.getTag());
    } else if (v.getId() == R.id.done_button) {
      doneButton.setVisibility(View.GONE);
      quickAddButton.setVisibility(View.VISIBLE);
      hideDoneTasksButton.setVisibility(View.VISIBLE);
      adapter.setEditionMode(false);
    } else if (v.getId() == R.id.quick_add_button) {
      switchQuickAddMode();
    } else if (v.getId() == R.id.hide_done_tasks_button) {
      switchHideDoneTasksMode();
    }
  }

  private void switchQuickAddMode() {
    if (quickAddMode) {
      quickAddButton.setBackgroundResource(R.drawable.nav_bar_button_multi_right);
      quickAddText.setVisibility(View.GONE);
      quickAddText.startAnimation(alphaOutAnim);
      if (showQuickActionsBar) {
        quickActionsBar.setVisibility(View.VISIBLE);
        quickActionsBar.startAnimation(alphaInAnim);
      }

      // Fermeture du clavier
      InputMethodManager manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      manager.hideSoftInputFromWindow(quickAddText.getWindowToken(), 0);
    } else {
      quickAddButton.setBackgroundResource(R.drawable.nav_bar_ok_button_multi_right);
      quickAddText.setVisibility(View.VISIBLE);
      quickAddText.startAnimation(alphaInAnim);
      quickActionsBar.setVisibility(View.GONE);
      quickAddText.requestFocus();

      // Ouverture du clavier
      InputMethodManager mgr = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      mgr.showSoftInput(quickAddText, InputMethodManager.SHOW_IMPLICIT);
    }
    quickAddMode = !quickAddMode;
  }

  private void switchHideDoneTasksMode() {
    if (hideDoneTasksMode) {
      hideDoneTasksButton.setBackgroundResource(R.drawable.nav_bar_button_multi_left);
    } else {
      hideDoneTasksButton.setBackgroundResource(R.drawable.nav_bar_ok_button_multi_left);
    }
    hideDoneTasksMode = !hideDoneTasksMode;
    adapter.notifyDataSetChanged();
  }

  private void deleteTaskWithConfirmation(final Task task) {
    // Confirmation de la suppression
    new AlertDialog.Builder(this).setMessage(R.string.warning_delete_task_message)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // L'utilisateur a confirme, on supprime
            delete(task);
          }
        }).setNegativeButton(android.R.string.cancel, null).show();
  }

  private void delete(Task selectedTask) {
    // Suppression de la tache dans le modele
    checklist.removeTask(selectedTask);

    // Mise a jour des vues
    updateViews(true);
  }

  private void editTask(Task task) {
    // Edition d'une tache
    taskToPass = task;
    Intent intent = new Intent(this, TaskEditActivity.class);
    startActivityForResult(intent, EDIT_NEW_TASK_CODE);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    final Task task = (Task) adapter.getItem(position);
    if (task.getProblem() != null) {
      // Ouverture d'une fenetre de dialogue pour confirmer la suppression
      // du probleme
      new AlertDialog.Builder(this).setTitle(R.string.lose_task_problem_title).setMessage(R.string.lose_task_problem_label)
          .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              switchTaskAndGoNext(task, true);
            }
          }).setNeutralButton(R.string.cancel_button_label, null).show();
    } else {
      switchTaskAndGoNext(task, true);
    }
  }

  private void switchTaskAndGoNext(Task selectedTask, boolean touched) {

    // L'etat de la tache est switche
    selectedTask.defaultSwitchState();

    // Mise a jour des vues (passage a l'item suivant, etat des boutons,
    // etc)
    updateViews(touched);
  }

  private void updateViews(boolean touched) {
    // Recherche de la prochaine tache a mettre en focus
    int focusedTaskPosition = checklist.findNextFocusedTask();

    // Mise a jour des vues de la liste
    adapter.notifyDataSetChanged();

    // Mise a jour des boutons
    buttonsUpdate(focusedTaskPosition);

    if (!touched) {
      if (focusedTaskPosition != ChecklistConstants.NOT_DEFINED) {
        // Vieille methode pour contourner le bug bizarre
        if (focusedTaskPosition <= listView.getFirstVisiblePosition()) {
          SmoothScroll.getInstance().smoothScrollBy(listView, -1, 0);
          SmoothScroll.getInstance().smoothScrollToPosition(listView, focusedTaskPosition);
        } else if (focusedTaskPosition >= listView.getLastVisiblePosition()) {
          SmoothScroll.getInstance().smoothScrollBy(listView, 1, 0);
          SmoothScroll.getInstance().smoothScrollToPosition(listView, focusedTaskPosition);
        }
      }
    }

    // Lecture de la tache
    if ((!touched || readOnTap) && checklist.isRead()) {
      readTask(checklist.getFocusedTask());
    }

    // Lancement de l'enregistrement pour la commande volcale
    // Speech recognition desactivee
    // speechRecognition();
  }

  @SuppressWarnings("unused")
  private void speechRecognition() {
    Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
    recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
    startActivityForResult(recognizerIntent, SPEECH_RECOGNITION_CODE);
  }

  private void readTask(Task task) {
    if (textToSpeech != null) {
      if (task == null) {
        // Lecture du message de terminaison
        textToSpeech.speak("All checked", TextToSpeech.QUEUE_FLUSH, null);
      } else {
        // Lecture du nom de la tache
        textToSpeech.speak(task.getName(), TextToSpeech.QUEUE_FLUSH, null);
      }
    }
  }

  private void buttonsUpdate(int focusedTaskPosition) {
    if (focusedTaskPosition != ChecklistConstants.NOT_DEFINED) {
      // Le bouton Check dit bien Check et le bouton skip est dispo
      checkButton.setText(R.string.check_button);
      skipButton.setVisibility(View.VISIBLE);
      problemButton.setVisibility(View.VISIBLE);
    } else {
      // On inscrit Done sur le bouton, et le bouton skip est indisponible
      checkButton.setText(R.string.quick_actions_done_button);
      skipButton.setVisibility(View.GONE);
      problemButton.setVisibility(View.GONE);
    }
  }

  @Override
  public boolean onTrackballEvent(MotionEvent event) {
    if (showFocusedTask && overrideTrackballBehavior) {
      switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        int focusedTaskPosition = checklist.getFocusedTaskPosition();
        if (focusedTaskPosition != ChecklistConstants.NOT_DEFINED) {
          switchTaskAndGoNext(checklist.getFocusedTask(), false);
        } else {
          finish();
        }
        break;
      }

      return true;
    }
    return false;
  }

  @Override
  public void onInit(int result) {
    if (result == TextToSpeech.SUCCESS) {

      // Selection de la langue
      setSpeechLocale();

      if (!stateRetrieved) {
        // Lecture du premier item
        readTask(checklist.getFocusedTask());
      }
    }
  }

  private void setSpeechLocale() {
    // Recuperation de la Locale de la checklist
    String speechLocale = checklist.getSpeechLocale();

    // Si elle n'est pas definie, on recupere celle par defaut des
    // preferences
    if (TextUtils.isEmpty(speechLocale)) {
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
      speechLocale = prefs.getString(getString(R.string.pref_tts_default_locale), Locale.US.toString());

      // Si la preference a ete specifiee a la valeur Default
      if (TextUtils.isEmpty(speechLocale)) {
        speechLocale = Locale.US.toString();
      }
    }

    // Selection de la langue
    String[] localeArray = TextUtils.split(speechLocale, "_");
    if (localeArray.length > 1) {
      // Selection de la langue avec pays
      textToSpeech.setLanguage(new Locale(localeArray[0], localeArray[1]));
    } else {
      // Selection de la langue simple
      textToSpeech.setLanguage(new Locale(localeArray[0]));
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    // Ajout du menu pour le check de l'etat des taches
    menu.add(0, MENU_CHECK_TASKS, 0, R.string.check_tasks_menu).setIcon(R.drawable.menu_check_tasks);

    // Ajout du menu pour le reset de l'etat des taches
    menu.add(0, MENU_RESET_TASKS, 0, R.string.reset_tasks_menu).setIcon(R.drawable.menu_clear_tasks);

    // Ajout du menu pour l'ajout de tache
    menu.add(0, MENU_NEW_TASK, 0, R.string.new_task_menu).setIcon(R.drawable.menu_add_task);

    // Ajout du menu pour l'edition de taches
    menu.add(0, MENU_EDIT_TASK, 0, R.string.edit_tasks_menu).setIcon(R.drawable.menu_edit_task);

    // Ajout du menu pour la reorganisation des taches
    menu.add(0, MENU_REORDER_TASKS, 0, R.string.reorder_tasks_menu).setIcon(R.drawable.menu_reorder_tasks);

    // Ajout du menu pour les parametres
    menu.add(0, MENU_SETTINGS, 0, R.string.settings_menu).setIcon(R.drawable.menu_settings);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case MENU_RESET_TASKS:
      checklist.resetAllTasks();
      updateViews(false);
      break;
    case MENU_CHECK_TASKS:
      checklist.checkAllTasks();
      updateViews(false);
      break;
    case MENU_NEW_TASK:
      Intent intent = new Intent(this, TaskEditActivity.class);
      startActivityForResult(intent, EDIT_NEW_TASK_CODE);
      break;
    case MENU_EDIT_TASK:
      if (quickAddMode) {
        switchQuickAddMode();
      }
      quickAddButton.setVisibility(View.GONE);
      hideDoneTasksButton.setVisibility(View.GONE);
      doneButton.setVisibility(View.VISIBLE);
      adapter.setEditionMode(true);
      break;
    case MENU_REORDER_TASKS:
      intent = new Intent(this, TaskReorderingActivity.class);
      startActivityForResult(intent, REORDER_TASKS_CODE);
      break;
    case MENU_SETTINGS:
      intent = new Intent(this, ChecklistPreferenceActivity.class);
      startActivityForResult(intent, PREFERENCES_CODE);
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, view, menuInfo);
    AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    Task selectedTask = (Task) adapter.getItem(info.position);

    switch (selectedTask.getTaskState()) {
    case DONE:
      menu.add(0, CONTEXT_MENU_BLANK, 0, R.string.context_menu_blank);
      menu.add(0, CONTEXT_MENU_SKIPPED, 0, R.string.context_menu_skipped);
      menu.add(0, CONTEXT_MENU_PROBLEM, 0, R.string.context_menu_problem);
      break;
    case SKIPPED:
      menu.add(0, CONTEXT_MENU_BLANK, 0, R.string.context_menu_blank);
      menu.add(0, CONTEXT_MENU_DONE, 0, R.string.context_menu_done);
      menu.add(0, CONTEXT_MENU_PROBLEM, 0, R.string.context_menu_problem);
      break;
    case PROBLEM:
      menu.add(0, CONTEXT_MENU_BLANK, 0, R.string.context_menu_blank);
      menu.add(0, CONTEXT_MENU_DONE, 0, R.string.context_menu_done);
      menu.add(0, CONTEXT_MENU_SKIPPED, 0, R.string.context_menu_skipped);
      break;
    default:
      menu.add(0, CONTEXT_MENU_DONE, 0, R.string.context_menu_done);
      menu.add(0, CONTEXT_MENU_SKIPPED, 0, R.string.context_menu_skipped);
      menu.add(0, CONTEXT_MENU_PROBLEM, 0, R.string.context_menu_problem);
      break;
    }

    if (!TextUtils.isEmpty(selectedTask.getComment())) {
      // Ajout du menu pour afficher la note associee
      menu.add(0, CONTEXT_MENU_INFO, 0, R.string.context_menu_info);
    }

    // Ajout du menu pour editer la tache
    menu.add(0, CONTEXT_MENU_EDIT, 0, R.string.context_menu_edit);

    // Ajout du menu pour supprimer la tache
    menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.context_menu_delete);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    Task selectedTask = (Task) adapter.getItem(info.position);

    switch (item.getItemId()) {
    case CONTEXT_MENU_EDIT:
      editTask(selectedTask);
      break;
    case CONTEXT_MENU_DELETE:
      deleteTaskWithConfirmation(selectedTask);
      break;
    case CONTEXT_MENU_INFO:
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(R.string.task_notes).setMessage(selectedTask.getComment()).setNeutralButton(R.string.close_button_label, null);
      builder.create().show();
      break;
    case CONTEXT_MENU_BLANK:
      selectedTask.setTaskState(TaskState.BLANK);
      updateViews(true);
      break;
    case CONTEXT_MENU_DONE:
      selectedTask.setTaskState(TaskState.DONE);
      updateViews(true);
      break;
    case CONTEXT_MENU_SKIPPED:
      selectedTask.setTaskState(TaskState.SKIPPED);
      updateViews(true);
      break;
    case CONTEXT_MENU_PROBLEM:
      selectedTask.setTaskState(TaskState.PROBLEM);
      askForProblemDetails(selectedTask);
      updateViews(true);
      break;
    }

    return super.onContextItemSelected(item);
  }

  public boolean isHideDoneTasksMode() {
    return hideDoneTasksMode;
  }

  public static Task getTaskToPass() {
    Task task = taskToPass;

    // Liberation de la variable statique
    taskToPass = null;

    return task;
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
        Task task = new Task();
        task.setName(s.toString().trim());

        checklist.addTask(task);

        // Serialization du XML
        ChecklistSerializer serializer = new ChecklistSerializer();
        serializer.writeXml(ChecklistController.getCurrentChecklistGroup(this), this);

        // Mise a jour des vues
        updateViews(true);
      }

      // Suppression du contenu du champs de texte
      quickAddText.setText("");
    }
  }

  private void askForProblemDetails(final Task task) {
    // Affiche une fenetre de dialogue pour entrer les details du probleme
    AlertDialog.Builder alert = new AlertDialog.Builder(this);

    alert.setTitle(R.string.task_problem_dialog_title);
    alert.setMessage(R.string.task_problem_dialog_label);

    // Set an EditText view to get user input
    final EditText input = new EditText(this);
    input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
    alert.setView(input);

    alert.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int whichButton) {
        String value = input.getText().toString();
        if (!TextUtils.isEmpty(value)) {
          // On stocke la valeur dans la tache
          task.setProblem(value);

          // Mise a jour de la vue
          adapter.notifyDataSetChanged();
        }
      }
    });
    alert.show();
  }

}
