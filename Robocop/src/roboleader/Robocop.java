package roboleader;


import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;
import robocode.MessageEvent;
import robocode.TeamRobot;

public class Robocop extends TeamRobot {
    
    
    private Estat estatActual;
@Override
    public void run() {
       estatActual = new Estat0(this);
        while (true) {
            estatActual.execute();
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        estatActual.onScannedRobot(e);
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        estatActual.onHitRobot(e);
    }


    @Override
    public void onMessageReceived(MessageEvent e) {
        estatActual.onMessageReceived(e);
        
    }
}
