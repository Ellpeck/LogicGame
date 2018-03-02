package de.ellpeck.logicgame;

import de.ellpeck.logicgame.level.tile.LogicTileConstructor;
import de.ellpeck.logicgame.level.tile.TileNone;
import de.ellpeck.logicgame.level.tile.TileWall;
import de.ellpeck.logicgame.level.tile.TileWire;
import de.ellpeck.logicgame.util.Colors;

import java.util.HashMap;
import java.util.Map;

public final class Registry{

    public static final Map<String, LogicTileConstructor> TILE_REGISTRY = new HashMap<>();

    public static void init(){
        TILE_REGISTRY.put("none", TileNone::new);
        TILE_REGISTRY.put("wall", TileWall::new);
        TILE_REGISTRY.put("blue_wire", (level, x, y) -> new TileWire(level, x, y, Colors.BLUE));
    }
}
