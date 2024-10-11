package robocop;
import robocode.AdvancedRobot;
import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;

public class Robocop extends AdvancedRobot {
    private double eX, eY;
    private double battlefieldWidth;
    private double battlefieldHeight;
    private double targetX, targetY;
    private boolean enemicDetectat = false;
    private boolean enemicDet1 = false;
    private double angleOffset = 15;  // Ajuste de ángulo en caso de detección de obstáculo
    private double edgeMargin = 50;   // Margen para evitar bordes
    private double angleRadar;
    private double distancia;
    private double angleCanon;

    private enum Estat { FASE0, FASE1, FASE2 };
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
                    setTurnRadarRight(10);  // Gira el radar contínuament
                    if (enemicDetectat) {
                        estatActual = Estat.FASE1;
                        enemicDet1 = true;
                    }
                    break;

                case FASE1:
                    // FASE1: Dirigir al robot en línea recta hacia la esquina calculada
                    dirigirACantonada(targetX, targetY);
                    setTurnRadarRight(normAngle(getHeading() - getRadarHeading()));  // Mantener el radar apuntando hacia el frente
                    if (hasArrived(targetX, targetY)) {
                        enemicDetectat = false;  // Reiniciar la detección
                        estatActual = Estat.FASE2;
                    }
                    break;
                    
                case FASE2:
                    // FASE2: Comportamiento en la esquina (disparo)
                    setTurnRadarRight(10);  // Vuelve a buscar enemigos

                    if (enemicDetectat) {
                        // Mantener el radar y el cañón apuntando al enemigo
                        setTurnRadarRight(normAngle(angleRadar));  // Ajustar el radar
                        setTurnGunRight(normAngle(angleCanon));  // Ajustar el cañón

                        // Calcular la potencia de disparo
                        double potenciaDispar = Math.max(1, Math.min(3, 500 / distancia));
                        fire(potenciaDispar);  // Disparar
                    }
                    break;
            }
            execute();
        }
    }

    @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        if (estatActual == Estat.FASE1) {
            // Si el robot detectado está enfrente mientras nos movemos, esquivarlo
            if (Math.abs(e.getBearing()) < 10) {  // Si el enemigo está directamente al frente
                esquivarObstaculo(e.getBearing());  // Decidir si esquivar hacia la derecha o izquierda
            }
        }

        // Captura las coordenadas del enemigo y calcula la esquina más lejana
        double angle = getHeading() + e.getBearing();
        eX = getX() + Math.sin(Math.toRadians(angle)) * e.getDistance();
        eY = getY() + Math.cos(Math.toRadians(angle)) * e.getDistance();

        if (!enemicDet1) {
            // Calcular la esquina más alejada, ajustando las coordenadas para evitar los bordes
            double dist0 = calcularDistancia(50, 50, eX, eY);
            double dist1 = calcularDistancia(battlefieldWidth - 50, 50, eX, eY);
            double dist2 = calcularDistancia(50, battlefieldHeight - 50, eX, eY);
            double dist3 = calcularDistancia(battlefieldWidth - 50, battlefieldHeight - 50, eX, eY);

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

            enemicDetectat = true;  
        }

        // Si ya hemos detectado un enemigo, ajustamos el radar y el cañón
        if (enemicDet1) {
            angleRadar = getHeading() + e.getBearing() - getRadarHeading();
            distancia = e.getDistance();
            angleCanon = getHeading() + e.getBearing() - getGunHeading(); 
            enemicDetectat = true; // Asegúrate de que esta línea esté activa para mantener la detección
        }
    }

    @Override
    public void onHitRobot(HitRobotEvent e) {
        // Si trobem un altre robot en el camí, modifiquem l'angle d'aproximació
        esquivarObstaculo(e.getBearing());
    }

    // Método para esquivar obstáculos (decidir si a la derecha o izquierda)
    public void esquivarObstaculo(double bearing) {
        if (bearing > 0) {
            // Si el obstáculo está a la derecha, girar hacia la izquierda para esquivar
            setTurnLeft(angleOffset);
        } else {
            // Si el obstáculo está a la izquierda, girar hacia la derecha para esquivar
            setTurnRight(angleOffset);
        }
        setAhead(50);  // Avanzar para esquivar el obstáculo
    }

    // Movimiento en línea recta hacia la esquina
    public void dirigirACantonada(double targetX, double targetY) {
        double currentX = getX();
        double currentY = getY();

        // Calcular el ángulo hacia la esquina
        double angleCapACantonada = Math.toDegrees(Math.atan2(targetX - currentX, targetY - currentY)) - getHeading();
        setTurnRight(normAngle(angleCapACantonada));
        setAhead(Math.hypot(targetX - currentX, targetY - currentY) - 25);  // Restar un pequeño margen para evitar bordes
    }

    private double calcularDistancia(double x1, double y1, double x2, double y2) {
        return Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));  // Fórmula corregida
    }

    // Funció per normalitzar un angle en el rang [-180, 180]
    public double normAngle(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    // Comprova si el robot ha arribat a la cantonada
    private boolean hasArrived(double targetX, double targetY) {
        return Math.abs(getX() - targetX) < 50 && Math.abs(getY() - targetY) < 50;  // Tolerància de 50 unitats
    }
}
