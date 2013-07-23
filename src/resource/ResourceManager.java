/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package resource;

import game.Game;
import game.GameObject;
import game.StandardManager;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import util.DebugMessages;

/**
 *
 * @author Andy
 */
public class ResourceManager extends StandardManager implements Runnable {

    LinkedList<Resource> queued;
    HashMap<String, Resource> loaded;
    private Thread resourceThread;
    static ResourceManager instance;

    @Override
    public void create() {
        super.create();
        queued = new LinkedList<Resource>();
        loaded = new HashMap<String, Resource>();
        resourceThread = new Thread(this);
        resourceThread.setPriority(Thread.NORM_PRIORITY - 1);
        DebugMessages.getInstance().write("ResourceManager created");
    }

    public static ResourceManager getInstance() {
        if (instance == null) {
            instance = new ResourceManager();
        }
        return instance;
    }

    @Override
    public void destroy() {
        super.destroy();
        queued.clear();
        loaded.clear();
        DebugMessages.getInstance().write("ResourceManager destroyed");
    }

    private boolean loadResource(Resource r) {
        if (r.load()) {
            loaded.put(r.getFullName(), r);
            r.setIsLoaded(true);
            DebugMessages.getInstance().write("Loading succeeded: " + r.getFullName());
            return true;
        } else {
            DebugMessages.getInstance().write("Loading failed: " + r.getFullName());
            return false;
        }
    }

    @Override
    public String getFullName() {
        return "ResourceManager";
    }

    @Override
    public boolean add(GameObject obj) {
        if (obj instanceof Resource) {
            queued.add((Resource) obj);
            hackyUpdate();
            return true;
        }
        return false;
    }

    @Override
    public void remove(GameObject obj) {
        if (loaded.containsKey(obj.getFullName())) {
            loaded.remove(obj.getFullName());
        }
    }

    @Override
    public void run() {
        while (Game.isRunning()) {
            /*while (isLoading()) {
                loadResource(queued.pop());
            }*/
            Thread.yield();
        }
    }

    public void hackyUpdate() {
        while (isLoading()) {
            loadResource(queued.pop());
        }
    }

    public boolean isLoading() {
        return !queued.isEmpty();
    }

    public void start() {
        resourceThread.start();
    }
}