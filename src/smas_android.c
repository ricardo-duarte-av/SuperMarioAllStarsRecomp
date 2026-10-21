/* Android only: lets the Java in-game menu (android/.../GameMenu.java) read and change the same option rows
 * the desktop Esc menu shows (kSmasMenuItems in smas_ui.c), through the same callbacks, so both stay in step
 * and the choices are saved to smas.ini the same way. */
#include <jni.h>
#include <string.h>
#include <stdio.h>

#include "smas_ui.h"

static const RecompRuntimeUiItem *find_item(JNIEnv *env, jstring key) {
    const char *k = (*env)->GetStringUTFChars(env, key, NULL);
    const RecompRuntimeUiItem *found = NULL;
    for (size_t i = 0; k && i < SMAS_MENU_ITEM_COUNT; i++)
        if (kSmasMenuItems[i].key && !strcmp(kSmasMenuItems[i].key, k)) found = &kSmasMenuItems[i];
    if (k) (*env)->ReleaseStringUTFChars(env, key, k);
    return found;
}

/* The on/off rows (sound voices, cheats) as "key\tsection\tlabel\tdescription". */
JNIEXPORT jobjectArray JNICALL
Java_com_tanookistudios_smas_GameMenu_nativeRows(JNIEnv *env, jclass cls) {
    (void)cls;
    size_t n = 0;
    for (size_t i = 0; i < SMAS_MENU_ITEM_COUNT; i++)
        if (kSmasMenuItems[i].type == RECOMP_RUNTIME_UI_BOOL) n++;
    jobjectArray out = (*env)->NewObjectArray(env, (jsize)n, (*env)->FindClass(env, "java/lang/String"), NULL);
    size_t j = 0;
    for (size_t i = 0; i < SMAS_MENU_ITEM_COUNT; i++) {
        const RecompRuntimeUiItem *it = &kSmasMenuItems[i];
        if (it->type != RECOMP_RUNTIME_UI_BOOL) continue;
        char line[1024];
        snprintf(line, sizeof line, "%s\t%s\t%s\t%s", it->key, it->section ? it->section : "",
                 it->label ? it->label : "", it->description ? it->description : "");
        (*env)->SetObjectArrayElement(env, out, (jsize)j++, (*env)->NewStringUTF(env, line));
    }
    return out;
}

JNIEXPORT jint JNICALL
Java_com_tanookistudios_smas_GameMenu_nativeGet(JNIEnv *env, jclass cls, jstring key) {
    (void)cls;
    const RecompRuntimeUiItem *it = find_item(env, key);
    int v = 0;
    if (it) kSmasMenuCallbacks.get_value(kSmasMenuCallbacks.context, it, &v);
    return v;
}

JNIEXPORT void JNICALL
Java_com_tanookistudios_smas_GameMenu_nativeSet(JNIEnv *env, jclass cls, jstring key, jint value) {
    (void)cls;
    const RecompRuntimeUiItem *it = find_item(env, key);
    if (it) kSmasMenuCallbacks.set_value(kSmasMenuCallbacks.context, it, value);
}

/* The About letter, paragraphs separated by blank lines. */
JNIEXPORT jstring JNICALL
Java_com_tanookistudios_smas_GameMenu_nativeAboutText(JNIEnv *env, jclass cls) {
    (void)cls;
    return (*env)->NewStringUTF(env, kSmasAboutText);
}
