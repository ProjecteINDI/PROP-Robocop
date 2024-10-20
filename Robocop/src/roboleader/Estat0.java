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
    private Roboleader robot;
    private boolean isLeaderElected = false;

    private int randomNumber;
    private String leaderName = null;
    private Map<String, Integer> randomNum;
    private Map<String, Double> distancias;
    private int size = 5; // Tamaño del equipo
    private double nummax = 0;
    private double leaderX, leaderY;
    private boolean hallegado = false;
    
    // Variables para los robots en orden
    private List<String> jerarquia;

    public Estat0(Roboleader robot) {
        this.robot = robot;
        this.randomNumber = new Random().nextInt(100);
        this.randomNum = new HashMap<>();
        this.distancias = new HashMap<>();
        this.jerarquia = new ArrayList<>();
        randomNum.put(robot.getName(), randomNumber);
    }

    @Override
    public void execute() {
        robot.setDebugProperty("ESTAT", String.valueOf(robot.lider));

        // Enviar el número aleatorio si aún no se ha elegido líder
        if (!isLeaderElected) {
            try {
                robot.broadcastMessage(randomNumber);
            } catch (IOException ex) {
                Logger.getLogger(Estat0.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Elegir al líder
        if (randomNum.size() == size && !isLeaderElected) {
            nummax = -1;
            for (Map.Entry<String, Integer> entry : randomNum.entrySet()) {
                if (entry.getValue() > nummax) {
                    nummax = entry.getValue();
                    leaderName = entry.getKey();
                }
            }
            robot.lider = leaderName.equals(robot.getName());
            isLeaderElected = true;
            robot.setDebugProperty("leader", String.valueOf(leaderName));

            // Difundir quién es el líder
            try {
                robot.broadcastMessage("LEADER:" + leaderName);
            } catch (IOException ex) {
                Logger.getLogger(Estat0.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Si este robot es el líder, difunde su posición
            if (robot.lider) {
                leaderX = robot.getX();
                leaderY = robot.getY();
                try {
                    robot.broadcastMessage("X:" + leaderX);
                    robot.broadcastMessage("Y:" + leaderY);
                } catch (IOException ex) {
                    Logger.getLogger(Estat0.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        // Calcular distancias hacia el líder si este ha sido elegido
        if (isLeaderElected && hallegado) {
            double distancia = robot.calcularDistancia(robot.getX(), robot.getY(), leaderX, leaderY);
            try {
                robot.broadcastMessage("Distancia:" + distancia);
            } catch (IOException ex) {
                Logger.getLogger(Estat0.class.getName()).log(Level.SEVERE, null, ex);
            }
            distancias.put(robot.getName(), distancia);
        }

        // Ordenar los robots una vez recibidas todas las distancias
        if (distancias.size() == (size - 1)) {
            List<Map.Entry<String, Double>> distanciaList = new ArrayList<>(distancias.entrySet());

            // Ordenar por distancia (burbuja mejorable, pero funcional aquí)
            distanciaList.sort(Map.Entry.comparingByValue());

            // Añadir los robots en orden jerárquico
            jerarquia.add(leaderName);  // Añadir el líder primero
            for (Map.Entry<String, Double> entry : distanciaList) {
                jerarquia.add(entry.getKey());  // Añadir el resto en orden de distancia
            }

            // Mostrar jerarquía en debug
            robot.setDebugProperty("Nombres ordenados por distancia", String.join(", ", jerarquia));

            // Cambiar al Estado 1 y pasar la jerarquía
            robot.canviEstat(new Estat1(robot, jerarquia));
        }
        robot.execute();
    }

    @Override
    public void onMessageReceived(MessageEvent e) {
        if (e.getMessage() instanceof Integer) {
            randomNum.put(e.getSender(), (Integer) e.getMessage());
        }

        if (e.getMessage() instanceof String) {
            String mensaje = (String) e.getMessage();

            // Procesar el mensaje del líder
            if (mensaje.startsWith("LEADER:")) {
                leaderName = mensaje.split(":")[1];
                robot.lider = leaderName.equals(robot.getName());
                isLeaderElected = true;
                robot.setDebugProperty("leader", leaderName);
            }

            // Recibir coordenadas del líder
            if (mensaje.startsWith("X:")) {
                leaderX = Double.parseDouble(mensaje.split(":")[1]);
            }
            if (mensaje.startsWith("Y:")) {
                leaderY = Double.parseDouble(mensaje.split(":")[1]);
                hallegado = true;
            }
            if (mensaje.startsWith("Distancia:")) {
                double dis = Double.parseDouble(mensaje.split(":")[1]);
                distancias.put(e.getSender(), dis);
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

    // Método de pintura para resaltar al líder
    @Override
    public void onPaint(java.awt.Graphics2D g) {
        if (robot.lider) {
            g.setColor(Color.YELLOW);
            int diameter = 100;
            int x = (int) (leaderX - diameter / 2);
            int y = (int) (leaderY - diameter / 2);
            g.drawOval(x, y, diameter, diameter);
        }
    }
}
