#include <stdio.h>
#include "jri.h"
#include "org_rosuda_JRI_Rengine.h"
#include <R_ext/Parse.h>
#include <R_ext/eventloop.h>

jobject engineObj;
jclass engineClass;
JNIEnv *eenv;

/* from Defn.h */
extern Rboolean R_Interactive;   /* TRUE during interactive use*/

extern FILE*    R_Consolefile;   /* Console output file */
extern FILE*    R_Outputfile;   /* Output file */
extern char*    R_TempDir;   /* Name of per-session dir */

/* from src/unix/devUI.h */

extern void (*ptr_R_Suicide)(char *);
extern void (*ptr_R_ShowMessage)();
extern int  (*ptr_R_ReadConsole)(char *, unsigned char *, int, int);
extern void (*ptr_R_WriteConsole)(char *, int);
extern void (*ptr_R_ResetConsole)();
extern void (*ptr_R_FlushConsole)();
extern void (*ptr_R_ClearerrConsole)();
extern void (*ptr_R_Busy)(int);
/* extern void (*ptr_R_CleanUp)(SA_TYPE, int, int); */
extern int  (*ptr_R_ShowFiles)(int, char **, char **, char *, Rboolean, char *);
extern int  (*ptr_R_ChooseFile)(int, char *, int);
extern void (*ptr_R_loadhistory)(SEXP, SEXP, SEXP, SEXP);
extern void (*ptr_R_savehistory)(SEXP, SEXP, SEXP, SEXP);

int Re_ReadConsole(char *prompt, unsigned char *buf, int len, int addtohistory)
{
    if (eenv && engineObj) {
        jstring r;
        jstring s=(*eenv)->NewStringUTF(eenv, prompt);
        jmethodID mid=(*eenv)->GetMethodID(eenv, engineClass, "jriReadConsole", "(Ljava/lang/String;I)Ljava/lang/String;");
        printf("jriReadconsole mid=%x\n", mid);
        if (!mid) return -1;
        r=(jstring) (*eenv)->CallObjectMethod(eenv, engineObj, mid, s, addtohistory);
        (*eenv)->DeleteLocalRef(eenv, s);
        if (r) {
            const char *c=(*eenv)->GetStringUTFChars(eenv, s, 0);
            if (!c) return -1;
            {
                int l=strlen(c);
                strncpy(buf, c, (l>len-1)?len-1:l);
                printf("Re_ReadConsole succeeded: \"%s\"\n",buf);
            }
            (*eenv)->ReleaseStringUTFChars(eenv, s, c);
            (*eenv)->DeleteLocalRef(eenv, r);
            return 1;
        }
    }
    return -1;
}

void Re_Busy(int which)
{
    jmethodID mid=(*eenv)->GetMethodID(eenv, engineClass, "jriBusy", "(I)V");
    printf("jriBusy mid=%x\n", mid);
    if (mid) (*eenv)->CallVoidMethod(eenv, engineObj, mid, which);
}

void Re_WriteConsole(char *buf, int len)
{
    jstring s=(*eenv)->NewStringUTF(eenv, buf);
    jmethodID mid=(*eenv)->GetMethodID(eenv, engineClass, "jriWriteConsole", "(Ljava/lang/String;)V");
    printf("jriWriteconsole mid=%x\n", mid);
    if (mid)
        (*eenv)->CallVoidMethod(eenv, engineObj, mid, s);
    (*eenv)->DeleteLocalRef(eenv, s);
}

/* Indicate that input is coming from the console */
void Re_ResetConsole()
{
}

/* Stdio support to ensure the console file buffer is flushed */
void Re_FlushConsole()
{
}

/* Reset stdin if the user types EOF on the console. */
void Re_ClearerrConsole()
{
}

int Re_ChooseFile(int new, char *buf, int len)
{
    int namelen;
    char *bufp;
    R_ReadConsole("Enter file name: ", (unsigned char *)buf, len, 0);
    namelen = strlen(buf);
    bufp = &buf[namelen - 1];
    while (bufp >= buf && isspace((int)*bufp))
        *bufp-- = '\0';
    return strlen(buf);
}

