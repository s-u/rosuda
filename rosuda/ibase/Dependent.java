import java.util.Vector;

/**
   Simple dependency interface.
   Any class that implements this interface can be notified upon changes.
   @version $Id$
*/
interface Dependent {
    /**
     * This method will be called when an even occured.
     * Currently {@link SMarker} and {@link Axis} use this method of notification.
     * There's no generic class for implemention the notification-list yet.
     *
     * @param src Object that sent the notification. The actual content is implementation-dependent.
     * @param path This parameter is <code>null<code> for non-cascaded notify - in that case further calls to NotifyAll are not allowed. Otherwise it contains a Vector with all objects notified so far during cascaded notify. To aviod cyclic notifications every instance must either reject cascaded notifications (i.e. no calls to NotifyAll at all) or check for occurence of itself in the chain before calling NotifyAll.
     * The only valid recursive calls in Notifying are NotifyAll(path) and NotifyAll(..,path), because only these two pass the "path" parameter to avoid cyclic loops. */
    public void Notifying(NotifyMsg msg, Object src, Vector path);
};
