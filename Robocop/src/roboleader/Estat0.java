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
    private int size = 5;
    private double nummax = 0;
    private double leaderX, leaderY;
    private boolean hallegado =false;
    private String segon;
    private String tercer;
    private String quart;
    private String cinque;

    public Estat0(Roboleader robot) {
        this.robot = robot;
        this.randomNumber = new Random().nextInt(100);
        this.randomNum = new HashMap<>();
        this.distancias = new HashMap<>();
        randomNum.put(robot.getName(), randomNumber);
    }

    @Override
    public void execute() {
        robot.setDebugProperty("ESTAT", String.valueOf(robot.lider));

        if (!isLeaderElected) {
            try {
                robot.broadcastMessage(randomNumber);
            } catch (IOException ex) {
                Logger.getLogger(Estat0.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

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
            
             try {
                robot.broadcastMessage("LEADER:" + leaderName);
            } catch (IOException ex) {
                Logger.getLogger(Estat0.class.getName()).log(Level.SEVERE, null, ex);
            }
             
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

        if (isLeaderElected && hallegado) {
            double distancia = robot.calcularDistancia(robot.getX(), robot.getY(), leaderX, leaderY);
              try {
                robot.broadcastMessage("Distancia:" + distancia);
            } catch (IOException ex) {
                Logger.getLogger(Estat0.class.getName()).log(Level.SEVERE, null, ex);
            }
            distancias.put(robot.getName(), distancia);
        }
        
       if (distancias.size() == (size-1)) {
           robot.setDebugProperty("hellegado","2");
            List<Map.Entry<String, Double>> distanciaList = new ArrayList<>(distancias.entrySet());

           for (int i = 0; i < distanciaList.size() - 1; i++) {
                for (int j = 0; j < distanciaList.size() - 1 - i; j++) {
                    if (distanciaList.get(j).getValue() > distanciaList.get(j + 1).getValue()) {
                        // intercambiem posicions
                        Map.Entry<String, Double> temp = distanciaList.get(j);
                        distanciaList.set(j, distanciaList.get(j + 1));
                        distanciaList.set(j + 1, temp);
                    }
                }
            }

            int i=0;
            for (Map.Entry<String, Double> entry : distanciaList) {
                if(i==0){
                    segon=entry.getKey();
                }
                if(i==1){
                    tercer=entry.getKey();
                }
                if(i==2){
                    quart=entry.getKey();
                }
                if(i==3){
                    cinque=entry.getKey();
                }
                ++i;
            }

           robot.setDebugProperty("Nombres ordenados por distancia (primeros 4)", 
                                   String.join(", ", Arrays.asList(segon, tercer, quart, cinque)));
           robot.canviEstat((Estat) new roboleader.Estat1(robot));
        }
        robot.execute();
    }

    // Método para calcular la distancia entre dos puntos
   

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

        // Recibir coordenada X del líder
        if (mensaje.startsWith("X:")) {
            leaderX = Double.parseDouble(mensaje.split(":")[1]);
        }

        // Recibir coordenada Y del líder
        if (mensaje.startsWith("Y:")) {
            leaderY = Double.parseDouble(mensaje.split(":")[1]);
            hallegado=true;
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

    // Método de pintura
    @Override
    public void onPaint(java.awt.Graphics2D g) {
          robot.setDebugProperty("onPaint called", "true");
    if (robot.lider) {
        g.setColor(Color.YELLOW);
        int diameter = 100; 
        int x = (int) (leaderX - diameter / 2);
        int y = (int) (leaderY - diameter / 2);
        g.drawOval(x, y, diameter, diameter);
        
    }
}
}
