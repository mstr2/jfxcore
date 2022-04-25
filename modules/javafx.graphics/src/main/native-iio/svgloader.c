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

static resvg_options* svg_options = 0;

static const char* svg_error_message(resvg_error err) {
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

/*
 * Class:     com_sun_javafx_iio_svg_SVGImageLoader
 * Method:    init
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_sun_javafx_iio_svg_SVGImageLoader_init(void) {
    svg_options = resvg_options_create();
    resvg_options_load_system_fonts(svg_options);
    resvg_options_set_shape_rendering_mode(svg_options, RESVG_SHAPE_RENDERING_GEOMETRIC_PRECISION);
    resvg_options_set_text_rendering_mode(svg_options, RESVG_TEXT_RENDERING_GEOMETRIC_PRECISION);
    resvg_options_set_image_rendering_mode(svg_options, RESVG_IMAGE_RENDERING_OPTIMIZE_QUALITY);
}

/*
 * Class:     com_sun_javafx_iio_svg_SVGImageLoader
 * Method:    parseDocument
 * Signature: ([B)J
 */
JNIEXPORT jlong JNICALL Java_com_sun_javafx_iio_svg_SVGImageLoader_parseDocument(
        JNIEnv* env, jclass cls, jbyteArray data) {
    if (data == 0) {
        return 0;
    }

    jbyte* dataPtr = (*env)->GetByteArrayElements(env, data, 0);
    if (dataPtr == 0) {
        return 0;
    }

    resvg_render_tree* tree = 0;
    resvg_error err = (resvg_error)resvg_parse_tree_from_data(
        (const char*)dataPtr, (*env)->GetArrayLength(env, data), svg_options, &tree);
    (*env)->ReleaseByteArrayElements(env, data, dataPtr, JNI_COMMIT);

    if (err != RESVG_OK) {
        jclass cls = (*env)->FindClass(env, "java/io/IOException");
        (*env)->ThrowNew(env, cls, svg_error_message(err));
        return 0;
    }

    return (jlong)tree;
}

/*
 * Class:     com_sun_javafx_iio_svg_SVGImageLoader
 * Method:    freeDocument
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_sun_javafx_iio_svg_SVGImageLoader_freeDocument(
        JNIEnv* env, jclass cls, jlong handle) {
    resvg_tree_destroy((resvg_render_tree*)handle);
}

/*
 * Class:     com_sun_javafx_iio_svg_SVGImageLoader
 * Method:    renderDocument
 * Signature: (JIIDD)Lcom/sun/javafx/iio/svg/SVGImageData;
 */
JNIEXPORT jobject JNICALL Java_com_sun_javafx_iio_svg_SVGImageLoader_renderDocument(
        JNIEnv* env, jclass cls, jlong handle, jint width, jint height, jdouble scaleX, jdouble scaleY) {
    jbyteArray pixels = (*env)->NewByteArray(env, width * height * 4);
    if ((*env)->ExceptionCheck(env)) {
        return 0;
    }

    resvg_fit_to fit_to;
    fit_to.type = RESVG_FIT_TO_TYPE_ORIGINAL;
    fit_to.value = 0;

    resvg_transform identity;
    identity.a = scaleX;
    identity.d = scaleY;
    identity.b = identity.c = identity.e = identity.f = 0;

    jbyte* pixelsPtr = (*env)->GetByteArrayElements(env, pixels, 0);
    resvg_render((resvg_render_tree*)handle, fit_to, identity, (uint32_t)width, (uint32_t)height, (char*)pixelsPtr);
    (*env)->ReleaseByteArrayElements(env, pixels, pixelsPtr, JNI_COMMIT);

    cls = (*env)->FindClass(env, "com/sun/javafx/iio/svg/SVGImageData");
    jmethodID ctor = (*env)->GetMethodID(env, cls, "<init>", "(II[B)V");
    jobject imageData = (*env)->NewObject(env, cls, ctor, width, height, pixels);
    (*env)->DeleteLocalRef(env, cls);
    return imageData;
}

/*
 * Class:     com_sun_javafx_iio_svg_SVGImageLoader
 * Method:    getImageSize
 * Signature: (J)[D
 */
JNIEXPORT jobject JNICALL Java_com_sun_javafx_iio_svg_SVGImageLoader_getImageSize(
        JNIEnv* env, jclass cls, jlong handle) {
    resvg_size size = resvg_get_image_size((resvg_render_tree*)handle);
    jdouble elements[] = { size.width, size.height };
    jdoubleArray imageSize = (*env)->NewDoubleArray(env, 2);
    (*env)->SetDoubleArrayRegion(env, imageSize, 0, 2, elements);
    return imageSize;
}
