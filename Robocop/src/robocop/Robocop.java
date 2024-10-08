package robocop;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;

public class Robocop extends AdvancedRobot {
    private double enemyX, enemyY;
    private double battlefieldWidth;
    private double battlefieldHeight;
    private double targetX, targetY;
    private boolean enemicDetectat = false;
    private double angleOffset = 15;  // Ajuste de ángulo en caso de detección de obstáculo
    private double edgeMargin = 50;    // Margen de distancia para evitar bordes

    private enum Estat { FASE0, FASE1 };
    private Estat estatActual = Estat.FASE0;

    @Override
    public void run() {
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
                    // FASE1: Moure's a la cantonada calculada, evitant obstacles
                    anarACantonada(targetX, targetY);
                    if (hasArrived(targetX, targetY)) {  // Si hem arribat a la cantonada
                        estatActual = Estat.FASE0;
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

        // Apuntar al enemic detectat i disparar
        double gunTurn = normalizeBearing(angleAbsolut - getGunHeading());
        setTurnGunRight(gunTurn);
        fire(1);  // Disparar al enemic amb potència de 1

        // Calcular la cantonada més allunyada
        double dist0 = calcularDistancia(0, 0, enemyX, enemyY);
        double dist1 = calcularDistancia(battlefieldWidth, 0, enemyX, enemyY);
        double dist2 = calcularDistancia(0, battlefieldHeight, enemyX, enemyY);
        double dist3 = calcularDistancia(battlefieldWidth, battlefieldHeight, enemyX, enemyY);

        if (dist0 > dist1 && dist0 > dist2 && dist0 > dist3) {
            targetX = 0;
            targetY = 0;
        } else if (dist1 > dist2 && dist1 > dist3) {
            targetX = battlefieldWidth;
            targetY = 0;
        } else if (dist2 > dist3) {
            targetX = 0;
            targetY = battlefieldHeight;
        } else {
            targetX = battlefieldWidth;
            targetY = battlefieldHeight;
        }

        enemicDetectat = true;  // Activem la següent fase
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        // Si trobem un altre robot en el camí, modifiquem l'angle d'aproximació
        double angleCapACantonada = Math.toDegrees(Math.atan2(targetX - getX(), targetY - getY())) - getHeading();

        // Canvia el angle depenent de la posició del enemic
        if (e.getBearing() > -90 && e.getBearing() < 90) {
            // Si el enemic està al davant, canviar l'angle per evitar col·lisions
            angleCapACantonada += angleOffset;
        } else {
            // Si el enemic està darrere, canviar l'angle cap a l'altre costat
            angleCapACantonada -= angleOffset;
        }

        setTurnRight(normalizeBearing(angleCapACantonada));
        fire(1);  // Disparar a l'enemic en cas de col·lisió
        setAhead(100);  // Mou-te 100 unitats en la direcció ajustada
    }

    public void anarACantonada(double targetX, double targetY) {
        // Ajustar el moviment per evitar bordes
        double currentX = getX();
        double currentY = getY();

        // Comprovar si s'apropa al bord
        if (currentX <= edgeMargin) {
            // Està a prop del costat esquerre
            setTurnRight(90);  // Gira a la dreta
        } else if (currentX >= battlefieldWidth - edgeMargin) {
            // Està a prop del costat dret
            setTurnLeft(90);  // Gira a l'esquerra
        } else if (currentY <= edgeMargin) {
            // Està a prop del costat superior
            setTurnRight(90);  // Gira a la dreta
        } else if (currentY >= battlefieldHeight - edgeMargin) {
            // Està a prop del costat inferior
            setTurnLeft(90);  // Gira a l'esquerra
        } else {
            // Mou-te cap a la cantonada
            setTurnRight(normalizeBearing(Math.toDegrees(Math.atan2(targetX - currentX, targetY - currentY)) - getHeading()));
            setAhead(Math.hypot(targetX - currentX, targetY - currentY));  // Després es mou cap endavant
        }
    }

    private double calcularDistancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));  // Fórmula corregida
    }

    // Funció per normalitzar un angle en el rang [-180, 180]
    public double normalizeBearing(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    // Comprova si el robot ha arribat a la cantonada
    private boolean hasArrived(double targetX, double targetY) {
        return Math.abs(getX() - targetX) < 50 && Math.abs(getY() - targetY) < 50;  // Tolerància de 20 unitats
    }
}
