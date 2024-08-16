package net.sgj0.cordova.plugin.foreground;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

public class ForegroundPlugin extends CordovaPlugin {

  @Override
  @TargetApi(26)
  public boolean execute(
    final String action,
    final JSONArray args,
    final CallbackContext command
  ) throws JSONException {
    if (android.os.Build.VERSION.SDK_INT < 26) {
      command.error("Require SDK 26 or higher.");
      return false;
    }

    Activity activity = cordova.getActivity();
    Intent intent = new Intent(activity, ForegroundService.class);

    switch (action) {
      case "askPermissions":
        // Tell the service we want permissions
        intent.setAction("askPermissions");

        // Ask permissions
        if (android.os.Build.VERSION.SDK_INT >= 33) {
          cordova.requestPermission(
            this,
            0,
            Manifest.permission.POST_NOTIFICATIONS
          );
        }
        break;
      case "start":
        // Tell the service we want to start it
        intent.setAction("start");

        // Pass the notification title/text/icon to the service
        intent
          .putExtra("title", args.getString(0))
          .putExtra("text", args.getString(1))
          .putExtra("icon", args.getString(2))
          .putExtra("importance", args.getString(3))
          .putExtra("id", args.getString(4));

        // Start the service
        activity.getApplicationContext().startForegroundService(intent);
        break;
      case "stop":
        // Tell the service we want to stop it
        intent.setAction("stop");

        // Stop the service
        activity.getApplicationContext().startService(intent);
        break;
      default:
    }

    command.success();
    return true;
  }
}
