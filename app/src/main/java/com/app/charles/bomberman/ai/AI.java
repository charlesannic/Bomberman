package com.app.charles.bomberman.ai;

import android.graphics.Point;

import com.app.charles.bomberman.activities.GameActivity;
import com.app.charles.bomberman.game.Bomb;
import com.app.charles.bomberman.game.Bot;
import com.app.charles.bomberman.game.Grid;
import com.app.charles.bomberman.game.Player;
import com.app.charles.bomberman.java.Direction;

import java.util.ArrayList;

/**
 * Classe gérant l'intelligence artificielle utilisée par les robots.
 */

public class AI {

    // côtés.
    private static final int MOVE_LEFT_OR_TOP = -1, MOVE_RIGHT_OR_BOTTOM = 1, NO_MOVE = 0;

    // mouvements.
    private static final int NONE = 10, LEFT = 11, TOP = 12, RIGHT = 13, BOTTOM = 14;

    // profondeur de recherche.
    private static final int DEPTH_SEARCH = 6;

    // valeur d'une case sur laquelle poser une bombe.
    private static final int CASE_POSE_BOMB = 9;

    // nombre de colonnes et de lignes de la grille.
    private int columns, rows;

    // liste des joueurs de la partie.
    private ArrayList<Player> players;

    // tableau utiles à la recherche de la meilleure direction : case libres et cases dangereuses.
    private int freeCases[][], unsafeCases[][];

    // activité dans laquelle est utilisée l'IA.
    private GameActivity activity;

    public AI(int columns, int rows, ArrayList<Player> players, GameActivity activity) {
        this.columns = columns;
        this.rows = rows;
        this.players = players;
        this.activity = activity;

        freeCases = new int[rows][columns];
        unsafeCases = new int[rows][columns];
    }

    /**
     * Recherche de la meilleure direction.
     * @param bot robot demandant sa direction.
     */
    public synchronized void whereIGo(Bot bot) {
        // position en x et y du joueur.
        int x = bot.getRoundX(),
                y = bot.getRoundY();

        // si une case est dangereuse ou qu'un joueur se trouve dessus, on indique la direction vers la case sûre la plus proche.
        if (unsafeCases[y][x] == 1 || playersPositionForSpecificPlayer(bot)[y][x] == 9) {
            bot.getDirection().setDirection(directionToClosestValue(x, y, unsafeCasesForSpecificPlayer(bot), 0, NONE, 1, DEPTH_SEARCH, false));
            return;
        }
        // si un autre joueur est à côté et qu'il y a un endroit sûr à côté, on pose une bombe.
        else if (someoneNear(bot)) {
            bot.getDirection().setPoseBomb(safePlaceNear(x, y));
            return;
        }

        // recherche de la direction vers le joueur ou le bloc le plus proche.
        int direction = directionToClosestValue(x, y, playersPositionForSpecificPlayer(bot), CASE_POSE_BOMB, NONE, 1, DEPTH_SEARCH, true);
        // si la recherche donne un résultat, on indique la direction.
        if (direction != Direction.STOP) {
            bot.getDirection().setDirection(direction);
            return;
        }
        // sinon si le joueur se trouve déjà sur une case où il est intéressant de poser une bombe et qu'il y a un endroit sûr à côté, il la pose.
        else if (freeCases[y][x] == CASE_POSE_BOMB) {
            bot.getDirection().setPoseBomb(safePlaceNear(x, y));
            return;
        }

        //recherche de la direction vers la case où poser une bombe la plus proche.
        direction = directionToClosestValue(x, y, copy(freeCases), CASE_POSE_BOMB, NONE, 1, DEPTH_SEARCH, true);
        if (direction != Direction.STOP) {
            bot.getDirection().setDirection(direction);
        }
        // si la recherche ne donne rien, on utilise une méthode plus simple pour se diriger vers le joueur le plus proche.
        else {
            bot.getDirection().setDirection(directionToClosestEnnemy(bot));
        }
    }

    /**
     * Indique si un autre joueur est proche.
     * @param player joueur de référence.
     * @return vrai si un autre joueur est proche.
     */
    private boolean someoneNear(Player player) {
        int x = player.getRoundX(),
                y = player.getRoundY();
        int[][] t = playersPositionForSpecificPlayer(player);

        return (y > 0 && t[y - 1][x] == CASE_POSE_BOMB) ||
                (x > 0 && t[y][x - 1] == CASE_POSE_BOMB) ||
                (t[y][x] == CASE_POSE_BOMB) ||
                (y < t.length - 1 && t[y + 1][x] == CASE_POSE_BOMB) ||
                (x < t[0].length - 1 && t[y][x + 1] == CASE_POSE_BOMB);
    }

