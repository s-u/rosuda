import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.awt.*;
import java.awt.event.*;

/** Funktionsweise:
 	readFile() liest Datei in den Speicher als Buffer
 	dieser wird dann nach Vorarbeiten (= Formatanalyse, Fehleranalyse, etc.) in einzelne items zerlegt.
 	In item[][] werden 2 Arten von Information gespeichert:
 		a) wenn Spalte numerisch ist, der dazugehörige numerische Wert als double
 		b) wenn Spalte alphanumerisch ist, die Verweise auf word[][][]
 	In word[][][] werden die einzelnen Wörter der alphanumerischen Spalten gespeichert und das pro Wort
 	genau einmal. In s_word[][][] (nicht implementiert als Objektattribut) die dazugehörige sortierte Liste.
 	In wordCount[][] wird die absolute Häufigkeit eines Elements gespeichert. Dabei gibt der erste Index
 	die Spalte und der zweite Index den Ort des Elements an, welches sich befinden kann in:
 		a) word[][][], wenn Spalte alphanumerisch ist
 		b) discretValue[][], wenn Spalte numerisch ist.
 	isDiscret[] gibt an, ob eine Spalte diskret ist oder nicht. Alphanumerische Spalte sind automatisch diskret.
 	Bei numerischen Spalten sind diejenigen Spalten diskret, die weniger als discretLimit verschiedene Elemente haben, d.h.
 	ab discretLimit wird die Spalte bereits als nichtdiskret angesehen.
 	wordStackSize[] gibt den Limit für existierende Wörter in word[][][] bzw. in discretValue[][] an, 
 	je nachdem ob Spalte numerisch diskret oder alphanumerisch ist.
 	In head[][] wird die Kopfzeile gespeichert. Achtung: Kopf MUSS existieren.
 	numericalColumn[] gibt an, ob eine Spalte numerisch ist oder nicht.
 	NA[][] gibt an, ob ein Element ein Missing ist.
 	softFehler und maximal 1 hardFehler werden in error[] gespeichert.
 	errorposition merkt sich die Position des harten Fehlers im Buffer. Dieser kann dann mit Hilfe von
 	findRegion() eine Umgebung des Fehlers ausgeben.
 	isPolygonAvailable gibt an, ob ein Polygon im Datensatz existiert.
 	TimeStamps[] gibt die Zeitabstände an, mit denen ein ActionEvent für eine ProgressBar ausgelöst wird.
 	
 	getItem() gibt ein Element der DataMatrix als Objekt aus. Dieses muss danach konvertiert werden in
 		a) char[], wenn es sich um ein Wort handelt
 		b) double[] und dann Zugriff auf das erste Element, wenn es eine Zahl ist.
 	checkIt() analysiert, ob die Wörter des Datensatzes auch richtig gespeichert wurden.
 		-> wird später noch auf Zahlen fortgesetzt.
*/


public class BufferTokenizer extends Component {

	final byte TAB = (byte) '\t';
	final byte SPACE = (byte) ' ';
	final byte NEWLINE = (byte) '\n';
	final byte RETURN = (byte) '\r';
	final byte DOT = (byte) '.';
	final byte MINUS = (byte) '-';
	final byte QUOTE = (byte) '"';
	final byte KOMMA = (byte) ',';

/** #columns and #lines */
	int columns, lines;
	
	int discretLimit = 0; // ab discretLimit inkl. bereits Behandlung als nicht-diskret
	String format;
	
/** headline (j,k) = (column, letter) */
	char[][] head;

/** items (j,i) = (column, line)
 	if column is numerical: numerical value saved
 	else: saved reference to item in word[][][] */
	double[][] item;

/** words (j,i,k) = (column, line, letter)
 	if column is not numerical: words saved
 	else: null
 	equal words are NOT saved twice
 	NOTE: first word, which is null in a column, indicates the end of a column
 		  -> see wordStackSize[] */
	char[][][] word;
	
/** sorted words: like words, but sorted
 	sorted list is not a class attribute for speed reasons */
	// char[][][] s_word;
	
/** numericalColumn (j) = (column)
 	if true: column is numerical
 	else: column is not numerical */
	boolean[] numericalColumn;
	
/** isDiscret (j) = (column)
 	if true: column is discret
 	else: column is continuous
 	NOTE: not numerical columns are always discret */ 
	boolean[] isDiscret;
	
/** NA (j,i) = (column, line)
 	saves missings (missings = NA or NaN)
 	if true: element (i,j) is a missing one
 	else: not a missing */
	boolean[][] NA;
	
/** wordCount (j,i) = (column, line) referenced to word[][][] or item[][]
 	counts #appearence of a word or numerical discret values in a column
 	NOTE: first element, which is 0 in a column, indicates the end of a column in wordCount 
 		  -> see wordStackSize[] */
	int[][] wordCount;
	
/** wordStackSize (j) = (column)
 	limit position for existing words in word[][][] for a column */
	int[] wordStackSize;
	
/** used for handling discret values in a numerical column */
	double[][] discretValue;
	
// 	i think i do not need it any more
	double[] doubleCover = new double[1];
	
/** default word sepearator in a line */
	byte SEPERATOR = TAB;
	
// 	for a routine
	boolean wordNotFound = false;

	
/** name of filename */
	String file;
	
/** buffer into which the file is loaded */
	ByteBuffer buffer;
	
/** error: String of softerrors, if harderror occurs System exits, harderror is saved in Sring[] error
 	hardReadError: if true: hard reading error occured
 	errorposition: position of hard reading error */
	String[] error = null;
	boolean hardReadError = false;
	int errorposition;
	
	int positionSecondLine = 0;

/** timeStamps: in which time distances new ActionEvent is processed
 	timeStampCounter: reference to timeStamps[]-elements */
	int[] timeStamps = {1000,1000,1000,1000,1000,1000,1000,1000,1000,10000, 10000, 10000, 10000, 10000, 10000, 10000, 10000, 10000, 100000};
	long timestart = 0;
	long timestop = 0;
	int timeStampCounter = 0;


/** isPolygonAvailable: indicates availability of a polygon
	polygonName: name of polygon in DataMatrix */
	boolean isPolygonAvailable = false;
	String polygonName = null;
	
/** sets discret limit for numerical columns */
	public void setDiscretLimit(int i) {
		discretLimit = i;
	}
	
/** returns item (j,i) = (column, line) as Object.
 	parse it to double[] if element is numerical
 	else parse it to char[] */	
	public Object getItem(int j, int i) {

		if (!numericalColumn[j]) {
			// word
			return pointToWord(j,i);
		} else {
			// double
			doubleCover[0] = item[j][i];
			return doubleCover;
		}

	}

