package roboleader;

import java.io.IOException;
import robocode.ScannedRobotEvent;
import robocode.HitRobotEvent;
import robocode.MessageEvent;
import robocode.util.Utils;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import robocode.RobotDeathEvent;

public class Estat1 implements Estat {
    private Roboleader robot;
    private List<String> jerarquia; 
    private int miPosicion;
    private boolean sentidoHorario = true; 
    private long tiempoCambio;
    private boolean ant = false;

    // Esquivar y colisiones
    public boolean chocar = false;
    public boolean esquivar = false;
    public boolean esquivarCompletado = false;
    public double esquivarTargetX, esquivarTargetY;
    private double esquivarAngle = 40;  // Ángulo para esquivar a derecha o izquierda
    public double dis;
    public double distanciaTangente;
    public double chocaDir = 0;  // Dirección de colisión

    // Coordenadas y movimiento
    private double[][] esquinas;
    private int esquinaActual = -1; 
    private boolean esquinaInicialSeleccionada = false;
    private boolean enemic = false;
    private final double distanciaMinima = 100;
    private final double distanciaMaxima = 200;
    private double enemigoX;
    private double enemigoY;
    private String enemigo = null;
    private long tiempoUltimoEscaneo;
    private final long intervaloEscaneo = 2000;
    
    public Estat1(Roboleader robot, List<String> jerarquia) {
        this.robot = robot;
        this.jerarquia = jerarquia;
        this.miPosicion = jerarquia.indexOf(robot.getName()); 

        double marginX = robot.battlefieldWidth * 0.1;
        double marginY = robot.battlefieldHeight * 0.1;

        esquinas = new double[][]{
            {marginX, marginY},                                        
            {robot.battlefieldWidth - marginX, marginY},                
            {robot.battlefieldWidth - marginX, robot.battlefieldHeight - marginY}, 
            {marginX, robot.battlefieldHeight - marginY}                
        };

        tiempoCambio = 100;
    }
    
    @Override
    public void execute() {
        if ((System.currentTimeMillis() - tiempoCambio) >= 15000) {
            invertirRolesYSentido();
            tiempoCambio = System.currentTimeMillis();
        }

        if (miPosicion == 0) {
            robot.setTurnRadarRight(robot.normAngle(robot.getHeading() - robot.getRadarHeading()));

            if (!esquinaInicialSeleccionada) {
                seleccionarEsquinaMasCercana(); 
                esquinaInicialSeleccionada = true; 
            }
            continuarTrayectoria();
            if (System.currentTimeMillis() - tiempoUltimoEscaneo >= intervaloEscaneo && !enemic) {
                robot.setTurnRadarRight(360);
                tiempoUltimoEscaneo = System.currentTimeMillis();
            }
        } else {
            if (ant) {
                seguirAntecesor();    
            }
        }

        // Comprobar si hay un enemigo
        if (enemigo != null) {
            atacarEnemigo();
        }
        
        
        if (esquivar) {
            double angleEsquiva = Math.toDegrees(Math.atan2(esquivarTargetX - robot.getX(), esquivarTargetY - robot.getY())) - robot.getHeading();
            robot.setTurnRight(robot.normAngle(angleEsquiva));
            robot.setAhead(Math.hypot(esquivarTargetX - robot.getX(), esquivarTargetY - robot.getY()) - 10);

            // Comprobación de finalización del esquive
            if (Math.abs(robot.getX() - esquivarTargetX) < 20 && Math.abs(robot.getY() - esquivarTargetY) < 20) {
                esquivar = false;
                esquivarCompletado = true;
                
                if(miPosicion == 0){
                    continuarTrayectoria();
                }else{
                    seguirAntecesor();
                }
            }

            
        }

        // Gestión de colisiones con compañeros
        if (chocar) {
            if (chocaDir > 0) {
                robot.setTurnLeft(esquivarAngle);
            } else {
                robot.setTurnRight(esquivarAngle);
            }
            robot.setAhead(50); 
            chocar = false; 
        }

        robot.execute();
    }


