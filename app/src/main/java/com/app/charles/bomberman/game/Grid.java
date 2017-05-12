package com.app.charles.bomberman.game;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.TransitionDrawable;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.RelativeLayout;

import com.app.charles.bomberman.R;
import com.app.charles.bomberman.activities.GameActivity;
import com.app.charles.bomberman.ai.AI;

import java.util.ArrayList;
import java.util.Random;

/**
 * Classe gérant les fonctionnalités liéées à la grille du jeu.
 */

@SuppressWarnings("FieldCanBeLocal")
public class Grid {

    // côtés, utilisés pour le déroulement d'algorithmes récursifs.
    private static final int SIDE_NONE = 0,
            SIDE_LEFT = 1,
            SIDE_TOP = 2,
            SIDE_RIGHT = 3,
            SIDE_BOTTOM = 4;

    // déplacements d'un joueurs.
    private static final int MOVE_LEFT_OR_TOP = -1,
            MOVE_RIGHT_OR_BOTTOM = 1,
            NO_MOVE = 0;

    // valeurs possibles des cases de la grille.
    public static final int CASE_EMPTY = 0,
            CASE_WALL = 1,
            CASE_BLOC = 2,
            CASE_PU_FASTER = 5,
            CASE_PU_ADD_BOMB = 6,
            CASE_PU_POWER = 7,
            CASE_PU_P_BOMB = 8,
            CASE_LAST_SECONDS_WALL = 9;

    // tailles possibles de la grille selon la difficulté.
    private final int GRID_COLUMNS_EASY = 9,
            GRID_ROWS_EASY = 7,
            GRID_COLUMNS_NORMAL = 11,
            GRID_ROWS_NORMAL = 7,
            GRID_COLUMNS_HARD = 13,
            GRID_ROWS_HARD = 9;

    // grille
    private int grid[][];

    private Context context; // un Context est utilisé pour certaines méthodes qui nécessitent de savoir depuis quelle activité elles sont appelées.

    // vues
    private GridLayout gridLayout;
    private RelativeLayout objectsContainer;

    // taille de la grille
    private int gridColumns = GRID_COLUMNS_NORMAL,
            gridRows = GRID_ROWS_NORMAL;

    // joueurs
    private Player player;
    private ArrayList<Bot> bots;

    // intelligence artificielle
    private AI ai;

    /**
     * Constructeur
     * @param context contexte de l'activité
     * @param gridL vue affichant la grille
     * @param objectsContainer conteneur dans lequel sera affiché les joueurs (ne pouvant pas être affichés sur la grille directement).
     * @param difficulty difficulté de la partie
     */
    public Grid(Context context, GridLayout gridL, RelativeLayout objectsContainer, final int difficulty) {
        this.context = context;
        this.gridLayout = gridL;
        this.objectsContainer = objectsContainer;

        // changement de la taille de la grille selon la difficulté.
        switch (difficulty) {
            case 0:
                gridColumns = GRID_COLUMNS_EASY;
                gridRows = GRID_ROWS_EASY;
                break;
            case 1:
                gridColumns = GRID_COLUMNS_NORMAL;
                gridRows = GRID_ROWS_NORMAL;
                break;
            case 2:
                gridColumns = GRID_COLUMNS_HARD;
                gridRows = GRID_ROWS_HARD;
                break;
        }
        gridLayout.setColumnCount(gridColumns);
        gridLayout.setRowCount(gridRows);

        // création de la grille.
        generateGrid();

        // création des joueurs.
        player = new Player(this.context, 1);
        bots = new ArrayList<>();
        bots.add(new Bot(this.context, 2));
        if (difficulty >= 1) {
            bots.add(new Bot(this.context, 3));
            if (difficulty >= 2)
                bots.add(new Bot(this.context, 4));
        }

        // création de l'intelligence artificielle et mise à jour des informations dont elle dispose.
        ai = new AI(gridColumns, gridRows, getAllPlayers(), (GameActivity) this.context);
        updateAIInformations();

        // une fois que la grille est créée et que l'on peut connaître sa taille, on ajoute son contenu.
        this.gridLayout.post(new Runnable() {
            @Override
            public void run() {
                setGridItems(gridLayout.getWidth() / gridColumns, difficulty);
            }
        });
    }

