package de.ellpeck.logicgame.level.tile;

import de.ellpeck.logicgame.level.Level;

public interface LogicTileConstructor<T extends LogicTile>{

    T makeTile(Level level, int x, int y);

}
