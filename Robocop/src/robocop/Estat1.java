package robocop;

import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;

public class Estat1 implements Estat {
    private Robocop robot;

    public Estat1(Robocop robot) {
        this.robot = robot;
    }
    public boolean esquivar = false;
    public double dis;
    
    @Override
    public void execute() {
        robot.enemicDetectat = false;
        robot.dirigirACantonada(robot.targetX, robot.targetY);
        robot.setTurnRadarRight(robot.normAngle(robot.getHeading() - robot.getRadarHeading()));

        if (robot.hasArrived(robot.targetX, robot.targetY)) {      
            robot.changeEstat(new Estat2(robot));
        }
        
        if(esquivar){
            robot.setDebugProperty("esquivar","esquivar");
            robot.setTurnLeft(180);
            robot.setAhead(500);
            
           if(Math.tan(Math.toRadians(40)*dis)==){
            esquivar=false;
           }
        }
        robot.execute();
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        if (Math.abs(e.getBearing()) < 10) {
                
                esquivar=true;
                dis=e.getDistance();
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        esquivar=true;
    }
}