    /**
     * Ajout du contenu dans la grille.
     * @param itemSize taille des items dans la grille
     *                 (valeur arrondie car la grille n'a pas une largeur nécessairement multiple du nombre de colonne).
     * @param difficulty difficulté de la partie.
     */
    private void setGridItems(int itemSize, int difficulty) {
        // modification de la taille de la grille du conteneur des autres vue
        // pour que leur taille soit un multiple du nombre de colonnes et de la taille de ses cellules.
        RelativeLayout.LayoutParams gridParams = (RelativeLayout.LayoutParams) gridLayout.getLayoutParams();
        gridParams.height = itemSize * gridRows;
        gridParams.width = itemSize * gridColumns;
        gridLayout.setLayoutParams(gridParams);

        RelativeLayout.LayoutParams objectsContainerParams = (RelativeLayout.LayoutParams) objectsContainer.getLayoutParams();
        objectsContainerParams.height = itemSize * gridRows;
        objectsContainerParams.width = itemSize * gridColumns;
        objectsContainer.setLayoutParams(objectsContainerParams);

        gridLayout.removeAllViews();

        // remplissage des cases de la grille.
        for (int i = 0; i < gridRows; i++) {
            for (int j = 0; j < gridColumns; j++) {
                // création d'une vue contenue dans la grille.
                LayoutInflater inflater = LayoutInflater.from(context);
                View item = inflater.inflate(R.layout.item_grid_0, gridLayout, false);
                // si la case correspond à un mur ou à un bloc, on modifie son apparence
                switch (grid[i][j]) {
                    case CASE_WALL:
                        item.setBackgroundResource(R.drawable.item_wall);
                        break;
                    case CASE_BLOC:
                        item.setBackgroundResource(R.drawable.item_bloc);
                        break;
                }
                // on ajoute la vue ...
                gridLayout.addView(item);

                // ... puis on la modifie à sa taille souhaitée
                // (les cellules d'un GridLayout ne sont pas nécessairement de la même taille et peuvent être nulles).
                GridLayout.LayoutParams params = (GridLayout.LayoutParams) item.getLayoutParams();
                params.height = itemSize;
                params.width = itemSize;
                item.setLayoutParams(params);
            }
        }

        // on créer la vue de chaque joueur nécessaires selon la difficulté de la partie ...
        player.setViewPosition(gridLayout.getX(), gridLayout.getY());
        bots.get(0).setViewPosition(gridLayout.getX() + (gridColumns - 1) * itemSize, gridLayout.getY() + (gridRows - 1) * itemSize);
        if (difficulty >= 1) {
            bots.get(1).setViewPosition(gridLayout.getX() + (gridColumns - 1) * itemSize, gridLayout.getY());
            if (difficulty >= 2)
                bots.get(2).setViewPosition(gridLayout.getX(), gridLayout.getY() + (gridRows - 1) * itemSize);
        }

        // ... puis on les ajoute au conteneur ...
        objectsContainer.addView(player.getView());
        objectsContainer.addView(bots.get(0).getView());
        if (difficulty >= 1) {
            objectsContainer.addView(bots.get(1).getView());
            if (difficulty >= 2)
                objectsContainer.addView(bots.get(2).getView());
        }

        // ... en modifiant leur taille.
        player.setView(itemSize);
        bots.get(0).setView(itemSize);
        if (difficulty >= 1) {
            bots.get(1).setView(itemSize);
            if (difficulty >= 2)
                bots.get(2).setView(itemSize);
        }

    }

    /**
     * Création d'une grille aléatoire.
     */
    private void generateGrid() {
        grid = new int[gridRows][gridColumns];

        Random randomGenerator = new Random();

        for (int i = 0; i < gridRows; i++)
            for (int j = 0; j < gridColumns; j++) {
                if (isWall(i, j))
                    grid[i][j] = CASE_WALL;
                // une chance sur 10 qu'une case soit vide.
                else if (isPlayerSpace(i, j) || randomGenerator.nextInt(100) % 10 == 0)
                    grid[i][j] = CASE_EMPTY;
                else
                    grid[i][j] = CASE_BLOC;
            }
    }

