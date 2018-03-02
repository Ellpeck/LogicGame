package de.ellpeck.logicgame.util;

public enum Direction{
    NONE(0, 0),

    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    public static final Direction[] DIRECTIONS = values();
    public static final Direction[] ADJACENT = new Direction[]{UP, RIGHT, DOWN, LEFT};
    public static final Direction[] ADJACENT_INCLUDING_NONE = new Direction[]{NONE, UP, RIGHT, DOWN, LEFT};
    public final int x;
    public final int y;

    Direction(int x, int y){
        this.x = x;
        this.y = y;
    }

    public Direction getOpposite(){
        switch(this){
            case UP:
                return DOWN;
            case DOWN:
                return UP;
            case LEFT:
                return RIGHT;
            case RIGHT:
                return LEFT;
            default:
                return NONE;
        }
    }
}