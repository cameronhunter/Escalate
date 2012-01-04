package uk.co.cameronhunter.escalate;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class EscalateReceiver extends BroadcastReceiver {

	@Override
	public void onReceive( Context context, Intent intent ) {

		if ( intent.getBooleanExtra( "stop", false ) ) {
			Log.i( "Receiver", "Cancelling alarms" );
			AlarmManager alarms = (AlarmManager) context.getSystemService( Context.ALARM_SERVICE );
			PendingIntent pendingIntent = PendingIntent.getBroadcast( context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT );
			alarms.cancel( pendingIntent );
			return;
		}

		String sender = intent.getStringExtra( "sender" );
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( context.getApplicationContext() );
		notification( "Escalate", String.format( "Message from %s has been escalated", sender ), context, preferences );
	}

	private Notification buildNotification( String title, SharedPreferences preferences, Context context ) {
		Notification notification = new Notification( android.R.drawable.ic_dialog_alert, title, System.currentTimeMillis() );

		notification.flags |= Notification.FLAG_INSISTENT;
		notification.flags |= Notification.FLAG_AUTO_CANCEL;

		// Setup ringtone
		String ringtone = preferences.getString( context.getString( R.string.ringtone_key ), RingtoneManager.getDefaultUri( RingtoneManager.TYPE_ALARM ).toString() );
		notification.sound = Uri.parse( ringtone );

		// Setup volume
		String volumePref = preferences.getString( context.getString( R.string.volume_key ), context.getString( R.string.volume_alarm ) );
		int volume = AudioManager.STREAM_ALARM;
		if ( context.getString( R.string.volume_media ).equals( volumePref ) ) {
			volume = AudioManager.STREAM_MUSIC;
		} else if ( context.getString( R.string.volume_ringer ).equals( volumePref ) ) {
			volume = AudioManager.STREAM_RING;
		}
		notification.audioStreamType = volume;

		// Setup vibrate
		boolean vibrate = preferences.getBoolean( context.getString( R.string.vibrate_key ), false );
		if ( vibrate ) {
			notification.defaults |= Notification.DEFAULT_VIBRATE;
			notification.vibrate = new long[] { 0, 800, 500, 800 };
		}

		// Setup notification light
		boolean notificationLight = preferences.getBoolean( context.getString( R.string.notification_light_key ), false );
		if ( notificationLight ) {
			notification.defaults |= Notification.DEFAULT_LIGHTS;
			notification.flags |= Notification.FLAG_SHOW_LIGHTS;
			notification.ledARGB = 0xff0000;
			notification.ledOnMS = 1000;
			notification.ledOffMS = 100;
		}

		return notification;
	}

	private void notification( String title, String message, Context context, SharedPreferences preferences ) {
		NotificationManager notificationManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );

		Notification notification = buildNotification( title, preferences, context );

		Intent intent = new Intent( context, EscalateReceiver.class );
		intent.putExtra( "stop", true );
		PendingIntent pendingIntent = PendingIntent.getBroadcast( context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT );
		
		notification.deleteIntent = pendingIntent;
		notification.setLatestEventInfo( context, title, message, pendingIntent );
		notificationManager.notify( 1, notification );
	}

}
