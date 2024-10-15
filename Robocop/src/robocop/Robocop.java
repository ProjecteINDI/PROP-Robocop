package robocop;

import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;

public class Robocop extends AdvancedRobot {
    public double eX, eY;
    public double battlefieldWidth;
    public double battlefieldHeight;
    public double targetX, targetY;
    public boolean enemicDetectat = false;
    
    public double angleRadar;
    public double distancia;
    public double angleCanon;

    private Estat estatActual;

    @Override
    public void run() {
        battlefieldWidth = getBattleFieldWidth();
        battlefieldHeight = getBattleFieldHeight();

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

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

    public void changeEstat(Estat newEstat) {
        this.estatActual = newEstat;
    }

   

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

    public boolean hasArrived(double targetX, double targetY) {
        return Math.abs(getX() - targetX) < 50 && Math.abs(getY() - targetY) < 50;  
    }

    public double calcularDistancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - y1) + (y2 - y1) * (y2 - y1));
    }
}
