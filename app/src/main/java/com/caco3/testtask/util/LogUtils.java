package com.caco3.testtask.util;

import android.util.Log;



public class LogUtils {
    private static final int MAX_LOG_TAG_LENGTH = 23;

    private static final boolean LOGGING_ENABLED = true;

    public static String makeLogTag(String str){
        if (str.length() > MAX_LOG_TAG_LENGTH){
            return str.substring(0, MAX_LOG_TAG_LENGTH);
        } else {
            return str;
        }
    }

    public static String makeLogTag(Class clazz){
        return makeLogTag(clazz.getSimpleName());
    }

    public static void LOGE(String tag, String msg, Throwable cause){
        if (LOGGING_ENABLED){
            Log.e(tag, msg, cause);
        }
    }

    public static void LOGE(String tag, String msg){
        if (LOGGING_ENABLED){
            Log.e(tag, msg);
        }
    }

    public static void LOGD(String tag, String msg, Throwable cause){
        if (LOGGING_ENABLED){
            Log.d(tag, msg, cause);
        }
    }

    public static void LOGD(String tag, String msg){
        if (LOGGING_ENABLED){
            Log.d(tag, msg);
        }
    }

    public static void LOGI(String tag, String msg, Throwable cause){
        if (LOGGING_ENABLED){
            Log.i(tag, msg, cause);
        }
    }

    public static void LOGI(String tag, String msg){
        if (LOGGING_ENABLED){
            Log.i(tag, msg);
        }
    }

    public static void LOGW(String tag, String msg, Throwable cause){
        if (LOGGING_ENABLED){
            Log.w(tag, msg, cause);
        }
    }

    public static void LOGW(String tag, String msg){
        if (LOGGING_ENABLED){
            Log.w(tag, msg);
        }
    }

    public static void LOGV(String tag, String msg, Throwable cause){
        if (LOGGING_ENABLED){
            Log.v(tag, msg, cause);
        }
    }

    public static void LOGV(String tag, String msg){
        if (LOGGING_ENABLED){
            Log.v(tag, msg);
        }
    }


}
