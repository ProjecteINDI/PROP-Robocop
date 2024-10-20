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
    private boolean sentidoHorario = true; // Controla si el movimiento es en sentido horario o antihorario
    private long tiempoCambio;      // Controla el tiempo del último cambio de roles

    // Coordenadas de las esquinas del rectángulo
    private double[][] esquinas;
    private int esquinaActual = -1; // Índice de la esquina hacia la cual se dirige el robot (se inicializa en -1 para forzar la selección al inicio)
    private boolean esquinaInicialSeleccionada = false; // Bandera para asegurarse de que se seleccione la esquina al inicio

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

        tiempoCambio = System.currentTimeMillis(); // Inicializa el tiempo del primer cambio de roles
    }

    @Override
    public void execute() {
        // Verificar si han pasado 15 segundos para invertir roles y sentido
        if ((System.currentTimeMillis() - tiempoCambio) >= 15000) {
            invertirRolesYSentido(); // Invertir roles y sentido de rotación
            tiempoCambio = System.currentTimeMillis(); // Reiniciar el temporizador
        }

        if (miPosicion == 0) {
            if (!esquinaInicialSeleccionada) {
                seleccionarEsquinaMasCercana(); // Seleccionar la esquina más cercana al inicio
                esquinaInicialSeleccionada = true; // Asegurarse de no volver a seleccionar
            }
            continuarTrayectoria(); // Moverse por el rectángulo
        } else {
            seguirAntecesor(); // Los demás robots siguen al líder
        }

        robot.execute();
    }

    // Detecta si el robot ha llegado a la esquina actual
    private boolean haLlegadoAEsquina(double targetX, double targetY) {
        return robot.calcularDistancia(robot.getX(), robot.getY(), targetX, targetY) < 25;
    }

    // Selecciona la esquina más cercana al robot al inicio
    private void seleccionarEsquinaMasCercana() {
        double dist0 = robot.calcularDistancia(esquinas[0][0], esquinas[0][1], robot.getX(), robot.getY());
        double dist1 = robot.calcularDistancia(esquinas[1][0], esquinas[1][1], robot.getX(), robot.getY());
        double dist2 = robot.calcularDistancia(esquinas[2][0], esquinas[2][1], robot.getX(), robot.getY());
        double dist3 = robot.calcularDistancia(esquinas[3][0], esquinas[3][1], robot.getX(), robot.getY());

        if (dist0 < dist1 && dist0 < dist2 && dist0 < dist3) {
            esquinaActual = 0;
        } else if (dist1 < dist2 && dist1 < dist3) {
            esquinaActual = 1;
        } else if (dist2 < dist3) {
            esquinaActual = 2;
        } else {
            esquinaActual = 3;
        }
        robot.setDebugProperty("Esquina inicial", "Seleccionada esquina " + esquinaActual);
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
        if (haLlegadoAEsquina(targetX, targetY)) {
            robot.setDebugProperty("Líder", "Esquina alcanzada: " + esquinaActual);
            // Cambiar a la siguiente esquina dependiendo del sentido de rotación
            if (sentidoHorario) {
                esquinaActual = (esquinaActual - 1 + 4) % 4; // Sentido horario
            } else {
                esquinaActual = (esquinaActual + 1) % 4; // Sentido antihorario
            }
        }
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
            } else if (distancia < distanciaMinima) {
                // Si está demasiado cerca, retroceder ligeramente
                robot.setAhead(-distanciaMinima);
            } else {
                // Mantener la distancia con el antecesor
                double angle = Math.atan2(antecesorX - robot.getX(), antecesorY - robot.getY());
                robot.setTurnRightRadians(Utils.normalRelativeAngle(angle - robot.getHeadingRadians()));
                robot.setAhead(distancia - distanciaMinima);
            }
        } else {
            // Si no se tiene la posición del antecesor, escanear en busca del antecesor
            robot.setTurnRadarRight(360); // Girar el radar para buscar al antecesor
        }
    }

    // Método que invierte la jerarquía y el sentido de rotación
    private void invertirRolesYSentido() {
        // Invertir el sentido de rotación
        sentidoHorario = !sentidoHorario;

        // Invertir la jerarquía
        java.util.Collections.reverse(jerarquia);

        // Actualizar la posición del robot
        miPosicion = jerarquia.indexOf(robot.getName());

        // Reiniciar el seguimiento de esquinas
        esquinaInicialSeleccionada = false; // Forzar la selección de una nueva esquina
        robot.setDebugProperty("Cambio de roles", "Invertido sentido y roles. Nueva posición: " + miPosicion);
    }


    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // Ignorar robots del mismo equipo
        if (robot.isTeammate(e.getName())) {
            return;
        }
        
        // Si se detecta un enemigo en la trayectoria, realizar un movimiento evasivo
        double distance = e.getDistance();
        if (distance < distanciaMinima) {
            double angleToEnemy = e.getBearing();
            robot.setTurnRight(angleToEnemy + 90); // Girar 90 grados para esquivar
            robot.setAhead(100); // Avanzar 100 unidades en la nueva dirección
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        // Si choca con otro robot, esquivar solo si es enemigo
        if (!robot.isTeammate(e.getName())) {
            double angle = e.getBearing();
            robot.setTurnRight(angle + 90); // Girar 90 grados para esquivar
            robot.setAhead(100); // Avanzar 100 unidades en la nueva dirección
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
