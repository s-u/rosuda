#include "jri.h"
#include "Rengine.h"
#include <R_ext/Parse.h>
#include <R_ext/eventloop.h>

int initR() {
    char *args[3]={"Rengine","--vanilla",0};

    //getenv("R_HOME","/Library/Frameworks/R.framework/Resources",1);
    if (!getenv("R_HOME")) {
        fprintf(stderr, "R_HOME is not set. Please set all required environment variables before running this program.\n");
        return -1;
    }
    
    int stat=Rf_initEmbeddedR(2,args);
    if (stat<0) {
        printf("Failed to initialize embedded R! (stat=%d)\n",stat);
        return -1;
    };
    return 0;
}

#define SEXP2L(s) ((long)(s))
#define L2SEXP(s) ((SEXP)((int)(s)))

JNIEXPORT jint JNICALL Java_Rengine_rniSetupR
  (JNIEnv *je, jobject this)
{
      printf("rniSetupR\n");
      return initR();
}

JNIEXPORT jlong JNICALL Java_Rengine_rniParse
  (JNIEnv *env, jobject this, jstring str, jint parts)
{
      ParseStatus ps;
      SEXP pstr, cv;

      PROTECT(cv=jri_getString(env, str));
      printf("parsing \"%s\"\n", CHAR(STRING_ELT(cv,0)));
      pstr=R_ParseVector(cv, parts, &ps);
      printf("parse status=%d, result=%x, type=%d\n", ps, (int) pstr, (pstr!=0)?TYPEOF(pstr):0);
      UNPROTECT(1);

      return SEXP2L(pstr);
}

JNIEXPORT jlong JNICALL Java_Rengine_rniEval
  (JNIEnv *env, jobject this, jlong exp, jlong rho)
{
      SEXP es, exps=L2SEXP(exp);
      int er=0;
      int i=0,l;

      if (exp<1) return -1;

      if (TYPEOF(exps)==EXPRSXP) { /* if the object is a list of exps, eval them one by one */
          l=LENGTH(exps);
          while (i<l) {
              es=R_tryEval(VECTOR_ELT(exps,i), R_GlobalEnv, &er);
              i++;
          }
      } else
          es=R_tryEval(exps, R_GlobalEnv, &er);

      return SEXP2L(es);
}

JNIEXPORT jstring JNICALL Java_Rengine_rniGetString
  (JNIEnv *env, jobject this, jlong exp)
{
      return jri_putString(env, L2SEXP(exp), 0);
}


JNIEXPORT jobjectArray JNICALL Java_Rengine_rniGetStringArray
  (JNIEnv *env, jobject this, jlong exp)
{
      return jri_putStringArray(env, L2SEXP(exp));
}

JNIEXPORT jintArray JNICALL Java_Rengine_rniGetIntArray
  (JNIEnv *env, jobject this, jlong exp)
{
      return jri_putIntArray(env, L2SEXP(exp));
}

JNIEXPORT jintArray JNICALL Java_Rengine_rniGetDoubleArray
  (JNIEnv *env, jobject this, jlong exp)
{
      return jri_putDoubleArray(env, L2SEXP(exp));
}

JNIEXPORT jint JNICALL Java_Rengine_rniExpType
  (JNIEnv *env, jobject this, jlong exp)
{
    return (exp<0)?0:TYPEOF(L2SEXP(exp));
}

JNIEXPORT void JNICALL Java_Rengine_rniIdle
  (JNIEnv *env, jobject this)
{
    R_runHandlers(R_InputHandlers, R_checkActivity(0, 1));
}
