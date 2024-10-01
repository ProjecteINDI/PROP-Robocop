package robocop;
import robocode.Robot;
import robocode.ScannedRobotEvent;

/*
 * @author 49188886M 47956474W
 */
public class Robocop extends Robot{

    @Override
    public void run(){
        while(true){
            ahead(1000);
            turnRight(90);
        }
    }
    
    public void OnScannedRobot(ScannedRobotEvent event){
        fire(1);
    }
}

