package de.ellpeck.logicgame.renderer;

import com.google.common.base.Charsets;
import org.lwjgl.opengl.GL20;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public final class AssetLoader{

    public static final List<IDisposable> DISPOSABLE_ASSETS = new ArrayList<>();

    public static ShaderProgram loadShaderProgram(String vertexPath, String fragmentPath){
        Shader vertex = loadShader(vertexPath, GL20.GL_VERTEX_SHADER);
        Shader fragment = loadShader(fragmentPath, GL20.GL_FRAGMENT_SHADER);

        ShaderProgram shader = new ShaderProgram(vertex, fragment);
        addResource(shader);

        vertex.dispose();
        fragment.dispose();

        return shader;
    }

    public static Texture loadTexture(String path){
        try{
            Texture texture = new Texture(getResource(path));
            addResource(texture);
            return texture;
        }
        catch(Exception e){
            throw new RuntimeException("Couldn't load texture at "+path, e);
        }
    }

    private static Shader loadShader(String path, int type){
        String shader = "";

        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(getResource(path), Charsets.UTF_8));
            while(true){
                String line = reader.readLine();
                if(line != null){
                    shader += line+"\n";
                }
                else{
                    break;
                }
            }
        }
        catch(Exception e){
            throw new RuntimeException("Couldn't load shader at "+path, e);
        }

        return new Shader(type, shader);
    }

    private static String makeFullPath(String path){
        return "/assets/logicgame"+path;
    }

    private static InputStream getResource(String path){
        return AssetLoader.class.getResourceAsStream(makeFullPath(path));
    }

    private static void addResource(Object resource){
        if(resource instanceof IDisposable){
            DISPOSABLE_ASSETS.add((IDisposable)resource);
        }
    }

    public static void dispose(){
        for(IDisposable disposable : DISPOSABLE_ASSETS){
            disposable.dispose();
        }
    }
}