    /**
     * Vérifie si un joueur est toujours sur la bombe afin de la rendre infranchissable sinon.
     */
    private void checkBombStatus() {
        for (Bomb bomb : getAllBombs()) {
            // si une bombe n'est pas encore "posée".
            if(bomb.getBombStatus() == Bomb.PLAYER_STILL_ON_BOMB) {
                boolean changeBombStatus = true;
                // vérification pour chaque joueur ...
                for (Player p : getAllPlayers()) {
                    float playerYCenter = p.getY() + p.getSize() / 2,
                            playerXCenter = p.getX() + p.getSize() / 2,
                            bombYCenter = bomb.getY() + bomb.getSize() / 2,
                            bombXCenter = bomb.getX() + bomb.getSize() / 2;

                    // si il est éloigné d'une case de la bombe.
                    if (Math.abs(playerYCenter - bombYCenter) < bomb.getSize() / 2
                            && (Math.abs(playerXCenter - bombXCenter) < bomb.getSize() / 2)) {
                        changeBombStatus = false;

                        break;
                    }
                }

                if (changeBombStatus) {
                    bomb.setBombStatus(Bomb.POSED);
                    grid[bomb.getYGrid()][bomb.getXGrid()] = Bomb.POSED;
                }
            }
        }
    }

    /**
     * Détermine si une case est un mur ou non.
     * @param i index des lignes.
     * @param j index des colonnes.
     * @return vrai si les indexes sont impairs.
     */
    private boolean isWall(int i, int j) {
        return i % 2 != 0
                && j % 2 != 0;
    }

    /**
     * Détermine s'il s'agit de l'espace de départ d'un joueur.
     * @param i index des lignes.
     * @param j index des colonnes.
     * @return vrai si les 5 cases les plus proches de lui doivent être vides.
     */
    private boolean isPlayerSpace(int i, int j) {
        return ((j == 0 || j == gridColumns - 1)
                && (i <= 2 || i >= gridRows - 3))

                || ((i == 0 || i == gridRows - 1)
                && (j <= 2 || j >= gridColumns - 3));
    }

    /**
     * Mise à jour des informations utilies à l'intelligence artificielle.
     * La synchronisation est utile car la même IA est utilisée par plusieurs threads.
     */
    public synchronized void updateAIInformations() {
        ai.updateBlocksCases(grid.clone(), getAllBombs());
    }

    /**
     * Déplace un joueur vers la gauche.
     * @param p joueur à déplacer.
     * @param distance distance de déplacement.
     */
    public void moveLeft(Player p, float distance) {
        // calcul de la position en x et y du joueur après un déplacement.
        float x = p.getX() + distance;
        float y = p.getY();

        // la valeur de x ne peut pas être inférieure à 0.
        if (x < 0)
            x = 0;
        else {
            // index du joueur dans la grille.
            float indexX = x / p.getSize();
            float indexY = y / p.getSize();

            int roundX = (int) Math.floor(indexX);
            int roundY = Math.round(indexY);

            // case sur la gauche du joueur (celle où il souhaite se déplacer).
            View caseOnLeft = gridLayout.getChildAt(roundX + gridColumns * roundY);

            // si le joueur ne peut pas se déplacer sur cette case
            if (grid[roundY][roundX] != CASE_EMPTY
                    && grid[roundY][roundX] != CASE_PU_FASTER
                    && grid[roundY][roundX] != CASE_PU_ADD_BOMB
                    && grid[roundY][roundX] != CASE_PU_POWER
                    && grid[roundY][roundX] != CASE_PU_P_BOMB
                    && grid[roundY][roundX] != Bomb.PLAYER_STILL_ON_BOMB)
                x = p.getX();//caseOnLeft.getX() + caseOnLeft.getWidth();
            // sinon, on ajuste la valeur en y du joueur pour être certain qu'il soit aligné avec la case à sa gauche.
            else {
                // la distance parcourue reste toujours la même quoiqu'il arrive (il peut se déplacer en x et y simultanément ...
                // ... si la distance à parcourir est inférieure à la différence en y.
                float diffY = caseOnLeft.getY() - y;
                if (diffY != 0) {
                    if (Math.abs(diffY) > Math.abs(distance)) {
                        if (diffY > 0) {
                            y -= distance;
                            x -= distance;
                        } else {
                            y += distance;
                            x -= distance;
                        }
                    } else {
                        y = caseOnLeft.getY();
                        x -= (distance + Math.abs(diffY));
                    }
                }
            }

            // le joueur prend le powerup sur la case, s'il y en a un.
            if (grid[roundY][roundX] == CASE_PU_FASTER
                    || grid[roundY][roundX] == CASE_PU_ADD_BOMB
                    || grid[roundY][roundX] == CASE_PU_POWER
                    || grid[roundY][roundX] == CASE_PU_P_BOMB)
                takePowerUp(p, roundX, roundY);
        }

        // animation de la vue avec une durée d'animation de 0ms car, malgré ce que l'on pourrait penser, un temps d'animation créer des sacades dans le déplacements.
        animateView(p.getView(), x, y, 0);
    }

