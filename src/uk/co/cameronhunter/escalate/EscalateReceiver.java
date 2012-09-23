package uk.co.cameronhunter.escalate;

import static android.app.Notification.PRIORITY_MAX;
import static android.content.Intent.ACTION_MAIN;
import static android.content.Intent.CATEGORY_APP_MESSAGING;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.BigTextStyle;
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
        String message = intent.getStringExtra( "body" );

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences( context.getApplicationContext() );
        notification( "Escalate", sender, message, context, preferences );
    }

    private void notification( String title, String sender, String message, Context context, SharedPreferences preferences ) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService( Context.NOTIFICATION_SERVICE );

        Notification notification = buildNotification( title, sender, message, preferences, context );

        notificationManager.notify( 1, notification );
    }

    private static Notification buildNotification( String title, String sender, String message, SharedPreferences preferences, Context context ) {

        // Setup ringtone
        String ringtone = preferences.getString( context.getString( R.id.ringtone_key ), RingtoneManager.getDefaultUri( RingtoneManager.TYPE_ALARM ).toString() );

        // Setup volume
        String volumePref = preferences.getString( context.getString( R.id.volume_key ), context.getString( R.string.volume_alarm ) );
        int volume = AudioManager.STREAM_ALARM;
        if ( context.getString( R.string.volume_media ).equals( volumePref ) ) {
            volume = AudioManager.STREAM_MUSIC;
        } else if ( context.getString( R.string.volume_ringer ).equals( volumePref ) ) {
            volume = AudioManager.STREAM_RING;
        }

        // Setup vibrate
        boolean vibrate = preferences.getBoolean( context.getString( R.id.vibrate_key ), false );

        // Setup notification light
        boolean notificationLight = preferences.getBoolean( context.getString( R.id.notification_light_key ), false );

        // Stop intent
        Intent intent = new Intent( context, EscalateReceiver.class );
        intent.putExtra( "stop", true );
        PendingIntent stopPendingIntent = PendingIntent.getBroadcast( context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT );

        // Open Messaging Intent
        Intent smsIntent = new Intent( ACTION_MAIN );
        smsIntent.addCategory( CATEGORY_APP_MESSAGING );
        PendingIntent smsPendingIntent = PendingIntent.getActivity( context, 0, smsIntent, 0 );

        NotificationCompat.Builder builder = new NotificationCompat.Builder( context );

        String tickerText = context.getString( R.string.notification_subtext, sender );

        builder.setSmallIcon( android.R.drawable.ic_dialog_alert ) //
                .setAutoCancel( true ) //
                .setContentTitle( title ) //
                .setContentText( message ) //
                .setSound( Uri.parse( ringtone ), volume ) //
                .setWhen( System.currentTimeMillis() ) //
                .setUsesChronometer( true ) //
                .setPriority( PRIORITY_MAX ) //
                .setLargeIcon( BitmapFactory.decodeResource( context.getResources(), android.R.drawable.ic_dialog_alert ) ) //
                .setTicker( tickerText ) //
                .setSubText( tickerText ) //
                .setContentIntent( smsPendingIntent ) //
                .setDeleteIntent( stopPendingIntent );

        if ( vibrate ) {
            builder.setVibrate( new long[] { 0, 800, 500, 800 } );
        }

        if ( notificationLight ) {
            builder.setLights( 0x00ff0000, 100, 100 );
        }

        BigTextStyle bigNotification = new NotificationCompat.BigTextStyle( builder ).bigText( message );

        Notification notification = bigNotification.build();

        if ( notificationLight ) {
            notification.flags |= Notification.FLAG_SHOW_LIGHTS;
        }

        notification.flags |= Notification.FLAG_INSISTENT;

        return notification;
    }

}
