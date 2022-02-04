package de.salomax.currencies.widget;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

@SuppressWarnings("unused")
public class LongSummaryPreference extends Preference {

    public LongSummaryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public LongSummaryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public LongSummaryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LongSummaryPreference(Context context) {
        super(context);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        // allow more text
        summaryView.setMaxLines(50);
        // make links clickable
        summaryView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
