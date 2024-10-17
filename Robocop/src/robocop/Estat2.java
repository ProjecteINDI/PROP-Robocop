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
        robot.setTurnRadarRight(10);  // Girem el radar en busca d'enemics 
        robot.execute();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        robot.angleRadar = robot.getHeading() + e.getBearing() - robot.getRadarHeading(); //Angle que ha de girar el radar per apuntar a l'enemic
        robot.angleCanon = robot.getHeading() + e.getBearing() - robot.getGunHeading(); //Angle que ha de girar el rcanó per apuntar a l'enemic
        robot.distancia = e.getDistance();

        robot.setTurnRadarRight(robot.normAngle(robot.angleRadar));//fixem el radar a l'enemic
        robot.setTurnGunRight(robot.normAngle(robot.angleCanon));// fixem el canó a l'enemic

        // Disparar
        double potenciaDispar =  Math.min(3, 500 / robot.distancia);
        robot.fire(potenciaDispar);
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
    }
}
