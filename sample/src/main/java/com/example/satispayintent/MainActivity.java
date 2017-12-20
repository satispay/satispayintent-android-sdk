package com.example.satispayintent;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;

import com.example.satispayintent.databinding.ActivityMainBinding;
import com.satispay.satispayintent.SatispayIntent;


public class MainActivity extends AppCompatActivity {
    private static final int darkRedColor = Color.rgb(200, 0, 0);
    private static final int darkGreenColor = Color.rgb(0, 200, 0);
    private static final int darkBlueColor = Color.rgb(0, 0, 200);
    private static final String TAG = MainActivity.class.getSimpleName();

    public class Handlers {
        public Uri uriForOpenAppOrNull(String scheme) {
            try {
                return SatispayIntent.uriForOpenApp(scheme);
            } catch (Exception e) { return null; }
        }

        public Uri uriForPayChargeIdOrNull(String scheme, String appId, String token) {
            try {
                return SatispayIntent.uriForPayChargeId(scheme, appId, token);
            } catch (Exception e) { return null; }
        }

        public Uri uriForPayPhoneAmountOrNull(String scheme, String phoneNumber, String amount) {
            try {
                return SatispayIntent.uriForPayPhoneAmount(scheme, phoneNumber, amount);
            } catch (Exception e) { return null; }
        }


        public Uri uriForDeveloperPlaygroundOrNull(String scheme, String version) {
            try {
                return SatispayIntent.uriForDeveloperPlayground(scheme, version);
            } catch (Exception e) { return null; }
        }

        public void openUri(View view, String uriString) {
            Uri uri = parseUriOrNull(uriString);
            if (uri != null) {
                Intent intent = SatispayIntent.intentFromUri(uri);
                if (SatispayIntent.isIntentSafe(MainActivity.this, intent)) {
                    appendToLogs(stringColor("--> [Open] " + uri, darkBlueColor));
                    int requestCode = 0;    // You should set different requestCode for different request
                    startActivityForResult(intent, requestCode);
                } else {
                    snack(view, "Cannot open this URI");
                }
            } else {
                snack(view, "URI not valid");
            }
        }

        public void getApiStatus(View view, String appPackage, String uriString) {
            Uri uri = parseUriOrNull(uriString);
            if (uri != null) {
                appendToLogs(stringColor("--> [Check] " + uri, darkBlueColor));
                SatispayIntent.ApiStatus apiStatus = SatispayIntent.getApiStatus(MainActivity.this, appPackage, uri);
                if (apiStatus.isValidRequest()) {
                    appendToLogs(stringColor("<-- " + apiStatus, darkGreenColor));
                } else {
                    appendToLogs(stringColor("<-- " + apiStatus + " | hint: " + getErrorHint(apiStatus.getCode()), darkRedColor));
                }
            } else {
                snack(view, "URI not valid");
            }
        }

        public void openPlayStore(View view, String appPackage) {
            Intent intent = SatispayIntent.openPlayStore(view.getContext(), appPackage);
            if (SatispayIntent.isIntentSafe(MainActivity.this, intent)) {
                appendToLogs(stringColor("--> [Open] " + intent.getDataString(), darkBlueColor));
                startActivity(intent);
            } else {
                snack(view, "Cannot open this URI");
            }
        }

        // Utils

        private Uri parseUriOrNull(String uriString) {
            try {
                return Uri.parse(uriString);
            } catch (Exception e) {
                return null;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SatispayIntent.ApiStatus apiStatus = SatispayIntent.ApiStatus.from(resultCode, data);
        Log.d(TAG, "onActivityResult: " + apiStatus);
        if (apiStatus.isValidRequest()) {
            appendToLogs(stringColor("<-- " + apiStatus, darkGreenColor));
        } else {
            appendToLogs(stringColor("<-- " + apiStatus + " | hint: " + getErrorHint(apiStatus.getCode()), darkRedColor));
        }
    }

    private String getErrorHint(int errorCode) {
        String hint;
        switch (errorCode) {
            case SatispayIntent.RESULT_CANCEL_BAD_REQUEST:
                hint = "BAD REQUEST";
                break;
            case SatispayIntent.RESULT_CANCEL_FORBIDDEN:
                hint = "FORBIDDEN: User cannot proceed (User is logged?)";
                break;
            case SatispayIntent.RESULT_CANCEL_NOT_FOUND:
                hint = "NOT FOUND: Wrong URI or Satispay app cannot handle this URI yet";
                break;
            case SatispayIntent.RESULT_CANCEL_GONE:
                hint = "GONE: Indicates that the resource requested is no longer available and will not be available again";
                break;
            case SatispayIntent.RESULT_CANCEL_UPGRADE_REQUIRED:
                hint = "UPGRADE REQUIRED: Upgrade SatispayIntent SDK is REQUIRED!";
                break;
            case SatispayIntent.RESULT_CANCEL_TOO_MANY_REQUESTS:
                hint = "TOO MANY REQUESTS: Try again later";
                break;
            case SatispayIntent.RESULT_ERROR_UNKNOWN:
                hint = "UNKNOWN: Old Satispay app? Wrong appPackage? Other reason?";
                break;
            case SatispayIntent.RESULT_ERROR_SCHEME_NOT_FOUND:
                hint = "SCHEME NOT FOUND: Wrong scheme? Is Satispay installed? Restricted access?";
                break;
            default:
                hint = "NEW ERROR CODE: Try to update SatispayIntent SDK!";
                break;
        }
        return hint;
    }


    // Activity utils

    private SpannableString stringColor(String string, int color) {
        SpannableString spannableString = SpannableString.valueOf(string);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannableString;
    }

    private void appendToLogs(CharSequence text) {
        Log.d(TAG, "Append to logs: " + text);
        fields.logs.set(TextUtils.concat(fields.logs.get(), "\n", text));
    }

    private void snack(View view, String text) {
        Log.d(TAG, "Snackbar: " + text);
        Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Android DataBinding (two way) code
     * Initialize this sample activity, handle lifecycle
     */

    private static final Fields fields = new Fields();

    public static class Fields {
        public final ObservableField<CharSequence> logs = new ObservableField<CharSequence>("API Logs:");
        public final ObservableField<String> scheme = new ObservableField<>(SatispayIntent.SANDBOX_SCHEME);
        public final ObservableField<String> appId = new ObservableField<>("generic");
        public final ObservableField<String> appPackage = new ObservableField<>(SatispayIntent.SANDBOX_APP_PACKAGE);
        public final ObservableField<String> chargeId = new ObservableField<>("t123");
        public final ObservableField<String> phone = new ObservableField<>();
        public final ObservableField<String> amount = new ObservableField<>("100");
        public final ObservableField<String> playgroundVersion = new ObservableField<>("1");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        binding.setFields(fields);
        binding.setHandlers(new Handlers());
    }
}
