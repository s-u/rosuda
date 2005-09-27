import java.awt.*;               // ScrollPane, PopupMenu, MenuShortcut, etc.
import java.awt.event.*;         // New event model.


class SelectionEvent extends AWTEvent {

  public SelectionEvent(DragBox s) {

    super( s, SELECTION_EVENT );

  }

  public static final int SELECTION_EVENT = AWTEvent.RESERVED_ID_MAX + 1;

}

