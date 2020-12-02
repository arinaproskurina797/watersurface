package com.example.watersurface;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


class P{
    P(){
        z=0;
        vz=0;
    }
    public float x,y,z,vz;
};

class MyGL20Renderer implements GLSurfaceView.Renderer {
    private Shader mShader;
    Texture mTexture0;
    private float xСamera, yCamera, zCamera;
    private float xLightPosition, yLightPosition, zLightPosition;
    private float[] modelMatrix;
    private float[] viewMatrix;
    private float[] modelViewMatrix;
    private float[] projectionMatrix;
    private float[] modelViewProjectionMatrix;
    private Context context;

    final int N=80;
    float K=0.06f;
    float DT=0.1f;
    int offs=0;
    public P [][] p;
    float time;
    float [] a, n;
    FloatBuffer f, fn;
    ByteBuffer b, bn;

    float sqr(float x){
        return x*x;
    }

    void NioBuff(){
        b=ByteBuffer.allocateDirect(2*2*3*N*N*4);
        b.order(ByteOrder.nativeOrder());
        f=b.asFloatBuffer();
        f.put(a);
        f.position(0);
    }

    void Init1(){
        time=0;
        for (int i = 0; i<N; i++){
            for (int j = 0; j<N; j++){
                p[i][j]=new P();
                (p[i][j]).x=1.0f*j/N;
                (p[i][j]).y=1.0f*i/N;
                (p[i][j]).z=0;
                (p[i][j]).vz=0;
            }
        }
    }

    void display(){
        offs=0;
        for (int i=0;i<N;i++) {
            for (int j = 0; j < N-1; j++) {
                a[N*i*3*2+j*3*2+0]=(float)j/N;
                a[N*i*3*2+j*3*2+1]=(float)i/N;
                a[N*i*3*2+j*3*2+2]=(p[i][j]).z;
                a[N*i*3*2+j*3*2+3]=(float)(j+1)/N;
                a[N*i*3*2+j*3*2+4]=(float)(i)/N;
                a[N*i*3*2+j*3*2+5]=(p[i][j+1]).z;
                offs+=6;
            }
        }
        for (int i=0;i<N-1;i++) {
            for (int j = 0; j < N; j++) {
                a[offs+N*i*3*2+j*3*2+0]=1.0f*j/N;
                a[offs+N*i*3*2+j*3*2+1]=1.0f*i/N;
                a[offs+N*i*3*2+j*3*2+2]=1.0f*(p[i][j]).z;
                a[offs+N*i*3*2+j*3*2+3]=1.0f*(j)/N;
            }
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        Init1();
                GLES20.glViewport(0, 0, width, height);
        float ratio = (float) width / height;
        float k=K;
        float left = -k*ratio;
        float right = k*ratio;
        float bottom = -k;
        float top = k;
        float near = 0.1f;
        float far = 20.0f;
        Matrix.frustumM(projectionMatrix, 0, left, right, bottom, top, near, far);
        Matrix.multiplyMM(
                modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0);
    }

    void Push1(){
        if (Math.random()*500>10){return;}
        int x0=(int)(Math.random()*N/2+1);
        int y0=(int)(Math.random()*N/2+1);
        for (int y = y0-5; y<y0+5; y++){
            if ((y<1)||(y>=N-1)) continue;
            for (int x = x0-5; x<x0+5; x++) {
                if ((x<1)||(x>=N-1)) continue;
                p[x][y].z = 10.0f / N - (float) (Math.sqrt(sqr(y - y0) + sqr(x - x0)) * 1.0 / N);
            }
        }
    }

    void MyTimer(){
        final int []dx={-1,0,1,0};
        final int []dy={0,1,0,-1};
        Push1();
        for (int y=1;y<N-1;++y){
            for (int x=1;x<N-1;++x){
                P p0=p[x][y];
                for (int i=0;i<4;++i){
                    P p1=p[x+dx[i]][y+dy[i]];
                    float d=(float)Math.sqrt(sqr(p0.x-p1.x)+sqr(p0.y-p1.y) +sqr(p0.z-p1.z));
                    p0.vz+=K*(p1.z-p0.z)/d*DT;
                    //p0.vz*=0.99f;
                }
            }
        }
        for (int y=1;y<N-1;++y)
            for (int x=1;x<N-1;++x){
                P p0=p[x][y];
                p0.z+=p0.vz;
            }
        display();
    }

    private final float[] scalerMatrix = new float[16];
    private final float[] mRotationMatrixY = new float[16];
    private final float[] mRotationMatrixX = new float[16];
    private final float[] mTempMatrix = new float[16];
    private final float[] projView = new float[16];

    public MyGL20Renderer(Context context) {
        this.context=context;
        xLightPosition=5f;
        yLightPosition=30f;
        zLightPosition=5f;

        a= new float [12*N*N];
        n= new float [12*N*N];
        p=new P[N][N];

        //матрицы
        modelMatrix=new float[16];
        viewMatrix=new float[16];
        modelViewMatrix=new float[16];
        projectionMatrix=new float[16];
        modelViewProjectionMatrix=new float[16];

        Matrix.setIdentityM(modelMatrix, 0);
        xСamera=0f;
        yCamera=0f;
        zCamera=1f;

        Init1();
        MyTimer();
        NioBuff();
    }

    

    public void onDrawFrame(GL10 unused) {

        Matrix.setLookAtM(
                viewMatrix, 0, xСamera, yCamera, zCamera, 0, 0, 0, 0, 1, 0);
        Matrix.multiplyMM(projView, 0, projectionMatrix, 0, viewMatrix, 0);
        Matrix.setRotateM(mRotationMatrixX,  0,  0.6f,  0,  1.0f,  0f);
        Matrix.setRotateM(mRotationMatrixY, 0, 0.6f, 1.0f, 0, 0);
        Matrix.multiplyMM(mTempMatrix,  0,  projView, 0, mRotationMatrixX,  0);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, mTempMatrix, 0, mRotationMatrixY, 0);
        Matrix.setIdentityM(scalerMatrix, 0);
        Matrix.scaleM(scalerMatrix, 0, 1f, 1f, 0f);
        Matrix.multiplyMM(mTempMatrix, 0, modelViewProjectionMatrix, 0, scalerMatrix, 0);
        Matrix.setIdentityM(mTempMatrix, 0);
        Matrix.translateM(mTempMatrix, 0, -0.5f, -0.5f, 0f);
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, modelViewProjectionMatrix, 0, mTempMatrix, 0);

        mShader.linkModelViewProjectionMatrix(modelViewProjectionMatrix);
        mShader.linkCamera(xСamera, yCamera, zCamera);
        mShader.linkLightSource(xLightPosition, yLightPosition, zLightPosition);


        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);
        GLES20.glDrawArrays(GL10.GL_LINES,0,4*N*N);
        MyTimer();
        f.put(a);
        f.position(0);


    }
}