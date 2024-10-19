package roboleader;

import java.awt.Color;
import java.io.IOException;
import robocode.MessageEvent;
import robocode.ScannedRobotEvent;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import robocode.HitRobotEvent;

public class Estat0 implements Estat {
    private Robocop robot;
    private boolean isLeaderElected = false;
    private boolean lider = false;
    private int randomNumber;
    private String leaderName = null;
    private Map<String, Integer> teamRandomNumbers;
    private Map<String, Double> distancesFromLeader; // Para almacenar las distancias
    private final int TEAM_SIZE = 5;
    private double leaderX, leaderY;

    public Estat0(Robocop robot) {
        this.robot = robot;
        this.randomNumber = new Random().nextInt(100);
        this.teamRandomNumbers = new HashMap<>();
        this.distancesFromLeader = new HashMap<>();
        teamRandomNumbers.put(robot.getName(), randomNumber);
    }

    @Override
    public void execute() {
        robot.setDebugProperty("ESTAT", String.valueOf(lider));

        if (!isLeaderElected) {
            try {
                robot.broadcastMessage(randomNumber);
            } catch (IOException ex) {
                Logger.getLogger(Estat0.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Si todos los robots han enviado sus números y no se ha elegido líder
        if (teamRandomNumbers.size() == TEAM_SIZE && !isLeaderElected) {
            leaderName = teamRandomNumbers.entrySet()
                          .stream()
                          .max(Map.Entry.comparingByValue())
                          .get()
                          .getKey();

            lider = leaderName.equals(robot.getName());
            isLeaderElected = true;
            robot.setDebugProperty("leader", String.valueOf(leaderName));

            if (lider) {
                leaderX = robot.getX();
                leaderY = robot.getY();
            }

            try {
                robot.broadcastMessage("LEADER:" + leaderName);
            } catch (IOException ex) {
                Logger.getLogger(Estat0.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Si ya hay un líder, calcular distancias
        if (isLeaderElected && !lider) {
            double distance = calcularDistancia(robot.getX(), robot.getY(), leaderX, leaderY);
            distancesFromLeader.put(robot.getName(), distance);
        }

        robot.execute();
    }

    // Método para calcular la distancia entre dos puntos
    public double calcularDistancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
    }

    @Override
    public void onMessageReceived(MessageEvent e) {
        if (e.getMessage() instanceof Integer) {
            teamRandomNumbers.put(e.getSender(), (Integer) e.getMessage());
        }

        if (e.getMessage() instanceof String && ((String) e.getMessage()).startsWith("LEADER:")) {
            leaderName = ((String) e.getMessage()).split(":")[1];
            lider = leaderName.equals(robot.getName());
            isLeaderElected = true;
            robot.setDebugProperty("leader", leaderName);
            if (lider) {
                leaderX = robot.getX();
                leaderY = robot.getY();
            }
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // No se necesita implementar nada aquí en esta fase
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        // No se necesita implementar nada aquí en esta fase
    }

    // Método de pintura
    public void onPaint(java.awt.Graphics2D g) {
        if (lider) {
            g.setColor(Color.YELLOW);
            g.drawString("Soy el líder", 10, 10);
        }
    }
}
