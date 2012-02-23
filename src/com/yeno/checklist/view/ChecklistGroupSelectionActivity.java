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

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Environment;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.yeno.checklist.ChecklistConstants;
import com.yeno.checklist.R;
import com.yeno.checklist.controller.ChecklistController;
import com.yeno.checklist.controller.ChecklistMailSender;
import com.yeno.checklist.controller.ChecklistParser;
import com.yeno.checklist.controller.ChecklistSerializer;
import com.yeno.checklist.model.ChecklistGroup;

/**
 * @author Yeno
 */
public class ChecklistGroupSelectionActivity extends Activity implements OnItemClickListener, OnClickListener {

  private static final int MENU_NEW_CHECKLIST_GROUP = Menu.FIRST;
  private static final int MENU_EDIT_CHECKLIST_GROUP = Menu.FIRST + 1;
  private static final int MENU_REORDER = Menu.FIRST + 2;
  private static final int MENU_IMPORT_EXPORT_CHECKLIST_GROUP = Menu.FIRST + 3;
  private static final int MENU_IMPORT_CHECKLIST_GROUP = Menu.FIRST + 4;
  private static final int MENU_EXPORT_CHECKLIST_GROUP = Menu.FIRST + 5;
  private static final int MENU_SEND_CHECKLIST_GROUP = Menu.FIRST + 6;
  private static final int MENU_SETTINGS = Menu.FIRST + 7;
  private static final int MENU_RELEASE_NOTES = Menu.FIRST + 8;
  private static final int MENU_CONTACT_DEV = Menu.FIRST + 9;

  private static final int CONTEXT_MENU_EDIT = Menu.FIRST + 10;
  private static final int CONTEXT_MENU_DELETE = Menu.FIRST + 11;

  private static final int EDIT_NEW_CHECKLIST_GROUP_CODE = 1;
  private static final int LICENSE_AGREEMENT_CODE = 2;
  private static final int PREFERENCES_CODE = 3;
  private static final int REORDER_CODE = 4;

  private static final String EDITION_MODE = "edition_mode";
  private static ChecklistGroup checklistGroupToPass;
  private ListView listView;
  private List<ChecklistGroup> checklistGroups;
  private ChecklistGroupAdapter adapter;
  private Button doneButton;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // If the application just got updated to a newer version, show the release
    // notes
    showReleaseNotesAfterUpdate();

    // Sets the activity's layout and user interface elements
    setContentView(R.layout.checklist_group_selection_list);

    // Gets GUI widgets
    listView = (ListView) findViewById(R.id.selection_list);
    registerForContextMenu(listView);
    doneButton = (Button) findViewById(R.id.done_button);
    doneButton.setVisibility(View.GONE);

    // Recuperation de la liste des checklist groups
    checklistGroups = ChecklistController.loadChecklistGroups(this);

    // Adapteur de contenu
    adapter = new ChecklistGroupAdapter(this);
    adapter.setChecklistGroups(checklistGroups);

    if (savedInstanceState != null) {
      // Recuperation de l'edition mode
      if (savedInstanceState.getBoolean(EDITION_MODE)) {
        adapter.setEditionMode(true);
        doneButton.setVisibility(View.VISIBLE);
      }
    } else {
      // Verification que la licence a ete acceptee
      if (!ChecklistController.isLicenseAccepted(this)) {
        Intent intent = new Intent(this, LicenseAgreementActivity.class);
        startActivityForResult(intent, LICENSE_AGREEMENT_CODE);
      }
    }

