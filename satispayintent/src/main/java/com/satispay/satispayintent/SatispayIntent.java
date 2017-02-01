package com.satispay.satispayintent;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Locale;

/**
 * Created on 14/06/16, 17:25
 */

public final class SatispayIntent {
    public static final String PRODUCTION_APP_PACKAGE = "com.satispay.customer";
    public static final String PRODUCTION_SCHEME = "satispay";

    public static final String HOST = "external";

    public static final int RESULT_ERROR_SCHEME_NOT_FOUND = -1;
    public static final int RESULT_ERROR_UNKNOWN = 0;
    public static final int RESULT_OK_VALID_REQUEST = 200;
    public static final int RESULT_CANCEL_BAD_REQUEST = 400;
    public static final int RESULT_CANCEL_FORBIDDEN = 403;     // User cannot proceed
    public static final int RESULT_CANCEL_NOT_FOUND = 404;
    public static final int RESULT_CANCEL_GONE = 410;   // After @deprecated: Indicates that the resource requested is no longer available and will not be available again.
    public static final int RESULT_CANCEL_UPGRADE_REQUIRED = 426;
    public static final int RESULT_CANCEL_TOO_MANY_REQUESTS = 429;


    // Intent utils

    public static boolean isSatispayAvailable(@NonNull Context context, @NonNull String scheme) {
        return isIntentSafe(context, intentFromUri(uriForOpenApp(scheme)));
    }

    /**
     * From: http://developer.android.com/training/basics/intents/sending.html#Verify
     *
     * Although the Android platform guarantees that certain intents will resolve to one of the built-in apps (such as the Phone, Email, or Calendar app),
     * you should always include a verification step before invoking an intent.
     *
     * Caution: If you invoke an intent and there is no app available on the device that can handle the intent, your app will crash.
     *
     * @param context Context: A Context of the application package implementing this class.
     * @param intent Intent: The description of the activity to start.
     * @return If it is true, then at least one app will respond to the intent. If it is false, then there aren't any apps to handle the intent.
     */
    public static boolean isIntentSafe(@NonNull Context context, @NonNull Intent intent) {
        return context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }

    /**
     * From: http://developer.android.com/reference/android/content/Intent.html#ACTION_VIEW
     *
     * Activity Action: Display the data to the user. This is the most common action performed on data -- it is the generic action you can use on a
     * piece of data to get the most reasonable thing to occur. For example, when used on a contacts entry it will view the entry; when used on a
     * mailto: URI it will bring up a compose window filled with the information supplied by the URI; when used with a tel: URI it will invoke the dialer.
     *
     * @param uri
     * @return
     */
    @NonNull
    public static Intent intentFromUri(@NonNull Uri uri) {
        return new Intent(Intent.ACTION_VIEW).setData(uri);
    }


    // Satispay scheme generators
    @NonNull
    public static Uri uriForApiProvider(@NonNull String appPackage, @NonNull String path) {
        return Uri.parse(String.format("content://%s.apiprovider/%s", appPackage, path));
    }

    @NonNull
    public static Uri uriForOpenApp(@NonNull String scheme) {
        if (TextUtils.isEmpty(scheme)) throw new IllegalArgumentException("Required: scheme");
        return Uri.parse(String.format("%s:", scheme));
    }

    @NonNull
    public static Uri uriForOpenPlayStoreWithMarket(@NonNull String appPackage) {
        if (TextUtils.isEmpty(appPackage)) throw new IllegalArgumentException("Required: appPackage");
        return Uri.parse(String.format("market://details?id=%s", appPackage));
    }

    @NonNull
    public static Uri uriForOpenPlayStoreWithHttps(@NonNull String appPackage) {
        if (TextUtils.isEmpty(appPackage)) throw new IllegalArgumentException("Required: appPackage");
        return Uri.parse(String.format("https://play.google.com/store/apps/details?id=%s", appPackage));
    }

    @Deprecated @NonNull
    public static Uri uriForPayToken(@NonNull String scheme, @NonNull String appId, @NonNull String token) {
        return uriForPayChargeId(scheme, appId, token);
    }

    @NonNull
    public static Uri uriForPayChargeId(@NonNull String scheme, @NonNull String appId, @NonNull String token) {
        if (TextUtils.isEmpty(scheme)) throw new IllegalArgumentException("Required: scheme");
        if (TextUtils.isEmpty(appId)) throw new IllegalArgumentException("Required: appId");
        if (TextUtils.isEmpty(token)) throw new IllegalArgumentException("Required: token");
        Uri.Builder builder = Uri.parse(String.format("%s://%s/%s/charge", scheme, HOST, appId)).buildUpon();
        builder.appendQueryParameter("token", token);
        return builder.build();
    }

    @NonNull
    public static Uri uriForPayPhoneAmount(@NonNull String scheme, @NonNull String phoneNumber, @Nullable String amount) {
        if (TextUtils.isEmpty(scheme)) throw new IllegalArgumentException("Required: scheme");
        if (TextUtils.isEmpty(phoneNumber)) throw new IllegalArgumentException("Required: phoneNumber");
        Uri.Builder builder = Uri.parse(String.format("%s://%s/generic/sendmoney", scheme, HOST)).buildUpon();
        builder.appendQueryParameter("to", phoneNumber);
        if (!TextUtils.isEmpty(amount)) builder.appendQueryParameter("amount", amount);
        return builder.build();
    }

