#ifndef __JRI_H__
#define __JRI_H__

#include <jni.h>
#include <R.h>
#include <Rinternals.h>
#include <Rdefines.h>

/* the viewpoint is from R, i.e. "get" means "Java->R" whereas "put" means "R->Java" */

#ifdef __cplusplus
extern "C" {
#endif
    
#define SEXP2L(s) ((long)(s))
#define L2SEXP(s) ((SEXP)((int)(s)))
        
jstring jri_callToString(JNIEnv *env, jobject o);

SEXP jri_getDoubleArray(JNIEnv *env, jarray o);
SEXP jri_getIntArray(JNIEnv *env, jarray o);
SEXP jri_getObjectArray(JNIEnv *env, jarray o);
SEXP jri_getString(JNIEnv *env, jstring s);
SEXP jri_getStringArray(JNIEnv *env, jarray o);
SEXP jri_getSEXPLArray(JNIEnv *env, jarray o);

SEXP jri_installString(JNIEnv *env, jstring s); /* as Rf_install, just for Java strings */

jarray  jri_putDoubleArray(JNIEnv *env, SEXP e);
jarray  jri_putIntArray(JNIEnv *env, SEXP e);
jstring jri_putString(JNIEnv *env, SEXP e, int ix); /* ix=index, 0=1st */
jarray  jri_putStringArray(JNIEnv *env, SEXP e);
jarray jri_putSEXPLArray(JNIEnv *env, SEXP e); /* SEXPs are strored as "long"s */

void jri_checkExceptions(JNIEnv *env, int describe);

#ifdef __cplusplus
}
#endif

#endif
