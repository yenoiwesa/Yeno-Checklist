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
package com.yeno.checklist.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.yeno.checklist.ChecklistConstants;
import com.yeno.checklist.R;
import com.yeno.checklist.model.Checklist;
import com.yeno.checklist.model.ChecklistGroup;

/**
 * @author Yeno
 */
public abstract class ChecklistController {

  private static ChecklistGroup currentChecklistGroup;

  public static ChecklistGroup getCurrentChecklistGroup(Context context) {

    if (currentChecklistGroup == null) {
      // Retrieving the checklist group from preferences, if it has been defined
      SharedPreferences prefs = context.getSharedPreferences(ChecklistConstants.CHECKLISTS_PREFS, Context.MODE_PRIVATE);

      String currentChecklistGroupFileName = prefs.getString(ChecklistConstants.CURRENT_CHECKLIST_GROUP_FILE_NAME, null);

      if (currentChecklistGroupFileName != null) {
        // Instantiating checklists parser
        ChecklistParser parser = new ChecklistParser();

        // Parsing the file
        currentChecklistGroup = parser.parse(currentChecklistGroupFileName, context);
      }
    }

    return currentChecklistGroup;
  }

  public static void setCurrentChecklistGroup(ChecklistGroup checklistGroup, Context context) {
    currentChecklistGroup = checklistGroup;

    // Storing the file's name into preferences
    SharedPreferences prefs = context.getSharedPreferences(ChecklistConstants.CHECKLISTS_PREFS, Context.MODE_PRIVATE);
    if (checklistGroup != null) {
      prefs.edit().putString(ChecklistConstants.CURRENT_CHECKLIST_GROUP_FILE_NAME, checklistGroup.getFileName()).commit();
    } else {
      prefs.edit().remove(ChecklistConstants.CURRENT_CHECKLIST_GROUP_FILE_NAME).commit();
    }
  }

  public static Checklist getCurrentChecklist(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(ChecklistConstants.CHECKLISTS_PREFS, Context.MODE_PRIVATE);
    int position = prefs.getInt(ChecklistConstants.CURRENT_CHECKLIST_POSITION, ChecklistConstants.NOT_DEFINED);

    if (position != ChecklistConstants.NOT_DEFINED) {
      return getCurrentChecklistGroup(context).getChecklists().get(position);
    }
    return null;
  }

  public static void setCurrentChecklist(Checklist checklist, Context context) {
    // Storing the checklist position inside preferences
    SharedPreferences prefs = context.getSharedPreferences(ChecklistConstants.CHECKLISTS_PREFS, Context.MODE_PRIVATE);
    if (checklist != null) {
      prefs.edit().putInt(ChecklistConstants.CURRENT_CHECKLIST_POSITION, getCurrentChecklistGroup(context).getChecklists().indexOf(checklist))
          .commit();
    } else {
      prefs.edit().remove(ChecklistConstants.CURRENT_CHECKLIST_POSITION).commit();
    }
  }

  public static List<ChecklistGroup> loadChecklistGroups(Context context) {
    // Instantiating the checklist parser
    ChecklistParser parser = new ChecklistParser();
    List<ChecklistGroup> checklistGroups = new ArrayList<ChecklistGroup>();
    boolean needsSaving = false;

    if (isInitDone(context)) {
      // First, let's retrieve the ordered list stored in the groups preferences
      List<String> checklistGroupFiles = getChecklistGroupFiles(context);
      for (String file : checklistGroupFiles) {
        parseChecklistGroupFile(parser, file, checklistGroups, context);
      }

      // For compatibility reasons, and to avoid errors, let's list the files contained in the application
      // and add them to the end of the list (they are not ordered)
      String[] files = context.fileList();
      for (String file : files) {
        if (!checklistGroupFiles.contains(file)) {
          needsSaving = true;
          Log.v(ChecklistConstants.LOG_TAG, "The file '" + file + "' has been loaded using a deprecated method.");
          parseChecklistGroupFile(parser, file, checklistGroups, context);
        }
      }
    } else {
      // If this is the application's first launch, retrieve the example files
      ChecklistGroup checklistGroup1 = parser.parse(context.getResources().openRawResource(R.raw.sailplane), ChecklistConstants.CHECKLIST_FILE_PREFIX
          + 0 + ChecklistConstants.CHECKLIST_FILE_SUFFIX);
      checklistGroups.add(checklistGroup1);
      ChecklistGroup checklistGroup2 = parser.parse(context.getResources().openRawResource(R.raw.shoppinglist),
          ChecklistConstants.CHECKLIST_FILE_PREFIX + 1 + ChecklistConstants.CHECKLIST_FILE_SUFFIX);
      checklistGroups.add(checklistGroup2);

      // Exporting the files in the internal storage space
      ChecklistSerializer serializer = new ChecklistSerializer();
      serializer.writeXml(checklistGroup1, context);
      serializer.writeXml(checklistGroup2, context);

      // App initialization is now done
      setInitDone(true, context);
      needsSaving = true;
    }

    // If changes that need saving have been performed, then save the current state of the list
    if (needsSaving) {
      saveChecklistGroupFiles(checklistGroups, context);
    }

    return checklistGroups;
  }

