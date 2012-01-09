package uk.co.cameronhunter.escalate;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import uk.co.cameronhunter.adapters.notificationBuilder.NotificationBuilder;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
		addPreferencesFromResource( R.xml.preferences );

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

		final CheckBoxPreference onCallEnabled = (CheckBoxPreference) findPreference( getString( R.string.on_call_key ) );
		final CheckBoxPreference showReminder = (CheckBoxPreference) findPreference( getString( R.string.show_notification_key ) );
		final EditTextPreference reminderMessage = (EditTextPreference) findPreference( getString( R.string.notification_message_key ) );

		OnPreferenceChangeListener onPreferenceChangeListener = new OnPreferenceChangeListener() {
			public boolean onPreferenceChange( Preference preference, Object newValue ) {
				if ( Boolean.TRUE.equals( (Boolean) newValue && (onCallEnabled.isChecked() || showReminder.isChecked()) ) ) {
					updateReminderNotification( reminderMessage.getText() );
				} else {
					removeReminderNotification();
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

				if ( onCallEnabled.isChecked() && showReminder.isChecked() ) {
					removeReminderNotification();
					updateReminderNotification( message );
				}

				return true;
			}
		} );

		reminderMessage.setSummary( isBlank( reminderMessage.getText() ) ? getString( R.string.notification_message_default ) : reminderMessage.getText() );

		if ( onCallEnabled.isChecked() && showReminder.isChecked() ) {
			updateReminderNotification( reminderMessage.getText() );
		}
	}

	private void updateReminderNotification( String message ) {
		NotificationManager notificationManger = (NotificationManager) getSystemService( NOTIFICATION_SERVICE );

		NotificationBuilder builder = NotificationBuilder.create( this );

		builder.setContentTitle( isBlank( message ) ? getString( R.string.notification_message_default ) : message );
		builder.setSmallIcon( android.R.drawable.ic_dialog_info );
		builder.setContentIntent( PendingIntent.getActivity( this, 0, new Intent( this, MainActivity.class ), 0 ) );
		builder.setOngoing( true );

		notificationManger.notify( "reminder", 1, builder.getNotification() );
	}

	private void removeReminderNotification() {
		NotificationManager notificationManger = (NotificationManager) getSystemService( NOTIFICATION_SERVICE );
		notificationManger.cancel( "reminder", 1 );
	}

	private static boolean isValidPattern( String pattern ) {
		try {
			Pattern.compile( pattern );
			return true;
		} catch ( PatternSyntaxException ex ) {
			return false;
		}
	}

	private boolean isBlank( String string ) {
		return string == null || string.trim().length() == 0;
	}
}
