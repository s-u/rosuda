import java.awt.*;               // ScrollPane, PopupMenu, MenuShortcut, etc.
import java.awt.event.*;         // New event model.


class DataEvent extends AWTEvent {
  public DataEvent(DragBox b) {
    super( b, DATA_EVENT );
  }
  public static final int DATA_EVENT = AWTEvent.RESERVED_ID_MAX + 3;
}
