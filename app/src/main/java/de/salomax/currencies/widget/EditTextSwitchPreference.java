package de.salomax.currencies.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceDataStore;
import androidx.preference.PreferenceViewHolder;

import java.util.Objects;

import de.salomax.currencies.R;

@SuppressWarnings("unused")
public class EditTextSwitchPreference extends EditTextPreference {

    private PreferenceViewHolder holder;

    private final String KEY_SWITCH;

    public EditTextSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWidgetLayoutResource(R.layout.preference_edit_text_switch);
        KEY_SWITCH = getKey() + "_switch";
    }

    public EditTextSwitchPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setWidgetLayoutResource(R.layout.preference_edit_text_switch);
        KEY_SWITCH = getKey() + "_switch";
    }

    public EditTextSwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public EditTextSwitchPreference(Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        this.holder = holder;
        SwitchCompat btnSwitch = (SwitchCompat) holder.findViewById(android.R.id.toggle);
        // state stuff
        btnSwitch.setChecked(isChecked());
        setEditTextEnabled(isChecked());
        btnSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> setChecked(isChecked));
    }

    @Override
    protected void onSetInitialValue(Object defaultValue) {
        setEditTextEnabled(isChecked());
    }

    private void setChecked(boolean checked) {
            persistBoolean(checked);
            setEditTextEnabled(checked);
    }

    private boolean isChecked() {
        return getPersistedBoolean(false);
    }

    private void setEditTextEnabled(boolean enabled) {
        if (holder != null) {
            holder.itemView.setEnabled(enabled);
            holder.findViewById(android.R.id.title).setEnabled(enabled);
            holder.findViewById(android.R.id.summary).setEnabled(enabled);
            holder.findViewById(android.R.id.summary).setTextDirection(View.TEXT_DIRECTION_LTR);
        }
    }

    @Override
    protected boolean persistBoolean(boolean value) {
        if (!shouldPersist()) {
            return false;
        }

        // It's already there, so the same as persisting
        if (value == isChecked()) {
            return true;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            dataStore.putBoolean(KEY_SWITCH, value);
        } else {
            Objects.requireNonNull(getPreferenceManager()
                    .getSharedPreferences())
                    .edit()
                    .putBoolean(KEY_SWITCH, value)
                    .apply();
        }
        callChangeListener(value);
        return true;
    }

    @Override
    protected boolean getPersistedBoolean(boolean defaultReturnValue) {
        if (!shouldPersist()) {
            return defaultReturnValue;
        }

        PreferenceDataStore dataStore = getPreferenceDataStore();
        if (dataStore != null) {
            return dataStore.getBoolean(KEY_SWITCH, defaultReturnValue);
        } else {
            return Objects.requireNonNull(getPreferenceManager()
                    .getSharedPreferences())
                    .getBoolean(KEY_SWITCH, defaultReturnValue);
        }
    }

}
