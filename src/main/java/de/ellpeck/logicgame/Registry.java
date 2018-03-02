package de.ellpeck.logicgame;

import de.ellpeck.logicgame.level.tile.LogicTileConstructor;
import de.ellpeck.logicgame.level.tile.TileWire;
import de.ellpeck.logicgame.render.AssetLoader;
import de.ellpeck.logicgame.render.TextureSheet;
import de.ellpeck.logicgame.util.Colors;

import java.util.HashMap;
import java.util.Map;

public final class Registry{

    public static final Map<String, LogicTileConstructor> TILE_REGISTRY = new HashMap<>();
    public static TextureSheet tileSheet;

    public static void init(){
        tileSheet = new TextureSheet(AssetLoader.loadTexture("/textures/tile_sheet.png"), 16, 16);
        TILE_REGISTRY.put("blue_wire", (level, x, y) -> new TileWire(level, x, y, Colors.BLUE));
    }
}
