package de.ellpeck.logicgame.render.engine;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public class Shader implements IDisposable{

    private final int id;
    private final int type;

    public Shader(int type, String source){
        this.id = GL20.glCreateShader(type);
        this.type = type;

        GL20.glShaderSource(this.id, source);
        GL20.glCompileShader(this.id);

        if(GL20.glGetShaderi(this.id, GL20.GL_COMPILE_STATUS) != GL11.GL_TRUE){
            throw new RuntimeException("Couldn't compile shader:\n"+GL20.glGetShaderInfoLog(this.id));
        }
    }

    public int getId(){
        return this.id;
    }

    public int getType(){
        return this.type;
    }

    @Override
    public void dispose(){
        GL20.glDeleteShader(this.id);
    }
}
