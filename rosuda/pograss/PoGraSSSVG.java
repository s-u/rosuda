//
//  PoGraSSSVG.java
//  Klimt
//
//  Created by Simon Urbanek on Thu Oct 10 2002.
//  Copyright (c) 2001 __MyCompanyName__. All rights reserved.
//
import java.awt.*;
import java.io.*;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.dom.GenericDOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.DOMImplementation;

public class PoGraSSSVG extends PoGraSSgraphics {
    DOMImplementation domImpl;
    Document document;
    SVGGraphics2D svgGenerator;
    PrintStream ps;
    
    public PoGraSSSVG(PrintStream nps, int layer) {
        super(null,layer);
        setOutPrintStream(nps);
    }

    public PoGraSSSVG(PrintStream nps) {
        this(nps,-1);
    }

    public PoGraSSSVG()  {
        super(null,-1);
    }

    public void setOutPrintStream(PrintStream ops) {
        ps=ops;
        // Get a DOMImplementation
        domImpl = GenericDOMImplementation.getDOMImplementation();
        document = domImpl.createDocument(null, "svg", null);
        // Create an instance of the SVG Generator
        g=svgGenerator= new SVGGraphics2D(document);
    }
    
    public void begin() { curLayer=0; }
    
    public void end() {
        boolean useCSS = true; // we want to use CSS style attribute
        try {
            Writer out = new OutputStreamWriter(ps, "UTF-8");
            svgGenerator.stream(out, useCSS);
        } catch(Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        };
    }
}
