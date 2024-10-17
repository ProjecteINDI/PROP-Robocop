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
    public double chocaDir = 0;  // Guardar la dirección del enemigo al chocar

    @Override
    public void execute() {
        robot.enemicDetectat = false;

        if (esquivar) {
            double angleEsquiva = Math.toDegrees(Math.atan2(esquivarTargetX - robot.getX(), esquivarTargetY - robot.getY())) - robot.getHeading(); //angle a esquivar
            robot.setTurnRight(robot.normAngle(angleEsquiva));
            robot.setAhead(Math.hypot(esquivarTargetX - robot.getX(), esquivarTargetY - robot.getY()) - 10); //avancem la distancia a esquivar

            if (Math.abs(robot.getX() - esquivarTargetX) < 20 && Math.abs(robot.getY() - esquivarTargetY) < 20) {// en cas que hagi avançat tota la distancia, tornem al cami cap a la cantonada
                esquivar = false;
                esquivarCompletado = true;
                robot.dirigirACantonada(robot.targetX, robot.targetY);
            }
        } 
        else {
            robot.dirigirACantonada(robot.targetX, robot.targetY);
        }

        if (chocar) {
            if (chocaDir > 0) {
                // Si l'enemic es a la dreta girem a l'esquerra
                robot.setTurnLeft(90);
            } else {
                // Si l'enemic es a la esquerra girem a la dreta
                robot.setTurnRight(90);
            }

            robot.setAhead(100);
            chocar = false; 
        }

        robot.setTurnRadarRight(robot.normAngle(robot.getHeading() - robot.getRadarHeading()));

        if (robot.cantonada(robot.targetX, robot.targetY)) {
            robot.changeEstat(new Estat2(robot));
        }

        robot.execute();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        if (Math.abs(e.getBearing()) < 10 && e.getDistance() <= 200 && !esquivarCompletado) {//si tenim un enemic en el nostra cami i si estem a una distancia mes propera de 200 d'aquest
            esquivar = true;
            dis = e.getDistance();

            double angleEsquivarDerecha = 40;
            distanciaTangente = Math.tan(Math.toRadians(angleEsquivarDerecha)) * dis;

            double angleDerecha = robot.getHeading() + 40;
            esquivarTargetX = robot.getX() + Math.sin(Math.toRadians(angleDerecha)) * distanciaTangente; //distancia en el eix x que ens hem de moure per esquivar
            esquivarTargetY = robot.getY() + Math.cos(Math.toRadians(angleDerecha)) * distanciaTangente; //distancia en el eix y que ens hem de moure per esquivar

            // Verifiquem que les coordenades desti per a esquivar no estiguin fora del camp
            if (esquivarTargetX < 20 || esquivarTargetX > robot.battlefieldWidth - 20 || 
                esquivarTargetY < 20 || esquivarTargetY > robot.battlefieldHeight - 20) {
                // En cas que les coordenades de la dreta estiguin fora, esquivem cap a l'esquerra
                double angleIzquierda = robot.getHeading() - 40;
                esquivarTargetX = robot.getX() + Math.sin(Math.toRadians(angleIzquierda)) * distanciaTangente; //distancia en el eix x que ens hem de moure per esquivar
                esquivarTargetY = robot.getY() + Math.cos(Math.toRadians(angleIzquierda)) * distanciaTangente; //distancia en el eix y que ens hem de moure per esquivar

                robot.setTurnLeft(40);//fem el gir cap a l'esquerra
            } else {
                // En altre cas cap a la derecha
                robot.setTurnRight(40);
            }

            // Avencem cap el objectiu per a esquivar
            robot.setAhead(distanciaTangente);
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        chocar = true;
        chocaDir = e.getBearing();  // Guardem el bearing del robot amb el que choquem
    }
}
