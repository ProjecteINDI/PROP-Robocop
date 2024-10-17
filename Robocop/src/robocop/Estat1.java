package robocop;

import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;

public class Estat1 implements Estat {
    private Robocop robot;
    private final double MARGEN = 20;  // Margen de seguridad para evitar los límites del campo

    public Estat1(Robocop robot) {
        this.robot = robot;
    }

    public boolean esquivar = false;
    public boolean esquivarCompletado = false;
    public double dis;
    public double distanciaTangente;
    public double esquivarTargetX, esquivarTargetY;
    public boolean chocar = false;
    public double chocaDir = 0;  // Guardar la dirección del enemigo al chocar

    @Override
    public void execute() {
        robot.enemicDetectat = false;

        if (esquivar) {
            double angleCapACoordenadaEsquive = Math.toDegrees(Math.atan2(esquivarTargetX - robot.getX(), esquivarTargetY - robot.getY())) - robot.getHeading();
            robot.setTurnRight(robot.normAngle(angleCapACoordenadaEsquive));
            robot.setAhead(Math.hypot(esquivarTargetX - robot.getX(), esquivarTargetY - robot.getY()) - 10);

            if (Math.abs(robot.getX() - esquivarTargetX) < 20 && Math.abs(robot.getY() - esquivarTargetY) < 20) {
                esquivar = false;
                esquivarCompletado = true;
                robot.dirigirACantonada(robot.targetX, robot.targetY);
            }
        } else {
            robot.dirigirACantonada(robot.targetX, robot.targetY);
        }

        if (chocar) {
            // Decidir si girar a la izquierda o derecha basándonos en la dirección del enemigo
            if (chocaDir > 0) {
                // Si el enemigo está a la derecha, girar hacia la izquierda
                robot.setTurnLeft(90);
            } else {
                // Si el enemigo está a la izquierda, girar hacia la derecha
                robot.setTurnRight(90);
            }

            robot.setAhead(100);  // Avanza para alejarse de la colisión
            chocar = false;  // Resetear la bandera después de girar
        }

        robot.setTurnRadarRight(robot.normAngle(robot.getHeading() - robot.getRadarHeading()));

        if (robot.hasArrived(robot.targetX, robot.targetY)) {
            robot.changeEstat(new Estat2(robot));
        }

        robot.execute();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        if (Math.abs(e.getBearing()) < 10 && e.getDistance() <= 200 && !esquivarCompletado) {
            esquivar = true;
            dis = e.getDistance();

            double angleEsquivarDerecha = 40;
            distanciaTangente = Math.tan(Math.toRadians(angleEsquivarDerecha)) * dis;

            double angleDerecha = robot.getHeading() + 40;
            esquivarTargetX = robot.getX() + Math.sin(Math.toRadians(angleDerecha)) * distanciaTangente;
            esquivarTargetY = robot.getY() + Math.cos(Math.toRadians(angleDerecha)) * distanciaTangente;

            // Verificar si las coordenadas están fuera del campo de batalla (con margen de seguridad)
            if (esquivarTargetX < MARGEN || esquivarTargetX > robot.battlefieldWidth - MARGEN || 
                esquivarTargetY < MARGEN || esquivarTargetY > robot.battlefieldHeight - MARGEN) {
                // Si las coordenadas de la derecha están fuera, calcular el esquive hacia la izquierda
                robot.setDebugProperty("Esquivar", "Ir a la izquierda");
                double angleIzquierda = robot.getHeading() - 40;
                esquivarTargetX = robot.getX() + Math.sin(Math.toRadians(angleIzquierda)) * distanciaTangente;
                esquivarTargetY = robot.getY() + Math.cos(Math.toRadians(angleIzquierda)) * distanciaTangente;

                // Girar hacia la izquierda
                robot.setTurnLeft(40);
            } else {
                // Ir a la derecha
                robot.setDebugProperty("Esquivar", "Ir a la derecha");
                robot.setTurnRight(40);
            }

            // Avanzar hacia el objetivo calculado
            robot.setAhead(distanciaTangente);
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        // Almacenar la dirección del robot enemigo cuando chocamos
        chocar = true;
        chocaDir = e.getBearing();  // Guardar el bearing del robot con el que chocamos
    }
}
