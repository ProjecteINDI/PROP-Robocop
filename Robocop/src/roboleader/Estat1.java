package roboleader;

import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;
import robocode.MessageEvent;


public class Estat1 implements Estat {
    private Roboleader robot;
    private double targetX, targetY;
    public Estat1(Roboleader robot) {
        this.robot = robot;
    }

    @Override
    public void execute() {
        double dist0 = robot.calcularDistancia(0, 0, robot.getX(), robot.getY());
        double dist1 = robot.calcularDistancia(robot.battlefieldWidth, 0, robot.getX(), robot.getY());
        double dist2 = robot.calcularDistancia(0, robot.battlefieldHeight , robot.getX(), robot.getY());
        double dist3 = robot.calcularDistancia(robot.battlefieldWidth , robot.battlefieldHeight , robot.getX(), robot.getY());
        if(robot.lider){
        if (dist0 > dist1 && dist0 > dist2 && dist0 > dist3) {
            targetX = 20;
            targetY = 20;
        } else if (dist1 > dist2 && dist1 > dist3) {
            targetX = robot.battlefieldWidth - 20;
            targetY = 20;
        } else if (dist2 > dist3) {
            targetX = 20;
            targetY = robot.battlefieldHeight - 20;
        } else {
            targetX = robot.battlefieldWidth - 20;
            targetY = robot.battlefieldHeight - 20;
        }
        robot.dirigirACantonada(targetX, targetY);
        }
         robot.execute();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
       
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
       
    }
    
    @Override
    public void onMessageReceived(MessageEvent e) {
        
    }
    @Override
    public void onPaint(java.awt.Graphics2D g) {
        
    }
}
