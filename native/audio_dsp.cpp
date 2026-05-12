#include <jni.h>
#include <math.h>

extern "C" JNIEXPORT jint JNICALL
Java_com_dam_audiodigital_1tfg_audio_CppAudioBridge_applySaturation(JNIEnv *env, jobject obj, jint velocity, jfloat drive) {

    float input = (float)velocity / 127.0f;

    // Si no hay drive, devolvemos la original
    if (drive <= 1.0f) return velocity;

    // Esta fórmula eleva el exponente para "empujar" las notas bajas hacia arriba
    // Si drive es alto, incluso un roce en la tecla sonará potente
    float power = 1.0f / (1.0f + (drive / 10.0f));
    float output = pow(input, power);

    int finalVelocity = (int)(output * 127.0f);

    if (finalVelocity > 127) finalVelocity = 127;
    return (jint)finalVelocity;
}