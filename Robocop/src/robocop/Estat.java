package robocop;

import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;

public interface Estat {
    void execute();
    void onScannedRobot(ScannedRobotEvent e);
    void onHitRobot(HitRobotEvent e);
}