    /**
     * Le comportement de cette méthode est semblable à celui de de moveLeft().
     * @param p joueur à déplacer.
     * @param distance distance de déplacement.
     */
    public void moveRight(Player p, float distance) {
        float x = p.getX() + distance;
        float y = p.getY();

        if (x >= gridLayout.getWidth() - p.getSize())
            x = gridLayout.getWidth() - p.getSize();
        else {
            float indexX = x / p.getSize();
            float indexY = y / p.getSize();

            int roundX = (int) Math.floor(indexX);
            int roundY = Math.round(indexY);

            View caseOnRight = gridLayout.getChildAt(roundX + 1 + gridColumns * roundY);

            if (grid[roundY][roundX + 1] != CASE_EMPTY
                    && grid[roundY][roundX + 1] != CASE_PU_FASTER
                    && grid[roundY][roundX + 1] != CASE_PU_ADD_BOMB
                    && grid[roundY][roundX + 1] != CASE_PU_POWER
                    && grid[roundY][roundX + 1] != CASE_PU_P_BOMB
                    && grid[roundY][roundX + 1] != Bomb.PLAYER_STILL_ON_BOMB)
                x = p.getX();//caseOnRight.getX() - caseOnRight.getWidth();
            else {
                float diffY = caseOnRight.getY() - y;
                if (diffY != 0) {
                    if (Math.abs(diffY) > Math.abs(distance)) {
                        if (diffY > 0) {
                            y += distance;
                            x -= distance;
                        } else {
                            y -= distance;
                            x -= distance;
                        }
                    } else {
                        y = caseOnRight.getY();
                        x -= (distance - Math.abs(diffY));
                    }
                }
            }

            if (grid[roundY][roundX + 1] == CASE_PU_FASTER
                    || grid[roundY][roundX + 1] == CASE_PU_ADD_BOMB
                    || grid[roundY][roundX + 1] == CASE_PU_POWER
                    || grid[roundY][roundX + 1] == CASE_PU_P_BOMB)
                takePowerUp(p, roundX + 1, roundY);
        }

        animateView(p.getView(), x, y, 0);
    }

    /**
     * Le comportement de cette méthode est semblable à celui de de moveLeft().
     * @param p joueur à déplacer.
     * @param distance distance de déplacement.
     */
    public void moveTop(Player p, float distance) {
        float y = p.getY() + distance,
                x = p.getX();

        if (y < 0)
            y = 0;
        else {
            float indexX = x / p.getSize();
            float indexY = y / p.getSize();

            int roundX = Math.round(indexX);
            int roundY = (int) Math.floor(indexY);

            View caseOnTop = gridLayout.getChildAt(roundX + gridColumns * roundY);

            if (grid[roundY][roundX] != CASE_EMPTY
                    && grid[roundY][roundX] != CASE_PU_FASTER
                    && grid[roundY][roundX] != CASE_PU_ADD_BOMB
                    && grid[roundY][roundX] != CASE_PU_POWER
                    && grid[roundY][roundX] != CASE_PU_P_BOMB
                    && grid[roundY][roundX] != Bomb.PLAYER_STILL_ON_BOMB)
                y = p.getY();//caseOnTop.getY() + caseOnTop.getHeight();
            else {
                float diffX = caseOnTop.getX() - x;
                if (diffX != 0) {
                    if (Math.abs(diffX) > Math.abs(distance)) {
                        if (diffX > 0) {
                            y -= distance;
                            x -= distance;
                        } else {
                            y -= distance;
                            x += distance;
                        }
                    } else {
                        x = caseOnTop.getX();
                        y -= (distance + Math.abs(diffX));
                    }
                }
            }

            if (grid[roundY][roundX] == CASE_PU_FASTER
                    || grid[roundY][roundX] == CASE_PU_ADD_BOMB
                    || grid[roundY][roundX] == CASE_PU_POWER
                    || grid[roundY][roundX] == CASE_PU_P_BOMB)
                takePowerUp(p, roundX, roundY);
        }

        animateView(p.getView(), x, y, 0);
    }

