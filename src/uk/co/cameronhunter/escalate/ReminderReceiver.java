package uk.co.cameronhunter.escalate;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static uk.co.cameronhunter.utils.StringUtils.isBlank;
import uk.co.cameronhunter.adapters.notificationBuilder.NotificationBuilder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ReminderReceiver extends BroadcastReceiver {

	private Context context;

	@Override
	public void onReceive( Context context, Intent intent ) {
		this.context = context;

		String action = intent.getAction();

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( context );

		boolean updateReminderIntent = context.getString( R.string.update_reminder_intent ).equals( action );
		boolean removeReminderIntent = !updateReminderIntent && context.getString( R.string.remove_reminder_intent ).equals( action );

		if ( ACTION_BOOT_COMPLETED.equals( action ) ) {

		}

		NotificationManager notificationManger = (NotificationManager) context.getSystemService( NOTIFICATION_SERVICE );
		if ( updateReminderIntent ) {

			String key = context.getString( R.string.notification_message_key );

			String message = null;
			if ( intent.hasExtra( key ) ) {
				message = intent.getStringExtra( key );
			} else {
				message = preferences.getString( key, null );
			}

			updateReminderNotification( notificationManger, message );
		} else if( removeReminderIntent ) {
			removeReminderNotification( notificationManger );
		}
	}

	private void updateReminderNotification( NotificationManager notificationManger, String message ) {
		NotificationBuilder builder = NotificationBuilder.create( context );

		builder.setContentTitle( isBlank( message ) ? context.getString( R.string.notification_message_default ) : message );
		builder.setSmallIcon( android.R.drawable.ic_dialog_info );
		builder.setContentIntent( PendingIntent.getActivity( context, 0, new Intent( context, MainActivity.class ), 0 ) );
		builder.setOngoing( true );

		notificationManger.notify( "reminder", 1, builder.getNotification() );
	}

	private void removeReminderNotification(NotificationManager notificationManger) {
		notificationManger.cancel( "reminder", 1 );
	}

}
