package roboleader;

import java.awt.Graphics2D;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;
import robocode.MessageEvent;
import robocode.TeamRobot;

public class Roboleader extends TeamRobot {
    private Estat estatActual; // Estado actual del robot
    public double battlefieldWidth;
    public double battlefieldHeight;
    public boolean lider = false;
    public double targetX, targetY; // Coordenadas objetivo

    // Mapa para almacenar posiciones de los robots
    private Map<String, Double[]> posiciones;

    public Roboleader() {
        posiciones = new HashMap<>();
    }

    @Override
    public void run() {
        battlefieldWidth = getBattleFieldWidth();
        battlefieldHeight = getBattleFieldHeight();
        estatActual = new Estat0(this); // Inicializa el estado
        while (true) {
            estatActual.execute(); // Ejecuta la lógica del estado
            enviarPosicion();      // Envía la posición a otros robots
            execute();             // Llama al método execute de TeamRobot
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        estatActual.onScannedRobot(e); // Redirige el evento al estado
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        estatActual.onHitRobot(e); // Redirige el evento al estado
    }

    @Override
    public void onMessageReceived(MessageEvent e) {
        estatActual.onMessageReceived(e); // Redirige el evento al estado
        
        // Manejar actualización de posición
        if (e.getMessage() instanceof String && ((String) e.getMessage()).startsWith("POS:")) {
            String[] parts = ((String) e.getMessage()).split(":");
            String robotName = parts[1];
            double posX = Double.parseDouble(parts[2]);
            double posY = Double.parseDouble(parts[3]);
            actualizarPosicion(robotName, posX, posY);
        }
    }

    // Método para calcular distancias
    public double calcularDistancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    // Método para dirigir al robot a una posición específica
    public void dirigirACantonada(double targetX, double targetY) {
        double currentX = getX();
        double currentY = getY();
        double angleCapACantonada = Math.toDegrees(Math.atan2(targetX - currentX, targetY - currentY)) - getHeading();
        setTurnRight(normAngle(angleCapACantonada));
        setAhead(Math.hypot(targetX - currentX, targetY - currentY) - 25);  
    }

    public double normAngle(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    public void canviEstat(Estat nouEstat) {
        this.estatActual = nouEstat;
    }

    @Override
    public void onPaint(Graphics2D g) {
        estatActual.onPaint(g); // Llama al método onPaint del estado
    }

    // Métodos para gestionar posiciones de otros robots
    public void actualizarPosicion(String robotName, double x, double y) {
        posiciones.put(robotName, new Double[]{x, y});
    }

    public Double[] getPosicion(String robotName) {
        return posiciones.get(robotName);
    }

    // Enviar la posición actual del robot al resto del equipo
    public void enviarPosicion() {
        try {
            String mensaje = "POS:" + getName() + ":" + getX() + ":" + getY();
            broadcastMessage(mensaje);
        } catch (IOException e) {
            Logger.getLogger(Roboleader.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    // Método para verificar si el robot ha alcanzado la posición objetivo
    public boolean cantonada(double targetX, double targetY) {
        double margin = 25; // Margen de error para considerar que ha llegado a la posición
        double currentX = getX();
        double currentY = getY();
        return Math.abs(currentX - targetX) <= margin && Math.abs(currentY - targetY) <= margin;
    }
}
