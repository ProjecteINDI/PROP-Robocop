package robocop;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;

public class Robocop extends AdvancedRobot {
    private double enemyX, enemyY;
    private double battlefieldWidth;
    private double battlefieldHeight;
    private double targetX, targetY;
    private boolean enemicDetectat = false;
    
    private enum Estat { FASE0, FASE1 };
    private Estat estatActual = Estat.FASE0;
    
    @Override
    public void run(){
        battlefieldWidth = getBattleFieldWidth();
        battlefieldHeight = getBattleFieldHeight();

        setAdjustGunForRobotTurn(true);
        setAdjustRadarForRobotTurn(true);
        setAdjustRadarForGunTurn(true);

        while (true) {
            switch (estatActual) {
                case FASE0:
                    // FASE0: Detectar enemic i calcular cantonada
                    setTurnRadarRight(10.0);  // Gira el radar contínuament
                    if (enemicDetectat) {
                        estatActual = Estat.FASE1;  // Si ja hem detectat un enemic, canviem a la següent fase
                    }
                    break;

                case FASE1:
                    // FASE1: Moure's a la cantonada calculada
                    anarACantonada(targetX, targetY);
                    if (hasArrived(targetX, targetY)) {  // Si hem arribat a la cantonada
                        // Potser podries fer alguna cosa aquí o només quedar-te quiet
                        enemicDetectat = false;
                    }
                    break;
            }
            execute();
        }
    }
    
    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // Captura les coordenades de l'enemic
        double angleAbsolut = getHeading() + e.getBearing();
        enemyX = getX() + Math.sin(Math.toRadians(angleAbsolut)) * e.getDistance();
        enemyY = getY() + Math.cos(Math.toRadians(angleAbsolut)) * e.getDistance();
        
        // Calcular la cantonada més allunyada
        double dist0 = calcularDistancia( 50, 50, enemyX, enemyY);
        double dist1 = calcularDistancia(battlefieldWidth - 50, 50, enemyX, enemyY);
        double dist2 = calcularDistancia(50, battlefieldHeight - 50, enemyX, enemyY);
        double dist3 = calcularDistancia(battlefieldWidth - 50, battlefieldHeight - 50, enemyX, enemyY);

        if (dist0 > dist1 && dist0 > dist2 && dist0 > dist3) {
            targetX = 50;
            targetY = 50;
        } else if (dist1 > dist2 && dist1 > dist3) {
            targetX = battlefieldWidth - 50;
            targetY = 50;
        } else if (dist2 > dist3) {
            targetX = 50;
            targetY = battlefieldHeight - 50;
        } else {
            targetX = battlefieldWidth - 50;
            targetY = battlefieldHeight - 50;
        }

        enemicDetectat = true;  // Activem la següent fase
    }

    private double calcularDistancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));  // Fórmula corregida
    }

    public void anarACantonada(double targetX, double targetY) {
        // Calcula l'angle per moure's cap a la cantonada
        double angleCapACantonada = Math.toDegrees(Math.atan2(targetX - getX(), targetY - getY())) - getHeading();
        setTurnRight(normalizeBearing(angleCapACantonada));
        execute();  // Fes que giri primer abans de moure's
        setAhead(Math.hypot(targetX - getX(), targetY - getY()));  // Després es mou cap endavant
    }

    // Funció per normalitzar un angle en el rang [-180, 180]
    public double normalizeBearing(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    // Comprova si el robot ha arribat a la cantonada
    private boolean hasArrived(double targetX, double targetY) {
        return Math.abs(getX() - targetX) < 20 && Math.abs(getY() - targetY) < 20;  // Tolerància de 20 unitats
    }
}
 