    // Detecta si el robot ha llegado a la esquina actual
    private boolean haLlegadoAEsquina(double targetX, double targetY) {
        return robot.calcularDistancia(robot.getX(), robot.getY(), targetX, targetY) < 25;
    }

    // Selecciona la esquina más cercana al robot al inicio
    private void seleccionarEsquinaMasCercana() {
        double dist0 = robot.calcularDistancia(esquinas[0][0], esquinas[0][1], robot.getX(), robot.getY());
        double dist1 = robot.calcularDistancia(esquinas[1][0], esquinas[1][1], robot.getX(), robot.getY());
        double dist2 = robot.calcularDistancia(esquinas[2][0], esquinas[2][1], robot.getX(), robot.getY());
        double dist3 = robot.calcularDistancia(esquinas[3][0], esquinas[3][1], robot.getX(), robot.getY());

        if (dist0 < dist1 && dist0 < dist2 && dist0 < dist3) {
            esquinaActual = 0;
        } else if (dist1 < dist2 && dist1 < dist3) {
            esquinaActual = 1;
        } else if (dist2 < dist3) {
            esquinaActual = 2;
        } else {
            esquinaActual = 3;
        }
        robot.setDebugProperty("Esquina inicial", "Seleccionada esquina " + esquinaActual);
    }

    private void continuarTrayectoria() {
        robot.setDebugProperty("Líder", "Moviéndose a la esquina " + esquinaActual);

        // Moverse hacia la esquina actual
        double targetX = esquinas[esquinaActual][0];
        double targetY = esquinas[esquinaActual][1];

        // Calcular el ángulo hacia la esquina
        double angle = Math.atan2(targetX - robot.getX(), targetY - robot.getY());
        double angleToTurn = Utils.normalRelativeAngle(angle - robot.getHeadingRadians());

        // Primero giramos sin avanzar
        if (Math.abs(angleToTurn) > Math.toRadians(1)) {
            robot.setTurnRightRadians(angleToTurn);
            // No avanzamos mientras giramos
            return;
        }

        // Una vez orientados correctamente, avanzamos hacia la esquina
        robot.setAhead(robot.calcularDistancia(targetX, targetY, robot.getX(), robot.getY()));

        // Comprobar si ha llegado a la esquina actual
        if (haLlegadoAEsquina(targetX, targetY)) {
            robot.setDebugProperty("Líder", "Esquina alcanzada: " + esquinaActual);

            // Enviar mensaje al siguiente robot en la jerarquía
            String siguienteRobot = jerarquia.get(miPosicion + 1);
            try {
                robot.sendMessage(siguienteRobot, "Cantonada");
            } catch (IOException ex) {
                Logger.getLogger(Estat1.class.getName()).log(Level.SEVERE, null, ex);
            }

            // Cambiar a la siguiente esquina
            if (!sentidoHorario) {
                esquinaActual = (esquinaActual - 1 + 4) % 4; // Sentido horario
            } else {
                esquinaActual = (esquinaActual + 1) % 4; // Sentido antihorario
            }
        }
    }

