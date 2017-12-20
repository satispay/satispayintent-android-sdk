# Satispay for third-party apps

[![Bintray](https://img.shields.io/bintray/v/satispay/maven/SatispayIntent.svg?maxAge=2592000)](https://bintray.com/satispay/maven/SatispayIntent)
[![GitHub license](https://img.shields.io/badge/license-Apache%202-blue.svg)](https://raw.githubusercontent.com/satispay/satispayintent-android-sdk/master/LICENSE)
[![Libraries.io for GitHub](https://img.shields.io/librariesio/github/satispay/satispayintent-android-sdk.svg?maxAge=2592000)]()
[![Website](https://img.shields.io/website-up-down-green-red/http/satispay.com.svg?maxAge=2592000)](https://www.satispay.com)

Documentation: v17.02.01

Satispay can be integrated in two ways:
- Option 1: Use SatispayIntent SDK (recommended)
- Option 2: Use Android Intent


# Option 1: Use SatispayIntent SDK

## Download

Download [JAR](https://bintray.com/satispay/maven/SatispayIntent#files/com/satispay/satispayintent) or grab via Maven:
```xml
<dependency>
  <groupId>com.satispay</groupId>
  <artifactId>satispayintent</artifactId>
  <version>1.0.3</version>
</dependency>
```
or Gradle:
```groovy
compile 'com.satispay:satispayintent:1.0.3'
```

## Constants
You should use these constants as parameter in SatispayIntent methods. See examples

```
SatispayIntent.PRODUCTION_SCHEME
SatispayIntent.PRODUCTION_APP_PACKAGE
```
or

```
SatispayIntent.SANDBOX_SCHEME
SatispayIntent.SANDBOX_APP_PACKAGE
```

## Generic utility

Satispay use Intent with URI

From Android Developer: https://developer.android.com/training/basics/intents/sending.html

> One of Android's most important features is an app's ability to send the user to another
> app based on an "action" it would like to perform. For example, if your app has the address
> of a business that you'd like to show on a map, you don't have to build an activity
> in your app that shows a map. Instead, you can create a request to view the address
> using an Intent.
>
> The Android system then starts an app that's able to show the address on a map.

This is an utility method to obtain an Intent from URI

```
SatispayIntent.intentFromUri(@NonNull Uri uri)
```

Before starting an Intent you should always check if current user is able to launch it.

From Android Developer: https://developer.android.com/training/basics/intents/sending.html#Verify

> NOTE: If there are no apps on the device that can receive the implicit intent, your app will
> crash when it calls startActivity().

```
SatispayIntent.isIntentSafe(@NonNull Context context, @NonNull Intent intent)
```

### Example - How to use it inside an Activity
```java
public class MyActivity extends AppCompatActivity {
    //
    // ...
    //
    private void myMethod() {
        Uri uri = Uri.parse(SatispayIntent.SANDBOX_SCHEME + ":");
        Intent intent = SatispayIntent.intentFromUri(uri);
        if (SatispayIntent.isIntentSafe(this, intent)) {
            startActivity(intent);
        } else {
            // Cannot open this URI
            // ...
        }
    }
    //
    // ...
    //
}
```

## isSatispayAvailable()

Check if Satispay app is available: installed and current the user can launch the app.
```
SatispayIntent.isSatispayAvailable(@NonNull Context context, @NonNull String scheme)
```

### Example - How to use it inside an Activity
```java
public class MyActivity extends AppCompatActivity {
    //
    // ...
    //
    private void myMethod() {
        boolean isSatispayAvailable = SatispayIntent.isSatispayAvailable(this, SatispayIntent.SANDBOX_SCHEME);

        if (isSatispayAvailable) {
            // Satispay is available
            // ...
        } else {
            // Satispay is not available
            Intent openPlayStoreIntent = SatispayIntent.openPlayStore(this, SatispayIntent.SANDBOX_APP_PACKAGE);
            startActivity(openPlayStoreIntent);
        }
    }
    //
    // ...
    //
}
```

## getApiStatus()

The third-party apps using the Android Content Providers are able to know if a URI can be
managed properly by the application that the user has installed.

```
SatispayIntent.getApiStatus(@NonNull Context context, @NonNull String appPackage, @NonNull Uri uriToCheck);
```

#### Response codes

- `SatispayIntent.RESULT_ERROR_SCHEME_NOT_FOUND`
  > Wrong scheme? Is Satispay installed? Restricted access?

- `SatispayIntent.RESULT_ERROR_UNKNOWN`
  > Old Satispay app? Wrong appPackage? Other reason?

- `SatispayIntent.RESULT_OK_VALID_REQUEST`
  > Request was handled, you may proceed.

- `SatispayIntent.RESULT_CANCEL_BAD_REQUEST`
  > Usually wrong parameters, check "message" for more info.

- `SatispayIntent.RESULT_CANCEL_FORBIDDEN`
  > User cannot proceed. Usually user is not logged.

- `SatispayIntent.RESULT_CANCEL_NOT_FOUND`
  > Wrong URI or Satispay app cannot handle this URI yet.

- `SatispayIntent.RESULT_CANCEL_GONE`
  > Indicates that the resource requested is no longer available and will not be available again, you should check the docs!

- `SatispayIntent.RESULT_CANCEL_UPGRADE_REQUIRED`
  > Probably this Intent was deprecated, you should check the docs!

- `SatispayIntent.RESULT_CANCEL_TOO_MANY_REQUESTS`
  > Try again later

### Example - How to use it inside an Activity
```java
public class MyActivity extends AppCompatActivity {
    //
    // ...
    //
    private void myMethod() {
        Uri uriToCheck = SatispayIntent.uriForOpenApp(SatispayIntent.SANDBOX_SCHEME);
        ApiStatus apiStatus = SatispayIntent.getApiStatus(this, SatispayIntent.SANDBOX_APP_PACKAGE, uriToCheck);
        if (apiStatus.isValidRequest()) {
            // proceed

        } else {
            // check error
            getErrorHint(apiStatus.getCode());
        }
    }
    //
    // ...
    //
}
```

## openApp() - Launch Satispay

If you want to open Satispay app you should check if `isSatispayAvailable()`,
after you could obtain the Intent from SDK and start it.

Steps:

1. Check if `isSatispayAvailable()`, if true you can proceed, else you can check the error code.
2. Obtain Intent, use `SatispayIntent.openApp(@NonNull String scheme)`
3. Call `startActivity()` using the Intent.


### Example - How to use it inside an Activity
```java
public class MyActivity extends AppCompatActivity {
    //
    // ...
    //
    public void satispayOpenApp() {
        boolean isSatispayAvailable = SatispayIntent.isSatispayAvailable(this, SatispayIntent.SANDBOX_SCHEME);
        
        if (isSatispayAvailable) {
            Intent openAppIntent = SatispayIntent.openApp(SatispayIntent.SANDBOX_SCHEME);
            startActivity(openAppIntent);
        } else {
            // Satispay is not available
            Intent openPlayStoreIntent = SatispayIntent.openPlayStore(this, SatispayIntent.SANDBOX_APP_PACKAGE);
            startActivity(openPlayStoreIntent);
        }
    }
    //
    // ...
    //
}
```

## Pay with charge id

You could start payment intent from your app using chargeId.

Steps:

1. Check if you could use `payChargeId()` on user device (obtain URI and use `getApiStatus()`)
2. Check response of `getApiStatus()`, if `isValidRequest()` is true you can proceed, else you can check the error code.
3. Get chargeId [from your backend](#more-info).
4. Obtain Intent, use `SatispayIntent.payChargeId(@NonNull String scheme, @NonNull String appId, @NonNull String chargeId)`
5. Call `startActivityForResult()` using the Intent, you should define a constant requestCode parameter.
6. Override `onActivityResult()` and use `SatispayIntent.ApiStatus.from(resultCode, data)` for parse the results.
7. Check `apiStatus.isValidRequest()`, if true you can proceed, else you can check the error code.
8. Now you should check your chargeId with your backend.

### Example - How to use it inside an Activity
```java
public class MyActivity extends AppCompatActivity {
    private static final int REQUEST_PAY_CHARGE_ID = 5471;
    private String chargeId;
    //
    // ...
    //
    public String obtainChargeId() {
        // get charge id from your backend
        // NOTE: You should persist the charge id, app may be killed by the system.
        // Suggest: override onSaveInstanceState(Bundle outState)
    }

    public void satispayPayChargeId() {
        Uri uriToCheck = SatispayIntent.uriForPayChargeId(SatispayIntent.SANDBOX_SCHEME, "generic", "TEST_API");
        ApiStatus apiStatus = SatispayIntent.getApiStatus(this, SatispayIntent.SANDBOX_APP_PACKAGE, uriToCheck);
        if (apiStatus.isValidRequest()) {
            String appId = "generic";
            chargeId = obtainChargeId();
            Intent intent = SatispayIntent.payChargeId(SatispayIntent.SANDBOX_SCHEME, appId, chargeId);
            if (SatispayIntent.isIntentSafe(this, intent)) {
                startActivityForResult(intent, REQUEST_PAY_CHARGE_ID);
            } else {
                // Cannot open this URI
                // ...
            }
        } else {
            // check error
            getErrorHint(apiStatus.getCode());
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PAY_CHARGE_ID) {
            SatispayIntent.ApiStatus apiStatus = SatispayIntent.ApiStatus.from(resultCode, data);
            if (apiStatus.isValidRequest()) {
                // Now you should check your charge id with your backend
                // ...
            } else {
                // There was an error, you should check getCode() for an hint
                // ...
            }
        }
    }
    //
    // ...
    //
}
```


# Option 2: Use Android Intent

From Android Developer: https://developer.android.com/training/basics/intents/sending.html

> One of Android's most important features is an app's ability to send the user to another
> app based on an "action" it would like to perform. For example, if your app has the address
> of a business that you'd like to show on a map, you don't have to build an activity
> in your app that shows a map. Instead, you can create a request to view the address
> using an Intent.
>
> The Android system then starts an app that's able to show the address on a map.

Satispay use Intent with Uri

## Constants

We recommend defining constants to identify the app Satispay in the production environment

```
public static final String SATISPAY_SCHEME = "satispay";
public static final String SATISPAY_APP_PACKAGE = "com.satispay.customer";
```

## Generic utility

Before starting an Intent you should always check if the current user is able to launch it.

From Android Developer: https://developer.android.com/training/basics/intents/sending.html#Verify

> NOTE: If there are no apps on the device that can receive the implicit intent, your app will
> crash when it calls startActivity().

### Example - How to use it inside an Activity

```java
public class MyActivity extends AppCompatActivity {
    //
    // ...
    //
    public boolean isIntentSafe(Intent intent) {
        return getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
    }
    //
    // ...
    //
}
```


## Check if Satispay app is available

To check if Satispay app is available, checks if you can open the URI `satispay:`

### Example - How to use it inside an Activity

```java
public class MyActivity extends AppCompatActivity {
    //
    // ...
    //
    public boolean isSatispayAvailable() {
        Uri uri = Uri.parse(SATISPAY_SCHEME + ":");
        Intent intent = new Intent(Intent.ACTION_VIEW).setData(uri);
        return isIntentSafe(intent);
    }
    //
    public void installSatispayIfNeeded() {
        if (!isSatispayAvailable()) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + SATISPAY_APP_PACKAGE));
            if (!isIntentSafe(intent)) {
                intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + SATISPAY_APP_PACKAGE));
            }
            startActivity(intent);
        }
    }
    //
    // ...
    //
}
```


## Check API availability

The third-party apps using the Android Content Providers are able to know if a URI can be
managed properly by the application that the user has installed.

```
content://com.satispay.customer.apiprovider/status?q=[uriToCheck]
```

Using a ContentResolver you can check the availability of an API

```
// Uri uriToCheck;
Uri uri = Uri.parse("content://" + SATISPAY_APP_PACKAGE + ".apiprovider/status")
               .buildUpon().appendQueryParameter("q", uriToCheck.toString()).build();
Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
```

### Example - How to use it inside an Activity

```java
public class MyActivity extends AppCompatActivity {
    //
    // ...
    //
    public boolean isSatispayApiAvailable(Uri uriToCheck) {
        Uri uri = Uri.parse("content://" + SATISPAY_APP_PACKAGE + ".apiprovider/status")
                       .buildUpon().appendQueryParameter("q", uriToCheck.toString()).build();
        Cursor cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int colId = cursor.getColumnIndex("validRequest");
            boolean validRequest = colId != -1 ? cursor.getInt(colId) != 0 : false;
            cursor.close();
            return validRequest;
        } else {
            // Cannot check API Availability: Please check appPackage.
            // Maybe old Satispay app?
        }
        return false;
    }
    //
    // ...
    //
}
```

#### Response codes

- `200` for `RESULT_OK_VALID_REQUEST`, request was handled, you may proceed.
- `400` for `RESULT_CANCEL_BAD_REQUEST`, usually wrong parameters, check "message" for more info.
- `403` for `RESULT_CANCEL_FORBIDDEN`, user cannot proceed. Usually user is not logged.
- `404` for `RESULT_CANCEL_NOT_FOUND`, wrong URI or Satispay app cannot handle this URI yet.
- `410` for `RESULT_CANCEL_GONE`, indicates that the resource requested is no longer available and will not be available again, you should check the docs!
- `426` for `RESULT_CANCEL_UPGRADE_REQUIRED`, probably this Intent was deprecated, you should check the docs!
- `429` for `RESULT_CANCEL_TOO_MANY_REQUESTS`, try again later




## Launch Satispay

If you want to open Satispay app you should check if `isSatispayAvailable()`,
after you could obtain the Intent and start it.

Steps:

1. Check if `isSatispayAvailable()` (defined in previous example), if true you can proceed
2. Build `openAppIntent` using follow URI: `satispay:`
3. Call `startActivity()` using the Intent

### Example - How to use it inside an Activity

```java
public class MyActivity extends AppCompatActivity {
    //
    // ...
    //
    public void satispayOpenApp() {
        if (isSatispayAvailable()) {
            Uri uri = Uri.parse(SATISPAY_SCHEME + ":");
            Intent openAppIntent = new Intent(Intent.ACTION_VIEW).setData(uri);

            // NOTE: isSatispayAvailable() already check if current user is able to launch the Intent
            startActivity(openAppIntent);
        } else {
            // Satispay is not available
            installSatispayIfNeeded();
        }
    }
    //
    // ...
    //
}
```

## Pay with charge charge id

To start a payment intent from your app, use the chargeId obtained from your server.

Please note: details on how to obtain a chargeId available at: [https://s3-eu-west-1.amazonaws.com/docs.online.satispay.com/index.html#create-a-charge](https://s3-eu-west-1.amazonaws.com/docs.online.satispay.com/index.html#create-a-charge)

Steps:

1. Check if you could use `payChargeId()` on user device (use `isSatispayApiAvailable("satispay://external/generic/charge?token=TEST_API")`)
2. Check response of `isSatispayApiAvailable()` is true you can proceed, else you can check the error code.
3. Get chargeId [from your backend](#more-info).
4. Build `payChargeIntent` using follow URI: `satispay://external/generic/charge?token=[ChargeId]`
5. Call `startActivityForResult()` using the Intent, you should define a constant requestCode parameter.
6. Override `onActivityResult()`.
7. Check `requestCode == REQUEST_PAY_CHARGE_ID` and `resultCode == Activity.RESULT_OK`, if true you can proceed, else you can check the error code.
8. Now you should check your token with your backend.

### Example - How to use it inside an Activity

```java
public class MyActivity extends AppCompatActivity {
    private static final int REQUEST_PAY_CHARGE_ID = 5471;
    private String chargeId;
    //
    // ...
    //
    public String obtainChargeId() {
        // get chargeId from your backend
        // NOTE: You should persist the charge id, app may be killed by the system.
        // Suggest: override onSaveInstanceState(Bundle outState)
    }

    public void satispayPayChargeId() {
        if (isSatispayApiAvailable(Uri.parse(SATISPAY_SCHEME + "://external/generic/charge?token=TEST_API"))) {
            // proceed
            chargeId = obtainChargeId();
            Uri uri = Uri.parse(SATISPAY_SCHEME + "://external/generic/charge?token=" + chargeId);
            Intent payChargeIdIntent = new Intent(Intent.ACTION_VIEW).setData(uri);
            if (isIntentSafe(payChargeIdIntent)) {
                startActivityForResult(payChargeIdIntent, REQUEST_PAY_CHARGE_ID);
            } else {
                // Cannot open this URI
                // ...
            }
        } else {
            // check error
            // getErrorHint(apiStatus.getCode());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_PAY_CHARGE_ID) {
            if (resultCode == Activity.RESULT_OK) {
                // Now you should check your charge id with your backend
                // ...
            } else {
                // There was an error, you should check code/message for an hint
                // int code = data.getIntExtra("code", 0);
                // ...
            }
        }
    }
    //
    // ...
    //
}
```

## More info

Additional info on how to create and handle charges are available in the Online API documentation at: https://s3-eu-west-1.amazonaws.com/docs.online.satispay.com/index.html

## License

    Copyright 2016 Satispay SpA.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
    All trademarks and registered trademarks are the property of their respective owners.
