package de.ellpeck.logicgame.renderer;

import de.ellpeck.logicgame.util.Colors;

public class VertexProcessor{

    public void addTexturedRegion(Renderer renderer, Texture texture, float x, float y, float x2, float y2, float x3, float y3, float x4, float y4, float u, float v, float u2, float v2, int topLeft, int bottomLeft, int bottomRight, int topRight){
        renderer.addTriangle(x, y, x2, y2, x3, y3, topLeft, bottomLeft, bottomRight, u, v, u, v2, u2, v2);
        renderer.addTriangle(x, y, x3, y3, x4, y4, topLeft, bottomRight, topRight, u, v, u2, v2, u2, v);
    }

    public void addTriangle(Renderer renderer, float x1, float y1, float x2, float y2, float x3, float y3, int color1, int color2, int color3, float u1, float v1, float u2, float v2, float u3, float v3){
        renderer.addVertex(x1, y1, color1, u1, v1);
        renderer.addVertex(x2, y2, color2, u2, v2);
        renderer.addVertex(x3, y3, color3, u3, v3);
    }

    public void addVertex(Renderer renderer, float x, float y, int color, float u, float v){
        renderer.put(x).put(y)
                .put(Colors.getR(color)).put(Colors.getG(color)).put(Colors.getB(color)).put(Colors.getA(color))
                .put(u).put(v);
    }

    public void onVertexCompleted(Renderer renderer){

    }

    public void onBegin(Renderer renderer){

    }

    public void onEnd(Renderer renderer){

    }

    public void onFlush(Renderer renderer){

    }
}
