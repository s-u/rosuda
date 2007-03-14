/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class JRIBootstrap */

#ifndef _Included_JRIBootstrap
#define _Included_JRIBootstrap
#ifdef __cplusplus
extern "C" {
#endif
#undef JRIBootstrap_HKLM
#define JRIBootstrap_HKLM 0L
#undef JRIBootstrap_HKCU
#define JRIBootstrap_HKCU 1L
/*
 * Class:     JRIBootstrap
 * Method:    getenv
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_JRIBootstrap_getenv
  (JNIEnv *, jclass, jstring);

/*
 * Class:     JRIBootstrap
 * Method:    setenv
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_JRIBootstrap_setenv
  (JNIEnv *, jclass, jstring, jstring);

/*
 * Class:     JRIBootstrap
 * Method:    regvalue
 * Signature: (ILjava/lang/String;Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_JRIBootstrap_regvalue
  (JNIEnv *, jclass, jint, jstring, jstring);

/*
 * Class:     JRIBootstrap
 * Method:    regsubkeys
 * Signature: (ILjava/lang/String;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_JRIBootstrap_regsubkeys
  (JNIEnv *, jclass, jint, jstring);

/*
 * Class:     JRIBootstrap
 * Method:    expand
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_JRIBootstrap_expand
  (JNIEnv *, jclass, jstring);

#ifdef __cplusplus
}
#endif
#endif