    // Association de l'adapteur a la listview
    listView.setAdapter(adapter);
  }

  @Override
  protected void onStart() {
    super.onStart();

    // Ajout du listener sur la vue list
    listView.setOnItemClickListener(this);
    doneButton.setOnClickListener(this);

    // Mise a jour de la liste
    adapter.notifyDataSetChanged();
  }

  @Override
  protected void onStop() {
    // Liberation des listeners
    listView.setOnItemClickListener(null);
    doneButton.setOnClickListener(null);

    super.onStop();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
  }

  @Override
  protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    switch (requestCode) {
    case EDIT_NEW_CHECKLIST_GROUP_CODE:
      if (resultCode == Activity.RESULT_OK) {
        // Ajout de la liste dans le cas d'une creation
        ChecklistGroup checklistGroupToAdd = ChecklistGroupEditActivity.getChecklistGroupToReturn();
        if (checklistGroupToAdd != null) {
          // Creation du fichier du groupe sur le disque
          ChecklistSerializer checklistSerializer = new ChecklistSerializer();
          checklistSerializer.writeXml(checklistGroupToAdd, this);

          // Ajout au model
          checklistGroups.add(checklistGroupToAdd);
          ChecklistController.saveChecklistGroupFiles(checklistGroups, this);
        }

        // Mise a jour de l'affichage
        adapter.notifyDataSetChanged();
      }
      break;
    case LICENSE_AGREEMENT_CODE:
      if (resultCode == RESULT_OK) {
        ChecklistController.setLicenseAccepted(true, this);
      } else {
        finish();
      }
      break;
    case PREFERENCES_CODE:
      // Ne rien faire
      break;
    case REORDER_CODE:
      // Rechargement de la liste
      checklistGroups = ChecklistController.loadChecklistGroups(this);

      // Mise a jour de l'interface
      adapter.setChecklistGroups(checklistGroups);
      break;
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    // Sauvegarde du mode edition
    outState.putBoolean(EDITION_MODE, adapter.isEditionMode());

    super.onSaveInstanceState(outState);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    ChecklistGroup selectedChecklistGroup = checklistGroups.get(position);

    // Stockage du groupe selectionne en static et dans les preferences
    ChecklistController.setCurrentChecklistGroup(selectedChecklistGroup, this);

    // Demarrage de l'activite de Selection de Checklist
    Intent intent = new Intent(this, ChecklistSelectionActivity.class);
    startActivity(intent);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    super.onCreateOptionsMenu(menu);

    // Ajout du menu pour l'ajout de checklist group
    menu.add(0, MENU_NEW_CHECKLIST_GROUP, 0, R.string.new_checklist_group_menu).setIcon(R.drawable.menu_add_checklist_group);

    // Ajout du menu pour l'edition de checklist group
    menu.add(0, MENU_EDIT_CHECKLIST_GROUP, 0, R.string.edit_checklist_groups_menu).setIcon(R.drawable.menu_edit_checklist_group);

    // Ajout du menu pour la reorganisation des checklist group
    menu.add(0, MENU_REORDER, 0, R.string.reorder_checklist_groups_menu).setIcon(R.drawable.menu_reorder_checklist_groups);

    // Ajout des menus d'importation et d'exportation de checklist group
    SubMenu importExportMenu = menu.addSubMenu(0, MENU_IMPORT_EXPORT_CHECKLIST_GROUP, 0, R.string.import_export_checklist_group_menu);
    importExportMenu.setIcon(R.drawable.menu_import_checklist_group);
    importExportMenu.add(0, MENU_IMPORT_CHECKLIST_GROUP, 0, R.string.import_checklist_group_menu).setIcon(R.drawable.menu_import_checklist_group);
    importExportMenu.add(0, MENU_EXPORT_CHECKLIST_GROUP, 0, R.string.export_checklist_group_menu).setIcon(R.drawable.menu_export_checklist_group);

    // Ajout du menu pour l'envoie de liste par email
    menu.add(0, MENU_SEND_CHECKLIST_GROUP, 0, R.string.send_menu).setIcon(R.drawable.menu_mail);

    // Ajout du menu pour les parametres
    menu.add(0, MENU_SETTINGS, 0, R.string.settings_menu).setIcon(R.drawable.menu_settings);

    // Ajout du menu pour afficher les notes de version
    menu.add(0, MENU_RELEASE_NOTES, 0, R.string.release_notes_menu);

    // Ajout du menu pour contacter le developpeur
    menu.add(0, MENU_CONTACT_DEV, 0, R.string.contact_dev_menu);

    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
    case MENU_NEW_CHECKLIST_GROUP:
      Intent intent = new Intent(this, ChecklistGroupEditActivity.class);
      startActivityForResult(intent, EDIT_NEW_CHECKLIST_GROUP_CODE);
      break;
    case MENU_REORDER:
      intent = new Intent(this, ChecklistGroupReorderingActivity.class);
      startActivityForResult(intent, REORDER_CODE);
      break;
    case MENU_IMPORT_CHECKLIST_GROUP:
      // Confirmation et information sur le fonctionnement
      new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_info).setTitle(R.string.info_import_checklist_group_title)
          .setMessage(R.string.info_import_checklist_group_message).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
              // L'utilisateur a confirme, on importe
              importChecklistGroup();
            }
          }).setNegativeButton(android.R.string.cancel, null).show();
      break;
    case MENU_EXPORT_CHECKLIST_GROUP:
      exportChecklistGroup();
      break;
    case MENU_EDIT_CHECKLIST_GROUP:
      doneButton.setVisibility(View.VISIBLE);
      adapter.setEditionMode(true);
      break;
    case MENU_SEND_CHECKLIST_GROUP:
      ChecklistMailSender checklistMailSender = new ChecklistMailSender(this);
      checklistMailSender.sendChecklistGroup(checklistGroups);
      break;
    case MENU_SETTINGS:
      intent = new Intent(this, GlobalPreferenceActivity.class);
      startActivityForResult(intent, PREFERENCES_CODE);
      break;
    case MENU_RELEASE_NOTES:
      showReleaseNotes();
      break;
    case MENU_CONTACT_DEV:
      ChecklistMailSender.sendMailToDev(this);
      break;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, view, menuInfo);

    // Ajout du menu pour editer le groupe
    menu.add(0, CONTEXT_MENU_EDIT, 0, R.string.context_menu_edit);

    // Ajout du menu pour supprimer le groupe
    menu.add(0, CONTEXT_MENU_DELETE, 0, R.string.context_menu_delete);
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
    ChecklistGroup selecteChecklistgroup = (ChecklistGroup) adapter.getItem(info.position);

    switch (item.getItemId()) {
    case CONTEXT_MENU_EDIT:
      editChecklistGroup(selecteChecklistgroup);
      break;
    case CONTEXT_MENU_DELETE:
      deleteWithConfirmation(selecteChecklistgroup);
      break;
    }

    return super.onContextItemSelected(item);
  }

  private void showReleaseNotesAfterUpdate() {
    try {
      // Recuperation du numero de version actuel
      PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_META_DATA);
      int currentVersion = packageInfo.versionCode;

      // Recuperation du numero de version stocke dans les prefernces
      SharedPreferences prefs = getSharedPreferences(ChecklistConstants.CHECKLISTS_PREFS, Context.MODE_PRIVATE);
      int previousVersion = prefs.getInt(ChecklistConstants.VERSION_CODE, 0);

      // S'ils different, on affiche les notes de version
      if (currentVersion > previousVersion) {

        // On n'affiche les notes que s'il ne s'agit pas de la premiere
        // installation (seulement pour les updates)
        if (ChecklistController.isLicenseAccepted(this)) {
          showReleaseNotes();
        }

        // On stocke la valeur actuelle de la version
        prefs.edit().putInt(ChecklistConstants.VERSION_CODE, currentVersion).commit();
      }

    } catch (NameNotFoundException e) {
      // Ne rien faire
    }
  }

  private void showReleaseNotes() {
    // Ouverture d'une fenetre de dialogue contenant les notes de version
    new AlertDialog.Builder(this).setTitle(R.string.release_notes_menu).setMessage(R.string.release_notes)
        .setNeutralButton(R.string.close_button_label, null).show();
  }

  private void exportChecklistGroup() {
    // Verification que le support externe est disponible
    String state = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(state)) {

      int groupCount = checklistGroups.size();

      if (groupCount == 0) {
        // Aucun checklist group a exporter
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_info)
            .setMessage(R.string.info_export_checklist_group_nothing_found_message).setNeutralButton(R.string.close_button_label, null).show();
        return;
      }

      // Creation de la liste des choix
      CharSequence[] groups = new CharSequence[groupCount];
      Iterator<ChecklistGroup> iterator = checklistGroups.iterator();
      int i = 0;
      while (iterator.hasNext()) {
        groups[i] = iterator.next().getName();
        i++;
      }

      // Affichage de la fenetre de dialogue de selection du groupe a
      // exporter
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(R.string.dialog_pick_checklist_group_file_export);
      builder.setItems(groups, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int item) {
          // Recuperation du checklist group selectionne
          ChecklistGroup selectedChecklistGroup = checklistGroups.get(item);

          // Fichier de sortie
          File outputFile = ChecklistController.getAvailableFileNameOnExternalMedia(ChecklistConstants.CHECKLIST_FILE_PREFIX,
              ChecklistConstants.CHECKLIST_FILE_SUFFIX);

          // Sauvegarde du fichier en externe
          ChecklistSerializer checklistSerializer = new ChecklistSerializer();
          checklistSerializer.writeXml(selectedChecklistGroup, outputFile);

          // Information sur le nom du fichier exporte
          new AlertDialog.Builder(ChecklistGroupSelectionActivity.this).setIcon(android.R.drawable.ic_dialog_info)
              .setMessage(getString(R.string.dialog_export_success, outputFile.getName())).setNeutralButton(R.string.close_button_label, null).show();
        }
      });
      builder.create().show();

    } else {
      // Le support SD n'est pas disponible
      new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.warning_sd_card_not_available_title)
          .setMessage(R.string.warning_sd_card_not_available_message).setNeutralButton(R.string.close_button_label, null).show();
    }
  }

  private void importChecklistGroup() {

    // Verification que le support externe est disponible
    String state = Environment.getExternalStorageState();
    if (Environment.MEDIA_MOUNTED.equals(state)) {
      // Recuperation de tous les fichiers de checklist group a la racine
      File path = Environment.getExternalStorageDirectory();

      // Instanciation du parser de checklists
      ChecklistParser parser = new ChecklistParser();
      final List<ChecklistGroup> tempChecklistGroups = new ArrayList<ChecklistGroup>();

      // On liste les fichiers contenus dans ce dossier
      File[] listFiles = path.listFiles();
      if (listFiles != null) {
        for (File file : listFiles) {
          if (!file.isDirectory() && file.getName().endsWith(ChecklistConstants.CHECKLIST_FILE_SUFFIX)) {
            // On parse le fichier
            ChecklistGroup checklistGroup = parser.parse(file);
            if (checklistGroup != null) {
              tempChecklistGroups.add(checklistGroup);
            }
          }
        }
      }

      int groupCount = tempChecklistGroups.size();

      if (groupCount == 0) {
        // Aucun fichier correspondant n'a ete trouve
        new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_info)
            .setMessage(R.string.info_import_checklist_group_nothing_found_message).setNeutralButton(R.string.close_button_label, null).show();
        return;
      }

      // Creation de la liste des choix
      CharSequence[] groups = new CharSequence[groupCount];
      Iterator<ChecklistGroup> iterator = tempChecklistGroups.iterator();
      int i = 0;
      while (iterator.hasNext()) {
        groups[i] = iterator.next().getName();
        i++;
      }

      // Affichage de la fenetre de dialogue de selection
      AlertDialog.Builder builder = new AlertDialog.Builder(this);
      builder.setTitle(R.string.dialog_pick_checklist_group_file_import);
      builder.setItems(groups, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int item) {
          // Recuperation du checklist group selectionne
          ChecklistGroup selectedChecklistGroup = tempChecklistGroups.get(item);

          // Affectation d'un nom libre au groupe
          selectedChecklistGroup.setName(ChecklistController.getAvailableChecklistGroupName(selectedChecklistGroup, checklistGroups));

          // Affectation du nouveau nom de fichier local au groupe
          selectedChecklistGroup.setFileName(ChecklistController.getAvailableFileName(ChecklistConstants.CHECKLIST_FILE_PREFIX,
              ChecklistConstants.CHECKLIST_FILE_SUFFIX, ChecklistGroupSelectionActivity.this));

          // Sauvegarde du fichier en interne
          ChecklistSerializer checklistSerializer = new ChecklistSerializer();
          checklistSerializer.writeXml(selectedChecklistGroup, ChecklistGroupSelectionActivity.this);

          // Ajout du groupe et mise a jour de la vue
          checklistGroups.add(selectedChecklistGroup);
          adapter.notifyDataSetChanged();
          ChecklistController.saveChecklistGroupFiles(checklistGroups, ChecklistGroupSelectionActivity.this);

        }
      });
      builder.create().show();

    } else {
      // Le support SD n'est pas disponible
      new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(R.string.warning_sd_card_not_available_title)
          .setMessage(R.string.warning_sd_card_not_available_message).setNeutralButton(R.string.close_button_label, null).show();
    }

  }

  public static ChecklistGroup getChecklistGroupToPass() {
    ChecklistGroup checklistGroup = checklistGroupToPass;

    // Liberation de la variable statique
    checklistGroupToPass = null;

    return checklistGroup;
  }

  @Override
  public void onClick(final View v) {
    if (v.getId() == R.id.delete_button) {
      deleteWithConfirmation((ChecklistGroup) v.getTag());
    } else if (v.getId() == R.id.edit_button) {
      editChecklistGroup((ChecklistGroup) v.getTag());
    } else if (v.getId() == R.id.done_button) {
      doneButton.setVisibility(View.GONE);
      adapter.setEditionMode(false);
    }
  }

  private void editChecklistGroup(ChecklistGroup checklistGroup) {
    checklistGroupToPass = checklistGroup;
    Intent intent = new Intent(this, ChecklistGroupEditActivity.class);
    startActivityForResult(intent, EDIT_NEW_CHECKLIST_GROUP_CODE);
  }

  private void deleteWithConfirmation(final ChecklistGroup selectedChecklistGroup) {
    // Confirmation de la suppression
    new AlertDialog.Builder(this).setMessage(R.string.warning_delete_checklist_group_message)
        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            // L'utilisateur a confirme, on supprime
            delete(selectedChecklistGroup);
          }
        }).setNegativeButton(android.R.string.cancel, null).show();
  }

  private void delete(ChecklistGroup selectedChecklistGroup) {
    // Suppression de la liste dans le modele
    checklistGroups.remove(selectedChecklistGroup);

    // Suppression de la liste sur le disque
    deleteFile(selectedChecklistGroup.getFileName());

    // Mise a jour de la liste
    adapter.notifyDataSetChanged();

    // Sauvegarde de l'ordre des fichiers
    ChecklistController.saveChecklistGroupFiles(checklistGroups, this);
  }

}
