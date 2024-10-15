package robocop;

import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;

public class Estat2 implements Estat {
    private Robocop robot;

    public Estat2(Robocop robot) {
        this.robot = robot;
    }
    @Override
    public void execute() {
        robot.setTurnRadarRight(10);  // Buscar enemigos
        robot.execute();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // Ajustar radar y cañón
        robot.angleRadar = robot.getHeading() + e.getBearing() - robot.getRadarHeading();
        robot.angleCanon = robot.getHeading() + e.getBearing() - robot.getGunHeading();
        robot.distancia = e.getDistance();

        robot.setTurnRadarRight(robot.normAngle(robot.angleRadar));
        robot.setTurnGunRight(robot.normAngle(robot.angleCanon));

        // Disparar
        double potenciaDispar = Math.max(1, Math.min(3, 500 / robot.distancia));
        robot.fire(potenciaDispar);
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
    }
}
