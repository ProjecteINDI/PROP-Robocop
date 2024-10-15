package robocop;

import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;

public class Estat1 implements Estat {
    private Robocop robot;

    public Estat1(Robocop robot) {
        this.robot = robot;
    }

    // Variables de esquivar
    public boolean esquivar = false;
    public boolean esquivarCompletado = false;  // Bandera para evitar múltiples esquives
    public double dis;  // Distancia al enemigo detectado
    public double distanciaTangente;  // Distancia calculada para esquivar
    public double esquivarTargetX, esquivarTargetY;  // Coordenadas objetivo después de esquivar

    @Override
    public void execute() {
        robot.enemicDetectat = false;

        // Depuración: Mostrar si estamos esquivando o no
        if (esquivar) {
            robot.setDebugProperty("Estado", "Esquivando");
        } else {
            robot.setDebugProperty("Estado", "Moviéndose hacia la esquina");
        }

        // Si estamos esquivando
        if (esquivar) {
            // Depuración: Mostramos las coordenadas objetivo del esquive
            robot.setDebugProperty("EsquivarTargetX", String.valueOf(esquivarTargetX));
            robot.setDebugProperty("EsquivarTargetY", String.valueOf(esquivarTargetY));

            // Recalcular la dirección en cada ciclo hacia las coordenadas del esquive
            double angleCapACoordenadaEsquive = Math.toDegrees(Math.atan2(esquivarTargetX - robot.getX(), esquivarTargetY - robot.getY())) - robot.getHeading();
            robot.setTurnRight(robot.normAngle(angleCapACoordenadaEsquive));
            
            // Avanzar hacia el punto objetivo del esquive
            robot.setAhead(Math.hypot(esquivarTargetX - robot.getX(), esquivarTargetY - robot.getY()) - 10);  // Restar 10 para mayor precisión cerca del destino

            // Verificamos si el robot ha alcanzado las coordenadas calculadas para el esquive
            if (Math.abs(robot.getX() - esquivarTargetX) < 20 && Math.abs(robot.getY() - esquivarTargetY) < 20) {
                robot.setDebugProperty("EntraEsquivar", "Entra");

                esquivar = false;  // Terminamos el esquive
                esquivarCompletado = true;  // Marcamos el esquivar como completado
                robot.dirigirACantonada(robot.targetX, robot.targetY);  // Volvemos a la trayectoria hacia la esquina
            }
        } else {
            // Si no estamos esquivando, continuamos hacia la esquina
            robot.dirigirACantonada(robot.targetX, robot.targetY);
        }

        // Mantener el radar apuntando hacia el frente
        robot.setTurnRadarRight(robot.normAngle(robot.getHeading() - robot.getRadarHeading()));

        // Si hemos llegado a la esquina, cambiamos a Fase 2
        if (robot.hasArrived(robot.targetX, robot.targetY)) {
            robot.changeEstat(new Estat2(robot));
        }

        // Ejecutar las acciones del robot
        robot.execute();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // Solo esquivamos si el enemigo está a 200 unidades o menos, está de frente, y aún no hemos completado el esquive
        if (Math.abs(e.getBearing()) < 10 && e.getDistance() <= 200 && !esquivarCompletado) {
            // Depuración: Mostrar información cuando detectamos un robot
            robot.setDebugProperty("Robot Detectado", "Sí");
            robot.setDebugProperty("Distancia al Enemigo", String.valueOf(e.getDistance()));
            robot.setDebugProperty("Ángulo del Enemigo", String.valueOf(e.getBearing()));

            esquivar = true;  // Activamos el modo esquivar
            dis = e.getDistance();  // Guardamos la distancia del enemigo

            // Calculamos la distancia tangente usando el ángulo de 40 grados
            double angleEsquivar = 40;
            distanciaTangente = Math.tan(Math.toRadians(angleEsquivar)) * dis;

            // Calculamos las coordenadas donde el robot debería llegar después de esquivar
            double angle = robot.getHeading() + 40;  // El ángulo actual del robot más el giro
            esquivarTargetX = robot.getX() + Math.sin(Math.toRadians(angle)) * distanciaTangente;
            esquivarTargetY = robot.getY() + Math.cos(Math.toRadians(angle)) * distanciaTangente;

            // Depuración: Mostramos el cálculo de la tangente y las coordenadas objetivo
            robot.setDebugProperty("Ángulo Esquivar", String.valueOf(angleEsquivar));
            robot.setDebugProperty("Distancia Tangente Calculada", String.valueOf(distanciaTangente));
            robot.setDebugProperty("EsquivarTargetX", String.valueOf(esquivarTargetX));
            robot.setDebugProperty("EsquivarTargetY", String.valueOf(esquivarTargetY));
        } else {
            // Si no cumplimos las condiciones para esquivar, no hacemos nada
            robot.setDebugProperty("Acción", "No se esquiva, enemigo muy lejos o ya esquivando");
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        // Si colisionamos con otro robot, activamos el modo esquivar
        if (!esquivarCompletado) {
            esquivar = true;
            robot.setDebugProperty("Colisión", "Sí");
        }
    }
}
