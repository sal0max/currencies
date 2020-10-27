package de.salomax.currencies.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

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
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        TextView summaryView = (TextView) holder.findViewById(android.R.id.summary);
        summaryView.setMaxLines(50);
    }
}
