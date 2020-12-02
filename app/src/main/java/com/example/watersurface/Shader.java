package com.example.watersurface;

import java.nio.FloatBuffer;
import android.opengl.GLES20;

public class Shader {
    private int program_Handle;

    public Shader(String vertexShaderCode, String fragmentShaderCode){
        createProgram(vertexShaderCode, fragmentShaderCode);
    }
    private void createProgram(String vertexShaderCode, String fragmentShaderCode){
        int vertexShader_Handle =
                GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
        GLES20.glShaderSource(vertexShader_Handle, vertexShaderCode);
        GLES20.glCompileShader(vertexShader_Handle);
        int fragmentShader_Handle =
                GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
        GLES20.glShaderSource(fragmentShader_Handle, fragmentShaderCode);
        GLES20.glCompileShader(fragmentShader_Handle);
        program_Handle = GLES20.glCreateProgram();
        GLES20.glAttachShader(program_Handle, vertexShader_Handle);
        GLES20.glAttachShader(program_Handle, fragmentShader_Handle);
        GLES20.glLinkProgram(program_Handle);
    }

    

    public void linkNormalBuffer(FloatBuffer normalBuffer){
        GLES20.glUseProgram(program_Handle);
        int a_normal_Handle = GLES20.glGetAttribLocation(program_Handle, "a_normal");
        GLES20.glEnableVertexAttribArray(a_normal_Handle);
        GLES20.glVertexAttribPointer(
                a_normal_Handle, 3, GLES20.GL_FLOAT, false, 0,normalBuffer);
    }


    public void linkModelViewProjectionMatrix(float [] modelViewProjectionMatrix){
        GLES20.glUseProgram(program_Handle);
        int u_modelViewProjectionMatrix_Handle =
                GLES20.glGetUniformLocation(program_Handle, "u_modelViewProjectionMatrix");
        GLES20.glUniformMatrix4fv(
                u_modelViewProjectionMatrix_Handle, 1, false, modelViewProjectionMatrix, 0);
    }

 

    public void linkCamera (float xCamera, float yCamera, float zCamera){
        GLES20.glUseProgram(program_Handle);
        int u_camera_Handle=GLES20.glGetUniformLocation(program_Handle, "u_camera");
        GLES20.glUniform3f(u_camera_Handle, xCamera, yCamera, zCamera);
    }


    public void linkLightSource (float xLightPosition, float yLightPosition, float zLightPosition){
        GLES20.glUseProgram(program_Handle);
        int u_lightPosition_Handle=GLES20.glGetUniformLocation(program_Handle, "u_lightPosition");
        GLES20.glUniform3f(u_lightPosition_Handle, xLightPosition, yLightPosition, zLightPosition);
    }


    public void linkTexture(Texture texture0,Texture texture1){
        GLES20.glUseProgram(program_Handle);
        if (texture0 != null){
            int u_texture0_Handle =
                    GLES20.glGetUniformLocation(program_Handle, "u_texture0");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture0.getName());
            GLES20.glUniform1i(u_texture0_Handle, 0);
        }
        if (texture1 != null){
            int u_texture1_Handle =
                    GLES20.glGetUniformLocation(program_Handle, "u_texture1");
            GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, texture1.getName());
            GLES20.glUniform1i(u_texture1_Handle, 1);
        }
    }



    public void useProgram(){
        GLES20.glUseProgram(program_Handle);
    }
}