    /**
     * Le comportement de cette méthode est semblable à celui de de moveLeft().
     * @param p joueur à déplacer.
     * @param distance distance de déplacement.
     */
    public void moveBottom(Player p, float distance) {
        float y = p.getY() + distance,
                x = p.getX();

        if (y >= gridLayout.getHeight() - p.getSize())
            y = gridLayout.getHeight() - p.getSize();
        else {
            float indexX = x / p.getSize();
            float indexY = y / p.getSize();

            int roundX = Math.round(indexX);
            int roundY = (int) Math.floor(indexY);

            View caseOnBottom = gridLayout.getChildAt(roundX + gridColumns * (roundY + 1));

            if (grid[roundY + 1][roundX] != CASE_EMPTY
                    && grid[roundY + 1][roundX] != CASE_PU_FASTER
                    && grid[roundY + 1][roundX] != CASE_PU_ADD_BOMB
                    && grid[roundY + 1][roundX] != CASE_PU_POWER
                    && grid[roundY + 1][roundX] != CASE_PU_P_BOMB
                    && grid[roundY + 1][roundX] != Bomb.PLAYER_STILL_ON_BOMB)
                y = p.getY();//caseOnBottom.getY() - caseOnBottom.getHeight();
            else {
                float diffX = caseOnBottom.getX() - x;
                if (diffX != 0) {
                    if (Math.abs(diffX) > Math.abs(distance)) {
                        if (diffX > 0) {
                            y -= distance;
                            x += distance;
                        } else {
                            y -= distance;
                            x -= distance;
                        }
                    } else {
                        x = caseOnBottom.getX();
                        y -= (distance - Math.abs(diffX));
                    }
                }
            }

            if (grid[roundY + 1][roundX] == CASE_PU_FASTER
                    || grid[roundY + 1][roundX] == CASE_PU_ADD_BOMB
                    || grid[roundY + 1][roundX] == CASE_PU_POWER
                    || grid[roundY + 1][roundX] == CASE_PU_P_BOMB)
                takePowerUp(p, roundX, roundY + 1);
        }

        animateView(p.getView(), x, y, 0);
    }

    /**
     * Déplace une vue.
     * @param view vue à déplacer.
     * @param x déplacement en x.
     * @param y déplacement en y.
     * @param duration durée de l'animation.
     */
    private void animateView(View view, float x, float y, int duration) {
        view.animate()
                .x(x)
                .y(y)
                .setDuration(duration)
                .start();
    }

    /**
     * Prise d'un powerup.
     * @param p joueur prenant le powerup.
     * @param x position en x.
     * @param y position en y.
     */
    private void takePowerUp(Player p, int x, int y) {
        // modification des attributs du joueur selon le powerup récupéré.
        switch (grid[y][x]) {
            case CASE_PU_FASTER:
                p.increaseSpeed();
                break;
            case CASE_PU_ADD_BOMB:
                p.incrementBombsCapacity();
                break;
            case CASE_PU_POWER:
                p.incrementBombsPower();
                break;
            case CASE_PU_P_BOMB:
                p.setHasPBomb();
                break;
        }

        // suppression du powerup sur la grille.
        removePowerUp(x, y);
    }

    /**
     * Pose d'une bombe sur la grille.
     * @param p joueur posant une bombe.
     */
    public void poseBomb(Player p) {
        // il est préférable de vérifier que le joueur est bien dans la partie avant la pose d'une bombe.
        if (p.isPlayerAlive()) {
            // si le joueur est un robot, on remet à faux la pose d'une bombe.
            if (p instanceof Bot)
                ((Bot) p).getDirection().setPoseBomb(false);

            // un joueur ne peut poser une bombe que si sa capacité le lui permet.
            if (p.getBombsCapacity() > p.getBombs().size()) {

                // ajout de la bombe selon les coordonnées en x et y du joueur.
                int roundX = p.getRoundX();
                int roundY = p.getRoundY();
                if (grid[roundY][roundX] == CASE_EMPTY) {
                    boolean isPBomb = p.hasPBomb() && p.isPBombAvailable();
                    Bomb bomb = new Bomb(context, roundX * p.getSize(), roundY * p.getSize(), p.getSize(), p.getBombsPower(), isPBomb);

                    grid[roundY][roundX] = bomb.getBombStatus();

                    objectsContainer.addView(bomb.getV(), 0);
                    bomb.resizeView();

                    p.getBombs().add(bomb);
                }
            }

        }
    }

