package com.app.charles.bomberman.game;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.app.charles.bomberman.R;

import java.util.ArrayList;

/**
 * Classe réprésentant le joueur d'une partie.
 */

@SuppressWarnings({"WeakerAccess", "FieldCanBeLocal"})
public class Player {

    // identifiant.
    private int id;

    // statut.
    public static final int ALIVE = 0;
    public static final int DEAD = 1;

    private int playerStatus;

    private Context context;

    // attributs d'un joueur.
    private ArrayList<Bomb> bombs;
    private int bombsCapacity;
    private int bombsPower;
    private int speed;
    private int aditionnalSpeed;
    private boolean hasPBomb;

    // vue et taille d'un joueur
    private View view;
    private int size;

    @SuppressWarnings("InflateParams")
    public Player(Context context, int player) {
        this.context = context;

        playerStatus = ALIVE;

        bombs = new ArrayList<>();
        bombsCapacity = 1;
        bombsPower = 1;
        speed = 0;
        aditionnalSpeed = 0;
        hasPBomb = false;

        this.id = player;

        // création de la vue du joueur selon son id.
        LayoutInflater inflater = LayoutInflater.from(this.context);
        switch (player) {
            case 1:
                view = inflater.inflate(R.layout.player1, null, false);
                break;
            case 2:
                view = inflater.inflate(R.layout.player2, null, false);
                break;
            case 3:
                view = inflater.inflate(R.layout.player3, null, false);
                break;
            default:
                view = inflater.inflate(R.layout.player4, null, false);
                break;
        }
    }

    /**
     * Modification de la taille de la vue d'un joueur.
     * @param size taille souhaitée du joueur.
     */
    public void setView(int size) {
        this.size = size;

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
        params.height = size;
        params.width = size;
        view.setLayoutParams(params);
    }

    public View getView() {
        return view;
    }

    public ArrayList<Bomb> getBombs() {
        return bombs;
    }

    public void setViewPosition(float x, float y) {
        setX(x);
        setY(y);
    }

    public void setX(float x) {
        view.setX(x);
    }

    public void setY(float y) {
        view.setY(y);
    }

    public float getX() {
        return view.getX();
    }

    public float getY() {
        return view.getY();
    }

    public int getRoundX() {
        return Math.round(getX() / getSize());
    }

    public int getRoundY() {
        return Math.round(getY() / getSize());
    }

    public int getSize() {
        return size;
    }

    public int getBombsCapacity() {
        return bombsCapacity;
    }

    public void incrementBombsCapacity() {
        bombsCapacity++;
    }

    public int getBombsPower() {
        return bombsPower;
    }

    public int getSpeed() {
        return speed + aditionnalSpeed;
    }

    public int getAditionnalSpeed() {
        return aditionnalSpeed / 2;
    }

    public void increaseSpeed() {
        aditionnalSpeed = aditionnalSpeed + 2;
    }

    public void incrementBombsPower() {
        bombsPower++;
    }

    public boolean isPlayerAlive() {
        return playerStatus == ALIVE;
    }

    public void killPlayer() {
        this.playerStatus = DEAD;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setHasPBomb() {
        hasPBomb = true;
    }

    public boolean hasPBomb() {
        return hasPBomb;
    }

    public boolean isPBombAvailable() {
        for (Bomb bomb : getBombs())
            if (bomb.isPBomb())
                return false;

        return true;
    }
}
