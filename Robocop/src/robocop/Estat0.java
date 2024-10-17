package robocop;

import robocode.HitRobotEvent;
import robocode.ScannedRobotEvent;

public class Estat0 implements Estat {
    private Robocop robot;

    public Estat0(Robocop robot) {
        this.robot = robot;
    }

    @Override
    public void execute() {
        robot.setTurnRadarRight(10);  // Girem el radar continuament
        if (robot.enemicDetectat) {
            robot.changeEstat(new Estat1(robot));
        }
        robot.execute();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        double angle = robot.getHeading() + e.getBearing();
        robot.eX = robot.getX() + Math.sin(Math.toRadians(angle)) * e.getDistance(); // Distancia del enemic en el eix X amb el nostrte robot
        robot.eY = robot.getY() + Math.cos(Math.toRadians(angle)) * e.getDistance(); // Distancia del enemic en el eix Y amb el nostrte robot
        
        //distancia a cada una de les cantonades
        double dist0 = robot.calcularDistancia(0, 0, robot.eX, robot.eY);
        double dist1 = robot.calcularDistancia(robot.battlefieldWidth, 0, robot.eX, robot.eY);
        double dist2 = robot.calcularDistancia(0, robot.battlefieldHeight , robot.eX, robot.eY);
        double dist3 = robot.calcularDistancia(robot.battlefieldWidth , robot.battlefieldHeight , robot.eX, robot.eY);

        if (dist0 > dist1 && dist0 > dist2 && dist0 > dist3) {
            robot.targetX = 20;
            robot.targetY = 20;
        } else if (dist1 > dist2 && dist1 > dist3) {
            robot.targetX = robot.battlefieldWidth - 20;
            robot.targetY = 20;
        } else if (dist2 > dist3) {
            robot.targetX = 20;
            robot.targetY = robot.battlefieldHeight - 20;
        } else {
            robot.targetX = robot.battlefieldWidth - 20;
            robot.targetY = robot.battlefieldHeight - 20;
        }

        robot.enemicDetectat = true;
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
    }
}