	public char[] pointToWord(int j, int i) {

		return word[j][(int)item[j][i]];

	}



/** BufferTokenizer:
 * 
 * @param discretLimit: discretLimit setting. for good speed use small discretLimit
 * @param acceptedErrors: make sure acceptedErrors should be >=1
 */
	public BufferTokenizer(int discretLimit, int acceptedErrors, String file) {
		long start, stop;
		long startfull, stopfull;
		error = new String[acceptedErrors];
		
		timestart = System.currentTimeMillis();
		
		
		// file = "Mappe1.txt";
		// file = "Experiment7.txt";
		// file = "WT-Klausur-NA";
		// file = "GeburtsGewichtNA.txt";
		// file = "Families '95(3).txt";
		// file = "BowlingAloneShorted.txt";
		// file = "access_log";
		// file = "Co89Test.txt";
		
		startfull = System.currentTimeMillis();
		
		start = System.currentTimeMillis();
		try {
			buffer = readFile(file);
		} catch (IOException e) {
			System.err.println(e);
			System.exit(0);
		}
		stop = System.currentTimeMillis();
		System.out.println("Harddrive to RAM: " + (stop - start));
		
		format = analyzeFormat(buffer);
		if(format == "TAB-Format") SEPERATOR = TAB;
		else if(format == "SPACE-Format") SEPERATOR = SPACE;
		else if(format == "KOMMA-Format") SEPERATOR = KOMMA;
		else if(format == "KOMMA-QUOTE-Format") SEPERATOR = KOMMA;
		else if(format == "UNKNOWN-Format") {
			System.out.println(format);
			System.exit(0);
		}
		
		columns = amountColumns(buffer, format);
		
		// polygontest
		isPolygonAvailable = isPolygonAvailableInHead(buffer, format);
		if(isPolygonAvailable) {
			polygonName = getPolygonName(buffer);
			// some errors during polygon seeking routine might happen before real error analysis
			// so here they are handled
			if(hardReadError) {
				System.out.println(error[0]);
				System.exit(0);
			}
		}
		setDiscretLimit(discretLimit);
		
		System.out.println("Format: " + format);

		if(format == "TAB-Format") {
			// Format testen
			start = System.currentTimeMillis();
			error = testUNQUOTEDFormat(buffer, SEPERATOR, acceptedErrors);
			stop = System.currentTimeMillis();
			System.out.println("testTABFormat: " + (stop - start));
			for (int x = 0; x < error.length && error[x] != null; x++) {
				System.out.println("" + (x + 1) + ". " + error[x]);
			}
			if(hardReadError) {
				System.out.println(findRegion(buffer,errorposition));
				System.exit(0);
			}
			
			buffer.rewind();
			
			// Anzahl der Zeilen lesen und Spaltenstruktur erkennen
			start = System.currentTimeMillis();
			numericalColumn = new boolean[columns];
			positionSecondLine = getPositionSecondLine(buffer);
			lines = amountLines(buffer, format);
			if(buffer.get(buffer.limit()-1) == NEWLINE) {
				if(buffer.get(buffer.limit()-2) == RETURN) {
					buffer.limit(buffer.limit()-2);
					lines--;
				} else {
					buffer.limit(buffer.limit()-1);
					lines--;
				}
			} else if(buffer.get(buffer.limit()-1) == RETURN) {
				buffer.limit(buffer.limit()-1);
				lines--;
			}
			stop = System.currentTimeMillis();
			System.out.println("Zeilen lesen: " + (stop - start));
			
			// Daten initialisieren
			start = System.currentTimeMillis();
			isDiscret = new boolean[columns];
			for(int i=0; i<isDiscret.length; i++) isDiscret[i] = true;
			item = new double[columns][lines]; progressing();
			NA = new boolean[columns][lines]; progressing();
			word = new char[columns][discretLimit][]; progressing();
			wordCount = new int[columns][lines]; progressing();
			discretValue = new double[columns][discretLimit]; progressing();
			wordStackSize = new int[columns]; progressing();
			stop = System.currentTimeMillis();
			System.out.println("Initialisierungen: " + (stop - start));
		
			buffer.rewind();
			
			// Daten einlesen und abspeichern
			head = readHead(buffer, format);
			
			start = System.currentTimeMillis();
			buffer.rewind();
			tokenizeUNQUOTEDBuffer(buffer, SEPERATOR);
			stop = System.currentTimeMillis();
			System.out.println("tokenize TABBuffer: " + (stop - start));


		} else if(format == "SPACE-Format") {
			// Format testen und Anzahl der Zeilen lesen
			start = System.currentTimeMillis();
			positionSecondLine = getPositionSecondLine(buffer);
			numericalColumn = new boolean[columns];
			error = testQUOTEDFormat(buffer, SEPERATOR, acceptedErrors);
			stop = System.currentTimeMillis();
			System.out.println("testSPACEFormat: " + (stop - start));
			for (int x = 0; x < error.length && error[x] != null; x++) {
				System.out.println("" + (x + 1) + ". " + error[x]);
			}
			if(hardReadError) {
				System.out.println(findRegion(buffer,errorposition));
				System.exit(0);
			}
			
			if(buffer.get(buffer.limit()-1) == NEWLINE) {
				if(buffer.get(buffer.limit()-2) == RETURN) {
					buffer.limit(buffer.limit()-2);
					lines--;
				} else {
					buffer.limit(buffer.limit()-1);
					lines--;
				}
			} else if(buffer.get(buffer.limit()-1) == RETURN) {
				buffer.limit(buffer.limit()-1);
				lines--;
			}
			
			buffer.rewind();

			// Daten initialisieren
			start = System.currentTimeMillis();
			isDiscret = new boolean[columns];
			for(int i=0; i<isDiscret.length; i++) isDiscret[i] = true;
			item = new double[columns][lines];progressing();
			NA = new boolean[columns][lines];progressing();
			word = new char[columns][discretLimit][];progressing();
			wordCount = new int[columns][lines]; progressing();
			discretValue = new double[columns][discretLimit]; progressing();
			wordStackSize = new int[columns]; progressing();
			stop = System.currentTimeMillis();
			System.out.println("Initialisierungen: " + (stop - start));
		
			buffer.rewind();
			
			// Daten einlesen und abspeichern
			head = readHead(buffer, format);
			
			start = System.currentTimeMillis();
			buffer.rewind();
			tokenizeQUOTEDBuffer(buffer, SEPERATOR);
			stop = System.currentTimeMillis();
			System.out.println("tokenize SPACEBuffer: " + (stop - start));
			
		} else if(format == "KOMMA-Format") {
			// Format testen
			start = System.currentTimeMillis();
			error = testUNQUOTEDFormat(buffer, SEPERATOR, acceptedErrors);
			stop = System.currentTimeMillis();
			System.out.println("testKOMMAFormat: " + (stop - start));
			for (int x = 0; x < error.length && error[x] != null; x++) {
				System.out.println("" + (x + 1) + ". " + error[x]);
			}
			if(hardReadError) {
				System.out.println(findRegion(buffer,errorposition));
				System.exit(0);
			}
			
			buffer.rewind();
			
			// Anzahl der Zeilen lesen und Spaltenstruktur erkennen
			start = System.currentTimeMillis();
			numericalColumn = new boolean[columns];
			positionSecondLine = getPositionSecondLine(buffer);
			lines = amountLines(buffer, format);
			if(buffer.get(buffer.limit()-1) == NEWLINE) {
				if(buffer.get(buffer.limit()-2) == RETURN) {
					buffer.limit(buffer.limit()-2);
					lines--;
				} else {
					buffer.limit(buffer.limit()-1);
					lines--;
				}
			} else if(buffer.get(buffer.limit()-1) == RETURN) {
				buffer.limit(buffer.limit()-1);
				lines--;
			}
			stop = System.currentTimeMillis();
			System.out.println("Zeilen lesen: " + (stop - start));
			
			// Daten initialisieren
			start = System.currentTimeMillis();
			isDiscret = new boolean[columns];
			for(int i=0; i<isDiscret.length; i++) isDiscret[i] = true;
			item = new double[columns][lines];progressing();
			NA = new boolean[columns][lines];progressing();
			word = new char[columns][discretLimit][];progressing();
			wordCount = new int[columns][lines]; progressing();
			discretValue = new double[columns][discretLimit]; progressing();
			wordStackSize = new int[columns]; progressing();
			stop = System.currentTimeMillis();
			System.out.println("Initialisierungen: " + (stop - start));
		
			buffer.rewind();
			
			// Daten einlesen und abspeichern
			head = readHead(buffer, format);
			
			start = System.currentTimeMillis();
			buffer.rewind();
			tokenizeUNQUOTEDBuffer(buffer, SEPERATOR);
			stop = System.currentTimeMillis();
			System.out.println("tokenize KOMMABuffer: " + (stop - start));

		} else if(format == "KOMMA-QUOTE-Format") {
			// Format testen und Anzahl der Zeilen lesen
			start = System.currentTimeMillis();
			positionSecondLine = getPositionSecondLine(buffer);
			numericalColumn = new boolean[columns];
			error = testQUOTEDFormat(buffer, SEPERATOR, acceptedErrors);
			stop = System.currentTimeMillis();
			System.out.println("testKOMMAQUOTEFormat: " + (stop - start));
			for (int x = 0; x < error.length && error[x] != null; x++) {
				System.out.println("" + (x + 1) + ". " + error[x]);
			}
			if(hardReadError) {
				System.out.println(findRegion(buffer,errorposition));
				System.exit(0);
			}
			
			if(buffer.get(buffer.limit()-1) == NEWLINE) {
				if(buffer.get(buffer.limit()-2) == RETURN) {
					buffer.limit(buffer.limit()-2);
					lines--;
				} else {
					buffer.limit(buffer.limit()-1);
					lines--;
				}
			} else if(buffer.get(buffer.limit()-1) == RETURN) {
				buffer.limit(buffer.limit()-1);
				lines--;
			}
			
			buffer.rewind();

			// Daten initialisieren
			start = System.currentTimeMillis();
			isDiscret = new boolean[columns];
			for(int i=0; i<isDiscret.length; i++) isDiscret[i] = true;
			item = new double[columns][lines];progressing();
			NA = new boolean[columns][lines];progressing();
			word = new char[columns][discretLimit][];progressing();
			wordCount = new int[columns][lines]; progressing();
			discretValue = new double[columns][discretLimit]; progressing();
			wordStackSize = new int[columns]; progressing();
			stop = System.currentTimeMillis();
			System.out.println("Initialisierungen: " + (stop - start));
		
			buffer.rewind();
			
			// Daten einlesen und abspeichern
			head = readHead(buffer, format);
			
			start = System.currentTimeMillis();
			buffer.rewind();
			tokenizeQUOTEDBuffer(buffer, SEPERATOR);
			stop = System.currentTimeMillis();
			System.out.println("tokenize KOMMAQUOTEBuffer: " + (stop - start));
			
		
		} else {
			System.err.println(format);
			System.exit(0);
		}
		
		stopfull = System.currentTimeMillis();
		System.out.println("Gesamtzeit: " + (stopfull - startfull));
		
		System.out.print("Total memory used: ");
		System.out.println((Runtime.getRuntime()).totalMemory());


/**		head output **/
/*
		System.out.print("	");
		for(int j=0; j<head.length; j++) {
			System.out.print(head[j]);
			System.out.print("	");
		}
		System.out.println();
*/
		
/** 	items output **/

/*		for(int i=0; i<lines; i++) {
			System.out.print(i); System.out.print("	");
			for(int j=0; j<columns; j++) {
				// System.out.print(item[j][i]); System.out.print("	");
				// System.out.print(NA[j][i]);
				if(numericalColumn[j]) System.out.print(((double[])getItem(j,i))[0]);
				else System.out.print((char[])getItem(j,i));
				System.out.print("	");
			}
			System.out.println();
		}
*/
		
/**		word correctness test 
		
 		start = System.currentTimeMillis();
		if(checkIt(SEPERATOR)) {
			System.out.println("words saved correctly");
		} else {
			System.out.println("words NOT saved correctly");
		}
		stop = System.currentTimeMillis();
		System.out.println("Time for wordcorrectness-test: " + (stop-start));

**/		
/** 	isDiscret[j] output **/ 
/*		for(int j=0; j<columns; j++) {
			System.out.print(isDiscret[j]); System.out.print("	");
		}
		System.out.println();
*/
	}

	public ByteBuffer readFile(String file) throws IOException {
		FileChannel fc = (new FileInputStream(file)).getChannel();
		ByteBuffer buffer = ByteBuffer.allocate((int) fc.size());
		fc.read(buffer);
		fc.close();
		return buffer;
	}

	private boolean isChar(byte b) {

		if ((b >= 65 && b <= 90) || (b >= 97 && b <= 122))
			return true;
		else
			return false;

	}

	private boolean isNumber(byte b) {

		if (b >= 48 && b <= 57)
			return true;
		else
			return false;

	}

	public String analyzeFormat(ByteBuffer buffer) {

		buffer.rewind();
		byte b;
		String format = "UNKNOWN-Format"; // default
		int amountTAB  = 0;
		int amountSPACE = 0;
		int amountKOMMA = 0;
		
		// untersuche Kopfzeile: WICHTIG: diese muss existieren!!!
		
		if(buffer.hasRemaining()) {
			b = buffer.get();
			if(b == QUOTE) {
				while(buffer.hasRemaining()) {
					b = buffer.get();
					if(b == QUOTE) {
						if(buffer.hasRemaining()) {
							b = buffer.get();
							if(b == SPACE || b == RETURN || b == NEWLINE) {
								format = "SPACE-Format";
								break;
							} else if(b == KOMMA) {
								format = "KOMMA-QUOTE-Format";
								break;
							} else {
								format = "UNKNOWN-Format";
								break;
							}
						} else {
							format = "SPACE-Format";
							break;
						}
					} else if(b == TAB) {
						format = "TAB-Format";
						break;
					}
				}
			} else {
				while(buffer.hasRemaining()) {
					b = buffer.get();
					if(b == TAB) {
						format = "TAB-Format";
						break;
					} else if(b == KOMMA) {
						format = "KOMMA-Format";
						break;
					} else if(b == RETURN || b == NEWLINE) {
						format = "TAB-Format"; // für den Fall, dass doch keine Kopfzeile existiert oder nur 1 Spalte
						break;
					}
				}
			}
		}
		
		// untersuche die ersten zwei Zeilen auf das ausgewählte Format:
		
		buffer.rewind();
		for(int i=0; i<2; i++) {
			while(buffer.hasRemaining()) {
				b = buffer.get();
				if(b == TAB) {
					amountTAB++;
				} else if(b == SPACE) {
					if(buffer.hasRemaining()) {
						if(buffer.get(buffer.position()-2) == QUOTE && buffer.get(buffer.position()) == QUOTE) {
							amountSPACE++;						
						}
					}
				} else if(b == KOMMA) {
					if(format == "KOMMA-QUOTE-Format") {
						if(buffer.hasRemaining()) {
							if(buffer.get(buffer.position()-2) == QUOTE && buffer.get(buffer.position()) == QUOTE) {
								amountKOMMA++;
							}
						}
					} else {
						amountKOMMA++;
					}
				} else if(b == RETURN) {
					if(buffer.hasRemaining()) {
						buffer.mark();
						if(buffer.get() == NEWLINE) {
							break;
						} else {
							buffer.reset();
							break;
						}
					}
				} else if(b == NEWLINE) {
					break;
				}
			}
		}
		
		if(format == "TAB-Format") {
			if(amountTAB >= 2*amountSPACE && amountTAB >= 2*amountKOMMA) {
				
			} else {
				format = "UNKNOWN-Format";
			}
		} else if(format == "SPACE-Format") {
			if(amountSPACE >= 2*amountTAB && amountSPACE >= 2*amountKOMMA) {
				
			} else {
				format = "UNKNOWN-Format";
			}
		} else if(format == "KOMMA-Format" || format == "KOMMA-QUOTE-Format") {
			if(amountKOMMA >= 2*amountTAB && amountKOMMA >= 2*amountSPACE) {
				
			} else {
				format = "UNKNOWN-Format";
			}
		}
		
		
		return format;

		// returnable formats are:
		// UNKNOWN-Format (default), TAB-Format, SPACE-Format, KOMMA-Format, KOMMA-QUOTE-Format
	}
	
	
	public char[][] readHead(ByteBuffer buffer, String format) {
		
		buffer.rewind();
		byte b;
		int k = 0;
		int j = 0;
		char[][] head = new char[columns][];
		
		if(format == "TAB-Format" || format == "KOMMA-Format") {
			while (buffer.hasRemaining()) {
				
				k = 0;
				buffer.mark();
				while (buffer.hasRemaining()) {
					b = buffer.get();
					if (b == SEPERATOR) {
						buffer.reset();
						break;
					} else if(b == RETURN || b == NEWLINE) {
						buffer.reset();
						break;
					} else
						k++;
				}
				if (!buffer.hasRemaining())
					buffer.reset();
				head[j] = new char[k];

				
				for (int l = 0; l < k; l++) {
					b = buffer.get();

					head[j][l] = (char) b;
					
				}
				
				if(buffer.hasRemaining()) {
					b = buffer.get();
					if(b == SEPERATOR) {
						j++;
					} else if(b == RETURN || b == NEWLINE) {
						buffer.rewind();
						break;
					}
				}
			}						
		} else if(format == "SPACE-Format" || format == "KOMMA-QUOTE-Format") {
		
		
			
			while (buffer.hasRemaining()) {
				
				k = 0;
				buffer.position(buffer.position()+1);
				
				buffer.mark();
				while (buffer.hasRemaining()) {
					b = buffer.get();
					if (b == QUOTE) {
						buffer.reset();
						break;
					} else k++;
				}

				if (!buffer.hasRemaining())
					buffer.reset();
				head[j] = new char[k];

				
				for (int l = 0; l < k; l++) {
					b = buffer.get();

					head[j][l] = (char) b;
					
				}

				buffer.position(buffer.position()+1);
				if(buffer.hasRemaining()) {
					b = buffer.get();
					if(b == SEPERATOR) {
						j++;
					} else if(b == RETURN || b == NEWLINE) {
						buffer.rewind();
						break;
					}
				}
			}
		}
		
		
		// Head-Analyzer
		char[] temp;
		for(j=0; j<head.length; j++) {
			if(head[j].length >= 2) {
				if(head[j][0] == '/') {
					if(head[j][1] == 'C') {
						isDiscret[j] = false;
						temp = new char[head[j].length - 2];
						for(k=0; k<head[j].length - 2; k++) {
							temp[k] = head[j][k+2];
						}
						head[j] = temp;
					} else if(head[j][1] == 'D') {
						isDiscret[j] = true;
						temp = new char[head[j].length - 2];
						for(k=0; k<head[j].length - 2; k++) {
							temp[k] = head[j][k+2];
						}
						head[j] = temp;
					}
				}
			}
		}
		return head;
	}
	
	
	
	
	public int getPositionSecondLine(ByteBuffer buffer) {
		
		buffer.rewind();
		byte b;
		int positionSecondLine = 0;
		while(buffer.hasRemaining()) {
			b = buffer.get();
			if(b == RETURN) {
				buffer.mark();
				if(buffer.get() == NEWLINE) {
					positionSecondLine = buffer.position();
					break;
				} else {
					buffer.reset();
					positionSecondLine = buffer.position();
					break;
				}
			} else if(b == NEWLINE) {
				positionSecondLine = buffer.position();
				break;
			}
		}
		buffer.rewind();
		return positionSecondLine;
	}
	
	
	int amountColumns(ByteBuffer buffer, String format) {
		buffer.rewind();
		int j = 0;
		int k = 0;
		byte b;

		if (format == "TAB-Format" || format == "KOMMA-Format") {
			while (buffer.hasRemaining() == true) {
				b = buffer.get();
				if (b == SEPERATOR)
					j++;
				if (b == NEWLINE || b == RETURN)
					break;
			}
		}

		else { // SPACE-Format oder KOMMA-QUOTE-Format
			buffer.mark();
			while(buffer.hasRemaining()) {
				b = buffer.get();
				if(b == QUOTE) {
					k++;
				} else if(b == RETURN || b == NEWLINE) {
					break;
				}
			}
			if(k%2 != 0) {
				System.out.println("ERROR: Uneven amount of quotes in headLine");
				System.exit(0);
			}
			buffer.reset();
			while (buffer.hasRemaining()) {
				b = buffer.get();
				if (b == QUOTE) {
					while (buffer.hasRemaining()) {
						b = buffer.get();
						if (b == QUOTE) {
							if (buffer.hasRemaining()) {
								b = buffer.get();
								if (b == SEPERATOR) {
									j++;
									break;
								} else if (b == RETURN || b == NEWLINE)
									return j + 1;
							}
						}
					}
				} else if (b == SEPERATOR) {
					j++;
				} else if (b == RETURN || b == NEWLINE) {
					return j + 1;
				} else {
					while(buffer.hasRemaining()) {
						b = buffer.get();
						if(b == SEPERATOR) {
							j++;
							break;
						} else if(b == RETURN || b == NEWLINE) {
							return j+1;
						}
					}
				}
			}
		}
		return j + 1;
	}

