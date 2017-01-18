//
// Created by Administrator on 2015/12/24.
//
#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <android/log.h>
#include <unistd.h>
#include <sys/inotify.h>

#include <android/log.h> //导入log.h

#define LOG_TAG "@@@"  //指定打印到logcat的Tag
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)


/**
 * 将Java中的String转换成c中的char字符串
 */
char *Jstring2CStr(JNIEnv *env, jstring jstr) {
    char *rtn = NULL;
    jclass clsstring = (*env)->FindClass(env, "java/lang/String"); //String
    jstring strencode = (*env)->NewStringUTF(env, "GB2312"); // 得到一个java字符串 "GB2312"
    jmethodID mid = (*env)->GetMethodID(env, clsstring, "getBytes", "(Ljava/lang/String;)[B"); //[ String.getBytes("gb2312");
    jbyteArray barr = (jbyteArray)(*env)->CallObjectMethod(env, jstr, mid, strencode); // String .getByte("GB2312");
    jsize alen = (*env)->GetArrayLength(env, barr); // byte数组的长度
    jbyte *ba = (*env)->GetByteArrayElements(env, barr, JNI_FALSE);
    if (alen > 0) {
        rtn = (char *) malloc(alen + 1); //""
        memcpy(rtn, ba, alen);
        rtn[alen] = 0;
    }
    (*env)->ReleaseByteArrayElements(env, barr, ba, 0); //
    return rtn;
}

JNICALL Java_com_xml_library_services_RestartSevice_restartAservice
        (JNIEnv *env, jobject thiz, jstring serviceName, jint sdkVersion) {
    char *name = Jstring2CStr(env, serviceName);

//fork子进程，以执行轮询任务
    pid_t pid = fork();
    if (pid < 0) {
// fork失败了
    } else if (pid == 0) {
// 可以一直采用一直判断文件是否存在的方式去判断，但是这样效率稍低，下面使用监听的方式，死循环，每个一秒判断一次
        int check = 1;
        while (check) {
            pid_t ppid = getppid();
            if (ppid == 1) {
                if (sdkVersion >= 17) {
                    int ret = execlp("am", "am", "startservice", "--user", "0",
                                     "-n", name,
                                     (char *) NULL);
                } else {
                    execlp("am", "am", "startservice", "-n",
                           name,
                           (char *) NULL);
                }
                check = 0;
            } else {
            }
            sleep(1);
        }
    } else {
//父进程直接退出，使子进程被init进程领养，以避免子进程僵死
    }

}

