package de.ellpeck.logicgame.renderer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

public class VertexArrayObject implements IDisposable{

    private static int boundVAO;

    private final int id;

    public VertexArrayObject(){
        this.id = GL30.glGenVertexArrays();
    }
    
    public void bind(){
        if(boundVAO != this.id){
            GL30.glBindVertexArray(this.id);
            boundVAO = this.id;
        }
    }
    
    public void draw(int amount){
        this.bind();
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, amount);
    }
    
    public void unbind(){
        if(boundVAO == this.id){
            unbindAll();
        }
    }

    public static void unbindAll(){
        GL30.glBindVertexArray(0);
        boundVAO = -1;
    }

    public int getId(){
        return this.id;
    }

    public void dispose(){
        this.unbind();
        GL30.glDeleteVertexArrays(this.id);
    }
}