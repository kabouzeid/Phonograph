package com.kabouzeid.gramophone.util;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.XmlResourceParser;
import android.os.Process;
import android.util.Base64;
import android.util.Log;

import com.kabouzeid.gramophone.R;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Beesham on 3/19/2017.
 *
 * Validates that the calling package is authorized to browse a
 * {@link android.service.media.MediaBrowserService}.
 *
 * The list of allowed signing certificates and their corresponding package names is defined in
 * res/xml/allowed_media_browser_callers.xml.
 *
 * If you add a new valid caller to allowed_media_browser_callers.xml and you don't know
 * its signature, this class will print to logcat (INFO level) a message with the proper base64
 * version of the caller certificate that has not been validated. You can copy from logcat and
 * paste into allowed_media_browser_callers.xml. Spaces and newlines are ignored.
 */
public class PackageValidator {
    private static final String TAG = PackageValidator.class.getCanonicalName();

    /**
     * Map allowed callers' certificate keys to the expected caller information.
     *
     */
    private final Map<String, ArrayList<CallerInfo>> mValidCertificates;

    public PackageValidator(Context ctx) {
        mValidCertificates = readValidCertificates(ctx.getResources().getXml(
                R.xml.allowed_media_browser_callers));
    }

    private Map<String, ArrayList<CallerInfo>> readValidCertificates(XmlResourceParser parser) {
        HashMap<String, ArrayList<CallerInfo>> validCertificates = new HashMap<>();
        try {
            int eventType = parser.next();
            while (eventType != XmlResourceParser.END_DOCUMENT) {
                if (eventType == XmlResourceParser.START_TAG
                        && parser.getName().equals("signing_certificate")) {

                    String name = parser.getAttributeValue(null, "name");
                    String packageName = parser.getAttributeValue(null, "package");
                    boolean isRelease = parser.getAttributeBooleanValue(null, "release", false);
                    String certificate = parser.nextText().replaceAll("\\s|\\n", "");

                    CallerInfo info = new CallerInfo(name, packageName, isRelease);

                    ArrayList<CallerInfo> infos = validCertificates.get(certificate);
                    if (infos == null) {
                        infos = new ArrayList<>();
                        validCertificates.put(certificate, infos);
                    }
                    /*Log.v(TAG, "Adding allowed caller: " + info.name +
                            " package=" + info.packageName + " release=" + info.release +
                            " certificate=" + certificate);*/
                    infos.add(info);
                }
                eventType = parser.next();
            }
        } catch (XmlPullParserException | IOException e) {
            Log.e(TAG, e + "Could not read allowed callers from XML.");
        }
        return validCertificates;
    }

    /**
     * @return false if the caller is not authorized to get data from this MediaBrowserService
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public boolean isCallerAllowed(Context context, String callingPackage, int callingUid) {
        // Always allow calls from the framework, self app or development environment.
        if (Process.SYSTEM_UID == callingUid || Process.myUid() == callingUid) {
            return true;
        }

        if (isPlatformSigned(context, callingPackage)) {
            return true;
        }

        PackageInfo packageInfo = getPackageInfo(context, callingPackage);
        if (packageInfo == null) {
            return false;
        }
        if (packageInfo.signatures.length != 1) {
            Log.w(TAG, "Caller does not have exactly one signature certificate!");
            return false;
        }
        String signature = Base64.encodeToString(
                packageInfo.signatures[0].toByteArray(), Base64.NO_WRAP);

        // Test for known signatures:
        ArrayList<CallerInfo> validCallers = mValidCertificates.get(signature);
        if (validCallers == null) {
            Log.v(TAG, "Signature for caller " + callingPackage  + " is not valid: \n"
                    + signature);
            if (mValidCertificates.isEmpty()) {
                Log.w(TAG, "The list of valid certificates is empty. Either your file " +
                        "res/xml/allowed_media_browser_callers.xml is empty or there was an error " +
                        "while reading it. Check previous log messages.");
            }
            return false;
        }

        // Check if the package name is valid for the certificate:
        StringBuffer expectedPackages = new StringBuffer();
        for (CallerInfo info: validCallers) {
            if (callingPackage.equals(info.packageName)) {
                Log.v(TAG, "Valid caller: " + info.name + "  package=" + info.packageName +
                        " release=" + info.release);
                return true;
            }
            expectedPackages.append(info.packageName).append(' ');
        }

        Log.i(TAG, "Caller has a valid certificate, but its package doesn't match any " +
                "expected package for the given certificate. Caller's package is " + callingPackage +
                ". Expected packages as defined in res/xml/allowed_media_browser_callers.xml are (" +
                expectedPackages + "). This caller's certificate is: \n" + signature);

        return false;
    }

    /**
     * @return true if the installed package signature matches the platform signature.
     */
    private boolean isPlatformSigned(Context context, String pkgName) {
        PackageInfo platformPackageInfo = getPackageInfo(context, "android");

        // Should never happen.
        if (platformPackageInfo == null || platformPackageInfo.signatures == null
                || platformPackageInfo.signatures.length == 0) {
            return false;
        }

        PackageInfo clientPackageInfo = getPackageInfo(context, pkgName);

        return (clientPackageInfo != null && clientPackageInfo.signatures != null
                && clientPackageInfo.signatures.length > 0 &&
                platformPackageInfo.signatures[0].equals(clientPackageInfo.signatures[0]));
    }

    /**
     * @return {@link PackageInfo} for the package name or null if it's not found.
     */
    private PackageInfo getPackageInfo(Context context, String pkgName) {
        try {
            final PackageManager pm = context.getPackageManager();
            return pm.getPackageInfo(pkgName, PackageManager.GET_SIGNATURES);
        } catch (PackageManager.NameNotFoundException e) {
            Log.w(TAG, e + "Package manager can't find package: " + pkgName);
        }
        return null;
    }

    private final static class CallerInfo {
        final String name;
        final String packageName;
        final boolean release;

        public CallerInfo(String name, String packageName, boolean release) {
            this.name = name;
            this.packageName = packageName;
            this.release = release;
        }
    }
}