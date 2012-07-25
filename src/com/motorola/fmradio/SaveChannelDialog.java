package com.motorola.fmradio;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.motorola.fmradio.FMDataProvider.Channels;

import java.util.ArrayList;

public class SaveChannelDialog extends AlertDialog implements
		DialogInterface.OnClickListener, CheckBox.OnCheckedChangeListener {
	private int mFrequency;
	private int mInitialPreset;
	private String mInitialName;

	private OnSaveListener mListener;
	private CheckBox mUseRdsName;
	private Spinner mPresetSpinner;
	private EditText mNameField;

	public interface OnSaveListener {
		void onPresetSaved(int preset);

		void onSaveCanceled();
	}

	public SaveChannelDialog(Context context, int frequency, int initialPreset,
			String initialName, OnSaveListener listener) {
		super(context);

		mFrequency = frequency;
		mInitialPreset = initialPreset;
		mInitialName = initialName;
		mListener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		View view = getLayoutInflater().inflate(R.layout.save_dialog, null);
		Context context = getContext();

		setIcon(0);
		setView(view);
		setInverseBackgroundForced(true);
		setTitle(R.string.save_preset);

		final TextView frequencyField = (TextView) view
				.findViewById(R.id.channel_frequency);
		frequencyField.setText(FMUtil.formatFrequency(context, mFrequency));

		mPresetSpinner = (Spinner) view.findViewById(R.id.preset_spinner);
		mUseRdsName = (CheckBox) view.findViewById(R.id.use_rds_name);
		mNameField = (EditText) view.findViewById(R.id.channel_name);

		mNameField.setText(mInitialName);
		mUseRdsName.setOnCheckedChangeListener(this);
		mUseRdsName.setChecked(TextUtils.isEmpty(mInitialName));

		setButton(DialogInterface.BUTTON_POSITIVE,
				context.getString(android.R.string.ok), this);
		setButton(DialogInterface.BUTTON_NEGATIVE,
				context.getString(android.R.string.cancel), this);

		initPresetSpinner();

		super.onCreate(savedInstanceState);
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			ContentValues cv = new ContentValues();
			int id = mPresetSpinner.getSelectedItemPosition();
			final Uri uri = Uri.withAppendedPath(Channels.CONTENT_URI,
					String.valueOf(id));

			cv.put(Channels.FREQUENCY, mFrequency);
			cv.put(Channels.NAME, mUseRdsName.isChecked() ? "" : mNameField
					.getText().toString());

			getContext().getContentResolver().update(uri, cv, null, null);

			if (mListener != null) {
				mListener.onPresetSaved(id);
			}
		} else {
			if (mListener != null) {
				mListener.onSaveCanceled();
			}
		}
		dismiss();
	}

	@Override
	public void onCheckedChanged(CompoundButton view, boolean isChecked) {
		mNameField.setVisibility(isChecked ? View.GONE : View.VISIBLE);
	}

	private void initPresetSpinner() {
		Context context = getContext();
		Cursor cursor = context.getContentResolver().query(
				Channels.CONTENT_URI, FMUtil.PROJECTION, null, null, null);
		if (cursor != null) {
			ArrayList<String> results = new ArrayList<String>();
			int i = 1;

			cursor.moveToFirst();
			while (!cursor.isAfterLast()) {
				results.add(FMUtil.getPresetUiString(context, cursor, i));
				cursor.moveToNext();
				i++;
			}
			cursor.close();

			ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
					android.R.layout.simple_spinner_item, results);
			adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			mPresetSpinner.setAdapter(adapter);
			mPresetSpinner.setSelection(mInitialPreset);
		}
	}
}
