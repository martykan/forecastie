package cz.martykan.forecastie.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import cz.martykan.forecastie.R;

public class AboutDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String VersionName;
        Context context = getContext();
        try {
            //noinspection ConstantConditions
            VersionName =  context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            VersionName = getString(R.string.about_unknown);
        }
        AlertDialog alertDialog = new AlertDialog.Builder(context)
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
        TextView message = alertDialog.findViewById(android.R.id.message);
        if (message != null) {
            message.setMovementMethod(LinkMovementMethod.getInstance());
        }
        return alertDialog;
    }
}