  private static void parseChecklistGroupFile(ChecklistParser parser, String file, List<ChecklistGroup> checklistGroups, Context context) {
    if (file.startsWith(ChecklistConstants.CHECKLIST_FILE_PREFIX)) {
      // Parsing the file
      ChecklistGroup checklistGroup = parser.parse(file, context);
      if (checklistGroup != null) {
        checklistGroups.add(checklistGroup);
      }
    }
  }

  public static boolean isInitDone(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(ChecklistConstants.CHECKLISTS_PREFS, Context.MODE_PRIVATE);
    return prefs.getBoolean(ChecklistConstants.INIT_DONE, false);
  }

  public static void setInitDone(boolean initDone, Context context) {
    SharedPreferences prefs = context.getSharedPreferences(ChecklistConstants.CHECKLISTS_PREFS, Context.MODE_PRIVATE);
    prefs.edit().putBoolean(ChecklistConstants.INIT_DONE, initDone).commit();
  }

  public static boolean isLicenseAccepted(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(ChecklistConstants.CHECKLISTS_PREFS, Context.MODE_PRIVATE);
    return prefs.getBoolean(ChecklistConstants.LICENSE_ACCEPTED, false);
  }

  public static void setLicenseAccepted(boolean licenseAccepted, Context context) {
    SharedPreferences prefs = context.getSharedPreferences(ChecklistConstants.CHECKLISTS_PREFS, Context.MODE_PRIVATE);
    prefs.edit().putBoolean(ChecklistConstants.LICENSE_ACCEPTED, licenseAccepted).commit();
  }

  public static void saveChecklistGroupFiles(List<ChecklistGroup> checklistGroups, Context context) {
    SharedPreferences prefs = context.getSharedPreferences(ChecklistConstants.CHECKLISTS_PREFS, Context.MODE_PRIVATE);

    StringBuilder sb = new StringBuilder();
    Iterator<ChecklistGroup> iter = checklistGroups.iterator();
    while (iter.hasNext()) {
      sb.append(iter.next().getFileName());
      if (iter.hasNext()) {
        sb.append(ChecklistConstants.SEPARATOR);
      }
    }

    prefs.edit().putString(ChecklistConstants.CHECKLIST_GROUP_FILES, sb.toString()).commit();
  }

  public static List<String> getChecklistGroupFiles(Context context) {
    SharedPreferences prefs = context.getSharedPreferences(ChecklistConstants.CHECKLISTS_PREFS, Context.MODE_PRIVATE);
    String filesStr = prefs.getString(ChecklistConstants.CHECKLIST_GROUP_FILES, null);

    if (TextUtils.isEmpty(filesStr)) {
      return new ArrayList<String>(0);
    }

    String[] files = filesStr.split(ChecklistConstants.SEPARATOR);
    return Arrays.asList(files);
  }

  public static String getAvailableFileName(String prefix, String suffix, Context context) {
    File directory = context.getFilesDir();
    int i = 0;
    while (new File(directory, prefix + i + suffix).exists()) {
      i++;
    }
    return prefix + i + suffix;
  }

  public static File getAvailableFileNameOnExternalMedia(String prefix, String suffix) {
    File directory = Environment.getExternalStorageDirectory();
    int i = 0;
    File file;
    while ((file = new File(directory, prefix + i + suffix)).exists()) {
      i++;
    }
    return file;
  }

  public static String getAvailableChecklistGroupName(ChecklistGroup group, List<ChecklistGroup> groups) {
    String groupName = group.getName();

    if (isChecklistGroupNameAvailable(group, groups, groupName)) {
      return groupName;
    }

    int i = 1;
    while (!isChecklistGroupNameAvailable(group, groups, groupName + " (" + i + ")")) {
      i++;
    }
    return groupName + " (" + i + ")";
  }

  private static boolean isChecklistGroupNameAvailable(ChecklistGroup group, List<ChecklistGroup> groups, String groupName) {
    for (ChecklistGroup checklistGroup : groups) {
      if (groupName.equals(checklistGroup.getName()) && !group.equals(checklistGroup)) {
        return false;
      }
    }
    return true;
  }

}
