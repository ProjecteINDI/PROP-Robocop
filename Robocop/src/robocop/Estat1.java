package robocop;

import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;

public class Estat1 implements Estat {
    private Robocop robot;

    public Estat1(Robocop robot) {
        this.robot = robot;
    }

    public boolean esquivar = false;
    public boolean esquivarCompletado = false;
    public double dis;
    public double distanciaTangente;
    public double esquivarTargetX, esquivarTargetY;
    public boolean chocar = false;
    public double chocaDir;
    public double distanciaInicialChoca; // Distancia inicial cuando choca

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
            // Si el enemigo está a la derecha (bearing positivo), giramos hacia la izquierda
            if (chocaDir > 0) {
                robot.setTurnLeft(20);  // Giramos 90 grados hacia la izquierda
            } else {
                robot.setTurnRight(20);  // Giramos 90 grados hacia la derecha
            }

            // Avanzar o retroceder para alejarnos del enemigo
            robot.setAhead(80);  // Avanza 100 unidades para alejarse

            // Verificamos si hemos avanzado lo suficiente para alejarnos del robot con el que chocamos
            double distanciaActual = robot.calcularDistancia(robot.getX(), robot.getY(), robot.getX() + Math.cos(Math.toRadians(robot.getHeading())), robot.getY() + Math.sin(Math.toRadians(robot.getHeading())));

            // Si ya nos hemos alejado una cantidad suficiente, desactivamos la bandera "chocar"
            if (distanciaActual - distanciaInicialChoca > 20) {  // Suponemos que nos alejamos al menos 50 unidades
                chocar = false;  // Desactivamos la bandera de colisión
                robot.dirigirACantonada(robot.targetX, robot.targetY);  // Volvemos a la esquina
            }
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

            // Verificar si las coordenadas están fuera del campo de batalla
            if (esquivarTargetX < 0 || esquivarTargetX > robot.battlefieldWidth || esquivarTargetY < 0 || esquivarTargetY > robot.battlefieldHeight) {
                // Si las coordenadas de la derecha están fuera, calcular el esquive hacia la izquierda
                double angleIzquierda = robot.getHeading() - 40;
                esquivarTargetX = robot.getX() + Math.sin(Math.toRadians(angleIzquierda)) * distanciaTangente;
                esquivarTargetY = robot.getY() + Math.cos(Math.toRadians(angleIzquierda)) * distanciaTangente;

                // Girar hacia la izquierda
                robot.setTurnLeft(40);
            } else {
                // Girar hacia la derecha
                robot.setTurnRight(40);
            }

            // Avanzar hacia el objetivo calculado
            robot.setAhead(distanciaTangente);
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        chocar = true;
        chocaDir = e.getBearing();
        distanciaInicialChoca = 10;  // Distancia inicial cuando choca
    }
}