	int amountLines(ByteBuffer buffer, String format) {
		buffer.rewind();
		int i = 0; // counts lines
		int j = 0; // counts columns
		// columns has to be initaliazed before
		numericalColumn = new boolean[columns];
		for (int k = 0; k < columns; k++) {
			numericalColumn[k] = true;
		}
		boolean dotAvailable = false;
		boolean minusAvailable = false;
		byte b;
		byte SEPERATOR;

		buffer.rewind();
		buffer.position(positionSecondLine);

		if (format == "TAB-Format" || format == "KOMMA-Format") {
			if(format == "TAB-Format") SEPERATOR = TAB;
			else SEPERATOR = KOMMA;
			
			while (buffer.hasRemaining()) {
				
				progressing();

				if (numericalColumn[j]) {
					dotAvailable = false;
					minusAvailable = false;
					while (buffer.hasRemaining()) {
						b = buffer.get();

						if (b == MINUS) {
							if (minusAvailable) {
								numericalColumn[j] = false;
								while (buffer.hasRemaining()) {
									b = buffer.get();
									if (b == SEPERATOR) {
										j++;
										break;
									} else if (b == RETURN) {
										buffer.mark();
										i++;
										j = 0;
										if (buffer.hasRemaining()) {
											if (buffer.get() == NEWLINE)
												break;
											else {
												buffer.reset();
												break;
											}
										}
									} else if (b == NEWLINE) {
										i++;
										j = 0;
										break;
									}
								}
								break;
							} else {
								minusAvailable = true;
								if (buffer.hasRemaining()) {
									buffer.mark();
									b = buffer.get();
									if (b == SEPERATOR || b == RETURN || b == NEWLINE) {
										buffer.reset();
										numericalColumn[j] = false;
										break;
									} else {
										buffer.reset();
										continue;
									}
								}
							}
						} else if (isNumber(b)) {
							continue;
						} else if (b == DOT) {
							minusAvailable = true;
							if (dotAvailable) {
								numericalColumn[j] = false;
								while (buffer.hasRemaining()) {
									b = buffer.get();
									if (b == SEPERATOR) {
										j++;
										break;
									} else if (b == RETURN) {
										buffer.mark();
										i++;
										j = 0;
										if (buffer.hasRemaining()) {
											if (buffer.get() == NEWLINE)
												break;
											else {
												buffer.reset();
												break;
											}
										}
									} else if (b == NEWLINE) {
										i++;
										j = 0;
										break;
									}
								}
								break;
							} else {
								dotAvailable = true;
								if (buffer.hasRemaining()) {
									buffer.mark();
									b = buffer.get();
									if (b == SEPERATOR || b == RETURN || b == NEWLINE) {
										buffer.reset();
										numericalColumn[j] = false;
										break;
									} else {
										buffer.reset();
										continue;
									}
								}
							}
						} else if (b == SEPERATOR) {
							j++;
							break;
						} else if (b == RETURN) {
							buffer.mark();
							i++;
							j = 0;
							if (buffer.hasRemaining()) {
								if (buffer.get() == NEWLINE)
									break;
								else {
									buffer.reset();
									break;
								}
							}
						} else if (b == NEWLINE) {
							i++;
							j = 0;
							break;
						} else {
							if (b == (byte) 'N') {
								if (buffer.position() > 1) {
									b = buffer.get(buffer.position() - 2);
									if (b == SEPERATOR || b == RETURN || b == NEWLINE) {
									} else {
										numericalColumn[j] = false;
										break;
									}
								}
								if (buffer.hasRemaining()) {
									b = buffer.get();
									if (b == (byte) 'A') {
										if (buffer.hasRemaining()) {
											b = buffer.get();
											if (b == SEPERATOR) {
												j++;
												break;
											} else if (b == RETURN) {
												buffer.mark();
												i++;
												j = 0;
												if (buffer.hasRemaining()) {
													if (buffer.get() == NEWLINE)
														break;
													else {
														buffer.reset();
														break;
													}
												}
											} else if (b == NEWLINE) {
												i++;
												j = 0;
												break;
											} else {
												numericalColumn[j] = false;
												break;
											}

										} else
											break;
									} else if (b == (byte) 'a') {
										if (buffer.hasRemaining()) {
											b = buffer.get();
											if (b == (byte) 'N') {
												if (buffer.hasRemaining()) {
													b = buffer.get();
													if (b == SEPERATOR) {
														j++;
														break;
													} else if (b == RETURN) {
														buffer.mark();
														i++;
														j = 0;
														if (buffer
																.hasRemaining()) {
															if (buffer.get() == NEWLINE)
																break;
															else {
																buffer.reset();
																break;
															}
														}
													} else if (b == NEWLINE) {
														i++;
														j = 0;
														break;
													} else {
														numericalColumn[j] = false;
														break;
													}

												} else
													break;
											}
										}
									} else {
										numericalColumn[j] = false;
										break;
									}
								} else {
									numericalColumn[j] = false;
									break;
								}
							} else {
								numericalColumn[j] = false;
								break;
							}
						}

					} // end while

				} else {
					while (buffer.hasRemaining()) {
						b = buffer.get();
						if (b == SEPERATOR) {
							j++;
							break;
						} else if (b == RETURN) {
							buffer.mark();
							i++;
							j = 0;
							if (buffer.hasRemaining()) {
								if (buffer.get() == NEWLINE)
									break;
								else {
									buffer.reset();
									break;
								}
							}
						} else if (b == NEWLINE) {
							i++;
							j = 0;
							break;
						}
					}
				}

				if (j < columns) {
				}
				else {
					System.err.println("Too long line in (i,j) = (" + (i + 2)
							+ "," + (j + 1) + ")");
					System.exit(0);
				}

			} // end big while
			
		} else if (format == "UNKNOWN-Format") {

			System.err.println(format);
			System.exit(0);
		}


		return i + 1;


	}

	private ByteBuffer gotoNextLine(ByteBuffer buffer) {
		byte b;
		while (buffer.hasRemaining()) {
			b = buffer.get();
			if (b == RETURN) {
				buffer.mark();
				if (buffer.hasRemaining()) {
					if (buffer.get() == NEWLINE) {
						break;
					} else {
						buffer.reset();
						break;
					}
				} else {
					break;
				}
			} else if (b == NEWLINE) {
				break;
			}
		}
		return buffer;
	}

