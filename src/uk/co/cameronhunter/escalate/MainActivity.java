package uk.co.cameronhunter.escalate;

import static uk.co.cameronhunter.adapters.booleanPreference.BooleanPreference.isChecked;
import static uk.co.cameronhunter.utils.StringUtils.isBlank;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.RingtonePreference;

public class MainActivity extends PreferenceActivity {

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );

		if ( android.os.Build.VERSION.SDK_INT < 14 || getApplicationInfo().targetSdkVersion < 14 ) {
			addPreferencesFromResource( R.xml.preferences );
		} else {
			addPreferencesFromResource( R.xml.preferences14 );
		}

		RingtonePreference ringtone = (RingtonePreference) findPreference( getString( R.string.ringtone_key ) );
		ringtone.setDefaultValue( RingtoneManager.getDefaultUri( RingtoneManager.TYPE_ALARM ).toString() );

		EditTextPreference regex = (EditTextPreference) findPreference( getString( R.string.regex_key ) );
		regex.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
			public boolean onPreferenceChange( Preference preference, Object newValue ) {
				String pattern = (String) newValue;
				if ( !isBlank( pattern ) && isValidPattern( pattern ) ) {
					preference.setSummary( pattern );
					return true;
				}
				return false;
			}
		} );

		if ( !isBlank( regex.getText() ) ) {
			regex.setSummary( regex.getText() );
		}

		final Preference onCallEnabled = findPreference( getString( R.string.on_call_key ) );
		final CheckBoxPreference showReminder = (CheckBoxPreference) findPreference( getString( R.string.show_notification_key ) );
		final EditTextPreference reminderMessage = (EditTextPreference) findPreference( getString( R.string.notification_message_key ) );

		OnPreferenceChangeListener onPreferenceChangeListener = new OnPreferenceChangeListener() {
			public boolean onPreferenceChange( Preference preference, Object newValue ) {
				if ( Boolean.TRUE.equals( (Boolean) newValue && (isChecked( onCallEnabled ) || isChecked( showReminder )) ) ) {
					Intent updateReminder = new Intent( getString( R.string.update_reminder_intent ) );
					updateReminder.putExtra( getString( R.string.notification_message_key ), reminderMessage.getText() );
					sendBroadcast( updateReminder );
				} else {
					sendBroadcast( new Intent( getString( R.string.remove_reminder_intent ) ) );
				}
				return true;
			}
		};

		onCallEnabled.setOnPreferenceChangeListener( onPreferenceChangeListener );
		showReminder.setOnPreferenceChangeListener( onPreferenceChangeListener );

		reminderMessage.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
			public boolean onPreferenceChange( Preference preference, Object newValue ) {
				String value = (String) newValue;
				String message = isBlank( value ) ? getString( R.string.notification_message_default ) : value;

				preference.setSummary( message );

				if ( isChecked( onCallEnabled ) && isChecked( showReminder ) ) {
					Intent updateReminder = new Intent( getString( R.string.update_reminder_intent ) );
					updateReminder.putExtra( getString( R.string.notification_message_key ), message );
					sendBroadcast( updateReminder );
				}

				return true;
			}
		} );

		reminderMessage.setSummary( isBlank( reminderMessage.getText() ) ? getString( R.string.notification_message_default ) : reminderMessage.getText() );

		if ( isChecked( onCallEnabled ) && isChecked( showReminder ) ) {
			sendBroadcast( new Intent( getString( R.string.update_reminder_intent ) ) );
		}
	}

	private static boolean isValidPattern( String pattern ) {
		try {
			Pattern.compile( pattern );
			return true;
		} catch ( PatternSyntaxException ex ) {
			return false;
		}
	}

}
