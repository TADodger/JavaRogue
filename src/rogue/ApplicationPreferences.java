/**
 * The MIT License (MIT)
 * Copyright (c) 2018 Mark Bernard
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy of 
 * this software and associated documentation files (the "Software"), to deal in the 
 * Software without restriction, including without limitation the rights to use, copy, 
 * modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, 
 * and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or 
 * substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, 
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE 
 * FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR 
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package rogue;

import java.awt.Point;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

/**
 * @author Mark Bernard
 *
 */
public class ApplicationPreferences {
    private static final String WINDOW_X = "window_x";
    private static final String WINDOW_Y = "window_y";
    private static final String WINDOW_MAXIMIZED = "window_maximized";
    private static final String POINT_SIZE = "point_size";
    
    /**
     * Load saved state for user convenience
     * 
     * @param rogue
     */
    public static void loadPrefs(Rogue rogue) {
        Preferences prefs = Preferences.userNodeForPackage(ApplicationPreferences.class);
        
        int posX = prefs.getInt(WINDOW_X, 0);
        int posY = prefs.getInt(WINDOW_Y, 0);
        rogue.parentFrame.setLocation(posX, posY);
        if (prefs.getBoolean(WINDOW_MAXIMIZED, false)) {
            rogue.parentFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        }
        while (rogue.view_list.size() < 1) {
            try { Thread.sleep(50); } catch (InterruptedException e) {}
        }
        rogue.pointsize = prefs.getInt(POINT_SIZE, 24);
    }
    
    /**
     * Save state for user convenience
     * 
     * @param rogue
     */
    public static void savePrefs(Rogue rogue) {
        Preferences prefs = Preferences.userNodeForPackage(ApplicationPreferences.class);
        
        Point point = rogue.parentFrame.getLocation();
        prefs.putInt(WINDOW_X, point.x);
        prefs.putInt(WINDOW_Y, point.y);
        prefs.putBoolean(WINDOW_MAXIMIZED, rogue.parentFrame.getExtendedState() == JFrame.MAXIMIZED_BOTH);
        prefs.putInt(POINT_SIZE, rogue.view_list.get(0).pointsize);
    }
    
    private ApplicationPreferences(){}
}
