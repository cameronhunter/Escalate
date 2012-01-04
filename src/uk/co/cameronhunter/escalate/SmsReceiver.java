package uk.co.cameronhunter.escalate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;

public class SmsReceiver extends BroadcastReceiver {

	@Override
	public void onReceive( Context context, Intent intent ) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( context.getApplicationContext() );

		boolean onCall = preferences.getBoolean( context.getApplicationContext().getString( R.string.on_call_key ), false );
		Pattern regex = getRegex( context, preferences );

		if ( !onCall || regex == null ) { return; }

		Bundle bundle = intent.getExtras();
		if ( bundle == null ) { return; }

		Object[] pdus = (Object[]) bundle.get( "pdus" );
		for ( int i = 0; i < pdus.length; i++ ) {
			SmsMessage message = SmsMessage.createFromPdu( (byte[]) pdus[i] );
			Matcher matcher = regex.matcher( message.getMessageBody() );
			if ( matcher.matches() ) {
				Intent escalateIntent = new Intent( context, EscalateReceiver.class );
				escalateIntent.putExtra( "sender", message.getOriginatingAddress() );
				escalateIntent.putExtra( "body", message.getMessageBody() );

				AlarmManager alarms = (AlarmManager) context.getSystemService( Context.ALARM_SERVICE );
				PendingIntent annoy = PendingIntent.getBroadcast( context, 0, escalateIntent, PendingIntent.FLAG_CANCEL_CURRENT );

				alarms.setRepeating( AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 3000, 20000, annoy );
			}
		}
	}

	private Pattern getRegex( Context context, SharedPreferences preferences ) {
		String regexString = preferences.getString( context.getString( R.string.regex_key ), null );
		if ( regexString == null ) return null;

		try {
			return Pattern.compile( regexString, Pattern.CASE_INSENSITIVE );
		}
		catch ( PatternSyntaxException e ) {
			return null;
		}
	}
}
