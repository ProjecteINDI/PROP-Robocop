package roboleader;

import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;
import robocode.MessageEvent;
import robocode.util.Utils;
import java.util.List;

public class Estat1 implements Estat {
    private Roboleader robot;
    private List<String> jerarquia; // Lista de la jerarquía de robots
    private int miPosicion;         // Posición del robot en la jerarquía

    // Coordenadas de las esquinas del rectángulo
    private double[][] esquinas;
    private int esquinaActual = 0; // Índice de la esquina hacia la cual se dirige el robot

    // Variables para mantener distancia prudencial
    private final double distanciaMinima = 100; // Distancia mínima de seguridad
    private final double distanciaMaxima = 200; // Distancia máxima antes de acelerar

    public Estat1(Roboleader robot, List<String> jerarquia) {
        this.robot = robot;
        this.jerarquia = jerarquia;
        this.miPosicion = jerarquia.indexOf(robot.getName()); // Obtener la posición del robot en la jerarquía

        double marginX = robot.battlefieldWidth * 0.1;
        double marginY = robot.battlefieldHeight * 0.1;

        // Definir las 4 esquinas del rectángulo (sentido horario)
        esquinas = new double[][]{
            {marginX, marginY},                                        // Esquina inferior izquierda
            {robot.battlefieldWidth - marginX, marginY},                // Esquina inferior derecha
            {robot.battlefieldWidth - marginX, robot.battlefieldHeight - marginY}, // Esquina superior derecha
            {marginX, robot.battlefieldHeight - marginY}                // Esquina superior izquierda
        };
    }

    @Override
    public void execute() {
        // Moverse hacia el objetivo dependiendo de si es el líder o sigue a otro robot
        if (miPosicion == 0) {
            // Si es el líder, continúa moviéndose a las esquinas
            continuarTrayectoria();
        } else {
            // Seguir al robot anterior manteniendo distancia prudencial
            seguirAntecesor();
        }

        robot.execute();
    }

    // Método para seguir al robot inmediatamente superior en la jerarquía
    private void seguirAntecesor() {
        String antecesor = jerarquia.get(miPosicion - 1); // Obtener el robot anterior
        Double[] antecesorPos = robot.getPosicion(antecesor); // Obtener la posición del antecesor

        if (antecesorPos != null) {
            double antecesorX = antecesorPos[0];
            double antecesorY = antecesorPos[1];

            double distancia = robot.calcularDistancia(antecesorX, antecesorY, robot.getX(), robot.getY());

            if (distancia > distanciaMaxima) {
                // Si está demasiado lejos, acelerar para alcanzarlo
                double angle = Math.atan2(antecesorX - robot.getX(), antecesorY - robot.getY());
                robot.setTurnRightRadians(Utils.normalRelativeAngle(angle - robot.getHeadingRadians()));
                robot.setAhead(distancia - distanciaMinima);
                robot.setDebugProperty("Estado", "Acelerando para alcanzar al antecesor");
            } else if (distancia < distanciaMinima) {
                // Si está demasiado cerca, reducir la velocidad o moverse lateralmente
                robot.setAhead(-distanciaMinima); // Retroceder ligeramente
                robot.setDebugProperty("Estado", "Demasiado cerca, retrocediendo");
                
                // Ajustar el ángulo para no colisionar
                robot.setTurnRight(30); // Pequeña corrección lateral
            } else {
                // Seguir al antecesor manteniendo la distancia
                double angle = Math.atan2(antecesorX - robot.getX(), antecesorY - robot.getY());
                robot.setTurnRightRadians(Utils.normalRelativeAngle(angle - robot.getHeadingRadians()));
                robot.setAhead(distancia - distanciaMinima);
                robot.setDebugProperty("Estado", "Siguiendo al antecesor");
            }
        } else {
            // Si no se tiene la posición del antecesor, escanear en busca del antecesor
            robot.setTurnRadarRight(360); // Girar el radar para buscar al antecesor
            robot.setDebugProperty("Estado", "Buscando al antecesor");
        }
    }

    private void continuarTrayectoria() {
        robot.setDebugProperty("Líder", "Moviéndose a la esquina " + esquinaActual);
        // Moverse hacia la esquina actual
        double targetX = esquinas[esquinaActual][0];
        double targetY = esquinas[esquinaActual][1];

        double angle = Math.atan2(targetX - robot.getX(), targetY - robot.getY());
        robot.setTurnRightRadians(Utils.normalRelativeAngle(angle - robot.getHeadingRadians()));
        robot.setAhead(robot.calcularDistancia(targetX, targetY, robot.getX(), robot.getY()));

        // Comprobar si ha llegado a la esquina actual
        if (robot.calcularDistancia(robot.getX(), robot.getY(), targetX, targetY) < 25) {
            robot.setDebugProperty("Líder", "Esquina alcanzada: " + esquinaActual);
            esquinaActual = (esquinaActual + 1) % 4; // Cambia a la siguiente esquina
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // Ignorar robots del mismo equipo
        if (robot.isTeammate(e.getName())) {
            robot.setDebugProperty("Estado", "Compañero detectado, no esquivar");
            return;
        }
        
        // Si se detecta un enemigo en la trayectoria, realizar un movimiento evasivo
        double distance = e.getDistance();
        if (distance < distanciaMinima) {
            double angleToEnemy = e.getBearing();
            robot.setTurnRight(angleToEnemy + 90); // Girar 90 grados para esquivar
            robot.setAhead(100); // Avanzar 100 unidades en la nueva dirección
            robot.setDebugProperty("Estado", "Esquivando enemigo detectado");
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        // Si choca con otro robot, esquivar solo si es enemigo
        if (!robot.isTeammate(e.getName())) {
            robot.setDebugProperty("Estado", "Chocó con un enemigo, esquivando");

            // Realizar un movimiento evasivo al chocar con un enemigo
            double angle = e.getBearing();
            robot.setTurnRight(angle + 90); // Girar 90 grados para esquivar
            robot.setAhead(100); // Avanzar 100 unidades en la nueva dirección
        } else {
            robot.setDebugProperty("Estado", "Chocó con un compañero, ignorando");
        }
    }

    @Override
    public void onMessageReceived(MessageEvent e) {
        // Manejar mensajes de otros robots
    }

    @Override
    public void onPaint(java.awt.Graphics2D g) {
        // Pintar el rectángulo del camino del líder
        g.setColor(java.awt.Color.YELLOW);
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            g.drawLine((int)esquinas[i][0], (int)esquinas[i][1], (int)esquinas[next][0], (int)esquinas[next][1]);
        }
    }
}