    /**
     * Indique si un bloc est à côté.
     * @param x index en x.
     * @param y index en y.
     * @return vrai si un bloc est à côté.
     */
    private boolean blocNearby(int x, int y) {
        boolean blocNearby = false;

        if (x > 0)
            blocNearby = freeCases[y][x - 1] == Grid.CASE_BLOC;
        if (!blocNearby && x < columns - 1)
            blocNearby = freeCases[y][x + 1] == Grid.CASE_BLOC;
        if (!blocNearby && y > 0)
            blocNearby = freeCases[y - 1][x] == Grid.CASE_BLOC;
        if (!blocNearby && y < rows - 1)
            blocNearby = freeCases[y + 1][x] == Grid.CASE_BLOC;

        return blocNearby;
    }

    /**
     * Indique si une case sûre est proche/
     * @param x index en x.
     * @param y index en y.
     * @return vrai si une case sûre est proche.
     */
    private boolean safePlaceNear(int x, int y) {
        return y > 0 && unsafeCases[y - 1][x] == 0 ||
                x > 0 && unsafeCases[y][x - 1] == 0 ||
                y < unsafeCases.length - 2 && unsafeCases[y + 1][x] == 0 ||
                x < unsafeCases[0].length - 2 && unsafeCases[y][x + 1] == 0;
    }

    /**
     * Recherche de la direction vers le chemin le plus court jusqu'à notre recherche.
     * @param x index en x.
     * @param y index en y.
     * @param tab tableau à explorer.
     * @param value valeur de la case recherchée.
     * @param excludedSide côté ne devant pas être exploré.
     * @param depth profondeur déjà explorée.
     * @param maxDepth profondeur maximale à explorer.
     * @param avoidUnsafeCases vrai si les cases dangereuses doivent être évitées durant la recherche.
     * @return la direction vers laquelle se diriger.
     */
    private int directionToClosestValue(int x, int y, int tab[][], int value, int excludedSide, int depth, int maxDepth, boolean avoidUnsafeCases) {
        // si la case est dangereuse alors que l'on souhaite les éviter, ou que la profondeur max est atteinte, on stoppe la recherche.
        if (avoidUnsafeCases && unsafeCases[y][x] == 1 || depth > maxDepth) {
            return Integer.MAX_VALUE;
        }
        // si la valeur recherchée est trouvée, on retourne la profondeur explorée pour la trouver.
        else if (tab[y][x] == value) {
            return depth;
        }
        else {
            // initialisation des côtés à explorer.
            int left = Integer.MAX_VALUE,
                    top = Integer.MAX_VALUE,
                    right = Integer.MAX_VALUE,
                    bottom = Integer.MAX_VALUE;

            // si le côté n'est pas exclu et que la case est vide, on continue la recherche.
            if (excludedSide != LEFT && x < columns - 1 && (freeCases[y][x + 1] == Grid.CASE_EMPTY || freeCases[y][x + 1] == CASE_POSE_BOMB))
                right = directionToClosestValue(x + 1,
                        y,
                        tab,
                        value,
                        RIGHT,
                        depth + 1,
                        maxDepth,
                        avoidUnsafeCases);

            if (excludedSide != TOP && y < rows - 1 && (freeCases[y + 1][x] == Grid.CASE_EMPTY || freeCases[y + 1][x] == CASE_POSE_BOMB))
                bottom = directionToClosestValue(x,
                        y + 1,
                        tab,
                        value,
                        BOTTOM,
                        depth + 1,
                        maxDepth,
                        avoidUnsafeCases);

            if (excludedSide != RIGHT && x > 0 && (freeCases[y][x - 1] == Grid.CASE_EMPTY || freeCases[y][x - 1] == CASE_POSE_BOMB))
                left = directionToClosestValue(x - 1,
                        y,
                        tab,
                        value,
                        LEFT,
                        depth + 1,
                        maxDepth,
                        avoidUnsafeCases);

            if (excludedSide != BOTTOM && y > 0 && (freeCases[y - 1][x] == Grid.CASE_EMPTY || freeCases[y - 1][x] == CASE_POSE_BOMB))
                top = directionToClosestValue(x,
                        y - 1,
                        tab,
                        value,
                        TOP,
                        depth + 1,
                        maxDepth,
                        avoidUnsafeCases);

            // on recherche la profondeur minimale retourner pour trouver la valeur.
            int min = Math.min(left, Math.min(top, Math.min(right, bottom)));

            // si il s'agit du premier appel de la méthode, on indique la direction à suivre selon le côté ayant renvoyé la plus petite profondeur.
            if (depth == 1) {
                if (min == Integer.MAX_VALUE)
                    return Direction.STOP;
                else if (min == left)
                    return Direction.LEFT;
                else if (min == top)
                    return Direction.TOP;
                else if (min == right)
                    return Direction.RIGHT;
                else
                    return Direction.BOTTOM;
            }
            // sinon on retourne le minimum trouvé.
            else
                return min;
        }
    }

