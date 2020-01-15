package cz.martykan.forecastie.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import cz.martykan.forecastie.R;

public class AboutDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String VersionName;
        Context Cont = getContext();
        try {
            VersionName =  Cont.getPackageManager().getPackageInfo(Cont.getPackageName(),0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            VersionName = getString(R.string.about_unknown);
        }
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getText(R.string.app_name))
                .setMessage(TextUtils.concat(VersionName, "\n\n",
                        getText(R.string.about_description), "\n\n",
                        getText(R.string.about_developers), "\n\n",
                        getText(R.string.about_src), "\n\n",
                        getText(R.string.about_issues), "\n\n",
                        getText(R.string.about_data), "\n\n",
                        getText(R.string.about_icons)))
                .setPositiveButton(R.string.dialog_ok, null)
                .create();
        alertDialog.show();
        ((TextView)alertDialog.findViewById(android.R.id.message)).setMovementMethod(LinkMovementMethod.getInstance());
        return alertDialog;
    }
}
