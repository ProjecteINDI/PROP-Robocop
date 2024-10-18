package roboleader;

import java.io.IOException;
import robocode.MessageEvent;
import robocode.ScannedRobotEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import robocode.HitRobotEvent;

public class Estat0 implements Estat {
    private Robocop robot;
    private boolean isLeaderElected = false;
    private boolean lider = false;
    private int randomNumber; // Número aleatorio de cada robot
    private String leaderName = null; // Nombre del líder
    private Map<String, Integer> teamRandomNumbers; // Almacenar números aleatorios de los compañeros
    private final int TEAM_SIZE = 5; // Tamaño del equipo

    public Estat0(Robocop robot) {
        this.robot = robot;
        this.randomNumber = new Random().nextInt(100); // Generar número aleatorio
        this.teamRandomNumbers = new HashMap<>();
        teamRandomNumbers.put(robot.getName(), randomNumber); // Guardar el número propio
    }

    @Override
    public void execute() {
        robot.setDebugProperty("ESTAT", String.valueOf(lider));
        
        // Si aún no se ha elegido un líder
        if (!isLeaderElected) {
            try {
                robot.broadcastMessage(randomNumber); // Enviar número aleatorio
            } catch (IOException ex) {
                Logger.getLogger(Estat0.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        // Verificar si ya se recibieron los números de todo el equipo
        if (teamRandomNumbers.size() == TEAM_SIZE && !isLeaderElected) {
            // Elegir el robot con el número más alto como líder
            leaderName = teamRandomNumbers.entrySet()
                          .stream()
                          .max(Map.Entry.comparingByValue())
                          .get()
                          .getKey();

            lider = leaderName.equals(robot.getName()); // Este robot es el líder si su nombre coincide
            isLeaderElected = true;
            robot.setDebugProperty("leader", String.valueOf(leaderName));
            
            try {
                robot.broadcastMessage("LEADER:" + leaderName); // Difundir quién es el líder
            } catch (IOException ex) {
                Logger.getLogger(Estat0.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        robot.execute();
    }

    @Override
    public void onMessageReceived(MessageEvent e) {
        // Recibir números aleatorios de otros robots
        if (e.getMessage() instanceof Integer) {
            teamRandomNumbers.put(e.getSender(), (Integer) e.getMessage());
        }

        // Recibir mensaje de liderazgo
        if (e.getMessage() instanceof String && ((String) e.getMessage()).startsWith("LEADER:")) {
            leaderName = ((String) e.getMessage()).split(":")[1];
            lider = leaderName.equals(robot.getName()); // Este robot es el líder si el nombre coincide
            isLeaderElected = true; // El líder ya ha sido decidido
            robot.setDebugProperty("leader", leaderName);
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
}
