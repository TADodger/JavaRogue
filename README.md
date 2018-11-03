# JavaRogue

The note file is from Hexatron (http://www.hexatron.com), the person who translated the original Rogue code from C to Java.

I've updated the code to run as a Java application instead of an Applet, to output a Score file (and display this at the end of the game), and to use Serialization allow saving / loading games.

The code compiles and runs (I wouldn't release it if it didn't), but has quite a bit of ugliness.  The original code is *VERY* C accented Java.  The entire code base should ideally be refactored at some point.  This would be a big job.

There's some ugliness to what I've added as well.  The code to setup the AWT is pretty hairy - in Rogue.java lines 236 to 278 and could be cleaned up quite a bit.  I had issues with the explored maze being marked as unseen when a game is loaded which was an issue that I fixed with the very hacky solution in the init_seen() method in man (lines 1625-1633).  The identified items was an issue with Id.java (they were stored as static variables, which aren't saved / loaded with serialization - I added a synchronization approach that saves these values when serializaion is occuring, then re-assigns them during deserialization).  This seems OK to me, but a better approach might be to create a non-static singleton to represent these values.