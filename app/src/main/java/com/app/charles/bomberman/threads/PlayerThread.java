package com.app.charles.bomberman.threads;

import com.app.charles.bomberman.activities.GameActivity;
import com.app.charles.bomberman.game.Bot;
import com.app.charles.bomberman.game.Grid;
import com.app.charles.bomberman.game.Player;
import com.app.charles.bomberman.java.Direction;
import com.app.charles.bomberman.views.JoyStick;

/**
 * Thread utilisé par chaque joueur pour intéragir dans le jeu.
 */

public class PlayerThread extends Thread {

    // attribut.
    private GameActivity activity;
    private Player player;
    private Grid grid;
    private JoyStick joyStick; // utile uniquement s'il s'agit d'un utilisateur et non d'un robot qui contrôle le joueur.
    private int speed;

    public PlayerThread(GameActivity activity, Player player, Grid grid, JoyStick joyStick, int speed) {
        this.activity = activity;
        this.player = player;
        this.grid = grid;
        this.joyStick = joyStick;
        this.speed = speed;
    }

    /**
     * Méthode appelée lorsque le thread est démaré.
     */
    @Override
    public void run() {
        // tant que la partie n'est pas en pause et que le joueur est en vie.
        while (!activity.gameStoped() && player.isPlayerAlive()) {
            // si le joueur est un robot, sa direction est déterminée depuis la fonction whereIGo de l'Intelligence Artificielle.
            Direction d;
            if(player instanceof Bot) {
                grid.getAi().whereIGo((Bot)player);
                d = ((Bot)player).getDirection();
            }
            // si le joueur est un utilisateur, la direction est déterminée selon la position du joystick.
            else
                d = joyStick.getDirection();

            // la direction doit être déclarée comme final pour être utilisée dans le thread principal
            final Direction direction = d;
            // sur Android, la modification des vues ne peut se faire que sur le thread principal. la fonction runOnUiThread permet d'exécuter le code sur ce thread principal.
            activity.runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    // pour se déplacer, la direction doit être différente de STOP et on doit s'assurer que sa vue à bien été créée.
                    if (direction.getDirection() != Direction.STOP && grid.getPlayerView() != null) {
                        // pose d'une bombe.
                        if (player instanceof Bot && direction.isPoseBomb())
                            grid.poseBomb(player);
                        // déplacements.
                        else if (direction.getDirection() == Direction.LEFT)
                            // déplacement selon la vitesse initiale, la vitesse supplémentaire du joueur et l'inclinaison du joystick pour un utilisateur.
                            grid.moveLeft(player, (speed + player.getSpeed()) * (-direction.getOffset()));
                        else if (direction.getDirection() == Direction.RIGHT)
                            grid.moveRight(player, (speed + player.getSpeed()) * direction.getOffset());
                        else if (direction.getDirection() == Direction.TOP)
                            grid.moveTop(player, (speed + player.getSpeed()) * (-direction.getOffset()));
                        else if (direction.getDirection() == Direction.BOTTOM)
                            grid.moveBottom(player, (speed + player.getSpeed()) * direction.getOffset());
                    }
                }
            });

            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
