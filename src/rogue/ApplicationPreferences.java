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
    private static final String WINDOW_WIDTH = "window_width";
    private static final String WINDOW_HEIGHT = "window_height";
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
        } else {
            //TODO still not working
            rogue.parentFrame.setSize(prefs.getInt(WINDOW_WIDTH, 1280),  prefs.getInt(WINDOW_HEIGHT, 1024));
        }
//        while (rogue.viewList.size() < 1) {
//            try { Thread.sleep(50); } catch (InterruptedException e) {}
//        }
        int pointsize = prefs.getInt(POINT_SIZE, 24);
        rogue.pointsize = pointsize;
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
        boolean maximized = rogue.parentFrame.getExtendedState() == JFrame.MAXIMIZED_BOTH;
        prefs.putBoolean(WINDOW_MAXIMIZED, maximized);
        if (!maximized) {
            //TODO still not working
            prefs.putInt(WINDOW_WIDTH, rogue.parentFrame.getWidth());
            prefs.putInt(WINDOW_HEIGHT, rogue.parentFrame.getHeight());
        }
        prefs.putInt(POINT_SIZE, rogue.viewList.get(0).pointsize);
    }
    
    private ApplicationPreferences(){}
}