	public String[] testUNQUOTEDFormat(ByteBuffer buffer, byte SEPERATOR, int maxamountErrors) {

		buffer.rewind();
		byte b;
		String[] error = new String[maxamountErrors];
		int k = 0;
		int i = -1, j = 0;

		while (buffer.hasRemaining()) {
			
			b = buffer.get();
			j = 0;
			i++;
			if (b == SEPERATOR) {
				// SEPERATOR at BOL
				if (k < error.length) {
					if(i == 0) {
						error[k++] = new String("hardError: SEPERATOR at BOL in headLine");
						errorposition = buffer.position();
						return error;
					}
					else error[k++] = new String("softError: SEPERATOR at BOL in line " + (i + 1));
				} else
					return error;
				// go to next line
				buffer = gotoNextLine(buffer);
				continue;
			} else if (b == RETURN) {
				// RETURN at BOL
				if (k < error.length) {
					error[k++] = new String("hardError: RETURN at BOL in line " + (i + 1));
					errorposition = buffer.position();
					hardReadError = true;
					return error;
				} else
					return error;
			} else if (b == NEWLINE) {
				// NEWLINE at BOL
				if (k < error.length) {
					error[k++] = new String("hardError: NEWLINE at BOL in line " + (i + 1));
					errorposition = buffer.position();
					hardReadError = true;
					return error;
				} else
					return error;
			}
			while (buffer.hasRemaining()) {
				b = buffer.get();
				if (b == SEPERATOR) {
					j++;
					// change: 17.08.2005
					if(j>=columns) {
						if (k < error.length) {
							error[k++] = new String("hardError: Too much entries in line "
									+ (i + 1));
							errorposition = buffer.position();
							hardReadError = true;
							return error;

						} else
							return error;
					}
					buffer.mark();
					if (buffer.hasRemaining()) {
						b = buffer.get();
						if (b == SEPERATOR) {
							// doubleSEPERATOR
							if (k < error.length) {
								if(i == 0){
									error[k++] = new String("hardError: doubleSEPERATOR in headLine");
									errorposition = buffer.position();
									return error;
								}
								else error[k++] = new String("softError: doubleSEPERATOR in line "
										+ (i + 1));
							} else
								return error;
							// go to next line
							buffer = gotoNextLine(buffer);
							break;
						} else if (b == RETURN) {
							// SEPERATOR at EOL
							if (k < error.length) {
								if(i == 0){
									error[k++] = new String("hardError: SEPERATOR at EOL in headLine");
									errorposition = buffer.position();
									return error;
								}
								else error[k++] = new String("softError: SEPERATOR at EOL in line " 
										+ (i + 1));
							} else
								return error;
							// go to next line
							buffer = gotoNextLine(buffer);
							break;
						} else if (b == NEWLINE) {
							// SEPERATOR at EOL
							if (k < error.length) {
								if(i == 0) {
									error[k++] = new String("hardError: SEPERATOR at EOL in headLine");
									errorposition = buffer.position();
									return error;
								}
								else error[k++] = new String("softError: SEPERATOR at EOL in line "
										+ (i + 1));
							} else
								return error;
							// go to next line
							buffer = gotoNextLine(buffer);
							break;
						} else
							buffer.reset();
					} else {
						// SEPERATOR at EOF
						if (k < error.length) {
							error[k++] = new String("softError: SEPERATOR at EOF");
						} else
							return error;
					}
				} else if (b == RETURN) {
					// Missing entries
					if (j < columns - 1) {
						if (k < error.length) {
							error[k++] = new String("hardError: Missing entries in line "
									+ (i + 1));
							errorposition = buffer.position();
							hardReadError = true;
							return error;
						} else
							return error;
					} else if(j >= columns) {
						if (k < error.length) {
							error[k++] = new String("hardError: Too much entries in line "
									+ (i + 1));
							errorposition = buffer.position();
							hardReadError = true;
							return error;

						} else
							return error;
					}
					buffer.mark();
					if (buffer.hasRemaining()) {
						if (buffer.get() == NEWLINE) {
							break;
						} else {
							buffer.reset();
							break;
						}
					}
				} else if (b == NEWLINE) {
					// Missing entries
					if (j < columns - 1) {
						if (k < error.length) {
							error[k++] = new String("hardError: Missing entries in line "
									+ (i + 1));
							errorposition = buffer.position();
							hardReadError = true;
							return error;
						} else
							return error;
					} else if(j >= columns) {
						if (k < error.length) {
							error[k++] = new String("hardError: Too much entries in line "
									+ (i + 1));
							errorposition = buffer.position();
							hardReadError = true;
							return error;
						} else
							return error;
					}
					break;
				}
			}
		}
		b = buffer.get(buffer.position() - 1);
		if (j < columns - 1) {
			if (k < error.length) {
				error[k++] = new String("hardError: Missing entries in last line");
				errorposition = buffer.position();
				hardReadError = true;
				return error;
			} else
				return error;

		} else if(j >= columns) {
			if (k < error.length) {
				error[k++] = new String("hardError: Too much entries in last line");
				errorposition = buffer.position();
				hardReadError = true;
				return error;
			} else
				return error;
		}
		if(b == SEPERATOR && j < columns) {
			if (k < error.length) {
				error[k++] = new String("hardError: SEPERATOR at EOF / Missing values in last line");
				errorposition = buffer.position();
				hardReadError = true;
				return error;
			} else
				return error;
		}
		else if (b == NEWLINE) {
			if (k < error.length) {
				error[k++] = new String("softError: NEWLINE at EOF");
			} else
				return error;
		} else if (b == RETURN) {
			if (k < error.length) {
				error[k++] = new String("softError: RETURN at EOF");
			} else
				return error;

		}

		return error;
	}

	public String[] testQUOTEDFormat(ByteBuffer buffer, byte SEPERATOR, int maxamountErrors) {

		for(int i = 0; i<columns; i++) {
			System.out.println(i + " " + numericalColumn[i]);
		}
		buffer.rewind();
		byte b;
		String[] error = new String[maxamountErrors];
		int k = 0;
		int i = -1, j = 0;
		boolean breaking = false;
		boolean dotAvailable = false;
		
		// Suche Fehler in Kopfzeile
		
		while(buffer.hasRemaining()) {
			
			b = buffer.get();
			if(b == QUOTE) {
				while(buffer.hasRemaining()) {
					b = buffer.get();
					if(b == QUOTE) {
						if(buffer.hasRemaining()) {
							b = buffer.get();
							if(b == SEPERATOR) {
								j++;
								break;
							} else if(b == RETURN || b == NEWLINE) {
								breaking = true;
								break;
							} else {
								if (k < error.length) {
									error[k++] = new String("hardError: error in headLine in j = " + (j + 1));
									errorposition = buffer.position();
									hardReadError = true;
									return error;
								}
								return error;
								
							}
						} else {
							// nur eine Spalte im Head
						}
					}
				}
			} else if(b == SEPERATOR) {
				if(buffer.hasRemaining()) {
					b = buffer.get();
					if(b != QUOTE) {
						if (k < error.length) {
							error[k++] = new String("hardError: error in headLine in j = " + (j + 1));
							errorposition = buffer.position();
							hardReadError = true;
							return error;
						}
						return error;
					}
				} else {
					// SEPERATOR at EOL
				}
			} else {
				if (k < error.length) {
					error[k++] = new String("hardError: error in headLine in j = " + (j + 1) + " (word not quoted)");
					errorposition = buffer.position();
					hardReadError = true;
					return error;
				}
				return error;
			}
			if(breaking) {
				break;
			}
		}
		
		j = 0; breaking = false;
		// lies erste Zeile ein, um Wort und Zahl zu erkennen. NA = Zahl
		buffer.rewind();
		buffer.position(positionSecondLine);
		
		while(buffer.hasRemaining()) {
			b = buffer.get();
			if(isNumber(b) || b == MINUS || b == DOT) {
				numericalColumn[j] = true;
				while(buffer.hasRemaining()) {
					b = buffer.get();
					if(b == SEPERATOR || b == RETURN || b == NEWLINE) {
						buffer.position(buffer.position()-1);
						break;
					}
				}
			} else if(b == 'N') {
				numericalColumn[j] = true;
				while(buffer.hasRemaining()) {
					b = buffer.get();
					if(b == SEPERATOR || b == RETURN || b == NEWLINE) {
						buffer.position(buffer.position()-1);
						break;
					}
				}
			} else if(b == SEPERATOR) {
				j++;
			} else if(b == RETURN || b == NEWLINE) {
				break;
			} else {
				numericalColumn[j] = false;
				while(buffer.hasRemaining()) {
					b = buffer.get();
					if(b == SEPERATOR || b == RETURN || b == NEWLINE) {
						buffer.position(buffer.position()-1);
						break;
					}
				}
			}
		}
		
		// finde Strukturfehler
		buffer.position(positionSecondLine);
		j = 0;
		while(buffer.hasRemaining()) {
			b = buffer.get();
			if(b == QUOTE) {
				if(numericalColumn[j]) {
					// Fehler
					if (k < error.length) {
						error[k++] = new String("hardError: QUOTE in numerical column (i,j) = (" + (i + 1) + "," + (j + 1) + ")");
						errorposition = buffer.position();
						hardReadError = true;
						return error;
					} else
						return error;
				}
				while(buffer.hasRemaining()) {
					b = buffer.get();
					if(b == QUOTE) {
						if(buffer.hasRemaining()) {
							b = buffer.get();
							if(b == SEPERATOR || b == RETURN || b == NEWLINE) {
								// alles in Ordnung
								buffer.position(buffer.position()-1);
								break;
							} else {
								// Fehler: QUOTE im Wort
								if (k < error.length) {
									error[k++] = new String("hardError: QUOTE in word (i,j) = (" + (i + 1) + "," + (j + 1) + ")");
									errorposition = buffer.position();
									hardReadError = true;
									return error;
								} else
									return error;
							}
						} else {
							// alles in Ordnung
						}
					} else {
						continue;
					}
				}
			} else if(isNumber(b) || b == MINUS || b == DOT) {
				if(!numericalColumn[j]) {
					// Fehler
					if (k < error.length) {
						error[k++] = new String("hardError: word not quoted (i,j) = (" + (i + 1) + "," + (j + 1) + ")");
						errorposition = buffer.position();
						hardReadError = true;
						return error;
					} else
						return error;
				}
				if(b == DOT) dotAvailable = true;
				else dotAvailable = false;
				while(buffer.hasRemaining()) {
					b = buffer.get();
					if(isNumber(b)) {
						// alles in Ordnung
					} else if(b == DOT) {
						if(buffer.hasRemaining()) {
							buffer.mark();
							b = buffer.get();
							if(b == SEPERATOR || b == NEWLINE || b == RETURN) {
								if (k < error.length) {
									error[k++] = new String("hardError: not a number (i,j) = (" + (i + 1) + "," + (j + 1) + ")");
									errorposition = buffer.position();
									hardReadError = true;
									return error;
								} else
									return error;
							} else {
								buffer.reset();
							}
						}
						if(dotAvailable) {
							// Fehler: >2 dots
							if (k < error.length) {
								error[k++] = new String("hardError: >=2 dots in number (i,j) = (" + (i + 1) + "," + (j + 1) + ")");
								errorposition = buffer.position();
								hardReadError = true;
								return error;
							} else
								return error;
						} else {
							dotAvailable = true;
							// alles in Ordnung
						}
					} else if(b == SEPERATOR || b == RETURN || b == NEWLINE) {
						// alles in Ordnung
						buffer.position(buffer.position()-1);
						break;
					} else {
						// Fehler: in Zahl
						if (k < error.length) {
							error[k++] = new String("hardError: not a number (i,j) = (" + (i + 1) + "," + (j + 1) + ")");
							errorposition = buffer.position();
							hardReadError = true;
							return error;
						} else
							return error;
					}
				}
			} else if(b == SEPERATOR) {
				j++;
				// change: 17.08.2005
				if(j>=columns) {
					if (k < error.length) {
						error[k++] = new String("hardError: Too much entries in line "
								+ (i + 1));
						errorposition = buffer.position();
						hardReadError = true;
						return error;

					} else
						return error;
				}
				if(buffer.hasRemaining()) {
					b = buffer.get();
					if(b == SEPERATOR) {
						buffer.position(buffer.position()-1);
						// doubleSEPERATOR
					} else if(b == RETURN || b == NEWLINE) {
						// SEPERATOR at EOL
					} else {
						buffer.position(buffer.position()-1);
					}
				} else {
					// Fehler
				}
			} else if(b == RETURN) {
				if(j > columns - 1) {
					if (k < error.length) {
						error[k++] = new String("hardError: Too much entries in line " + (i + 2));
						errorposition = buffer.position();
						hardReadError = true;
						return error;
					} else
						return error;
				} else if(j < columns - 1) {
					if (k < error.length) {
						error[k++] = new String("hardError: Missing entries in line " + (i + 2));
						errorposition = buffer.position();
						hardReadError = true;
						return error;
					} else
						return error;
				}
				i++; j=0;
				if(buffer.hasRemaining()) {
					buffer.mark();
					if(buffer.get() == NEWLINE) {
					} else {
						buffer.reset();
					}
				}
				// leere Zeilen
				if(buffer.hasRemaining()) {
					buffer.mark();
					b = buffer.get();
					if(b == RETURN || b == NEWLINE) {
						if (k < error.length) {
							error[k++] = new String("hardError: empty line i = " + (i + 2));
							errorposition = buffer.position();
							hardReadError = true;
							return error;
						} else
							return error;
					} else {
						buffer.reset();
					}
				}
			} else if(b == NEWLINE) {
				if(j > columns - 1) {
					if (k < error.length) {
						error[k++] = new String("hardError: Too much entries in line " + (i + 2));
						errorposition = buffer.position();
						hardReadError = true;
						return error;
					} else
						return error;
				} else if(j < columns - 1) {
					if (k < error.length) {
						error[k++] = new String("hardError: Missing entries in line " + (i + 2));
						errorposition = buffer.position();
						hardReadError = true;
						return error;
					} else
						return error;
				}
				i++; j=0;
				if(buffer.hasRemaining()) {
					buffer.mark();
					b = buffer.get();
					if(b == RETURN || b == NEWLINE) {
						if (k < error.length) {
							error[k++] = new String("hardError: empty line i = " + (i + 2));
							errorposition = buffer.position();
							hardReadError = true;
							return error;
						} else
							return error;
					} else {
						buffer.reset();
					}
				}

			} else if(b == 'N') {
				if(!numericalColumn[j]) {
					// Fehler
					if (k < error.length) {
						error[k++] = new String("hardError: word not quoted (i,j) = (" + (i + 1) + "," + (j + 1) + ")");
						errorposition = buffer.position();
						hardReadError = true;
						return error;
					} else
						return error;
				}
				if(buffer.hasRemaining()) {
					b = buffer.get();
					if(b == 'A') {
						if(buffer.hasRemaining()) {
							b = buffer.get();
							if(b == SEPERATOR || b == RETURN || b == NEWLINE) {
								// alles in Orndung
								buffer.position(buffer.position()-1);
							} else {
								// Fehler
								if (k < error.length) {
									error[k++] = new String("hardError: word not quoted (i,j) = (" + (i + 1) + "," + (j + 1) + ")");
									errorposition = buffer.position();
									hardReadError = true;
									return error;
								} else
									return error;
							}
						} else {
							// alles in Ordnung
						}
					} else if(b == 'a') {
						if(buffer.hasRemaining()) {
							b = buffer.get();
							if(b == 'N') {
								b = buffer.get();
								if(b == SEPERATOR || b == RETURN || b == NEWLINE) {
									// alles in Ordnung
									buffer.position(buffer.position()-1);
								} else {
									// Fehler
									if (k < error.length) {
										error[k++] = new String("hardError: word not quoted (i,j) = (" + (i + 1) + "," + (j + 1) + ")");
										errorposition = buffer.position();
										hardReadError = true;
										return error;
									} else
										return error;
								}
							} else {
								// Fehler
								if (k < error.length) {
									error[k++] = new String("hardError: word not quoted (i,j) = (" + (i + 1) + "," + (j + 1) + ")");
									errorposition = buffer.position();
									hardReadError = true;
									return error;
								} else
									return error;
							}
						} else {
							// Fehler
							if (k < error.length) {
								error[k++] = new String("hardError: word not quoted (i,j) = (" + (i + 1) + "," + (j + 1) + ")");
								errorposition = buffer.position();
								hardReadError = true;
								return error;
							} else
								return error;
						}
					} else {
						// Fehler
						if (k < error.length) {
							error[k++] = new String("hardError: word not quoted (i,j) = (" + (i + 1) + "," + (j + 1) + ")");
							errorposition = buffer.position();
							hardReadError = true;
							return error;
						} else
							return error;
					}
				} else {
					// Fehler
					if (k < error.length) {
						error[k++] = new String("hardError: word not quoted (i,j) = (" + (i + 1) + "," + (j + 1) + ")");
						errorposition = buffer.position();
						hardReadError = true;
						return error;
					} else
						return error;
				}
			} else {
				// Fehler
				if (k < error.length) {
					error[k++] = new String("hardError: error in (i,j) = (" + (i + 1) + "," + (j + 1) + ")");
					errorposition = buffer.position();
					hardReadError = true;
					return error;
				} else
					return error;
			}
		}
		
		lines = i+2;
		return error;
	}


