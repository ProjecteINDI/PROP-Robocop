package roboleader;

import java.awt.Graphics2D;
import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;
import robocode.MessageEvent;
import robocode.TeamRobot;

public class Roboleader extends TeamRobot {
    private Estat estatActual; // Cambié el nombre a estatActual para mayor claridad
    public double battlefieldWidth;
    public double battlefieldHeight;
    public boolean lider = false;
    @Override
    public void run() {
        battlefieldWidth = getBattleFieldWidth();
        battlefieldHeight = getBattleFieldHeight();
        estatActual = new Estat0(this); // Inicializa el estado
        while (true) {
            estatActual.execute(); // Ejecuta la lógica del estado
            execute(); // Llama al método execute de TeamRobot
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
    }
     public double calcularDistancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
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
      public void canviEstat(Estat nouEstat) {
    this.estatActual = nouEstat;
}

    @Override
    public void onPaint(Graphics2D g) {
        estatActual.onPaint(g); // Llama al método onPaint del estado
    }
}
