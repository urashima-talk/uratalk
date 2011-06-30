$.showRequestLink = function(callback) {
	if (window.webkitNotifications && $("#content").size() < 1) {
		$("a").attr("id", "showRequestLink").text("デスクトップ通知を有効にする").bind("click", function(){
			$.requestPermission(callback);
			return false;
		}).prependTo("#content");
	}
}

$.requestPermission = function(callback) {
	window.webkitNotifications.requestPermission(callback);
	$("#showRequestLink").remove();
}

$.showNotificationFunction = function(img, title, text) {
	return function() {
		$.showNotification(img, title, text);
	}
}

$.showNotification = function(img, title, text) {
	if (window.webkitNotifications) {
		if (window.webkitNotifications.checkPermission() > 0) {
			$.showRequestLink($.showNotificationFunction(img, title, text));
		} else {
			window.webkitNotifications.createNotification(img, title, text)
					.show();
		}
	}
}