	public void tokenizeUNQUOTEDBuffer(ByteBuffer buffer, byte SEPERATOR) {
		byte b;
		int i, j, k; // i = Zeilenindex, j = Spaltenindex, k = zählt Wortlänge
		int[] discretLimit_c = new int[columns]; // specified discretLimit for column
		for (int l = 0; l < wordStackSize.length; l++) {
			wordStackSize[l] = 0;
			discretLimit_c[l] = discretLimit;
		}
		boolean wordFound = false;
		boolean valueFound = false;
		char[] temp = null;
		double tempNumber;
		int[] discretLimitCounter = new int[columns];
		int findWordIndex = 0;
		int findValueIndex = 0;
		char[][] previousWord = new char[columns][];
		int[] previousWordPosition = new int[columns];
		char[][][] s_word = new char[columns][discretLimit][];
		int[][] pointer = new int[columns][discretLimit];
		
		buffer.position(positionSecondLine);
		i = 0; j = 0; k = 0;
		
		while (buffer.hasRemaining()) {
			k = 0;
			
			if (numericalColumn[j] == false) {

				
				if(buffer.hasRemaining()) {
					buffer.mark();
					b = buffer.get();
					
					// doubleSEPERATOR-Behandlung als NA
					if(b == SEPERATOR) {
						temp = new char[2];
						temp[0] = 'N'; temp[1] = 'A';
						NA[j][i] = true;
						if(wordStackSize[j] != 0) {
							if(eqCharArray(previousWord[j],temp)) {
								findWordIndex = previousWordPosition[j];
								wordNotFound = false;
							} else {
								findWordIndex = findWord(s_word[j],wordStackSize[j],temp);
								previousWord[j] = temp;
								previousWordPosition[j] = findWordIndex;
							}
							findWordIndex = findWord(s_word[j],wordStackSize[j],temp);
							if(wordNotFound) {
								wordNotFound = false;
								if(wordStackSize[j] >= discretLimit_c[j]) {
									char[][] tempArray = word[j];
									char[][] s_tempArray = s_word[j];
									int[] tempPointer = pointer[j]; 
									discretLimit_c[j] *= 2;
									word[j] = new char[2 * wordStackSize[j]][];
									s_word[j] = new char[2 * wordStackSize[j]][];
									pointer[j] = new int[2 * wordStackSize[j]];
									System.arraycopy(tempArray, 0, word[j], 0, wordStackSize[j]);
									System.arraycopy(s_tempArray, 0, s_word[j], 0, wordStackSize[j]);
									System.arraycopy(tempPointer, 0, pointer[j], 0, wordStackSize[j]);
								}
								int z=0;
								System.arraycopy(s_word[j],findWordIndex,s_word[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
								System.arraycopy(pointer[j],findWordIndex,pointer[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
								s_word[j][findWordIndex] = temp;
								pointer[j][findWordIndex] = wordStackSize[j];
								word[j][wordStackSize[j]] = temp;
								item[j][i] = wordStackSize[j];
								wordStackSize[j]++;
								wordCount[j][pointer[j][findWordIndex]] = 1; // new word saved
							} else {
								item[j][i] = pointer[j][findWordIndex];
								wordCount[j][pointer[j][findWordIndex]]++;
								wordNotFound = true;
							}
						} else {
							previousWord[j] = temp;
							previousWordPosition[j] = 0;
							s_word[j][0] = temp;
							pointer[j][0] = 0;
							word[j][0] = temp;
							item[j][i] = 0;
							wordCount[j][0]++;
							wordStackSize[j]++;
						}
						j++; continue;
						
												
					} else if(b == RETURN) { // hier kommt er nie rein
						if(buffer.get(buffer.position()-2) == SEPERATOR) {
							temp = new char[2];
							temp[0] = 'N'; temp[1] = 'A';
							NA[j][i] = true;
							if(wordStackSize[j] != 0) {
								if(eqCharArray(previousWord[j],temp)) {
									findWordIndex = previousWordPosition[j];
									wordNotFound = false;
								} else {
									findWordIndex = findWord(s_word[j],wordStackSize[j],temp);
									previousWord[j] = temp;
									previousWordPosition[j] = findWordIndex;
								}
								findWordIndex = findWord(s_word[j],wordStackSize[j],temp);
								if(wordNotFound) {
									wordNotFound = false;
									if(wordStackSize[j] >= discretLimit_c[j]) {
										char[][] tempArray = word[j];
										char[][] s_tempArray = s_word[j];
										int[] tempPointer = pointer[j]; 
										discretLimit_c[j] *= 2;
										word[j] = new char[2 * wordStackSize[j]][];
										s_word[j] = new char[2 * wordStackSize[j]][];
										pointer[j] = new int[2 * wordStackSize[j]];
										System.arraycopy(tempArray, 0, word[j], 0, wordStackSize[j]);
										System.arraycopy(s_tempArray, 0, s_word[j], 0, wordStackSize[j]);
										System.arraycopy(tempPointer, 0, pointer[j], 0, wordStackSize[j]);
									}
									int z=0;
									System.arraycopy(s_word[j],findWordIndex,s_word[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
									System.arraycopy(pointer[j],findWordIndex,pointer[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
									s_word[j][findWordIndex] = temp;
									pointer[j][findWordIndex] = wordStackSize[j];
									word[j][wordStackSize[j]] = temp;
									item[j][i] = wordStackSize[j];
									wordStackSize[j]++;
									wordCount[j][pointer[j][findWordIndex]] = 1; // new word saved
								} else {
									item[j][i] = pointer[j][findWordIndex];
									wordCount[j][pointer[j][findWordIndex]]++;
									wordNotFound = true;
								}
							} else {
								previousWord[j] = temp;
								previousWordPosition[j] = 0;
								s_word[j][0] = temp;
								pointer[j][0] = 0;
								word[j][0] = temp;
								item[j][i] = 0;
								wordCount[j][0]++;
								wordStackSize[j]++;
							}
						}
						i++; j=0;
						if(buffer.hasRemaining()) {
							if(buffer.get(buffer.position()) == NEWLINE) {
								buffer.position(buffer.position()+1);
							}
							
						}
						continue;
					} else if(b == NEWLINE) { // hier auch nicht
						if(buffer.get(buffer.position()-2) == SEPERATOR) {
							temp = new char[2];
							temp[0] = 'N'; temp[1] = 'A';
							NA[j][i] = true;
							if(wordStackSize[j] != 0) {
								if(eqCharArray(previousWord[j],temp)) {
									findWordIndex = previousWordPosition[j];
									wordNotFound = false;
								} else {
									findWordIndex = findWord(s_word[j],wordStackSize[j],temp);
									previousWord[j] = temp;
									previousWordPosition[j] = findWordIndex;
								}
								findWordIndex = findWord(s_word[j],wordStackSize[j],temp);
								if(wordNotFound) {
									wordNotFound = false;
									if(wordStackSize[j] >= discretLimit_c[j]) {
										char[][] tempArray = word[j];
										char[][] s_tempArray = s_word[j];
										int[] tempPointer = pointer[j]; 
										discretLimit_c[j] *= 2;
										word[j] = new char[2 * wordStackSize[j]][];
										s_word[j] = new char[2 * wordStackSize[j]][];
										pointer[j] = new int[2 * wordStackSize[j]];
										System.arraycopy(tempArray, 0, word[j], 0, wordStackSize[j]);
										System.arraycopy(s_tempArray, 0, s_word[j], 0, wordStackSize[j]);
										System.arraycopy(tempPointer, 0, pointer[j], 0, wordStackSize[j]);
									}
									int z=0;
									System.arraycopy(s_word[j],findWordIndex,s_word[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
									System.arraycopy(pointer[j],findWordIndex,pointer[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
									s_word[j][findWordIndex] = temp;
									pointer[j][findWordIndex] = wordStackSize[j];
									word[j][wordStackSize[j]] = temp;
									item[j][i] = wordStackSize[j];
									wordStackSize[j]++;
									wordCount[j][pointer[j][findWordIndex]] = 1; // new word saved
								} else {
									item[j][i] = pointer[j][findWordIndex];
									wordCount[j][pointer[j][findWordIndex]]++;
									wordNotFound = true;
								}
							} else {
								previousWord[j] = temp;
								previousWordPosition[j] = 0;
								s_word[j][0] = temp;
								pointer[j][0] = 0;
								word[j][0] = temp;
								item[j][i] = 0;
								wordCount[j][0]++;
								wordStackSize[j]++;
							}
						}
						i++; j=0;
						continue;
					} else {
						buffer.reset();
					}
				}
				
				// lies Wortlänge ein
				buffer.mark();
				while (buffer.hasRemaining()) {
					b = buffer.get();
					if (b == SEPERATOR || b == RETURN || b == NEWLINE) {
						buffer.reset();
						break;
					} else
						k++;
				}
				if (!buffer.hasRemaining())
					buffer.reset();
				temp = new char[k]; // geht schnell
				for (int l = 0; l < k; l++) {
					b = buffer.get();
					
					temp[l] = (char) b;
				}
				
				// System.out.println("temp " + new String(temp));
				
				// modified
				
				if(wordStackSize[j] != 0) {
					//System.out.println("word.length = " + word[j].length);
					if(eqCharArray(previousWord[j],temp)) {
						// System.out.println("previous " + new String(previousWord));
						// System.out.println("temp2 " + new String(temp));
						findWordIndex = previousWordPosition[j];
						// System.out.println("findWordIndex2 " + findWordIndex);
						wordNotFound = false;
					} else {
						findWordIndex = findWord(s_word[j],wordStackSize[j],temp);
						previousWord[j] = temp;
						previousWordPosition[j] = findWordIndex;
					}


					//System.out.println("wordStackSize " + wordStackSize[j]);
					// System.out.println("findWordIndex " + findWordIndex);
					if(wordNotFound) {
						wordNotFound = false;
						// wordFound = true;
						
						if(wordStackSize[j] >= discretLimit_c[j]) {
							char[][] tempArray = word[j];
							char[][] s_tempArray = s_word[j];
							int[] tempPointer = pointer[j]; 
							discretLimit_c[j] *= 2;
							word[j] = new char[2 * wordStackSize[j]][];
							s_word[j] = new char[2 * wordStackSize[j]][];
							pointer[j] = new int[2 * wordStackSize[j]];
							System.arraycopy(tempArray, 0, word[j], 0, wordStackSize[j]);
							System.arraycopy(s_tempArray, 0, s_word[j], 0, wordStackSize[j]);
							System.arraycopy(tempPointer, 0, pointer[j], 0, wordStackSize[j]);
						}
						int z=0;
						/*System.out.println("/// start before");
						while(z < item[j].length) {
							System.out.print(z); System.out.print("	");
							// System.out.print(word[j][z]); System.out.print("	");
							System.out.print(item[j][z]); System.out.print("	");
							System.out.println(wordCount[j][z]);
							z++;
						}
						System.out.println("/// end before");*/
						System.arraycopy(s_word[j],findWordIndex,s_word[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
						System.arraycopy(pointer[j],findWordIndex,pointer[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
						/* for(int m=0; m<i; m++) {
							if(item[j][m] >= findWordIndex) {
								item[j][m]++;
							}
						} */
						s_word[j][findWordIndex] = temp;
						pointer[j][findWordIndex] = wordStackSize[j];
						word[j][wordStackSize[j]] = temp;
						item[j][i] = wordStackSize[j];
						wordStackSize[j]++;
						wordCount[j][pointer[j][findWordIndex]] = 1; // new word saved
						/*System.out.println("/// start after");
						z = 0;
						while(z < item[j].length) {
							System.out.print(z); System.out.print("	");
							// System.out.print(word[j][z]); System.out.print("	");
							System.out.print(item[j][z]); System.out.print("	");
							System.out.println(wordCount[j][z]);
							z++;
						}
						System.out.println("/// end after");*/
						
					} else {
						//System.out.println("bbb " + i);
						item[j][i] = pointer[j][findWordIndex];
						wordCount[j][pointer[j][findWordIndex]]++;
						wordNotFound = true;
					}
				} else {
					previousWord[j] = temp;
					previousWordPosition[j] = 0;
					s_word[j][0] = temp;
					pointer[j][0] = 0;
					word[j][0] = temp;
					item[j][i] = 0;
					wordCount[j][0]++;
					wordStackSize[j]++;
				}
				
				// end modified
				
				
				
				if (temp.length == 2 && temp[0] == 'N' && temp[1] == 'A') NA[j][i] = true;
				else if (temp.length == 3 && temp[0] == 'N' && temp[1] == 'a' && temp[2] == 'N') NA[j][i] = true;
				
				if (buffer.hasRemaining()) {
					b = buffer.get();
					if (b == SEPERATOR) {
						j++;
					} else if (b == RETURN) {
						progressing(); // execute progressing() every new line
						buffer.mark();
						if (buffer.hasRemaining()) {
							if (buffer.get() == NEWLINE) {
								i++;
								j = 0;
							} else {
								buffer.reset();
								i++;
								j = 0;
							}
						}
					} else if (b == NEWLINE) {
						progressing(); // execute progressing() every new line
						i++;
						j = 0;
					}
				}

				// numericalColumn[j] = true
			} else {
				if(buffer.hasRemaining()) {
					buffer.mark();
					b = buffer.get();
					if(b == SEPERATOR) {
						NA[j][i] = true;
						item[j][i] = 0.0/0.0;
						j++;
						continue;
					} else if(b == RETURN) {
						if(buffer.get(buffer.position()-2) == SEPERATOR) {
							NA[j][i] = true;
							item[j][i] = 0.0/0.0;
						}
						buffer.position(buffer.position()-1);
						

					} else if(b == NEWLINE) {
						if(buffer.get(buffer.position()-2) == SEPERATOR) {
							NA[j][i] = true;
							item[j][i] = 0.0/0.0;
						}
						buffer.position(buffer.position()-1);

					} else {
						buffer.reset();
					}
				}
				while (buffer.hasRemaining()) {
					b = buffer.get();
					if (b == (byte) 'N') {
						item[j][i] = 0.0 / 0.0;
						if (buffer.get() == 'A') {
							NA[j][i] = true;
							break;
						} // NA
						else if (buffer.get() == 'N') {
							NA[j][i] = true;
							break;
						} // NaN
					} else if (b == MINUS) {
						while (buffer.hasRemaining()) {
							b = buffer.get();
							if (b == DOT) {
								while (buffer.hasRemaining()) {
									b = buffer.get();
									if (b == SEPERATOR || b == NEWLINE || b == RETURN) {
										break;
									} else {
										item[j][i] = item[j][i] + (b - 48) * Math.pow(10, -(++k));
									}
								}
								break;
							} else if (b == SEPERATOR || b == RETURN || b == NEWLINE) {
								break;
							} else
								item[j][i] = item[j][i] * 10 + (b - 48);
						}
						item[j][i] = -item[j][i];
						break;
					} else if (b == DOT) {
						while (buffer.hasRemaining()) {
							b = buffer.get();
							if (b == SEPERATOR || b == NEWLINE || b == RETURN)
								break;
							else {
								item[j][i] = item[j][i] + (b - 48) * Math.pow(10, -(++k));
							}
						}
						break;
					} else if(b == SEPERATOR || b == RETURN || b == NEWLINE) {
						break;
					} else {
						item[j][i] = item[j][i] * 10 + (b - 48);
					}
				} // end while

				// modified
				if(isDiscret[j]) {
					for(int m=0; m<wordStackSize[j]; m++) {
						if(discretValue[j][m] == item[j][i]) {
							valueFound = true;
							findValueIndex = m;
							break;
						}
					}
					if(!valueFound) {
						wordCount[j][wordStackSize[j]] = 1; // new item inserted
						discretValue[j][wordStackSize[j]] = item[j][i];
						wordStackSize[j]++;
						if(wordStackSize[j] >= discretLimit_c[j]) {
							isDiscret[j] = false;
						}
					} else {
						wordCount[j][findValueIndex]++;
						valueFound = false;
					}
				}
				
				// end modified

				buffer.position(buffer.position() - 1);
				if (buffer.hasRemaining()) {
					b = buffer.get();
					if (b == SEPERATOR)
						j++;
					else if (b == RETURN) {
						progressing(); // execute progressing() every new line
						buffer.mark();
						if (buffer.hasRemaining()) {
							if (buffer.get() == NEWLINE) {
								i++;
								j = 0;
							} else {
								buffer.reset();
								i++;
								j = 0;
							}
						}
					} else if (b == NEWLINE) {
						progressing(); // execute progressing() every new line
						i++;
						j = 0;
					}
				}
			}

		}
	}



	public void tokenizeQUOTEDBuffer(ByteBuffer buffer, byte SEPERATOR) {

		byte b;
		int i, j, k; // i = Zeilenindex, j = Spaltenindex, k = zählt Wortlänge
		int[] discretLimit_c = new int[columns];// letzter Eintrag in der Zeile
		for (int l = 0; l < wordStackSize.length; l++) {
			wordStackSize[l] = 0;
			discretLimit_c[l] = discretLimit;
		}
		boolean wordFound = false;
		boolean valueFound = false;
		char[] temp = null;
		double tempNumber;
		int[] discretLimitCounter = new int[columns];
		int findWordIndex = 0;
		int findValueIndex = 0;
		char[][] previousWord = new char[columns][];
		int[] previousWordPosition = new int[columns];
		char[][][] s_word = new char[columns][discretLimit][];
		int[][] pointer = new int[columns][discretLimit];

		
		buffer.position(positionSecondLine);
		i = 0; j = 0; k = 0;

		while (buffer.hasRemaining()) {
			k = 0;
			
			if (numericalColumn[j] == false) {
				
				if(buffer.hasRemaining()) {
					buffer.mark();
					b = buffer.get();

					// doubleSEPERATOR-Behandlung als NA
					if(b == SEPERATOR) {
						temp = new char[2];
						temp[0] = 'N'; temp[1] = 'A';
						NA[j][i] = true;
						if(wordStackSize[j] != 0) {
							if(eqCharArray(previousWord[j],temp)) {
								findWordIndex = previousWordPosition[j];
								wordNotFound = false;
							} else {
								findWordIndex = findWord(s_word[j],wordStackSize[j],temp);
								previousWord[j] = temp;
								previousWordPosition[j] = findWordIndex;
							}
							findWordIndex = findWord(s_word[j],wordStackSize[j],temp);
							if(wordNotFound) {
								wordNotFound = false;
								if(wordStackSize[j] >= discretLimit_c[j]) {
									char[][] tempArray = word[j];
									char[][] s_tempArray = s_word[j];
									int[] tempPointer = pointer[j]; 
									discretLimit_c[j] *= 2;
									word[j] = new char[2 * wordStackSize[j]][];
									s_word[j] = new char[2 * wordStackSize[j]][];
									pointer[j] = new int[2 * wordStackSize[j]];
									System.arraycopy(tempArray, 0, word[j], 0, wordStackSize[j]);
									System.arraycopy(s_tempArray, 0, s_word[j], 0, wordStackSize[j]);
									System.arraycopy(tempPointer, 0, pointer[j], 0, wordStackSize[j]);
								}
								int z=0;
								System.arraycopy(s_word[j],findWordIndex,s_word[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
								System.arraycopy(pointer[j],findWordIndex,pointer[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
								s_word[j][findWordIndex] = temp;
								pointer[j][findWordIndex] = wordStackSize[j];
								word[j][wordStackSize[j]] = temp;
								item[j][i] = wordStackSize[j];
								wordStackSize[j]++;
								wordCount[j][pointer[j][findWordIndex]] = 1; // new word saved
							} else {
								item[j][i] = pointer[j][findWordIndex];
								wordCount[j][pointer[j][findWordIndex]]++;
								wordNotFound = true;
							}
						} else {
							previousWord[j] = temp;
							previousWordPosition[j] = 0;
							s_word[j][0] = temp;
							pointer[j][0] = 0;
							word[j][0] = temp;
							item[j][i] = 0;
							wordCount[j][0]++;
							wordStackSize[j]++;
						}
						j++; continue;
						
												
					} else if(b == RETURN) { // hier kommt er nie rein
						if(buffer.get(buffer.position()-2) == SEPERATOR) {
							temp = new char[2];
							temp[0] = 'N'; temp[1] = 'A';
							NA[j][i] = true;
							if(wordStackSize[j] != 0) {
								if(eqCharArray(previousWord[j],temp)) {
									findWordIndex = previousWordPosition[j];
									wordNotFound = false;
								} else {
									findWordIndex = findWord(s_word[j],wordStackSize[j],temp);
									previousWord[j] = temp;
									previousWordPosition[j] = findWordIndex;
								}
								findWordIndex = findWord(s_word[j],wordStackSize[j],temp);
							if(wordNotFound) {
									wordNotFound = false;
									if(wordStackSize[j] >= discretLimit_c[j]) {
										char[][] tempArray = word[j];
										char[][] s_tempArray = s_word[j];
										int[] tempPointer = pointer[j]; 
										discretLimit_c[j] *= 2;
										word[j] = new char[2 * wordStackSize[j]][];
										s_word[j] = new char[2 * wordStackSize[j]][];
										pointer[j] = new int[2 * wordStackSize[j]];
										System.arraycopy(tempArray, 0, word[j], 0, wordStackSize[j]);
										System.arraycopy(s_tempArray, 0, s_word[j], 0, wordStackSize[j]);
										System.arraycopy(tempPointer, 0, pointer[j], 0, wordStackSize[j]);
									}
									int z=0;
									System.arraycopy(s_word[j],findWordIndex,s_word[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
									System.arraycopy(pointer[j],findWordIndex,pointer[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
									s_word[j][findWordIndex] = temp;
									pointer[j][findWordIndex] = wordStackSize[j];
									word[j][wordStackSize[j]] = temp;
									item[j][i] = wordStackSize[j];
									wordStackSize[j]++;
									wordCount[j][pointer[j][findWordIndex]] = 1; // new word saved
								} else {
									item[j][i] = pointer[j][findWordIndex];
									wordCount[j][pointer[j][findWordIndex]]++;
									wordNotFound = true;
								}
							} else {
								previousWord[j] = temp;
								previousWordPosition[j] = 0;
								s_word[j][0] = temp;
								pointer[j][0] = 0;
								word[j][0] = temp;
								item[j][i] = 0;
								wordCount[j][0]++;
								wordStackSize[j]++;
							}
						}
						i++; j=0;
						if(buffer.hasRemaining()) {
							if(buffer.get(buffer.position()) == NEWLINE) {
								buffer.position(buffer.position()+1);
							}
							
						}
						continue;
					} else if(b == NEWLINE) { // hier auch nicht
						if(buffer.get(buffer.position()-2) == SEPERATOR) {
							temp = new char[2];
							temp[0] = 'N'; temp[1] = 'A';
							NA[j][i] = true;
							if(wordStackSize[j] != 0) {
								if(eqCharArray(previousWord[j],temp)) {
									findWordIndex = previousWordPosition[j];
									wordNotFound = false;
								} else {
									findWordIndex = findWord(s_word[j],wordStackSize[j],temp);
									previousWord[j] = temp;
									previousWordPosition[j] = findWordIndex;
								}
								findWordIndex = findWord(s_word[j],wordStackSize[j],temp);
								if(wordNotFound) {
									wordNotFound = false;
									if(wordStackSize[j] >= discretLimit_c[j]) {
										char[][] tempArray = word[j];
										char[][] s_tempArray = s_word[j];
										int[] tempPointer = pointer[j]; 
										discretLimit_c[j] *= 2;
										word[j] = new char[2 * wordStackSize[j]][];
										s_word[j] = new char[2 * wordStackSize[j]][];
										pointer[j] = new int[2 * wordStackSize[j]];
										System.arraycopy(tempArray, 0, word[j], 0, wordStackSize[j]);
										System.arraycopy(s_tempArray, 0, s_word[j], 0, wordStackSize[j]);
										System.arraycopy(tempPointer, 0, pointer[j], 0, wordStackSize[j]);
									}
									int z=0;
									System.arraycopy(s_word[j],findWordIndex,s_word[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
									System.arraycopy(pointer[j],findWordIndex,pointer[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
									s_word[j][findWordIndex] = temp;
									pointer[j][findWordIndex] = wordStackSize[j];
									word[j][wordStackSize[j]] = temp;
									item[j][i] = wordStackSize[j];
									wordStackSize[j]++;
									wordCount[j][pointer[j][findWordIndex]] = 1; // new word saved
								} else {
									item[j][i] = pointer[j][findWordIndex];
									wordCount[j][pointer[j][findWordIndex]]++;
									wordNotFound = true;
								}
							} else {
								previousWord[j] = temp;
								previousWordPosition[j] = 0;
								s_word[j][0] = temp;
								pointer[j][0] = 0;
								word[j][0] = temp;
								item[j][i] = 0;
								wordCount[j][0]++;
								wordStackSize[j]++;
							}
						}
						i++; j=0;
						continue;
					} else {
						buffer.reset();
					}
				}
				
				// lies Wortlänge ein
				if(numericalColumn[j] == false) {
					buffer.position(buffer.position()+1);
					buffer.mark();
					while (buffer.hasRemaining()) {
						b = buffer.get();
						if (b == QUOTE) {
							buffer.reset();
							break;
						} else k++;
					}
				}
				if (!buffer.hasRemaining())
					buffer.reset();
				temp = new char[k]; // geht schnell
				for (int l = 0; l < k; l++) {
					b = buffer.get();
					
					temp[l] = (char) b;
					
				}


				// modified
				
				if(wordStackSize[j] != 0) {
					//System.out.println("word.length = " + word[j].length);
					if(eqCharArray(previousWord[j],temp)) {
						// System.out.println("previous " + new String(previousWord));
						// System.out.println("temp2 " + new String(temp));
						findWordIndex = previousWordPosition[j];
						// System.out.println("findWordIndex2 " + findWordIndex);
						wordNotFound = false;
					} else {
						findWordIndex = findWord(s_word[j],wordStackSize[j],temp);
						previousWord[j] = temp;
						previousWordPosition[j] = findWordIndex;
					}

					findWordIndex = findWord(s_word[j],wordStackSize[j],temp);
					//System.out.println("wordStackSize " + wordStackSize[j]);
					// System.out.println("findWordIndex " + findWordIndex);
					if(wordNotFound) {
						wordNotFound = false;
						// wordFound = true;
						
						if(wordStackSize[j] >= discretLimit_c[j]) {
							char[][] tempArray = word[j];
							char[][] s_tempArray = s_word[j];
							int[] tempPointer = pointer[j]; 
							discretLimit_c[j] *= 2;
							word[j] = new char[2 * wordStackSize[j]][];
							s_word[j] = new char[2 * wordStackSize[j]][];
							pointer[j] = new int[2 * wordStackSize[j]];
							System.arraycopy(tempArray, 0, word[j], 0, wordStackSize[j]);
							System.arraycopy(s_tempArray, 0, s_word[j], 0, wordStackSize[j]);
							System.arraycopy(tempPointer, 0, pointer[j], 0, wordStackSize[j]);
						}
						int z=0;
						/*System.out.println("/// start before");
						while(z < item[j].length) {
							System.out.print(z); System.out.print("	");
							// System.out.print(word[j][z]); System.out.print("	");
							System.out.print(item[j][z]); System.out.print("	");
							System.out.println(wordCount[j][z]);
							z++;
						}
						System.out.println("/// end before");*/
						System.arraycopy(s_word[j],findWordIndex,s_word[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
						System.arraycopy(pointer[j],findWordIndex,pointer[j],findWordIndex+1,wordStackSize[j]-findWordIndex);
						/* for(int m=0; m<i; m++) {
							if(item[j][m] >= findWordIndex) {
								item[j][m]++;
							}
						} */
						s_word[j][findWordIndex] = temp;
						pointer[j][findWordIndex] = wordStackSize[j];
						word[j][wordStackSize[j]] = temp;
						item[j][i] = wordStackSize[j];
						wordStackSize[j]++;
						wordCount[j][pointer[j][findWordIndex]] = 1; // new word saved
						/*System.out.println("/// start after");
						z = 0;
						while(z < item[j].length) {
							System.out.print(z); System.out.print("	");
							// System.out.print(word[j][z]); System.out.print("	");
							System.out.print(item[j][z]); System.out.print("	");
							System.out.println(wordCount[j][z]);
							z++;
						}
						System.out.println("/// end after");*/
						
					} else {
						//System.out.println("bbb " + i);
						item[j][i] = pointer[j][findWordIndex];
						wordCount[j][pointer[j][findWordIndex]]++;
						wordNotFound = true;
					}
				} else {
					previousWord[j] = temp;
					previousWordPosition[j] = 0;
					s_word[j][0] = temp;
					pointer[j][0] = 0;
					word[j][0] = temp;
					item[j][i] = 0;
					wordCount[j][0]++;
					wordStackSize[j]++;
				}
				
				// end modified

				if (temp.length == 2 && temp[0] == 'N' && temp[1] == 'A') NA[j][i] = true;
				else if (temp.length == 3 && temp[0] == 'N' && temp[1] == 'a' && temp[2] == 'N') NA[j][i] = true;
				
				if(!numericalColumn[j]) {
					buffer.position(buffer.position()+1); // QUOTE wird eingelesen
				}
				if (buffer.hasRemaining()) {
					b = buffer.get();
					if (b == SEPERATOR) {
						j++;
					}
					else if (b == RETURN) {
						progressing(); // execute progressing() every new line
						buffer.mark();
						if (buffer.hasRemaining()) {
							if (buffer.get() == NEWLINE) {
								i++;
								j = 0;
							} else {
								buffer.reset();
								i++;
								j = 0;
							}
						}
					} else if (b == NEWLINE) {
						progressing(); // execute progressing() every new line
						i++;
						j = 0;
					}
				}

				// numericalColumn[j] = true
			} else {

				if(buffer.hasRemaining()) {
					buffer.mark();
					b = buffer.get();
					if(b == SEPERATOR) {
						NA[j][i] = true;
						item[j][i] = 0.0/0.0;
						j++;
						continue;
					} else if(b == RETURN) {
						if(buffer.get(buffer.position()-2) == SEPERATOR) {
							NA[j][i] = true;
							item[j][i] = 0.0/0.0;
						}
						buffer.position(buffer.position()-1);
						

					} else if(b == NEWLINE) {
						if(buffer.get(buffer.position()-2) == SEPERATOR) {
							NA[j][i] = true;
							item[j][i] = 0.0/0.0;
						}
						buffer.position(buffer.position()-1);

					} else {
						buffer.reset();
					}
				}
				while (buffer.hasRemaining()) {
					b = buffer.get();
					if (b == (byte) 'N') {
						item[j][i] = 0.0 / 0.0;
						if (buffer.get() == 'A') {
							NA[j][i] = true;
							break;
						} // NA
						else if (buffer.get() == 'N') {
							NA[j][i] = true;
							break;
						} // NaN
					} else if (b == MINUS) {
						while (buffer.hasRemaining()) {
							b = buffer.get();
							if (b == DOT) {
								while (buffer.hasRemaining()) {
									b = buffer.get();
									if (b == SEPERATOR || b == NEWLINE || b == RETURN) {
										break;
									} else {
										item[j][i] = item[j][i] + (b - 48) * Math.pow(10, -(++k));
									}
								}
								break;
							} else if (b == SEPERATOR || b == RETURN || b == NEWLINE) {
								break;
							} else
								item[j][i] = item[j][i] * 10 + (b - 48);
						}
						item[j][i] = -item[j][i];
						break;
					} else if (b == DOT) {
						while (buffer.hasRemaining()) {
							b = buffer.get();
							if (b == SEPERATOR || b == NEWLINE || b == RETURN)
								break;
							else {
								item[j][i] = item[j][i] + (b - 48) * Math.pow(10, -(++k));
							}
						}
						break;
					} else if(b == SEPERATOR || b == RETURN || b == NEWLINE) {
						break;
					} else {
						item[j][i] = item[j][i] * 10 + (b - 48);
					}
				} // end while
				
				// modified
				if(isDiscret[j]) {
					for(int m=0; m<wordStackSize[j]; m++) {
						if(discretValue[j][m] == item[j][i]) {
							valueFound = true;
							findValueIndex = m;
							break;
						}
					}
					if(!valueFound) {
						wordCount[j][wordStackSize[j]] = 1; // new item inserted
						discretValue[j][wordStackSize[j]] = item[j][i];
						wordStackSize[j]++;
						if(wordStackSize[j] >= discretLimit_c[j]) {
							isDiscret[j] = false;
						}
					} else {
						wordCount[j][findValueIndex]++;
						valueFound = false;
					}
				}
				
				// end modified

				
				buffer.position(buffer.position() - 1);
				if (buffer.hasRemaining()) {
					b = buffer.get();
					if (b == SEPERATOR) { 
						j++;}
					else if (b == RETURN) {
						progressing(); // execute progressing() every new line
						buffer.mark();
						if (buffer.hasRemaining()) {
							if (buffer.get() == NEWLINE) {
								i++;
								j = 0;
							} else {
								buffer.reset();
								i++;
								j = 0;
							}
						}
					} else if (b == NEWLINE) {
						progressing(); // execute progressing() every new line
						i++;
						j = 0;
					}
				}
			}

		}
	}
	
	
	
	
	
	private boolean eqCharArray(char[] char1, char[] char2) {

		int i = char1.length;
		int j = char2.length;

		if (i != j)
			return false;

		for (int k = 0; k < i; k++) {
			if (char1[k] == char2[k]) {
			} else
				return false;
		}

		return true;

	}
	
	public double charArraytoDouble(char[] charArray, boolean isNA) {
		double number = 0;
		boolean dotAvailable = false;
		int l=0;
		int j=0;
		
		if(isNA) return 0.0/0.0;
		
		if(charArray[0] != '-') {
			for(int k=0; k<charArray.length; k++) {
				if(charArray[k] == '.') {
					dotAvailable = true;
					l = k;
					break;
				} else {
					number = number * 10 + (charArray[k] - 48);
				}
			}
			if(dotAvailable) {
				for(int k=l+1; k<charArray.length; k++) {
					j++;
					number = number + ((charArray[k] -48) * Math.pow(10,-j));

				}
			}

		} else {
			for(int k=1; k<charArray.length; k++) {
				if(charArray[k] == '.') {
					dotAvailable = true;
					l = k;
					break;
				} else {
					number = number * 10 + (charArray[k] - 48);
				}
				
			}
			if(dotAvailable) {
				for(int k=l+1; k<charArray.length; k++) {
					j++;
					number = number + ((charArray[k] -48) * Math.pow(10,-j));
				}
			}
			number = - number;
		}
		return number;
	}
	
	public StringBuffer findRegion(ByteBuffer buffer, int position) {
		
		byte b;
		StringBuffer region = new StringBuffer();
		buffer.position(position);
		int k = 0;
		int i = 0;
		
		while(buffer.position()>0) {
			buffer.position(buffer.position()-1);
			b = buffer.get(buffer.position());
			if(b == RETURN || b == NEWLINE) {
				break;
			} else {
				if(k>=15) break;
				k++;
			}
		}
		buffer.position(buffer.position()+1);
		while(buffer.hasRemaining()) {
			b = buffer.get();
			if(b == RETURN || b == NEWLINE) {
				break;
			} else {
				if(i == 15+k) break;
				else i++;
				region.append((char)b);
			}
			
		}
		return region;

	}
	
	// compare char arrays by lexicographical order
	// NullPointerException not implemented cause of speed performance
	// return -1 if char1 < char2, 0 if char1 = char2, +1 if char1 > char2
	private int compareCharArrays(char[] char1, char[] char2) {

		if(char1.length < char2.length) {
			for(int i=0; i<char1.length; i++) {
				if(char1[i] < char2[i]) {
					return -1;
				} else if(char1[i] > char2[i]) {
					return 1;
				} else {
					continue;
				}
			}
			return -1;
		} else if(char1.length > char2.length) {
			for(int i=0; i<char2.length; i++) {
				if(char1[i] < char2[i]) {
					return -1;
				} else if(char1[i] > char2[i]) {
					return 1;
				} else {
					continue;
				}
			}
			return 1;
		} else {
			for(int i=0; i<char1.length; i++) {
				if(char1[i] < char2[i]) {
					return -1;
				} else if(char1[i] > char2[i]) {
					return 1;
				} else {
					continue;
				}
			}
			return 0;
		}
	}
	
	
	// executes ActionEvent for ProgressBar
	private void progressing() {

		timestop = System.currentTimeMillis();
		
		if(timeStampCounter < timeStamps.length && timestop - timestart >= timeStamps[timeStampCounter]) {
			Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,"PROGRESS"));
			timeStampCounter++;
			timestart = System.currentTimeMillis();
			System.out.println(timeStampCounter);
		}
		

	}
	
	
	// list has to be sorted
	// returns position if word found or added to list and
	// sets wordNotFound = true or false
	private int findWord(char[][] list, int listStackSize, char[] word) {
		
		int lb = 0;
		int rb = listStackSize - 1;
		int pivot = (rb - lb) / 2;
		int compareNumber;
		
		while(true) {
			// no word found
			if(rb - lb <= 0) {
				wordNotFound = true;
				
				/*System.out.println("leftBound = " + lb);
				System.out.println("rightBound = " + rb);
				System.out.println("pivot = " + pivot);*/
				
				compareNumber = compareCharArrays(word,list[pivot]);
				//System.out.println("" + new String(word) + "	" + new String(list[pivot]) + "	" + compareNumber);

				if(compareNumber == -1) {
					wordNotFound = true;
					return lb;
				} else if(compareNumber == 1) {
					wordNotFound = true;
					return lb+1;
				} else {
					wordNotFound = false;
					return lb;
				}
			}

			compareNumber = compareCharArrays(word,list[pivot]);
			if(compareNumber == -1) {
				rb = pivot - 1;
				pivot = lb + (rb - lb) / 2;
				continue;
			} else if(compareNumber == 1) {
				lb = pivot + 1;
				pivot = lb + (rb - lb) / 2;
				continue;
			} else {
				wordNotFound = false;
				return pivot;
			}
		}
	}
	
	
	// very easy check-text method, doesn't check doubles (not yet) 
	private boolean checkIt(byte SEPERATOR) {
		
		int countWord = 0, countNumber = 0;
		boolean dotAvailable = false;
		StringBuffer text = new StringBuffer();
		String text1 = new String();
		String text2 = new String();
		byte b;
		int i = 0,j = 0;
		
		buffer.rewind();
		buffer.position(getPositionSecondLine(buffer));
		while(buffer.hasRemaining()) {
			
			b = buffer.get();
			
			if(b == SEPERATOR) {
				text1 = new String(text);
				text = new StringBuffer();
				if(!numericalColumn[j]) {
					text2 = new String((char[])getItem(j,i));
					countWord++;
				} else {
					text2 = text1;
					countNumber++;
				}
				if(!text1.equals(text2)) {
					System.out.println("text1 does not equal text2");
					return false;
				} else {
					text1 = "";
					text2 = "";
				}
				j++; dotAvailable = false;
				continue;
			} else if(b == RETURN) {
				buffer.mark();
				if(buffer.get() == NEWLINE) {
				} else {
					buffer.reset();
				}
				text1 = new String(text);
				text = new StringBuffer();
				if(!numericalColumn[j]) {
					text2 = new String((char[])getItem(j,i));
					countWord++;
				} else {
					text2 = text1;
					countNumber++;
				}
				if(!text1.equals(text2)) {
					System.out.println("text1 does not equal text2, i = " + i + " j = " + j);
					System.exit(0);
				} else {
					text1 = "";
					text2 = "";
				}
				i++; j=0; dotAvailable = false;
				continue;
			} else if(b == NEWLINE) {
				text1 = new String(text);
				text = new StringBuffer();
				if(!numericalColumn[j]) {
					text2 = new String((char[])getItem(j,i));
					countWord++;
				} else {
					text2 = text1;
					countNumber++;
				}
				if(!text1.equals(text2)) {
					System.out.println("text1 does not equal text2");
					System.exit(0);
				} else {
					text1 = "";
					text2 = "";
				}
				i++; j=0; dotAvailable = false;
				continue;
			} else {
				if(b == DOT) {
					dotAvailable = true;
				}
				text.append((char)b);
			}
		}
		return true;
	}
	
	private char[] doubleToCharArray(double d, boolean dotAvailable) {
		
		
		
		
		return null;
	}
	

/** isPolygonAvailable(ByteBuffer): returns true, if polygon is available
 	additionally remarks hard reading error, if more then 1 polygon is available */
	public boolean isPolygonAvailableInHead(ByteBuffer buffer, String format) {
		
		byte b;
		boolean polyavailable = false;
		byte SEPERATOR = TAB; // default SEPERATOR
		buffer.rewind();
		
		if(format == "TAB-Format") {
			SEPERATOR = TAB;
		} else if(format == "KOMMA-Format") {
			SEPERATOR = KOMMA;
		} else if(format == "SPACE-Format") {
			SEPERATOR = SPACE;
		} else if(format == "KOMMA-QUOTE-Format") {
			SEPERATOR = KOMMA;
		}
		
		if(format == "TAB-Format" || format == "KOMMA-Format") {
			b = buffer.get();
			if(b == (byte)'/') {
				if(buffer.hasRemaining()) {
					b = buffer.get();
					if(b == (byte)'P') {
						polyavailable = true;
					} else {
						buffer.rewind();
					}
				}
			} else {
				buffer.rewind();
			}
		
			while(buffer.hasRemaining()) {
				b = buffer.get();
			
				if(b == SEPERATOR) {
					if(buffer.hasRemaining()) {
						b = buffer.get();
						if(b == (byte)'/') {
							if(buffer.hasRemaining()) {
								b = buffer.get();
								if(b == 'P') {
									if(polyavailable) {
										error[0] = "hardError: more than 1 polygon available";
										errorposition = buffer.position();
										hardReadError = true;
									} else {
										polyavailable = true;
									}
								}
								
							}
						}
					}
				} else if(b == RETURN || b == NEWLINE) {
					buffer.rewind();
					return polyavailable;
				}
			}
		
			return true;
		} else {
			// format == "SPACE-Format" or format == "KOMMA-QUOTE-Format"
			boolean QUOTEAvailable = false;
			b = buffer.get();
			if(b == (byte)'"' && buffer.hasRemaining()) {
				b = buffer.get();
				if(b == (byte)'/' && buffer.hasRemaining()) {
					b = buffer.get();
					if(b == (byte)'P') {
						polyavailable = true;
					} else {
						buffer.rewind();
					}
				} else {
					buffer.rewind();
				}
			}
			while(buffer.hasRemaining()) {
				b = buffer.get();
			
				if(b == SEPERATOR && buffer.hasRemaining()) {
					b = buffer.get();
					if(b == (byte)'"' && !QUOTEAvailable && buffer.hasRemaining()) {
						b = buffer.get(); QUOTEAvailable = true;
						if(b == (byte)'/' && buffer.hasRemaining()) {
							b = buffer.get();
							if(b == 'P') {
								if(polyavailable) {
									error[0] = "hardReadError: more than 1 polygon available";
									errorposition = buffer.position();
									hardReadError = true;
								} else {
									polyavailable = true;
								}
							}
						
						}
					} else continue;
				} else if(b == RETURN || b == NEWLINE) {
					buffer.rewind();
					return polyavailable;
				}
			}
		
			return true;			
		}
	}
	
/** getPolygonName: returns polygonname if available, else null */
	private String getPolygonName(ByteBuffer buffer) {
		
		byte b;
		int startposition = buffer.limit()-1;
		int limitposition = buffer.limit();
		StringBuffer strbuf = new StringBuffer();
		
		b = buffer.get(buffer.limit()-1);
		if(b == RETURN || b == NEWLINE) {
			return null;
		}
			
		buffer.position(buffer.limit()-1);
		
		while(buffer.position() > 0) {
			b = buffer.get(buffer.position());
			if(b == RETURN || b == NEWLINE) {
				startposition = buffer.position()+1;
				break;
			} else {
				buffer.position(buffer.position()-1);
			}
		}
		
		buffer.position(startposition);
		
		while(buffer.hasRemaining()) {
			strbuf.append((char)buffer.get()); 
		}
		
		buffer.position(startposition-1);
		
		// test existance of empty lines before polygonName
		// sets buffer.limit() to last element
		while(buffer.position() > 0) {
			b = buffer.get(buffer.position());
			if(b == RETURN || b == NEWLINE) {
				buffer.position(buffer.position()-1);
			} else {
				limitposition = buffer.position()+1;
				break;
			}
		}
		
		buffer.position(limitposition);
		b = buffer.get();
		if(b == RETURN) {
			b = buffer.get();
			if(b == RETURN) {
				// alles ok.
			} else if(b == NEWLINE) {
				b = buffer.get();
				if(b == RETURN || b == NEWLINE) {
					// alles ok.
				} else {
					// ERROR
					error[0] = "hardError: no empty line before PolygonName in file";
					hardReadError = true;
					errorposition = buffer.position();
				}
			} else {
				// ERROR
				error[0] = "hardError: no empty line before PolygonName in file";
				hardReadError = true;
				errorposition = buffer.position();
			}
		} else if(b == NEWLINE) {
			b = buffer.get();
			if(b == RETURN) {
				// alles ok.
			} else if(b == NEWLINE) {
				// alles ok.
			} else {
				// ERROR
				error[0] = "hardError: no empty line before PolygonName in file";
				hardReadError = true;
				errorposition = buffer.position();
			}
		} else {
			// ERROR
			error[0] = "hardError: no empty line before PolygonName in file";
			hardReadError = true;
			errorposition = buffer.position();
		}
		
		
		buffer.limit(limitposition);
		
		if(error[0] != null) {
			return null;
		} else {
			return new String(strbuf);
		}
	}
}