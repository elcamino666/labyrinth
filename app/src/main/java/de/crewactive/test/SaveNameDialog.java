package de.crewactive.test;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;

import org.jetbrains.annotations.NotNull;

/**
 * Dialog to get name from Player
 */
public class SaveNameDialog extends AppCompatDialogFragment {

    private TextView nameEditText;
    private Singleton singleton;
    private ISaveNameDialogListener listener;

    /**
     * Callback to set listener
     *
     * @param callBack ISaveNameDialogListener
     */
    public void setCallBack(ISaveNameDialogListener callBack) {
        listener = callBack;
    }

    /**
     * Initialize dialog variables und infiltrate View
     *
     * @param savedInstanceState saved Instance State
     * @return builder
     */
    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        singleton = Singleton.getInstance();
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.save_name_dialog_layout, null);

        builder.setView(view)
                .setTitle("Success")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Save", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String name = nameEditText.getText().toString();
                        listener.applyName(name);
                    }
                });
        nameEditText = view.findViewById(R.id.nameEditTxt);
        TextView timeDialogTxt = view.findViewById(R.id.timeDialogTxt);
        timeDialogTxt.setText(singleton.getTimePlayed());

        return builder.create();
    }

}
