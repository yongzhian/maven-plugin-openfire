package ${groupId}.openfire.plugin.timer;

import java.io.File;

import org.jivesoftware.openfire.IQRouter;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.handler.IQHandler;

import ${groupId}.openfire.plugin.timer.handler.IQTimerHandler;

/**
 * Get System Time
 * 
 * @author yanricheng@163.com
 * 
 */
public class TimerPlugin implements Plugin {
    IQHandler timerHandler = null;

    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        timerHandler = new IQTimerHandler();
        XMPPServer server = XMPPServer.getInstance();
        IQRouter iqRouter = server.getIQRouter();
        iqRouter.addHandler(timerHandler);
    }

    public void destroyPlugin() {
        XMPPServer server = XMPPServer.getInstance();
        IQRouter iqRouter = server.getIQRouter();
        iqRouter.removeHandler(timerHandler);
        timerHandler = null;
    }

}
