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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import com.yeno.checklist.R;
import com.yeno.checklist.model.Checklist;
import com.yeno.checklist.model.ChecklistGroup;
import com.yeno.checklist.model.Task;
import com.yeno.checklist.model.TaskState;

/**
 * @author Yeno
 */
public class ChecklistMailSender {

  private static final String SUBJECT_FORMAT_NAME = "name";
  private static final String SUBJECT_FORMAT_COMMENT = "comment";
  private static final String SUBJECT_FORMAT_NAME_COMMENT = "name+comment";
  private static final String SUBJECT_FORMAT_BLANK = "blank";

  private Context context;
  protected ChecklistGroup selectedChecklistGroup;
  private String recipients;
  private String subjectPref;
  private boolean showNotes;

  public ChecklistMailSender(Context context) {
    this.context = context;
  }

  private void sendTextChecklistGroup() {
    // Creating a text output object for the group
    SpannableStringBuilder mail = new SpannableStringBuilder();

    // Header
    mail.append(selectedChecklistGroup.getName());
    mail.append('\n');
    int start = 0;
    int end = mail.length();
    mail.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    mail.setSpan(new ForegroundColorSpan(0xff39485e), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    mail.setSpan(new AbsoluteSizeSpan(24, true), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    if (!TextUtils.isEmpty(selectedChecklistGroup.getComment())) {
      start = end;
      mail.append(selectedChecklistGroup.getComment());
      mail.append('\n');
      end = mail.length();
      mail.setSpan(new ForegroundColorSpan(0xff9a9a9a), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    mail.append('\n');

    // Summup all issues with this group
    List<Task> problematicTasks = new ArrayList<Task>();
    for (Checklist checklist : selectedChecklistGroup.getChecklists()) {
      for (Task task : checklist.getTasks()) {
        if (task.getTaskState().equals(TaskState.PROBLEM)) {
          problematicTasks.add(task);
        }
      }
    }
    // Write down all issues
    if (!problematicTasks.isEmpty()) {
      // Header
      start = mail.length();
      mail.append(context.getString(R.string.mail_sender_problems_review));
      end = mail.length();
      mail.setSpan(new ForegroundColorSpan(0xffc54d39), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      mail.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      mail.append('\n');

      start = mail.length();
      for (Task problemTask : problematicTasks) {
        mail.append("   - ");
        mail.append(problemTask.getName());

        String problem = problemTask.getProblem();
        if (!TextUtils.isEmpty(problem)) {
          int start1 = mail.length();
          mail.append("\n      \u21B3 ");
          mail.append(problem);
          int end1 = mail.length();
          mail.setSpan(new StyleSpan(Typeface.BOLD), start1, end1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        mail.append('\n');
      }
      end = mail.length();
      mail.setSpan(new ForegroundColorSpan(0xffc54d39), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      mail.append('\n');
    }

    // Checklists
    for (Checklist checklist : selectedChecklistGroup.getChecklists()) {
      int start1 = mail.length();
      mail.append(" - ");
      mail.append(checklist.getName());
      end = mail.length();
      mail.setSpan(new ForegroundColorSpan(0xff536680), start1, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      if (!TextUtils.isEmpty(checklist.getComment())) {
        start = end;
        mail.append(" (");
        mail.append(checklist.getComment());
        mail.append(')');
        end = mail.length();
        mail.setSpan(new ForegroundColorSpan(0xff4a4a4a), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      }
      mail.setSpan(new StyleSpan(Typeface.BOLD), start1, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
      mail.append('\n');

      // Tasks
      for (Task task : checklist.getTasks()) {
        start = mail.length();
        mail.append("    [");
        end = mail.length();
        mail.setSpan(new ForegroundColorSpan(0xff4a4a4a), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        start = end;

        switch (task.getTaskState()) {
        case DONE:
          mail.append('\u2713');
          end = mail.length();
          mail.setSpan(new ForegroundColorSpan(0xff55955e), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          break;
        case SKIPPED:
          mail.append('>');
          end = mail.length();
          mail.setSpan(new ForegroundColorSpan(0xffd4772b), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          break;
        case PROBLEM:
          mail.append(" ! ");
          end = mail.length();
          mail.setSpan(new ForegroundColorSpan(0xffc54d39), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          break;
        default:
          mail.append(' ');
          end = mail.length();
          break;
        }
        start = end;
        mail.append("] ");
        end = mail.length();
        mail.setSpan(new ForegroundColorSpan(0xff4a4a4a), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        mail.append(task.getName());
        start = mail.length();
        if (!TextUtils.isEmpty(task.getResult())) {
          mail.append(" - ");
          mail.append(task.getResult());
        }
        if (showNotes && !TextUtils.isEmpty(task.getComment())) {
          mail.append(" (");
          mail.append(task.getComment());
          mail.append(')');
        }
        end = mail.length();
        mail.setSpan(new ForegroundColorSpan(0xff4a4a4a), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        String problem = task.getProblem();
        if (!TextUtils.isEmpty(problem)) {
          start = mail.length();
          mail.append("\n      \u21B3 ");
          mail.append(problem);
          end = mail.length();
          mail.setSpan(new ForegroundColorSpan(0xffc54d39), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          mail.setSpan(new StyleSpan(Typeface.BOLD), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        mail.append("\n");
      }
      mail.append('\n');
    }

    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    emailIntent.setType("plain/text");
    if (!TextUtils.isEmpty(recipients)) {
      emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients.split(","));
    }
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getSubject());
    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, mail);
    context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
  }

  private void sendXMLChecklistGroup() {
    // XML Serialization
    ChecklistSerializer serializer = new ChecklistSerializer();

    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    emailIntent.setType("plain/text");
    if (!TextUtils.isEmpty(recipients)) {
      emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipients.split(","));
    }
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, getSubject());
    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, serializer.writeStringXml(selectedChecklistGroup));
    context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
  }

  private String getSubject() {
    String subject = null;
    if (subjectPref.equals(SUBJECT_FORMAT_NAME)) {
      subject = selectedChecklistGroup.getName();
    } else if (subjectPref.equals(SUBJECT_FORMAT_COMMENT)) {
      subject = selectedChecklistGroup.getComment();
    } else if (subjectPref.equals(SUBJECT_FORMAT_NAME_COMMENT)) {
      subject = selectedChecklistGroup.getName() + " (" + selectedChecklistGroup.getComment() + ")";
    } else if (subjectPref.equals(SUBJECT_FORMAT_BLANK)) {
      subject = "";
    }
    return subject;
  }

  public void sendChecklistGroup(final List<ChecklistGroup> checklistGroups) {
    // Load preferences
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    recipients = prefs.getString(context.getString(R.string.pref_email_default_recipients), null);
    subjectPref = prefs.getString(context.getString(R.string.pref_email_default_subject), SUBJECT_FORMAT_NAME);
    showNotes = prefs.getBoolean(context.getString(R.string.pref_email_include_task_notes), true);

    // Creating the list of choices
    int groupCount = checklistGroups.size();
    CharSequence[] groups = new CharSequence[groupCount];
    Iterator<ChecklistGroup> iterator = checklistGroups.iterator();
    int i = 0;
    while (iterator.hasNext()) {
      ChecklistGroup checklistgroup = iterator.next();
      StringBuilder sb = new StringBuilder();
      sb.append(checklistgroup.getName());
      String comment = checklistgroup.getComment();
      if (!TextUtils.isEmpty(comment)) {
        sb.append(" (");
        sb.append(comment);
        sb.append(')');
      }
      groups[i] = sb.toString();
      i++;
    }

    // Display the dialog window for selecting the group to export
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(R.string.dialog_pick_checklist_group_send);
    builder.setItems(groups, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int item) {
        // Retrieve the selected checklist group
        selectedChecklistGroup = checklistGroups.get(item);

        chooseSendMethod();
      }
    });
    builder.create().show();
  }

  private void chooseSendMethod() {
    // Display the dialog window for selecting the email sending option
    AlertDialog.Builder builder = new AlertDialog.Builder(context);
    builder.setTitle(R.string.send_menu);
    builder.setItems(R.array.email_options, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int item) {
        // Send the email using the chosen method
        switch (item) {
        case 0:
          sendTextChecklistGroup();
          break;
        case 1:
          sendXMLChecklistGroup();
          break;
        }
      }
    });
    builder.create().show();
  }

  public static void sendMailToDev(Context context) {
    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
    emailIntent.setType("plain/text");
    String[] recipient = { "team@yeno.eu" };
    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, recipient);
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Support - " + context.getString(context.getApplicationInfo().labelRes));
    context.startActivity(Intent.createChooser(emailIntent, "Send email..."));
  }

}
