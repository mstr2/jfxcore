/*
 * Copyright (c) 2022, JFXcore and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  JFXcore designates this
 * particular file as subject to the "Classpath" exception as provided
 * in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 */

#include "jni.h"
#include "resvg/c-api/resvg.h"

extern "C" {

namespace {
    const char* errorMessage(resvg_error err) {
        switch (err) {
            case RESVG_ERROR_NOT_AN_UTF8_STR: return "Only UTF-8 content is supported";
            case RESVG_ERROR_FILE_OPEN_FAILED: return "Failed to open the provided file";
            case RESVG_ERROR_MALFORMED_GZIP: return "Compressed SVG must use the GZip algorithm";
            case RESVG_ERROR_ELEMENTS_LIMIT_REACHED: return "SVG element limit exceeded";
            case RESVG_ERROR_INVALID_SIZE: return "Invalid size";
            case RESVG_ERROR_PARSING_FAILED: return "Failed to parse SVG data";
            default: return "Unknown error";
        }
    }

    jint throwInvalidArgumentException(JNIEnv* env, const char* message) {
        jclass cls = env->FindClass("java/lang/InvalidArgumentException");
        return env->ThrowNew(cls, message);
    }

    jobject createImageData(JNIEnv* env, jint width, jint height, jbyteArray data) {
        jclass cls = env->FindClass("com/sun/javafx/iio/svg/SVGImageData");
        jmethodID ctor = env->GetMethodID(cls, "<init>", "(II[B)V");
        jobject inst  = env->NewObject(cls, ctor, width, height, data);
        env->DeleteLocalRef(cls);
        return inst;
    }

    jobject createSize(JNIEnv* env, jdouble width, jdouble height) {
        jdouble elements[] = { width, height };
        jdoubleArray size = env->NewDoubleArray(2);
        env->SetDoubleArrayRegion(size, 0, 2, elements);
        return size;
    }

    struct global_t {
        resvg_options* options;
        global_t() {
            resvg_init_log();
            options = resvg_options_create();
            resvg_options_load_system_fonts(options);
            resvg_options_set_shape_rendering_mode(options, RESVG_SHAPE_RENDERING_GEOMETRIC_PRECISION);
            resvg_options_set_text_rendering_mode(options, RESVG_TEXT_RENDERING_GEOMETRIC_PRECISION);
            resvg_options_set_image_rendering_mode(options, RESVG_IMAGE_RENDERING_OPTIMIZE_QUALITY);
        }
        ~global_t() {
            resvg_options_destroy(options);
        }
    } global;
}

/*
 * Class:     com_sun_javafx_iio_svg_SVGImageLoader
 * Method:    parseDocument
 * Signature: ([B)J
 */
JNIEXPORT jlong JNICALL Java_com_sun_javafx_iio_svg_SVGImageLoader_parseDocument(
        JNIEnv* env, jclass, jbyteArray data) {
    if (data == nullptr) {
        return 0;
    }

    jbyte* dataPtr = env->GetByteArrayElements(data, 0);
    resvg_render_tree* tree = nullptr;
    resvg_error err = (resvg_error)resvg_parse_tree_from_data(
        (const char*)dataPtr, env->GetArrayLength(data), global.options, &tree);
    env->ReleaseByteArrayElements(data, dataPtr, JNI_COMMIT);

    if (err != RESVG_OK) {
        throwInvalidArgumentException(env, errorMessage(err));
        return 0;
    }

    return jlong(tree);
}

/*
 * Class:     com_sun_javafx_iio_svg_SVGImageLoader
 * Method:    freeDocument
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_sun_javafx_iio_svg_SVGImageLoader_freeDocument(
        JNIEnv*, jclass, jlong handle) {
    resvg_tree_destroy((resvg_render_tree*)handle);
}

/*
 * Class:     com_sun_javafx_iio_svg_SVGImageLoader
 * Method:    renderDocument
 * Signature: (JIIDD)Lcom/sun/javafx/iio/svg/SVGImageData;
 */
JNIEXPORT jobject JNICALL Java_com_sun_javafx_iio_svg_SVGImageLoader_renderDocument(
        JNIEnv* env, jclass, jlong handle, jint width, jint height, jdouble scaleX, jdouble scaleY) {
    jbyteArray pixels = env->NewByteArray(width * height * 4);
    if (env->ExceptionCheck()) {
        return nullptr;
    }

    jbyte* pixelsPtr = env->GetByteArrayElements(pixels, 0);
    resvg_fit_to fit_to { RESVG_FIT_TO_TYPE_ORIGINAL, 0 };
    resvg_transform identity { scaleX, 0.0, 0.0, scaleY, 0.0, 0.0 };
    resvg_render((resvg_render_tree*)handle, fit_to, identity, (uint32_t)width, (uint32_t)height, (char*)pixelsPtr);
    env->ReleaseByteArrayElements(pixels, pixelsPtr, JNI_COMMIT);

    return createImageData(env, width, height, pixels);
}

/*
 * Class:     com_sun_javafx_iio_svg_SVGImageLoader
 * Method:    getImageSize
 * Signature: (J)[D
 */
JNIEXPORT jobject JNICALL Java_com_sun_javafx_iio_svg_SVGImageLoader_getImageSize(
        JNIEnv* env, jclass, jlong handle) {
    resvg_size size = resvg_get_image_size((resvg_render_tree*)handle);
    return createSize(env, size.width, size.height);
}

}
