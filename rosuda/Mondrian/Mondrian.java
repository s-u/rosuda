import java.util.Vector;         //

public class Mondrian
{

  protected Vector dataSets = new Vector(5,5);

  /** A very simple main() method for our program. */
  public static void main(String[] args) { new Mondrian(); }

  public Mondrian() {
    new Join(dataSets, false, false);
  }
}

