
/** messages passed with NotifyAllDep
    @version $Id$ */
public class NotifyMsg {
    Object source;
    int    messageID;

    public NotifyMsg(Object src, int msgid) {
        source=src; messageID=msgid;
    };

    public NotifyMsg(Object src) {
        this(src,0);
    }

    public Object getSource() {
        return source;
    }

    public int getMessageID() {
        return messageID;
    }

    public String toString() {
        return "NotifyMsg["+messageID+"]from["+source+"]";
    }
}