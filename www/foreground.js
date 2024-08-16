const exec = require("cordova/exec");

module.exports = {
  askPermissions: () => {
    exec(null, null, "ForegroundPlugin", "askPermissions", []);
  },
  start: (title, text, icon, importance, notificationId) => {
    exec(null, null, "ForegroundPlugin", "start", [
      title || "",
      text || "",
      icon || "",
      importance || "1",
      notificationId || "",
    ]);
  },
  stop: () => {
    exec(null, null, "ForegroundPlugin", "stop", []);
  },
};
