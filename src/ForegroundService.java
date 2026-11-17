package net.sgj0.cordova.plugin.foreground;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

public class ForegroundService extends Service {

  private static final int DEFAULT_NOTIFICATION_ID = 197812504;

  @Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent == null) {
      stopSelf();
      return START_NOT_STICKY;
    }

    if ("start".equals(intent.getAction())) {
      startPluginForegroundService(intent.getExtras());
      return START_REDELIVER_INTENT;
    } else {
      stopPluginForegroundService();
      return START_NOT_STICKY;
    }
  }

  @Override
  public void onDestroy() {
    stopForeground(Service.STOP_FOREGROUND_REMOVE);
    super.onDestroy();
  }

  @Override
  public void onTaskRemoved(Intent rootIntent) {
    stopPluginForegroundService();
    super.onTaskRemoved(rootIntent);
  }

  private void stopPluginForegroundService() {
    stopForeground(Service.STOP_FOREGROUND_REMOVE);
    stopSelf();
  }

  @TargetApi(26)
  private void startPluginForegroundService(Bundle extras) {
    if (extras == null) {
      extras = new Bundle();
    }

    Context context = getApplicationContext();

    // Get notification channel importance
    int importanceLevel;
    try {
      importanceLevel = Integer.parseInt(extras.getString("importance", "1"));
    } catch (NumberFormatException e) {
      importanceLevel = 1;
    }

    int importance;
    switch (importanceLevel) {
      case 2:
        importance = NotificationManager.IMPORTANCE_DEFAULT;
        break;
      case 3:
        importance = NotificationManager.IMPORTANCE_HIGH;
        break;
      default:
        importance = NotificationManager.IMPORTANCE_LOW;
        break;
      // We are not using IMPORTANCE_MIN because we want the notification to be visible
    }

    // Create or update notification channel per importance level
    String channelId = "foreground.service.channel." + importanceLevel;
    NotificationManager manager = getSystemService(NotificationManager.class);
    if (manager != null) {
      NotificationChannel channel = new NotificationChannel(
        channelId,
        "Background Services",
        importance
      );
      channel.setDescription("Enables background processing.");
      manager.createNotificationChannel(channel);
    }

    // Get notification icon
    String iconName = extras.getString("icon", "");
    int icon = 0;
    if (iconName != null && !iconName.isEmpty()) {
      icon = getResources()
        .getIdentifier(iconName, "drawable", context.getPackageName());
      if (icon == 0) {
        icon = getResources()
          .getIdentifier(iconName, "mipmap", context.getPackageName());
      }
    }
    if (icon == 0) {
      icon = context.getApplicationInfo().icon;
    }
    if (icon == 0) {
      icon = android.R.drawable.star_big_on;
    }

    // PendingIntent to bring the app back to the foreground on notification tap
    Intent launchIntent = context
      .getPackageManager()
      .getLaunchIntentForPackage(context.getPackageName());
    PendingIntent pendingIntent = null;
    if (launchIntent != null) {
      launchIntent.setFlags(
        Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP
      );
      int pendingFlags = PendingIntent.FLAG_UPDATE_CURRENT;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        pendingFlags |= PendingIntent.FLAG_IMMUTABLE;
      }
      pendingIntent = PendingIntent.getActivity(
        context,
        0,
        launchIntent,
        pendingFlags
      );
    }

    // Make notification
    Notification.Builder builder = new Notification.Builder(context, channelId)
      .setContentTitle(extras.getString("title", ""))
      .setContentText(extras.getString("text", ""))
      .setOngoing(true)
      .setSmallIcon(icon);

    if (pendingIntent != null) {
      builder.setContentIntent(pendingIntent);
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
      builder.setForegroundServiceBehavior(
        Notification.FOREGROUND_SERVICE_IMMEDIATE
      );
    }

    Notification notification = builder.build();

    // Get notification ID
    int id;
    try {
      id = Integer.parseInt(extras.getString("id", "0"));
    } catch (NumberFormatException e) {
      id = 0;
    }
    int notificationId = id != 0 ? id : DEFAULT_NOTIFICATION_ID;

    // Put service in foreground and show notification (id of 0 is not allowed)
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      startForeground(notificationId, notification);
    } else {
      startForeground(
        notificationId,
        notification,
        ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
      );
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }
}
