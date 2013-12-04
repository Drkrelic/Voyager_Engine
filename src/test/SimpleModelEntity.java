/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package test;

import java.util.ArrayList;
import org.lwjgl.input.Keyboard;
import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import resource.WavefrontModel;
import util.Utilities;

/**
 *
 * @author Andy
 */
public class SimpleModelEntity extends physics.DynamicEntity implements graphics.ThreeD {

    graphics.ThreeDModel m;

    public SimpleModelEntity(graphics.ThreeDModel m) {
        super(m.getBounds());
        this.m = m;
    }

    public static SimpleModelEntity simpleModelEntityFromPath(String prefix, String path) {
        return simpleModelEntityFromWavefront(new WavefrontModel(prefix, path));
    }

    public static SimpleModelEntity simpleModelEntityFromPath(String mpath) {
        return simpleModelEntityFromWavefront(new WavefrontModel(mpath));
    }

    private static SimpleModelEntity simpleModelEntityFromWavefront(WavefrontModel w) {
        w.create();
        while (!w.isLoaded()) {
            Thread.yield();
        }
        graphics.ThreeDModel model = new graphics.ThreeDModel(w);
        model.create();
        while (!model.isProcessed()) {
            Thread.yield();
        }
        return new SimpleModelEntity(model);
    }
    @Override
    public void render() {
        Vector3f pos = getPosition();
        Quaternion orientation = getOrientation();
        float angle = (float) (Math.acos(orientation.getW()) * 2 * 180 / Math.PI);
        glPushMatrix();
        glTranslatef(pos.getX(), pos.getY(), pos.getZ());
        glRotatef(-angle, orientation.getX(), orientation.getY(), orientation.getZ());
        m.render();
        glPopMatrix();
    }
}
