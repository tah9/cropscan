//
// Created by tah9 on 2023/3/19.
//
#include <ctime>
#include <iostream>
#include <utility>
#include <vector>
#include <dirent.h>
#include <list>
#include <sys/types.h>
#include <sys/stat.h>
#include <unistd.h>
#include "./FileClass/Scanner.cpp"
#include "unistd.h"
#include "FileClass/Scanner.h"


#define JNI_TARGET_CLASS "com/nuist/gallery/ActGallery"  //Java类的路径：包名+类名
#define JNI_CLASS_PICTURE_BEAN "com/nuist/gallery/bean/PictureBean"  //Java Bean类路径
#define JNI_CLASS_FOLDER_BEAN "com/nuist/gallery/bean/FolderBean"


static jclass jcls;
static jclass pc_cls;
static jclass folder_cls;
static jobject jobj;
static jmethodID jCallbackMid;
static jmethodID jCallbackFolderMid;
static jmethodID pc_construct_method;
static jmethodID folder_construct_method;
static jclass list_cls;
JNIEnv *env = nullptr;
JavaVM *jvm = nullptr;

/*
* env包含java线程
*/
void Scanner::doCallbackFolder() {
    //Attach主线程
//    jvm->AttachCurrentThread(reinterpret_cast<JNIEnv **>(reinterpret_cast<void **>(&env)),
//                             nullptr);
//    jvm->AttachCurrentThread(&env, nullptr); //绑定当前线程，获取当前线程的JNIEnv

    //获得ArrayList类引用，结束后释放
    list_cls = env->FindClass("java/util/ArrayList");
    if (list_cls == nullptr) {
        cout << "listcls is null \n";
    }
    jmethodID list_costruct = env->GetMethodID(list_cls, "<init>", "()V"); //获得集合构造函数Id
//
//    //创建list局部引用，结束后释放
    jobject list_obj = env->NewLocalRef(
            env->NewObject(list_cls, list_costruct)); //创建一个Arraylist集合对象
    //或得Arraylist类中的 add()方法ID，其方法原型为： boolean add(Object object) ;
    jmethodID list_add = env->GetMethodID(list_cls, "add", "(Ljava/lang/Object;)Z");

    for (int i = 0; i < v_folder.size(); ++i) {
        auto &folder = v_folder[i];

        jstring first_path = env->NewStringUTF(folder.first_path.c_str());
        jstring path = env->NewStringUTF(folder.path.c_str());
        jstring name = env->NewStringUTF(folder.name.c_str());
        jlong time = folder.m_time;
        jint size = folder.size;
        //构造一个javabean文件对象
        jobject java_PcBean = env->NewObject(folder_cls, folder_construct_method,
                                             name, first_path, path, size, time);
        //执行Arraylist类实例的add方法，添加一个对象
        env->CallBooleanMethod(list_obj, list_add, java_PcBean);

        //释放局部引用
        env->DeleteLocalRef(first_path);
        env->DeleteLocalRef(path);
        env->DeleteLocalRef(name);

        env->DeleteLocalRef(java_PcBean);
    }
    //调用java回调方法
    env->CallVoidMethod(jobj, jCallbackFolderMid, list_obj);

    //释放局部引用
    env->DeleteLocalRef(list_cls);
    env->DeleteLocalRef(list_obj);


//    jvm->DetachCurrentThread();
}

void Scanner::doCallback(size_t left, size_t right) {
    if (right == 0) return;
    //Attach主线程
//    jvm->AttachCurrentThread(reinterpret_cast<JNIEnv **>(reinterpret_cast<void **>(&env)),
//                             nullptr);
//    jvm->AttachCurrentThread(&env, nullptr); //绑定当前线程，获取当前线程的JNIEnv

    //获得ArrayList类引用，结束后释放
    list_cls = env->FindClass("java/util/ArrayList");
    if (list_cls == nullptr) {
        cout << "listcls is null \n";
    }
    jmethodID list_costruct = env->GetMethodID(list_cls, "<init>", "()V"); //获得集合构造函数Id
//
//    //创建list局部引用，结束后释放
    jobject list_obj = env->NewLocalRef(
            env->NewObject(list_cls, list_costruct)); //创建一个Arraylist集合对象
    //或得Arraylist类中的 add()方法ID，其方法原型为： boolean add(Object object) ;
    jmethodID list_add = env->GetMethodID(list_cls, "add", "(Ljava/lang/Object;)Z");

    for (size_t i = left; i < right; ++i) {
        auto &file = allFile[i];
        jstring path = env->NewStringUTF(file.path.c_str());
        jlong time = file.time;
        //构造一个javabean文件对象
        jobject java_PcBean = env->NewObject(pc_cls, pc_construct_method,
                                             path, time);
        //执行Arraylist类实例的add方法，添加一个对象
        env->CallBooleanMethod(list_obj, list_add, java_PcBean);

        //释放局部引用
        env->DeleteLocalRef(path);
        env->DeleteLocalRef(java_PcBean);
    }
    //调用java回调方法
    env->CallVoidMethod(jobj, jCallbackMid, list_obj);

    //释放局部引用
    env->DeleteLocalRef(list_cls);
    env->DeleteLocalRef(list_obj);


//    jvm->DetachCurrentThread();
}


/**
 * 动态注册的方法一定要有  JNIEnv env, jobject thiz 两个参数
 */
void scan(JNIEnv *env, jobject thiz, jstring root_path) {
    ::env = env;

    LOGI("scan begin time> %ld", getMs());
    jobj = env->NewGlobalRef(thiz);
    //获取回调目标类
    jcls = (jclass) env->NewGlobalRef(env->FindClass(JNI_TARGET_CLASS));
//    jmethodID mainId = env->GetMethodID(jcls, "<init>", "()V");
//    jobj = env->NewGlobalRef(env->NewObject(jcls, mainId));
    //获取回调方法ID
    jCallbackMid = env->GetMethodID(jcls, "nativeCallback", "(Ljava/util/ArrayList;)V");
    jCallbackFolderMid = env->GetMethodID(jcls, "nativeCallbackFolder", "(Ljava/util/ArrayList;)V");


    pc_cls = (jclass) (env->NewGlobalRef(
            env->FindClass(JNI_CLASS_PICTURE_BEAN)));//获得图片类引用
    folder_cls = (jclass) (env->NewGlobalRef(
            env->FindClass(JNI_CLASS_FOLDER_BEAN)));//获得文件夹类引用

    //获得该类型的构造函数  函数名为 <init> 返回类型必须为 void 即 V
    pc_construct_method = env->GetMethodID(pc_cls, "<init>",
                                           "(Ljava/lang/String;J)V");
    folder_construct_method = env->GetMethodID(folder_cls, "<init>",
                                               "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;IJ)V");



    // TODO: implement scan()
    auto path = env->GetStringUTFChars(root_path, nullptr);
    Scanner scanner(path);
    //释放字符串，放入jstring和env创建的字符串
    env->ReleaseStringUTFChars(root_path, path);

    env->DeleteGlobalRef(jobj);
    env->DeleteGlobalRef(jcls);
    env->DeleteGlobalRef(pc_cls);
    env->DeleteGlobalRef(folder_cls);

    LOGI("Release");
    JNI_OnUnload(jvm, nullptr);
}

