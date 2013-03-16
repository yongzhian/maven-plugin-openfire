package ${groupId}.openfire.plugin.timer.handler;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.QName;
import org.jivesoftware.openfire.IQHandlerInfo;
import org.jivesoftware.openfire.auth.UnauthorizedException;
import org.jivesoftware.openfire.handler.IQHandler;
import org.jivesoftware.util.JiveGlobals;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;

/**
 * IQ Timer Handler
 * 
 * @author yanricheng@163.com
 * 
 */
public class IQTimerHandler extends IQHandler {

    protected static final String QUERY = "query";

    protected static final String XMLNS = "http://www.xxxx.com.cn/protocol/timer";

    protected static final String SERVYOU_DATE_FORMAT_KEY = "date.format";

    protected static final String SERVYOU_DATE_FORMAT_VALUE = "yyyy-MM-dd HH:mm:ss";

    protected IQHandlerInfo info;

    /**
     * default constructor
     */
    public IQTimerHandler() {
        super("servyou timer handler");
        info = new IQHandlerInfo(QUERY, XMLNS);
    }

    @Override
    public IQ handleIQ(IQ packet) throws UnauthorizedException {
        IQ response = IQ.createResultIQ(packet);
        response.setTo(packet.getFrom());
        response.setType(Type.result);
        Element query = DocumentHelper.createElement(QName.get(QUERY, XMLNS));
        query.addElement("time").setText(getCurrentDateString());
        response.setChildElement(query);
        return response;
    }

    @Override
    public IQHandlerInfo getInfo() {
        return info;
    }

    private static String getCurrentDateString() {
        Date currentTime = new Date();
        String datefromat = JiveGlobals.getProperty(SERVYOU_DATE_FORMAT_KEY, SERVYOU_DATE_FORMAT_VALUE);
        SimpleDateFormat formatter = new SimpleDateFormat(datefromat);
        String dateString = formatter.format(currentTime);
        return dateString;
    }

}
