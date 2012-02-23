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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.widget.Button;

import com.yeno.checklist.R;

/**
 * @author Yeno
 */
public class LicenseAgreementActivity extends Activity implements OnClickListener {

  private Button agreeButton;
  private WebView licenseView;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // Sets the activity's layout and user interface elements
    setContentView(R.layout.license_agreement);

    // Gets GUI widgets
    agreeButton = (Button) findViewById(R.id.done_button);
    agreeButton.setOnClickListener(this);

    licenseView = (WebView) findViewById(R.id.license);
    licenseView.setHorizontalScrollBarEnabled(false);
    licenseView.getSettings().setJavaScriptEnabled(false);
    licenseView.getSettings().setLoadsImagesAutomatically(false);

    StringBuilder webcontent = new StringBuilder();
    try {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.eula)));
      String line;
      while ((line = bufferedReader.readLine()) != null) {
        webcontent.append(line);
      }
      bufferedReader.close();
    } catch (IOException e) {
      e.printStackTrace();
      webcontent = new StringBuilder("Failed loading E.U.L.A.");
    }

    licenseView.loadData(webcontent.toString(), "text/html", "utf-8");
  }

  @Override
  public void onClick(View view) {
    if (view.getId() == R.id.done_button) {
      setResult(Activity.RESULT_OK);
      finish();
    }
  }
}
