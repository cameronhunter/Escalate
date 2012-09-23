package uk.co.cameronhunter.escalate;

import static android.content.Intent.ACTION_DELETE;
import static android.content.Intent.ACTION_INSERT_OR_EDIT;
import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.RingtonePreference;
import android.preference.TwoStatePreference;

public class MainActivity extends PreferenceActivity {

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        addPreferencesFromResource( R.xml.preferences );

        RingtonePreference ringtone = (RingtonePreference) findPreference( getString( R.id.ringtone_key ) );
        ringtone.setDefaultValue( RingtoneManager.getDefaultUri( RingtoneManager.TYPE_ALARM ).toString() );

        EditTextPreference regex = (EditTextPreference) findPreference( getString( R.id.regex_key ) );
        regex.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
            public boolean onPreferenceChange( Preference preference, Object newValue ) {
                String pattern = (String) newValue;
                if ( isNotBlank( pattern ) && isValidPattern( pattern ) ) {
                    preference.setSummary( pattern );
                    return true;
                }
                return false;
            }
        } );

        if ( isNotBlank( regex.getText() ) ) {
            regex.setSummary( regex.getText() );
        }

        final Preference onCallEnabled = findPreference( getString( R.id.on_call_key ) );
        final CheckBoxPreference showReminder = (CheckBoxPreference) findPreference( getString( R.id.show_notification_key ) );
        final EditTextPreference reminderMessage = (EditTextPreference) findPreference( getString( R.id.notification_message_key ) );

        OnPreferenceChangeListener onPreferenceChangeListener = new OnPreferenceChangeListener() {
            public boolean onPreferenceChange( Preference preference, Object newValue ) {
                if ( Boolean.TRUE.equals( (Boolean) newValue && (isChecked( onCallEnabled ) || isChecked( showReminder )) ) ) {
                    Intent updateReminder = new Intent( ACTION_INSERT_OR_EDIT );
                    updateReminder.putExtra( getString( R.id.notification_message_key ), reminderMessage.getText() );
                    sendBroadcast( updateReminder );
                } else {
                    sendBroadcast( new Intent( ACTION_DELETE ) );
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
                    Intent updateReminder = new Intent( ACTION_INSERT_OR_EDIT );
                    updateReminder.putExtra( getString( R.id.notification_message_key ), message );
                    sendBroadcast( updateReminder );
                }

                return true;
            }
        } );

        reminderMessage.setSummary( isBlank( reminderMessage.getText() ) ? getString( R.string.notification_message_default ) : reminderMessage.getText() );

        if ( isChecked( onCallEnabled ) && isChecked( showReminder ) ) {
            sendBroadcast( new Intent( ACTION_INSERT_OR_EDIT ) );
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

    @SuppressLint( "NewApi" )
    private static boolean isChecked( Preference preference ) {
        if ( Build.VERSION.SDK_INT >= 14 ) {
            if ( preference instanceof TwoStatePreference ) {
                return ((TwoStatePreference) preference).isChecked();
            }
        }

        if ( preference instanceof CheckBoxPreference ) {
            return ((CheckBoxPreference) preference).isChecked();
        }

        throw new RuntimeException( "Not supported preference" );
    }

}
