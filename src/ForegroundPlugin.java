package net.sgj0.cordova.plugin.foreground;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

public class ForegroundPlugin extends CordovaPlugin {

  private static final int REQUEST_POST_NOTIFICATIONS = 1001;
  private CallbackContext permissionCallback;

  @Override
  @TargetApi(26)
  public boolean execute(
    final String action,
    final JSONArray args,
    final CallbackContext command
  ) throws JSONException {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
      command.error("Require SDK 26 or higher.");
      return true;
    }

    Activity activity = cordova.getActivity();
    Context context = activity.getApplicationContext();

    switch (action) {
      case "askPermissions":
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          if (cordova.hasPermission(Manifest.permission.POST_NOTIFICATIONS)) {
            command.success();
          } else {
            this.permissionCallback = command;
            cordova.requestPermission(
              this,
              REQUEST_POST_NOTIFICATIONS,
              Manifest.permission.POST_NOTIFICATIONS
            );
          }
        } else {
          command.success();
        }
        return true;

      case "start":
        Intent startIntent = new Intent(context, ForegroundService.class);
        startIntent.setAction("start");
        startIntent
          .putExtra("title", args.optString(0, ""))
          .putExtra("text", args.optString(1, ""))
          .putExtra("icon", args.optString(2, ""))
          .putExtra("importance", args.optString(3, "1"))
          .putExtra("id", args.optString(4, ""));

        try {
          context.startForegroundService(startIntent);
          command.success();
        } catch (Exception e) {
          command.error(e.getMessage());
        }
        return true;

      case "stop":
        Intent stopIntent = new Intent(context, ForegroundService.class);
        context.stopService(stopIntent);
        command.success();
        return true;

      default:
        return false;
    }
  }

  @Override
  public void onRequestPermissionResult(
    int requestCode,
    String[] permissions,
    int[] grantResults
  ) throws JSONException {
    if (
      requestCode == REQUEST_POST_NOTIFICATIONS &&
      this.permissionCallback != null
    ) {
      if (
        grantResults.length > 0 &&
        grantResults[0] == PackageManager.PERMISSION_GRANTED
      ) {
        this.permissionCallback.success();
      } else {
        this.permissionCallback.error("POST_NOTIFICATIONS permission denied.");
      }
      this.permissionCallback = null;
    }
  }
}