    /**
     * Trouve une bombe sur la grille selon ses coordonnées.
     * @param x position de la bombe en x.
     * @param y position de la bombe en y.
     * @return retourne la position de la bombe dans la liste des bombes.
     */
    private int findBombAtXY(int x, int y) {
        Bomb bomb;
        for (int i = 0; i < getAllBombs().size(); i++) {
            bomb = getAllBombs().get(i);
            if (bomb.getXGrid() == x && bomb.getYGrid() == y)
                return i;
        }
        return -1;
    }

    /**
     * Vérifie si des bombes doivent exploser.
     */
    public void checkBombs() {
        boolean vibrate = false;
        for (int i = 0; i < getAllBombs().size(); i++) {
            Bomb bomb = getAllBombs().get(i);

            // si une bombe est sur le plateau depuis plus de 3 secondes, elle explose.
            if (getAllBombs().get(i).getSeconds() > 3) {
                int power = removeBomb(i, bomb.getXGrid(), bomb.getYGrid(), CASE_EMPTY);

                performExplosion(bomb.getXGrid(),
                        bomb.getYGrid(), SIDE_NONE, power);

                vibrate = true;
            }
            // si une bombe est recouverte lors de la réduction de la taille de la grille en fin de partie, elle est simplement supprimée.
            else if ((grid[bomb.getYGrid()][bomb.getXGrid()] == CASE_LAST_SECONDS_WALL))
                removeBomb(i, bomb.getXGrid(), bomb.getYGrid(), CASE_LAST_SECONDS_WALL);
        }

        // vibration du téléphpne selon les préférences de l'utilisateur.
        if (vibrate && PreferenceManager.getDefaultSharedPreferences(context).getBoolean(context.getResources().getString(R.string.vibrate), true)) {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(50);
        }

        checkBombStatus();
    }

    /**
     * Stoppe le compte à rebours des bombes.
     */
    public void stopBombs() {
        for (Bomb bomb : getAllBombs()) {
            bomb.stopTimer();
        }
    }

    /**
     * Reprend le compte à rebour des bombes.
     */
    public void resumeBombs() {
        for (Bomb bomb : getAllBombs()) {
            bomb.resumeTimer();
        }
    }

    /**
     * Supprime une bombe de la grille.
     * @param index index de la bombe dans la liste des bombes.
     * @param x index en x sur la grille.
     * @param y index en y sur la grille.
     * @param value valeur de la case de la grille après avoir supprimé la bombe.
     * @return retourne la puissance de la bombe supprimée.
     */
    private int removeBomb(int index, int x, int y, int value) {
        Player p = player;
        Bomb bomb = getAllBombs().get(index);

        for (Bot bot : bots)
            if (bot.getBombs().contains(bomb))
                p = bot;

        int power = bomb.getPower();
        grid[y][x] = value;
        objectsContainer.removeView(bomb.getV());
        p.getBombs().remove(bomb);
        return power;
    }

    /**
     * Supprime un bloc de la grille.
     * @param x index en x sur la grille.
     * @param y index en y sur la grille.
     */
    private void removeBloc(int x, int y) {
        double rnd = Math.random();
        // chances d'avoir tel powerup sur la case explosée.
        if (rnd < 0.08) {
            grid[y][x] = CASE_PU_FASTER;
            gridLayout.getChildAt(x + gridColumns * y).setBackgroundResource(R.drawable.ic_pu_faster);
        } else if (rnd < 0.16) {
            grid[y][x] = CASE_PU_ADD_BOMB;
            gridLayout.getChildAt(x + gridColumns * y).setBackgroundResource(R.drawable.ic_pu_add_bomb);
        } else if (rnd < 0.24) {
            grid[y][x] = CASE_PU_POWER;
            gridLayout.getChildAt(x + gridColumns * y).setBackgroundResource(R.drawable.ic_pu_power);
        } else if (rnd < 0.27) {
            grid[y][x] = CASE_PU_P_BOMB;
            gridLayout.getChildAt(x + gridColumns * y).setBackgroundResource(R.drawable.ic_pu_p_bomb);
        } else {
            grid[y][x] = 0;
            gridLayout.getChildAt(x + gridColumns * y).setBackgroundResource(R.drawable.item_empty);
        }
    }

    /**
     * Suppression d'un powerup sur la grille.
     * @param x index en x.
     * @param y index en y.
     */
    private void removePowerUp(int x, int y) {
        grid[y][x] = 0;
        gridLayout.getChildAt(x + gridColumns * y).setBackgroundResource(R.drawable.item_empty);
    }

