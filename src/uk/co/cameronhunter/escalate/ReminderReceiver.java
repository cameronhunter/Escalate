package uk.co.cameronhunter.escalate;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.content.Intent.ACTION_BOOT_COMPLETED;
import static android.content.Intent.ACTION_DELETE;
import static android.content.Intent.ACTION_INSERT_OR_EDIT;
import static org.apache.commons.lang3.StringUtils.isBlank;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

	private Context context;

	@Override
	public void onReceive( Context context, Intent intent ) {
		this.context = context;

		String action = intent.getAction();

		boolean showReminderIntent = ACTION_INSERT_OR_EDIT.equals( action );
		boolean removeReminderIntent = !showReminderIntent && ACTION_DELETE.equals( action );

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( context );

		if ( ACTION_BOOT_COMPLETED.equals( action ) ) {
			boolean onCall = preferences.getBoolean( context.getString( R.id.on_call_key ), false );
			boolean showNotification = preferences.getBoolean( context.getString( R.id.show_notification_key ), false );
			if ( onCall && showNotification ) {
				showReminderIntent = true;
				removeReminderIntent = false;
			} else {
				removeReminderIntent = true;
				showReminderIntent = false;
			}
		}

		NotificationManager notificationManger = (NotificationManager) context.getSystemService( NOTIFICATION_SERVICE );
		if ( showReminderIntent ) {

			String key = context.getString( R.id.notification_message_key );

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
	    NotificationCompat.Builder builder = new NotificationCompat.Builder( context );

		builder.setContentTitle( isBlank( message ) ? context.getString( R.string.notification_message_default ) : message );
		builder.setSmallIcon( android.R.drawable.ic_dialog_info );
		builder.setContentIntent( PendingIntent.getActivity( context, 0, new Intent( context, MainActivity.class ), 0 ) );
		builder.setOngoing( true );

		notificationManger.notify( "reminder", 1, builder.build() );
	}

	private void removeReminderNotification(NotificationManager notificationManger) {
		notificationManger.cancel( "reminder", 1 );
	}

}
