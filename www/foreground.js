const exec = require("cordova/exec");

module.exports = {
  askPermissions: (success, error) => {
    exec(
      success || null,
      error || null,
      "ForegroundPlugin",
      "askPermissions",
      [],
    );
  },
  start: (title, text, icon, importance, notificationId, success, error) => {
    exec(success || null, error || null, "ForegroundPlugin", "start", [
      title ? String(title) : "",
      text ? String(text) : "",
      icon ? String(icon) : "",
      importance ? String(importance) : "1",
      notificationId ? String(notificationId) : "",
    ]);
  },
  stop: (success, error) => {
    exec(success || null, error || null, "ForegroundPlugin", "stop", []);
  },
};
