/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package physics;

import game.GameObject;
import graphics.ThreeDGraphicsManager;
import graphics.VectorGraphic;
import java.util.ArrayList;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Quaternion;
import org.lwjgl.util.vector.Vector3f;
import util.Utilities;
import static util.Utilities.*;

/**
 *
 * @author Andy
 */
public abstract class PhysicalEntity extends GameObject implements update.Updateable, Boundable {

    private BoundingBox orientedBounds;
    private Vector3f velocity;
    private Vector3f angularVelocity;
    private Vector3f acceleration;
    private Vector3f angularAcceleration;
    private ArrayList<ForceGenerator> forceGenerators;
    private Vector3f forceBuffer;
    private Vector3f torqueBuffer;
    private float invMass;
    private float mass;
    private Matrix3f inertiaTensor;
    private Matrix3f invInertiaTensor;
    private float linearDrag = 0.99f;
    private float angularDrag = .95f;
    private boolean awake;
    private float minSpeedSquared = 0.001f;
    private float vAvg;
    private static Vector3f zero = new Vector3f();
        VectorGraphic vg;

    public PhysicalEntity(BoundingBox bounds) {
        this(bounds, 1f);
    }

    public PhysicalEntity(BoundingBox bounds, float mass) {
        this(bounds, mass, getRectangularPrismInertiaTensor(bounds, mass));
    }

    public PhysicalEntity(BoundingBox bounds, float mass, Matrix3f inertiaTensor) {
        this.orientedBounds = bounds;
        velocity = new Vector3f();
        angularVelocity = new Vector3f();
        acceleration = new Vector3f();
        angularAcceleration = new Vector3f();
        forceGenerators = new ArrayList<ForceGenerator>();
        forceBuffer = new Vector3f();
        torqueBuffer = new Vector3f();
        invMass = 1f / mass;
        this.mass = mass;
        this.inertiaTensor = inertiaTensor;
        invInertiaTensor = (Matrix3f) inertiaTensor.invert();
        vAvg = 100;
        awake = true;
        vg = new VectorGraphic(new Vector3f(), new Vector3f());
        ThreeDGraphicsManager.getInstance().addGraphic3D(vg, 100);
    }

    @Override
    public BoundingBox getBounds() {
        return orientedBounds;
    }

    public abstract void collide(ArrayList<Plane> collisions);

    public abstract void collide(PhysicalEntity collision);

    public void updateForces() {
        for (ForceGenerator f : forceGenerators) {
            f.applyForce(this);
        }
    }

    public synchronized void integrate(float delta) {
        // Calculate linear acceleration from force inputs.
        acceleration = (Vector3f) forceBuffer.scale(invMass);
        // Calculate angular acceleration from torque inputs.
        angularAcceleration = Matrix3f.transform(invInertiaTensor, torqueBuffer, null);
        // Adjust velocities
        // Update linear velocity from both acceleration and impulse.
        velocity = addScaledVector(velocity, acceleration, delta);
        // Update angular velocity from both acceleration and impulse.
        angularVelocity = addScaledVector(angularVelocity, angularAcceleration, delta);
        // Impose drag.
        velocity.scale((float) Math.pow(linearDrag, delta * 100));
        angularVelocity.scale((float) Math.pow(angularDrag, delta * 100));
        // Adjust positions
        // Update linear position.
        orientedBounds.setPosition(addScaledVector(orientedBounds.getPosition(), velocity, delta));
        // Update angular position.
        orientedBounds.setOrientation(addScaledVector(orientedBounds.getOrientation(), angularVelocity, -delta).normalise(null));
        // Normalize the orientation, and update the matrices with the new
        // position and orientation.

        // Clear accumulators.
        forceBuffer.set(zero);
        torqueBuffer.set(zero);

        vAvg += velocity.lengthSquared() + angularVelocity.lengthSquared();
        vAvg *= 0.7;
    }

    public void addForceGenerator(ForceGenerator fg) {
        forceGenerators.add(fg);
    }

    public synchronized void applyForce(Vector3f force) {
        Vector3f.add(forceBuffer, force, forceBuffer);
    }

    public synchronized void applyForceAtPoint(Vector3f force, Vector3f application) {
        Vector3f.add(forceBuffer, force, forceBuffer);
        Vector3f.add(torqueBuffer, Vector3f.cross(Vector3f.sub(application, orientedBounds.getPosition(), null), force, null), torqueBuffer);
    }

    public synchronized void applyTorque(Vector3f torque) {
        Vector3f.add(torqueBuffer, torque, torqueBuffer);
    }

    public synchronized void applyImpulseAtPoint(Vector3f impulse, Vector3f application) {
        velocity = Utilities.addScaledVector(velocity, impulse, invMass);
        Vector3f angularImpulse = Vector3f.cross(Vector3f.sub(application, orientedBounds.getPosition(), null), impulse, null);vg.setPosition(application);
        Vector3f yo = new Vector3f(angularImpulse);
        yo.normalise();
        yo.scale(4f);
        vg.setVector(yo);
        Vector3f deltaAngularVelocity = Matrix3f.transform(invInertiaTensor, angularImpulse, null);        
        Vector3f.add(angularVelocity, deltaAngularVelocity, angularVelocity);
    }
    
    public synchronized Vector3f getVelocity() {
        return velocity;
    }
    
    public synchronized Vector3f getAngularVelocity() {
        return angularVelocity;
    }
    
    public synchronized Vector3f getAcceleration() {
        return acceleration;
    }
    
    public float getInvMass() {
        return invMass;
    }
    
    public float getMass() {
        return mass;
    }
    
    public synchronized boolean canSleep() {
        return vAvg < minSpeedSquared;
    }

    public synchronized void setAwake(boolean awake) {
        this.awake = awake;
        if (awake) {
            vAvg = 100;
        } else {
            velocity.set(zero);
            angularVelocity.set(zero);
            vAvg = 0;
        }
    }

    public synchronized void setVelocity(Vector3f vel) {
        this.velocity = vel;
    }

    public synchronized void setAngularVelocity(Vector3f angVel) {
        this.angularVelocity = angVel;
    }
    
    public synchronized boolean isAwake() {
        return true;
    }

    public synchronized Matrix3f getInertiaTensor() {
        return inertiaTensor;
    }
    
    public synchronized Matrix3f getInvInertiaTensor() {
        return invInertiaTensor;
    }
    
    public static Matrix3f getRectangularPrismInertiaTensor(final BoundingBox b, final float mass) {
        return new Matrix3f() {

            {
                float widthSquare = (float) Math.pow(b.getDimension().getX(), 2);
                float heightSquare = (float) Math.pow(b.getDimension().getY(), 2);
                float depthSquare = (float) Math.pow(b.getDimension().getZ(), 2);
                float mult = mass/12f;
                m00 = mult * (heightSquare * depthSquare);
                m11 = mult * (widthSquare * depthSquare);
                m22 = mult * (heightSquare * widthSquare);
            }
        };
    }
    
    public synchronized Vector3f getPosition() {
        return orientedBounds.getPosition();
    }
    
    public synchronized void setPosition(Vector3f r) {
        orientedBounds.setPosition(r);
    }

    public synchronized Quaternion getOrientation() {
        return getBounds().getOrientation();
    }

    public synchronized void setOrientation(Quaternion orientation) {
        orientedBounds.setOrientation(orientation);
    }
    
    public synchronized void setLinearDrag(float drag) {
        this.linearDrag = drag;
    }
    
    public synchronized void setAngularDrag(float drag) {
        this.angularDrag = drag;
    }

}
