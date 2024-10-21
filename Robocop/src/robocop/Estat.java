package robocop;

import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;

/**
 * @author Oscar Màrquez i Sergi Cabezas
 * Interface que representa un estat del robot.
 */
public interface Estat {
    /**
     * Executa les accions associades a l'estat.
     */
    void execute();

    /**
     * Gestió de l'event quan es detecta un altre robot.
     * @param e L'event de detecció d'un altre robot.
     */
    void onScannedRobot(ScannedRobotEvent e);

    /**
     * Gestió de l'event quan es col·lisiona amb un altre robot.
     * @param e L'event de col·lisió amb un altre robot.
     */
    void onHitRobot(HitRobotEvent e);
}
