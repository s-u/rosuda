//
//  RSrvException.java
//  Klimt
//
//  Created by Simon Urbanek on Mon Aug 18 2003.
//  Copyright (c) 2003 __MyCompanyName__. All rights reserved.
//
//  $Id$
//

package org.rosuda.JRclient;

public class RSrvException extends Exception {
    protected Rconnection conn;
    protected String err;
    protected int reqReturnCode;
    
    public RSrvException(Rconnection c, String msg) {
        this(c,msg,0);
    }

    public RSrvException(Rconnection c, String msg, int requestReturnCode) {
        super(msg);
        conn=c; reqReturnCode=requestReturnCode;
    }

    public int getRequestReturnCode() {
        return reqReturnCode;
    }
}
