package uk.co.cameronhunter.escalate;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.media.RingtoneManager;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.RingtonePreference;
import android.preference.Preference.OnPreferenceChangeListener;

public class MainActivity extends PreferenceActivity {

	@Override
	public void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		addPreferencesFromResource( R.xml.preferences );

		RingtonePreference ringtone = (RingtonePreference)findPreference( getString( R.string.ringtone_key ) );
		ringtone.setDefaultValue( RingtoneManager.getDefaultUri( RingtoneManager.TYPE_ALARM ).toString() );
		
		EditTextPreference regex = (EditTextPreference) findPreference( getString( R.string.regex_key ) );
		regex.setOnPreferenceChangeListener( new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange( Preference preference, Object newValue ) {
				String pattern = (String) newValue;
				if ( isValidPattern( pattern ) ) {
					preference.setSummary( pattern );
					return true;
				}
				return false;
			}
		} );

		if ( regex.getText() != null && regex.getText() != "" ) regex.setSummary( regex.getText() );
	}

	private static boolean isValidPattern( String pattern ) {
		try {
			Pattern.compile( pattern );
			return true;
		}
		catch ( PatternSyntaxException ex ) {
			return false;
		}
	}

}
