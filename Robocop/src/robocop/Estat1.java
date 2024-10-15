package robocop;

import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;

public class Estat1 implements Estat {
    private Robocop robot;

    public Estat1(Robocop robot) {
        this.robot = robot;
    }

    @Override
    public void execute() {
        robot.dirigirACantonada(robot.targetX, robot.targetY);
        robot.setTurnRadarRight(robot.normAngle(robot.getHeading() - robot.getRadarHeading()));

        if (robot.hasArrived(robot.targetX, robot.targetY)) {
            robot.enemicDetectat = false;
            robot.changeEstat(new Estat2(robot));
        }
        robot.execute();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        if (Math.abs(e.getBearing()) < 10) {  
            robot.esquivarObstaculo(e.getBearing());  
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        robot.esquivarObstaculo(e.getBearing());
    }
}
