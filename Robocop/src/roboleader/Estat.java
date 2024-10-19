package roboleader;

import java.awt.Graphics2D;
import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;
import robocode.MessageEvent;

public interface Estat {
    void execute();
    void onScannedRobot(ScannedRobotEvent e);
    void onHitRobot(HitRobotEvent e);
    void onMessageReceived(MessageEvent e);
    public void onPaint(Graphics2D g);
}