void Re_ShowMessage(char *buf)
{
    jstring s=(*eenv)->NewStringUTF(eenv, buf);
    jmethodID mid=(*eenv)->GetMethodID(eenv, engineClass, "jriShowMessage", "(Ljava/lang/String)V");
    printf("jriShowMessage mid=%x\n", mid);
    if (mid)
        (*eenv)->CallVoidMethod(eenv, engineObj, mid, s);
    (*eenv)->DeleteLocalRef(eenv, s);
}

void Re_read_history(char *buf)
{
}

void Re_loadhistory(SEXP call, SEXP op, SEXP args, SEXP env)
{
    SEXP sfile;
    char file[PATH_MAX], *p;

    /*
    checkArity(op, args);
    sfile = CAR(args);
    if (!isString(sfile) || LENGTH(sfile) < 1)
        errorcall(call, "invalid file argument");
    p = R_ExpandFileName(CHAR(STRING_ELT(sfile, 0)));
    if(strlen(p) > PATH_MAX - 1)
        errorcall(call, "file argument is too long");
    strcpy(file, p);
     */
}

void Re_savehistory(SEXP call, SEXP op, SEXP args, SEXP env)
{
    SEXP sfile;
    char file[PATH_MAX], *p;
    /*
    checkArity(op, args);
    sfile = CAR(args);
    if (!isString(sfile) || LENGTH(sfile) < 1)
        errorcall(call, "invalid file argument");
    p = R_ExpandFileName(CHAR(STRING_ELT(sfile, 0)));
    if(strlen(p) > PATH_MAX - 1)
        errorcall(call, "file argument is too long");
    strcpy(file, p);
        write_history(file);
        history_truncate_file(file, R_HistorySize);
     */
}


/*
 R_CleanUp is invoked at the end of the session to give the user the
 option of saving their data.
 If ask == SA_SAVEASK the user should be asked if possible (and this
                                                            option should not occur in non-interactive use).
 If ask = SA_SAVE or SA_NOSAVE the decision is known.
 If ask = SA_DEFAULT use the SaveAction set at startup.
 In all these cases run .Last() unless quitting is cancelled.
 If ask = SA_SUICIDE, no save, no .Last, possibly other things.
 */

/*
void Re_CleanUp(SA_TYPE saveact, int status, int runLast)
{
    unsigned char buf[1024];
    char * tmpdir;

    if(saveact == SA_DEFAULT)
        saveact = SaveAction;

    if(saveact == SA_SAVEASK) {
        if(R_Interactive) {
qask:
            R_ClearerrConsole();
            R_FlushConsole();
            R_ReadConsole("Save workspace image? [y/n/c]: ",
                          buf, 128, 0);
            switch (buf[0]) {
                case 'y':
                case 'Y':
                    saveact = SA_SAVE;
                    break;
                case 'n':
                case 'N':
                    saveact = SA_NOSAVE;
                    break;
                case 'c':
                case 'C':
                    jump_to_toplevel();
                    break;
                default:
                    goto qask;
            }
        } else
            saveact = SaveAction;
    }
    switch (saveact) {
        case SA_SAVE:
            if(runLast) R_dot_Last();
            if(R_DirtyImage) R_SaveGlobalEnv();
                 stifle_history(R_HistorySize);
                 write_history(R_HistoryFile);
                 break;
        case SA_NOSAVE:
            if(runLast) R_dot_Last();
            break;
        case SA_SUICIDE:
        default:
            break;
    }
    R_RunExitFinalizers();
    CleanEd();
    if(saveact != SA_SUICIDE) KillAllDevices();
    if((tmpdir = getenv("R_SESSION_TMPDIR"))) {
        snprintf((char *)buf, 1024, "rm -rf %s", tmpdir);
        R_system((char *)buf);
    }
    if(saveact != SA_SUICIDE && R_CollectWarnings)
        PrintWarnings();
    fpu_setup(FALSE);

    exit(status);
}

void Rstd_Suicide(char *s)
{
    REprintf("Fatal error: %s\n", s);
    R_CleanUp(SA_SUICIDE, 2, 0);
}

*/
int Re_ShowFiles(int nfile, 		/* number of files */
                 char **file,		/* array of filenames */
                 char **headers,	/* the `headers' args of file.show. Printed before each file. */
                 char *wtitle,          /* title for window = `title' arg of file.show */
                 Rboolean del,	        /* should files be deleted after use? */
                 char *pager)		/* pager to be used */
{
    return 1;
}

