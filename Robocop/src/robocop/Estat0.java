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
        robot.setTurnRadarRight(10);  // Gira el radar continuamente
        if (robot.enemicDetectat) {
            robot.changeEstat(new Estat1(robot));
        }
        robot.execute();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // Captura las coordenadas del enemigo y calcula la esquina más lejana
        double angle = robot.getHeading() + e.getBearing();
        robot.eX = robot.getX() + Math.sin(Math.toRadians(angle)) * e.getDistance();
        robot.eY = robot.getY() + Math.cos(Math.toRadians(angle)) * e.getDistance();

        double dist0 = robot.calcularDistancia(50, 50, robot.eX, robot.eY);
        double dist1 = robot.calcularDistancia(robot.battlefieldWidth - 50, 50, robot.eX, robot.eY);
        double dist2 = robot.calcularDistancia(50, robot.battlefieldHeight - 50, robot.eX, robot.eY);
        double dist3 = robot.calcularDistancia(robot.battlefieldWidth - 50, robot.battlefieldHeight - 50, robot.eX, robot.eY);

        if (dist0 > dist1 && dist0 > dist2 && dist0 > dist3) {
            robot.targetX = 50;
            robot.targetY = 50;
        } else if (dist1 > dist2 && dist1 > dist3) {
            robot.targetX = robot.battlefieldWidth - 50;
            robot.targetY = 50;
        } else if (dist2 > dist3) {
            robot.targetX = 50;
            robot.targetY = robot.battlefieldHeight - 50;
        } else {
            robot.targetX = robot.battlefieldWidth - 50;
            robot.targetY = robot.battlefieldHeight - 50;
        }

        robot.enemicDetectat = true;
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        // No hace nada en Fase 0
    }
}
