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

import android.os.Build;
import android.widget.ListView;

/**
 * @author Yeno
 */
public abstract class SmoothScroll {

  public static SmoothScroll getInstance() {
    if (Build.VERSION.SDK_INT < 8) {
      return PreFroyo.Holder.instance;
    }
    return FroyoAndBeyond.Holder.instance;
  }

  public abstract void smoothScrollToPosition(ListView listView, int position);

  public abstract void smoothScrollBy(ListView listView, int distance, int duration);

  private static class PreFroyo extends SmoothScroll {

    private static class Holder {
      private static final PreFroyo instance = new PreFroyo();
    }

    @Override
    public void smoothScrollBy(ListView listView, int distance, int duration) {
      // Ne fait rien
    }

    @Override
    public void smoothScrollToPosition(ListView listView, int position) {
      // Ne fait rien
    }
  }

  private static class FroyoAndBeyond extends SmoothScroll {

    private static class Holder {
      private static final FroyoAndBeyond instance = new FroyoAndBeyond();
    }

    @Override
    public void smoothScrollBy(ListView listView, int distance, int duration) {
      listView.smoothScrollBy(distance, duration);
    }

    @Override
    public void smoothScrollToPosition(ListView listView, int position) {
      listView.smoothScrollToPosition(position);
    }
  }

}