    @NonNull
    public static Uri uriForDeveloperPlayground(@NonNull String scheme, @NonNull String version) {
        if (TextUtils.isEmpty(scheme)) throw new IllegalArgumentException("Required: scheme");
        if (TextUtils.isEmpty(version)) throw new IllegalArgumentException("Required: version");
        return Uri.parse(String.format("%s://%s/generic/playground/v%s", scheme, HOST, version));
    }


    // Check API availability

    @NonNull
    public static ApiStatus getApiStatus(@NonNull Context context, @NonNull String appPackage, @NonNull Uri uriToCheck) {
        if (TextUtils.isEmpty(appPackage)) throw new IllegalArgumentException("Required: appPackage");
        if (uriToCheck == null) throw new IllegalArgumentException("Required: uri");
        Uri uri = SatispayIntent.uriForApiProvider(appPackage, "status").buildUpon().appendQueryParameter("q", uriToCheck.toString()).build();
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        ApiStatus apiStatus;
        if (cursor != null && cursor.moveToFirst()) {
            apiStatus = ApiStatus.from(cursor);
            cursor.close();
        } else {
            apiStatus = new ApiStatus();
            apiStatus.message = "Cannot check API Availability: Please check appPackage. Maybe old Satispay app?";
        }
        return apiStatus;
    }


    // Satispay intent generators

    @NonNull
    public static Intent openApp(@NonNull String scheme) {
        return intentFromUri(uriForOpenApp(scheme));
    }

    @NonNull
    public static Intent openPlayStore(@NonNull Context context, @NonNull String appPackage) {
        Intent intent = intentFromUri(uriForOpenPlayStoreWithMarket(appPackage));
        if (!isIntentSafe(context, intent)) intent = intentFromUri(uriForOpenPlayStoreWithHttps(appPackage));
        return intent;
    }

    @Deprecated @NonNull
    public static Intent payToken(@NonNull String scheme, @NonNull String appId, @NonNull String token) {
        return payChargeId(scheme, appId, token);
    }

    @NonNull
    public static Intent payChargeId(@NonNull String scheme, @NonNull String appId, @NonNull String token) {
        return intentFromUri(uriForPayToken(scheme, appId, token));
    }

    @NonNull
    public static Intent payPhoneAmount(@NonNull String scheme, @NonNull String phoneNumber, @Nullable Long amount) {
        return intentFromUri(uriForPayPhoneAmount(scheme, phoneNumber, amount == null ? null : amount.toString()));
    }


    // Support classes

    public static class ApiStatus {
        private boolean validRequest;
        private int version;
        private int code;
        private String message;
        private boolean deprecated;
        private int maxVersion;

        private ApiStatus() {
        }

        private ApiStatus initCompleted() {
            if (!validRequest && message == null) {
                message = "Request not valid!";
            }
            return this;
        }

        private static int getColumnInt(Cursor cursor, String columnName, int defaultValue) {
            int colId = cursor.getColumnIndex(columnName);
            return colId != -1 ? cursor.getInt(colId) : defaultValue;
        }

        private static String getColumnString(Cursor cursor, String columnName, String defaultValue) {
            int colId = cursor.getColumnIndex(columnName);
            return colId != -1 ? cursor.getString(colId) : defaultValue;
        }

        public static ApiStatus from(int resultCode, @Nullable Intent data) {
            ApiStatus apiStatus = new ApiStatus();
            apiStatus.validRequest = resultCode == Activity.RESULT_OK;
            if (data != null) {
                apiStatus.code = data.getIntExtra("code", apiStatus.code);
                apiStatus.version = data.getIntExtra("version", apiStatus.version);
                apiStatus.message = data.getStringExtra("message");
                apiStatus.deprecated = data.getBooleanExtra("deprecated", apiStatus.deprecated);
                apiStatus.maxVersion = data.getIntExtra("maxVersion", apiStatus.maxVersion);
            }
            return apiStatus.initCompleted();
        }

        public static ApiStatus from(@NonNull Cursor cursor) {
            ApiStatus apiStatus = new ApiStatus();
            apiStatus.validRequest = getColumnInt(cursor, "validRequest", 0) != 0;
            apiStatus.version = getColumnInt(cursor, "version", 0);
            apiStatus.code = getColumnInt(cursor, "code", 0);
            apiStatus.message = getColumnString(cursor, "message", null);
            apiStatus.deprecated = getColumnInt(cursor, "deprecated", 0) != 0;
            apiStatus.maxVersion = getColumnInt(cursor, "maxVersion", 0);
            return apiStatus.initCompleted();
        }

        public boolean isValidRequest() {
            return validRequest;
        }

        public int getVersion() {
            return version;
        }

        public int getCode() {
            return code;
        }

        @Nullable
        public String getMessage() {
            return message;
        }

        public boolean isDeprecated() {
            return deprecated;
        }

        public int getMaxVersion() {
            return maxVersion;
        }

        @Override
        public String toString() {
            return String.format(Locale.ENGLISH, "[%s/%d] v%d [max:v%d] %s%s", validRequest ? "OK" : "KO", code, version, maxVersion, deprecated ? "DEPRECATED! " : "", message);
        }
    }
}