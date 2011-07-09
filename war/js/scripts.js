$(function() {
  $.requestPermission = function(callback) {
    window.webkitNotifications.requestPermission(callback);
  }

  $.showNotificationFunction = function(img, title, text) {
    return function() {
      $.showNotification(img, title, text);
    }
  }

  $.showNotification = function(img, title, text) {
    if (window.webkitNotifications) {
      if (window.webkitNotifications.checkPermission() > 0) {
      } else {
        var notification = window.webkitNotifications.createNotification(img, title, text);
          notification.ondisplay = function(){
            setTimeout(function(){
              notification.cancel();
            }, 10000);
          };
          notification.show();
      }
    }
  }

  $.favorites = {
        reload : false,
    getList : function() {
      var cookieValue = $.cookie('favorites');
      if (cookieValue == null) {
        return null;
      } else {
    	  $.cookie('favorites', cookieValue, null);
    	  $.cookie('favorites', cookieValue, { expires: 7, path: '/' });
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
      $.cookie('favorites', temp.join(","), { expires: 7, path: '/' });
      $("#favorite_switch_icon_" + topicId).attr("src", "/img/icon/favorites_on.png");
      $("#favorite_switch_" + topicId).unbind('click touchend').bind('click touchend', function(){
            $.favorites.remove(topicId);
      });
      this.reload = true;
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
        $.cookie('favorites', temp.join(","), { expires: 7, path: '/' });
      } else {
        $.cookie('favorites', null);
      }
      $("#favorite_switch_icon_" + topicId).attr("src", "/img/icon/favorites_off.png");
      $("#favorite_switch_" + topicId).unbind('click touchend').bind('click touchend', function(){
            $.favorites.add(topicId);
      });
      this.reload = true;
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