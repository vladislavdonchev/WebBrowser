package com.example.webbrowser.webbrowser;

import java.util.HashMap;

/**
 * Created by Asus on 6/21/2017.
 */

public interface BrowserPreferenceReadListener {
    void browserTabsLoaded(HashMap<String, String>[] tabs);
}
