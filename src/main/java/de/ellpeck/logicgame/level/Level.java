package de.ellpeck.logicgame.level;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.ellpeck.logicgame.Registry;
import de.ellpeck.logicgame.level.tile.LogicTile;
import de.ellpeck.logicgame.level.tile.LogicTileConstructor;
import de.ellpeck.logicgame.render.LevelRenderer;

import java.util.HashMap;
import java.util.Map;

public class Level{

    private final LogicTile[][] tiles;
    private final Table<Integer, Integer, IOInfo> ioInfos = HashBasedTable.create();
    public final String name;
    public final String description;
    public final int width;
    public final int height;
    public final LevelRenderer renderer;

    public Level(String name, String description, int width, int height){
        this.name = name;
        this.description = description;
        this.tiles = new LogicTile[width][height];
        this.width = width;
        this.height = height;
        this.renderer = new LevelRenderer(this);
    }

    public LogicTile getTile(int x, int y){
        return this.isInBounds(x, y) ? this.tiles[x][y] : null;
    }

    public boolean setTile(int x, int y, LogicTileConstructor constructor){
        if(this.isInBounds(x, y)){
            this.tiles[x][y] = constructor.makeTile(this, x, y);
            return true;
        }
        else{
            return false;
        }
    }

    public boolean isInBounds(int x, int y){
        return x >= 0 && y >= 0 && x < this.width && y < this.height;
    }

    public void update(){

    }

    public IOInfo getIOInfo(int x, int y){
        return this.ioInfos.get(x, y);
    }

    public static Level parse(JsonElement element){
        String name = null;
        try{
            JsonObject object = element.getAsJsonObject();
            name = object.get("name").getAsString();
            String desc = object.get("description").getAsString();

            Map<Character, LogicTileConstructor> tileMap = new HashMap<>();
            JsonObject tiles = object.get("tiles").getAsJsonObject();
            for(Map.Entry<String, JsonElement> entry : tiles.entrySet()){
                LogicTileConstructor constructor = Registry.TILE_REGISTRY.get(entry.getValue().getAsString());
                tileMap.put(entry.getKey().charAt(0), constructor);
            }

            Level level = null;

            JsonArray layout = object.get("layout").getAsJsonArray();
            for(int y = 0; y < layout.size(); y++){
                String rowStrg = layout.get(y).getAsString();

                if(level == null){
                    level = new Level(name, desc, rowStrg.length(), layout.size());
                }

                char[] chars = rowStrg.toCharArray();
                for(int x = 0; x < chars.length; x++){
                    if(chars[x] != ' '){
                        level.setTile(x, y, tileMap.get(chars[x]));
                    }
                }
            }

            JsonObject inputs = object.get("inputs").getAsJsonObject();
            parseIO(level, inputs, true);

            JsonObject outputs = object.get("outputs").getAsJsonObject();
            parseIO(level, outputs, false);

            return level;
        }
        catch(Exception e){
            throw new RuntimeException("Couldn't parse level with name "+name, e);
        }
    }

    private static void parseIO(Level level, JsonObject object, boolean isInput){
        for(Map.Entry<String, JsonElement> entry : object.entrySet()){
            JsonArray pos = entry.getValue().getAsJsonArray();
            int x = pos.get(0).getAsInt();
            int y = pos.get(1).getAsInt();

            level.ioInfos.put(x, y, new IOInfo(x, y, entry.getKey(), isInput));
        }
    }
}