int initR() {
    char *args[3]={"Rengine","--no-save",0};

    //getenv("R_HOME","/Library/Frameworks/R.framework/Resources",1);
    if (!getenv("R_HOME")) {
        fprintf(stderr, "R_HOME is not set. Please set all required environment variables before running this program.\n");
        return -1;
    }
    
    int stat=Rf_initialize_R(2,args);
    if (stat<0) {
        printf("Failed to initialize embedded R! (stat=%d)\n",stat);
        return -1;
    };

    printf("R primary initialization done. Setting up parameters.\n");

    R_Outputfile = NULL;
    R_Consolefile = NULL;
    R_Interactive = 1;

    /* ptr_R_Suicide = Re_Suicide; */
    /* ptr_R_CleanUp = Re_CleanUp; */
    ptr_R_ShowMessage = Re_ShowMessage;
    ptr_R_ReadConsole = Re_ReadConsole;
    ptr_R_WriteConsole = Re_WriteConsole;
    ptr_R_ResetConsole = Re_ResetConsole;
    ptr_R_FlushConsole = Re_FlushConsole;
    ptr_R_ClearerrConsole = Re_ClearerrConsole;
    ptr_R_Busy = Re_Busy;
    /*
    ptr_R_ShowFiles = Re_ShowFiles;
    ptr_R_ChooseFile = Re_ChooseFile;
    ptr_R_loadhistory = Re_loadhistory;
    ptr_R_savehistory = Re_savehistory;
     */
    printf("Setting up R event loop\n");
    
    setup_Rmainloop();

    printf("R initialized.\n");
    
    return 0;
}

#define SEXP2L(s) ((long)(s))
#define L2SEXP(s) ((SEXP)((int)(s)))

JNIEXPORT jint JNICALL Java_org_rosuda_JRI_Rengine_rniSetupR
  (JNIEnv *env, jobject this)
{
      int initRes;
      printf("rniSetupR\n");

      engineObj=(*env)->NewGlobalRef(env, this);
      engineClass=(*env)->NewGlobalRef(env, (*env)->GetObjectClass(env, engineObj));
      eenv=env;
      
      initRes=initR();

      return initRes;
}

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniParse
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

JNIEXPORT jlong JNICALL Java_org_rosuda_JRI_Rengine_rniEval
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

JNIEXPORT jstring JNICALL Java_org_rosuda_JRI_Rengine_rniGetString
  (JNIEnv *env, jobject this, jlong exp)
{
      return jri_putString(env, L2SEXP(exp), 0);
}


JNIEXPORT jobjectArray JNICALL Java_org_rosuda_JRI_Rengine_rniGetStringArray
  (JNIEnv *env, jobject this, jlong exp)
{
      return jri_putStringArray(env, L2SEXP(exp));
}

JNIEXPORT jintArray JNICALL Java_org_rosuda_JRI_Rengine_rniGetIntArray
  (JNIEnv *env, jobject this, jlong exp)
{
      return jri_putIntArray(env, L2SEXP(exp));
}

JNIEXPORT jintArray JNICALL Java_org_rosuda_JRI_Rengine_rniGetDoubleArray
  (JNIEnv *env, jobject this, jlong exp)
{
      return jri_putDoubleArray(env, L2SEXP(exp));
}

JNIEXPORT jint JNICALL Java_org_rosuda_JRI_Rengine_rniExpType
  (JNIEnv *env, jobject this, jlong exp)
{
    return (exp<0)?0:TYPEOF(L2SEXP(exp));
}

JNIEXPORT void JNICALL Java_org_rosuda_JRI_Rengine_rniIdle
  (JNIEnv *env, jobject this)
{
    R_runHandlers(R_InputHandlers, R_checkActivity(0, 1));
}

JNIEXPORT void JNICALL Java_org_rosuda_JRI_Rengine_rniRunMainLoop
  (JNIEnv *env, jobject this)
{
      /* this doesn't work yet!! we need nuch more from main/main.c :(
      SETJMP(R_Toplevel.cjmpbuf);
      R_GlobalContext = R_ToplevelContext = &R_Toplevel;
      R_ReplConsole(R_GlobalEnv, 0, 0);
      */
}