    /**
     * Changement d'un case de la grille.
     * @param x index en x sur la grille.
     * @param y index en y sur la grille.
     * @param value valeur de la case.
     * @param res vue de la case.
     */
    public void changeCase(int x, int y, int value, int res) {
        grid[y][x] = value;
        gridLayout.getChildAt(x + gridColumns * y).setBackgroundResource(res);

        if (value == CASE_BLOC || value == CASE_LAST_SECONDS_WALL || value == CASE_WALL) {
            checkBombs();
            killPlayers(x, y);
        }
    }

    /**
     * Animation montrant l'explosion sur la grille.
     * @param view vue de la case de la grille à animer.
     */
    private void showExplosion(View view) {
        view.setBackgroundResource(R.drawable.explosion_transition);
        TransitionDrawable transition = (TransitionDrawable) view.getBackground();
        transition.startTransition(500);
    }

    /**
     * Fait exploser une bombe.
     * @param xGrid index de la bombe en x sur la grille.
     * @param yGrid index de la bombe en y sur la grille.
     * @param excludedSide côté que l'explosion ne doit pas explorer.
     * @param power puissance de la bombe.
     */
    private void performExplosion(int xGrid, int yGrid, int excludedSide, int power) {
        if (xGrid >= 0 && xGrid < gridColumns && yGrid >= 0 && yGrid < gridRows)
            if (grid[yGrid][xGrid] == CASE_EMPTY) {

                // animation de l'explosion.
                showExplosion(gridLayout.getChildAt(xGrid + gridColumns * yGrid));

                // les éventuels joueurs présents sur la case meurt.
                killPlayers(xGrid, yGrid);

                // propagation de l'explosion.
                if (excludedSide != SIDE_LEFT)
                    spreadExplosion(xGrid + MOVE_LEFT_OR_TOP,
                            yGrid,
                            MOVE_LEFT_OR_TOP,
                            NO_MOVE,
                            SIDE_RIGHT,
                            power);
                if (excludedSide != SIDE_TOP)
                    spreadExplosion(xGrid,
                            yGrid + MOVE_LEFT_OR_TOP,
                            NO_MOVE,
                            MOVE_LEFT_OR_TOP,
                            SIDE_BOTTOM,
                            power);
                if (excludedSide != SIDE_RIGHT)
                    spreadExplosion(xGrid + MOVE_RIGHT_OR_BOTTOM,
                            yGrid,
                            MOVE_RIGHT_OR_BOTTOM,
                            NO_MOVE,
                            SIDE_LEFT,
                            power);
                if (excludedSide != SIDE_BOTTOM)
                    spreadExplosion(xGrid,
                            yGrid + MOVE_RIGHT_OR_BOTTOM,
                            NO_MOVE,
                            MOVE_RIGHT_OR_BOTTOM,
                            SIDE_TOP,
                            power);
            }
    }

    /**
     * Propage l'explosion d'une bombe.
     * @param xGrid index en x sur la grille.
     * @param yGrid index en y sur la grille.
     * @param moveX sens sur l'axe x (gauche ou droite).
     * @param moveY sens sur l'axe y (haut ou bas).
     * @param from côté d'où provient l'explosion.
     * @param depth profondeur de l'explosion restant à parcourir.
     */
    private void spreadExplosion(int xGrid, int yGrid, int moveX, int moveY, int from, int depth) {
        // si l'on atteint pas les limites de la grille ou de la puissance de l'explosion.
        if (depth > 0 && xGrid >= 0 && xGrid < gridColumns && yGrid >= 0 && yGrid < gridRows)
            switch (grid[yGrid][xGrid]) {
                // si la case est vide on propage l'explosion dans le même sens.
                case CASE_EMPTY:
                    showExplosion(gridLayout.getChildAt(xGrid + gridColumns * yGrid));

                    killPlayers(xGrid, yGrid);

                    spreadExplosion(xGrid + moveX, yGrid + moveY, moveX, moveY, from, depth - 1);
                    break;
                // si la case contient un powerup, on le retire.
                case CASE_PU_FASTER:
                case CASE_PU_ADD_BOMB:
                case CASE_PU_POWER:
                case CASE_PU_P_BOMB:
                    removePowerUp(xGrid, yGrid);
                    break;
                // si la case contient un bloc, on l'enlève.
                case CASE_BLOC:
                    removeBloc(xGrid, yGrid);
                    break;
                // si l'explosion rencontre une autre bombe, elle explose à son tour en excluant le côté d'où provient l'explosion précédente.
                case Bomb.PLAYER_STILL_ON_BOMB:
                case Bomb.POSED:
                    int index = findBombAtXY(xGrid, yGrid);
                    if (index >= 0) {
                        int power = removeBomb(index, xGrid, yGrid, CASE_EMPTY);
                        performExplosion(xGrid, yGrid, from, power);
                    }
                    break;
            }
    }