    /**
     * Mise à jour des tableaux.
     * @param grid grille du jeu.
     * @param bombs bombes sur le plateau.
     */
    public void updateBlocksCases(int grid[][], ArrayList<Bomb> bombs) {
        for (int i = 0; i < grid.length; i++)
            for (int j = 0; j < grid[i].length; j++) {
                unsafeCases[i][j] = Grid.CASE_EMPTY;
                if (grid[i][j] == Grid.CASE_EMPTY
                        || grid[i][j] == Grid.CASE_PU_FASTER
                        || grid[i][j] == Grid.CASE_PU_ADD_BOMB
                        || grid[i][j] == Grid.CASE_PU_POWER
                        || grid[i][j] == Grid.CASE_PU_P_BOMB
                        || grid[i][j] == Bomb.PLAYER_STILL_ON_BOMB)
                    freeCases[i][j] = Grid.CASE_EMPTY;
                else if (grid[i][j] == Grid.CASE_BLOC)
                    freeCases[i][j] = Grid.CASE_BLOC;
                else {
                    freeCases[i][j] = Grid.CASE_WALL;
                    unsafeCases[i][j] = 1;
                }
            }
        updateUnsafeCases(bombs);
        updateBlocNearbyCases();
    }

    /**
     * Mise à jour des cases proches d'un bloc.
     * Elles sont considérées comme intéressantes pour poser une bombe.
     */
    private void updateBlocNearbyCases() {
        for (int i = 0; i < freeCases.length; i++)
            for (int j = 0; j < freeCases[i].length; j++)
                if (blocNearby(j, i) && freeCases[i][j] == 0)
                    freeCases[i][j] = CASE_POSE_BOMB;
    }

    /**
     * positions des autres joueurs pour un joueur précis.
     * @param player joueur souhaitant connaître la position des autres joueurs.
     * @return retourne le tableau de la position des joueurs mis à jour.
     */
    private int[][] playersPositionForSpecificPlayer(Player player) {
        int[][] tab = new int[rows][columns];
        for (int i = 0; i < tab.length; i++)
            for (int j = 0; j < tab[i].length; j++)
                tab[i][j] = 0;

        // toutes les cases où il y a au moins un joueur adverse
        for (Player p : players)
            if (p.getId() != player.getId() && p.isPlayerAlive()) {
                tab[p.getRoundY()][p.getRoundX()] = CASE_POSE_BOMB;
            }

        return tab;
    }

    /**
     * Mise à jour des cases dangereuses pour un joueur précis.
     * @param player joueur souhaitant mettre à jour les cases dangereuses.
     * @return retourne le tableau des cases dangereuses mis à jour.
     */
    private int[][] unsafeCasesForSpecificPlayer(Player player) {
        int[][] tab = copy(unsafeCases);
        // toutes les cases où il y a au moins un joueur adverse
        for (Player p : players)
            if (p.getId() != player.getId() && p.isPlayerAlive()) {
                tab[p.getRoundY()][p.getRoundX()] = 1;
            }

        return tab;
    }