    // Método para seguir al robot inmediatamente superior en la jerarquía
    private void seguirAntecesor() {
        String antecesor = jerarquia.get(miPosicion - 1); // Obtener el robot anterior
        Double[] antecesorPos = robot.getPosicion(antecesor); // Obtener la posición del antecesor

        if (antecesorPos != null) {
            double antecesorX = antecesorPos[0];
            double antecesorY = antecesorPos[1];

            double distancia = robot.calcularDistancia(antecesorX, antecesorY, robot.getX(), robot.getY());

            if (distancia > distanciaMaxima) {
                // Si está demasiado lejos, acelerar para alcanzarlo
                double angle = Math.atan2(antecesorX - robot.getX(), antecesorY - robot.getY());
                robot.setTurnRightRadians(Utils.normalRelativeAngle(angle - robot.getHeadingRadians()));
                robot.setAhead(distancia - distanciaMinima);
            } else if (distancia < distanciaMinima) {
                // Si está demasiado cerca, retroceder ligeramente
                robot.setAhead(-distanciaMinima);
            } else {
                if (miPosicion + 1 < jerarquia.size()) {
                String siguienteRobot = jerarquia.get(miPosicion + 1);
                    try {
                        robot.sendMessage(siguienteRobot, "Cantonada");
                    } catch (IOException ex) {
                        Logger.getLogger(Estat1.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                double angle = Math.atan2(antecesorX - robot.getX(), antecesorY - robot.getY());
                robot.setTurnRightRadians(Utils.normalRelativeAngle(angle - robot.getHeadingRadians()));
                robot.setAhead(distancia - distanciaMinima);
            }
        } else {
            // Si no se tiene la posición del antecesor, escanear en busca del antecesor
            robot.setTurnRadarRight(360); // Girar el radar para buscar al antecesor
        }
    }

    // Método que invierte la jerarquía y el sentido de rotación
    private void invertirRolesYSentido() {
        // Invertir el sentido de rotación
        sentidoHorario = !sentidoHorario;

        // Invertir la jerarquía
        java.util.Collections.reverse(jerarquia);

        // Actualizar la posición del robot
        miPosicion = jerarquia.indexOf(robot.getName());

        // Reiniciar el seguimiento de esquinas
        esquinaInicialSeleccionada = false; // Forzar la selección de una nueva esquina
        robot.setDebugProperty("Cambio de roles", "Invertido sentido y roles. Nueva posición: " + miPosicion);
    }


   @Override
    public void onScannedRobot(ScannedRobotEvent e) {
        // Ignorar robots del mismo equipo
        if (robot.isTeammate(e.getName())) {
            return;
        }

        // Esquivar si el enemigo está en trayectoria y cerca
        if (Math.abs(e.getBearing()) < 10 && e.getDistance() <= 200 && !esquivarCompletado) {
            esquivar = true;
            dis = e.getDistance();

            double angleEsquivarDerecha = esquivarAngle;
            distanciaTangente = Math.tan(Math.toRadians(angleEsquivarDerecha)) * dis;

            double angleDerecha = robot.getHeading() + angleEsquivarDerecha;
            esquivarTargetX = robot.getX() + Math.sin(Math.toRadians(angleDerecha)) * distanciaTangente;
            esquivarTargetY = robot.getY() + Math.cos(Math.toRadians(angleDerecha)) * distanciaTangente;

            // Verificar si el punto de esquiva está fuera del campo de batalla
            if (esquivarTargetX < 20 || esquivarTargetX > robot.battlefieldWidth - 20 || 
                esquivarTargetY < 20 || esquivarTargetY > robot.battlefieldHeight - 20) {
                double angleIzquierda = robot.getHeading() - esquivarAngle;
                esquivarTargetX = robot.getX() + Math.sin(Math.toRadians(angleIzquierda)) * distanciaTangente;
                esquivarTargetY = robot.getY() + Math.cos(Math.toRadians(angleIzquierda)) * distanciaTangente;
                robot.setTurnLeft(esquivarAngle);
            } else {
                robot.setTurnRight(esquivarAngle);
            }

            // Avanzar hacia el objetivo de esquiva
            robot.setAhead(distanciaTangente);
        }

        // Lógica de disparo
        if (!enemic) {
            enemigo = e.getName();
            enemic = true;

            // Calcular las coordenadas del enemigo
            double angle = robot.getHeading() + e.getBearing();
            enemigoX = robot.getX() + Math.sin(Math.toRadians(angle)) * e.getDistance(); 
            enemigoY = robot.getY() + Math.cos(Math.toRadians(angle)) * e.getDistance();

            // Enviar las coordenadas del enemigo al equipo
            try {
                robot.broadcastMessage("enemic: " + e.getName());
                robot.broadcastMessage("x: " + enemigoX);
                robot.broadcastMessage("y: " + enemigoY);
            } catch (IOException ex) {
                Logger.getLogger(Estat1.class.getName()).log(Level.SEVERE, null, ex);
            }
            robot.setDebugProperty("Nuevo objetivo", "Fijado enemigo: " + enemigo);
        }

        // Llamar a la función de atacar enemigo
        atacarEnemigo();
    }

    private void atacarEnemigo() {
        // Calculamos el ángulo hacia el enemigo usando las coordenadas
        double targetX = enemigoX;
        double targetY = enemigoY;

        // Calcular el ángulo absoluto hacia el enemigo
        double angleToEnemy = Math.atan2(targetX - robot.getX(), targetY - robot.getY());
        double gunTurnAngle = Utils.normalRelativeAngle(angleToEnemy - robot.getGunHeadingRadians());

        // Depurar la dirección actual del cañón y el ángulo de giro necesario
        robot.setDebugProperty("Apuntando", "Angulo hacia enemigo: " + Math.toDegrees(angleToEnemy) + 
                               " | Angulo del cañon: " + Math.toDegrees(robot.getGunHeadingRadians()) + 
                               " | Necesario girar: " + Math.toDegrees(gunTurnAngle));

        // Girar el cañón hacia el enemigo
        robot.setTurnGunRightRadians(gunTurnAngle);

        // Esperar a que el cañón esté alineado antes de disparar
        if (Math.abs(gunTurnAngle) < Math.toRadians(10)) {
            robot.setFire(1); // Disparar al enemigo cuando el cañón está alineado
        }
    }
    
    

    @Override
    public void onHitRobot(HitRobotEvent e) {
        chocar = true;  // Activa la señal de colisión
        chocaDir = e.getBearing();  // Guardamos la dirección de colisión
        
    }

    @Override
    public void onMessageReceived(MessageEvent e) {
        if (e.getMessage() instanceof String && e.getMessage().equals("Cantonada")) {
             ant=true;
            robot.setDebugProperty("LC", "lidercantonada");
         }
        if (e.getMessage() instanceof String) {
            String mensaje = (String) e.getMessage();
            if (mensaje.startsWith("enemic:")) {
                        enemigo = mensaje.split(":")[1];
            }
            if (mensaje.startsWith("x:")) {
                        enemigoX = Double.parseDouble(mensaje.split(":")[1].trim());
            }
            if (mensaje.startsWith("y:")) {
                        enemigoY = Double.parseDouble(mensaje.split(":")[1].trim());
            }
        }
    }

    @Override
    public void onPaint(java.awt.Graphics2D g) {
        // Pintar el rectángulo del camino del líder
        g.setColor(java.awt.Color.YELLOW);
        for (int i = 0; i < 4; i++) {
            int next = (i + 1) % 4;
            g.drawLine((int)esquinas[i][0], (int)esquinas[i][1], (int)esquinas[next][0], (int)esquinas[next][1]);
        }
    }
    @Override
    public void onRobotDeath(RobotDeathEvent e) {
        // Si el robot que ha muerto es un enemigo (no un compañero)
        if (!robot.isTeammate(e.getName())) {
            robot.setDebugProperty("Estado", "Enemigo " + e.getName() + " destruido. Escaneando nuevo objetivo.");
            enemigo = null;  // Reiniciar el enemigo actual
            enemic = false;  // No hay enemigo activo

            // Realizar un escaneo completo con el radar para buscar otro enemigo
            robot.setTurnRadarRight(360);
        }

        // Si un compañero ha muerto, actualizar la jerarquía
        if (robot.isTeammate(e.getName())) {
            int indiceMuerto = jerarquia.indexOf(e.getName());
            if (miPosicion > indiceMuerto) {
                miPosicion--;
            }
        }
    }

}
    
   