    /**
     * Tue un joueur.
     * @param x index du joueur en x sur la grille.
     * @param y index du joueur en y sur la grille.
     */
    private void killPlayers(int x, int y) {
        for (Player player : getAllPlayers())
            if (player.isPlayerAlive() && player.getRoundX() == x && player.getRoundY() == y) {
                replacePowerUps(player);

                player.killPlayer();
                objectsContainer.removeView(player.getView());
            }
    }

    /**
     * Replace les powerups d'un joueur mort.
     * @param player joueur mort.
     */
    private void replacePowerUps(Player player) {
        // recherche de toutes les cases vides de la grille.
        ArrayList<Point> emptyCases = new ArrayList<>();
        for (int i = 0; i < grid.length; i++)
            for (int j = 0; j < grid[i].length; j++)
                if (grid[i][j] == CASE_EMPTY)
                    emptyCases.add(new Point(j, i));

        // replace les powerups du joeur sur les cases vides
        Random rnd = new Random();
        if (player.hasPBomb()) {
            int rndInt = rnd.nextInt(emptyCases.size());

            // vérification de la valeur aléatoire trouvée (vérifie la taille de la liste des cases vides finalement) pour s'assurer que la case est disponible.
            // en effetn lorsque la grille se réduit, il peut ne plus y avoir assez de place pour placer les powerups.
            if(rndInt >= 0) {
                Point c = emptyCases.get(rndInt);
                grid[c.y][c.x] = CASE_PU_P_BOMB;
                gridLayout.getChildAt(c.x + gridColumns * c.y).setBackgroundResource(R.drawable.ic_pu_p_bomb);
                emptyCases.remove(rndInt);
            }
        }

        for (int i = 1; i < player.getBombsCapacity(); i++) {
            int rndInt = rnd.nextInt(emptyCases.size());
            if(rndInt >= 0) {
                Point c = emptyCases.get(rndInt);
                grid[c.y][c.x] = CASE_PU_ADD_BOMB;
                gridLayout.getChildAt(c.x + gridColumns * c.y).setBackgroundResource(R.drawable.ic_pu_add_bomb);
                emptyCases.remove(rndInt);
            }
        }

        for (int i = 1; i < player.getBombsPower(); i++) {
            int rndInt = rnd.nextInt(emptyCases.size());
            if(rndInt >= 0) {
                Point c = emptyCases.get(rndInt);
                grid[c.y][c.x] = CASE_PU_POWER;
                gridLayout.getChildAt(c.x + gridColumns * c.y).setBackgroundResource(R.drawable.ic_pu_power);
                emptyCases.remove(rndInt);
            }
        }

        for (int i = 0; i < player.getAditionnalSpeed(); i++) {
            int rndInt = rnd.nextInt(emptyCases.size());
            if(rndInt >= 0) {
                Point c = emptyCases.get(rndInt);
                grid[c.y][c.x] = CASE_PU_FASTER;
                gridLayout.getChildAt(c.x + gridColumns * c.y).setBackgroundResource(R.drawable.ic_pu_faster);
                emptyCases.remove(rndInt);
            }
        }
    }

    private ArrayList<Bomb> getAllBombs() {
        ArrayList<Bomb> bombs = new ArrayList<>();
        bombs.addAll(player.getBombs());
        for (Bot bot : bots)
            bombs.addAll(bot.getBombs());
        return bombs;
    }

    public ArrayList<Player> getAllPlayers() {
        ArrayList<Player> players = new ArrayList<>();
        players.add(player);
        players.addAll(bots);
        return players;
    }

    public Player getPlayer() {
        return player;
    }

    public View getPlayerView() {
        return player.getView();
    }

    public ArrayList<Bot> getBots() {
        return bots;
    }

    public AI getAi() {
        return ai;
    }

    public int getGridColumns() {
        return gridColumns;
    }

    public int getGridRows() {
        return gridRows;
    }

    public int[][] getGrid() {
        return grid;
    }
}