    /**
     * Mise à jour des cases dangereuses.
     * @param bombs bombs sur la grille.
     */
    private void updateUnsafeCases(ArrayList<Bomb> bombs) {
        for (Bomb bomb : bombs) {
            int x = bomb.getXGrid(),
                    y = bomb.getYGrid(),
                    power = bomb.getPower();

            unsafeCases[y][x] = 1;

            spreadExplosion(x + MOVE_LEFT_OR_TOP,
                    y,
                    MOVE_LEFT_OR_TOP,
                    NO_MOVE,
                    power);
            spreadExplosion(x,
                    y + MOVE_LEFT_OR_TOP,
                    NO_MOVE,
                    MOVE_LEFT_OR_TOP,
                    power);
            spreadExplosion(x + MOVE_RIGHT_OR_BOTTOM,
                    y,
                    MOVE_RIGHT_OR_BOTTOM,
                    NO_MOVE,
                    power);
            spreadExplosion(x,
                    y + MOVE_RIGHT_OR_BOTTOM,
                    NO_MOVE,
                    MOVE_RIGHT_OR_BOTTOM,
                    power);
        }

        for(Point point : activity.getUnsafeCases())
            unsafeCases[point.y][point.x] = 1;
    }

    /**
     * Propagation de l'explosion.
     * @param xGrid index en x.
     * @param yGrid index en y.
     * @param moveX déplacement sur l'axe x.
     * @param moveY déplacement sur l'axe y.
     * @param depth puissance de l'explosion.
     */
    private void spreadExplosion(int xGrid, int yGrid, int moveX, int moveY, int depth) {
        if (depth > 0 && xGrid >= 0 && xGrid < columns && yGrid >= 0 && yGrid < rows)
            switch (freeCases[yGrid][xGrid]) {
                case 0:
                    unsafeCases[yGrid][xGrid] = 1;

                    spreadExplosion(xGrid + moveX, yGrid + moveY, moveX, moveY, depth - 1);
                    break;
            }
    }

    /**
     * Méthode simple de recherche de joueur le plus proche.
     * @param player joueur ayant lancé la recherche.
     * @return la direction vers le joueur le plus proche.
     */
    private int directionToClosestEnnemy(Player player) {
        int x = player.getRoundX(),
                y = player.getRoundY();
        int[][] tab = playersPositionForSpecificPlayer(player);

        // initialistion des distances en x et y du joueur le plus proche, égal à la taille du tableau.
        int xClosest = tab[0].length - 1,
                yClosest = tab.length - 1;

        for (int i = 0; i < tab.length; i++)
            for (int j = 0; j < tab[i].length; j++) {
                if (tab[i][j] == CASE_POSE_BOMB) {
                    int diffY = i - y,
                            diffX = j - x;

                    // recherche de la plus faible différence en longueur Manhattan.
                    if (Math.abs(diffY) + Math.abs(diffX)
                            <= Math.abs(yClosest) + Math.abs(xClosest)) {
                        yClosest = diffY;
                        xClosest = diffX;
                    }
                }
            }

        int direction;

        // recherche d'une direction où aller selon la position du joueur ayant lancé la recherche.
        if (x % 2 == 0 && y % 2 != 0)
            direction = yClosest < 0 ? Direction.TOP : Direction.BOTTOM;
        else if (x % 2 != 0 && y % 2 == 0)
            direction = xClosest < 0 ? Direction.LEFT : Direction.RIGHT;
        else if (xClosest == 0)
            direction = yClosest < 0 ? Direction.TOP : Direction.BOTTOM;
        else
            direction = xClosest < 0 ? Direction.LEFT : Direction.RIGHT;

        // vérification que la direction ne mène pas vers une case dangereuse.
        switch (direction) {
            case Direction.LEFT:
                if (x > 0 && unsafeCases[y][x - 1] == 1)
                    direction = Direction.STOP;
                break;
            case Direction.TOP:
                if (y > 0 && unsafeCases[y - 1][x] == 1)
                    direction = Direction.STOP;
                break;
            case Direction.RIGHT:
                if (x < unsafeCases[0].length - 1 && unsafeCases[y][x + 1] == 1)
                    direction = Direction.STOP;
                break;
            case Direction.BOTTOM:
                if (y < unsafeCases.length - 1 && unsafeCases[y + 1][x] == 1)
                    direction = Direction.STOP;
                break;
        }

        return direction;
    }

    /**
     * Copie d'un tableau en créant une nouvelle instance.
     * @param original tableau à copier.
     * @return tableau copié.
     */
    @SuppressWarnings("ManualArrayCopy")
    private int[][] copy(int[][] original) {
        int[][] copy = new int[original.length][original[0].length];

        for (int i = 0; i < original.length; i++)
            for (int j = 0; j < original[i].length; j++)
                copy[i][j] = original[i][j];

        return copy;
    }
}
