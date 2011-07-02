$(function() {
	$.showRequestLink = function(callback) {
		if (window.webkitNotifications && $("#content").size() < 1) {
			$("a").attr("id", "showRequestLink").text("デスクトップ通知を有効にする").bind(
					"click", function() {
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

	$.favorites = {
		getList : function() {
			var cookieValue = $.cookie('favorites');
			if (cookieValue == null) {
				return null;
			}
			return cookieValue.split(",");
		},
		add : function(topicId) {
			var list = this.getList();
			var temp;
			if (list == null) {
				temp = new Array(topicId);
			} else {
				list.push(topicId);
				temp = list;
			}
			$.cookie('favorites', temp.join(","));
			$("#favorite_switch_icon").attr("src", "/img/icon/favorites_on.png");
			$("#favorite_switch").unbind('click touchend').bind('click touchend', function(){
        		$.favorites.remove(topicId);
			});
		},
		remove : function(topicId) {
			var list = this.getList();
			if (list != null) {
				var temp = new Array();
				var size = list.length;
				for ( var i = 0; i < size; i++) {
					var item = list[i];
					if (item != topicId) {
						temp.push(item);
					}
				}
				$.cookie('favorites', temp.join(","));
			} else {
				$.cookie('favorites', null);
			}
			$("#favorite_switch_icon").attr("src", "/img/icon/favorites_off.png");
			$("#favorite_switch").unbind('click touchend').bind('click touchend', function(){
        		$.favorites.add(topicId);
			});
		},
		isFavorite : function(topicId) {
			var list = this.getList();
			if (list == null) {
				return false;
			} else {
				return (list.indexOf(topicId) >= 0);
			}
		}
	};
	$.template( "topicItem", '<li><a data-ajax="false" href="/topic/comment?topicId=${id}">${title} (${lastCommentNumber}) <span class="alignright small">${lastCommentAt}</span><\/a><\/li>' );
});