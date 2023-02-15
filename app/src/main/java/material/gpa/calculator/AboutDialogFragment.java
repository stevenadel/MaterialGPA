package material.gpa.calculator;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

public class AboutDialogFragment extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String versionName;
        Context context = getContext();
        try {
            assert context != null;
            versionName =  context.getPackageManager().getPackageInfo(context.getPackageName(),0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            versionName = "Unknown version";
        }
        AlertDialog alertDialog = new AlertDialog.Builder(context)
                .setTitle(getText(R.string.app_name))
                .setMessage(TextUtils.concat(versionName, "\n\n",
                        "A simple tool for calculating GPA.", "\n\n\n",
                        getText(R.string.emoji)))
                .setPositiveButton("ok", null)
                .create();
        alertDialog.show();
        return alertDialog;
    }
}

