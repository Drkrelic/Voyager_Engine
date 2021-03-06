/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import game.GameObject;
import org.lwjgl.util.vector.Vector2f;
import static org.lwjgl.opengl.GL11.*;
import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.TextureImpl;

/**
 *
 * @author Andy
 */
public class TestGraphic extends GameObject implements graphics.TwoD {

    Vector2f[] points;
    resource.TextureResource t;
    //Color c;
    static resource.TextureResource[] textures;

    public TestGraphic() {
        points = new Vector2f[3];
        for (int i = 0; i < points.length; i++) {
            points[i] = getRandomPoint();
        }
        //c = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
        if (textures == null) {
            int size = 3;
            textures = new resource.TextureResource[size];
            for (int i = 0; i < size; i++) {
                textures[i] = resource.TextureManager.getInstance().loadTextureResource("" + i + ".png");
            }
        }
        t = textures[(int) (Math.random() * textures.length)];
        //System.out.println("Name " + t.getName());
    }
    
    @Override
    public void render() {
        Color.white.bind();
        t.bind();
        glBegin(GL_TRIANGLES);
        //c.bind();
        for (int i = 0; i < points.length; i += 3) {
            glTexCoord2f(0, 0);
            glVertex2f(points[i].getX(), points[i].getY());
            glTexCoord2f(1, 0);
            glVertex2f(points[i + 1].getX(), points[i + 1].getY());
            glTexCoord2f(0, 1);
            glVertex2f(points[i + 2].getX(), points[i + 2].getY());
        }
        glEnd();

    }

    private Vector2f getRandomPoint() {
        return new Vector2f((float) Math.random() * 640, (float) Math.random() * 480);
    }
}
