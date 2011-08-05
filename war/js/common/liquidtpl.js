/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
$.listColor = function(selector) {
    $(selector).find('.row').removeClass('odd');
    $(selector).find('.row:odd').addClass('odd');
};

$.isString = function(obj) {
    return typeof (obj) == "string" || obj instanceof String;
};

$.evalJsonCommentFiltered = function(value) {
    uncommentJsonString = function(value) {
        var startIndex = value.indexOf("\/*");
        var endIndex = value.lastIndexOf("*\/");
        if (startIndex == -1 || endIndex == -1) {
            throw new Error("JSON was not comment filtered");
        }
        var data = value.substring(startIndex + 2, endIndex);
        return data;
    };
    return $.secureEvalJSON(uncommentJsonString(value));
};

$.getUrlParams = function(url) {
    if (!url) {
        url = location.href;
    }
    var result = {};
    url = url.replace(/.*\?(.*?)/, "$1");
    var keyValueList = url.split("&");
    for (var i = 0; i < keyValueList.length; i++) {
        var keyValue = keyValueList[i].split("=");
        result[keyValue[0]] = keyValue[1];
    }
    return result
}

$.escapeHTML = function(value) {
    replaceChars = function(ch) {
        switch (ch) {
            case "<":
                return "&lt;";
            case ">":
                return "&gt;";
            case "&":
                return "&amp;";
            case "'":
                return "&#39;";
            case '"':
                return "&quot;";
        }
        return "?";
    };

    return String(value).replace(/[<>&"']/g, replaceChars);
};

$.unescapeHTML = function(value) {
    return value.replace(/&lt;/g, '<')
        .replace(/&gt;/g, '>')
        .replace(/&amp;/g, '&')
        .replace(/&#039;/g, '\'')
        .replace(/&#034;/g, '\"')
        .replace(/&quot;/g, '\"');
};

$.simpleHTML = function(value) {
    var html = "";
    var re = /((http|https|ftp):\/\/[\w?=&.\/-;#~%-]+(?![\w\s?&.\/;#~%"=-]*>))/g;
    var lines = $.escapeHTML(value).replace(re, '<a target="_blank" href="$1">$1<\/a>').split(/\r|\n|\r\n/);
    var size = lines.length;
    for (var i = 0; i < size; i++) {
        html += "<p>"
        html += lines[i];
        html += "<\/p>"
    }
    return html;
};

$.wrapLongText = function(str, step) {
    if (!lucoz.isString(str)) {
        return "";
    }
    if (!step || !isNaN(step)) {
        step = 6;
    }
    var size = str.length;
    var count = size / step;
    var j = 0;
    var res = "";
    for (var i = 0; i < count; i++) {
        res += escapeHTML(str.substring(j, j + step));
        res += "<wbr/>";
        j += step;
    }

    if (count * step < size) {
        res += str.substring(count * step);
    }
    return res;
};

/**
 * initialize list data
 */
$.initList = function(listId, itemTplId, showEmptyMsg, url, params, callback, onEmpty, onError) {
    var postParams = $.getUrlParams();
    postParams["mode"] = "list";
    for (var i in params) {
        postParams[i] = params[i];
    }
    $.ajax({
        type : "POST",
        url : url,
        cache : false,
        dataType : "text",
        data : postParams,
        success : function(result) {
            var jsonData = $.evalJsonCommentFiltered(result);
            var values = jsonData.values;
            if (values && values["redirect"]) {
                $("#" + listId).empty();
                if ($.mobile && $.mobile.changePage) {
                    $.mobile.changePage(values["redirect"], { transition: "slideup"});
                } else {
                    location.href = values["redirect"];
                }
                return;
            }

            if (jsonData["result"] == "success") {
                if (callback) {
                    callback(jsonData);
                } else {
                    $.setListValues(listId, itemTplId, showEmptyMsg, jsonData, onEmpty)
                }
            } else {
                if (onError) {
                    onError();
                } else {
                    var error = "Unknown Error";
                    if (jsonData["errors"] && jsonData["errors"]["grobal"]) {
                        error = jsonData["errors"]["grobal"];
                    }
                    var errorHtml = $("<div>").addClass("mt10").addClass(
                        "warningMessage").text(error);
                    $("#" + listId).replaceWith(errorHtml);
                }
            }
            $("#" + listId).show();
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            if (onError) {
                onError();
            } else {
                var error = "Request Error: " + errorThrown + ". " + url;
                errorHtml = $("<div>").addClass("mt10").addClass("warningMessage")
                    .text(error);
                $("#" + listId).replaceWith(errorHtml);
                $("#" + listId).show();
            }
        }
    });
};

$.setListValues = function(listId, itemTplId, showEmptyMsg, jsonData) {
    var values = jsonData.values;
    if (values && (values.length > 0)) {
        if ($("#" + itemTplId).size() > 0) {
            $("#" + itemTplId).tmpl(values).appendTo("#" + listId);
        } else {
            $.tmpl(itemTplId, values).appendTo("#" + listId);
        }
        $.listColor("#" + listId);
    } else if (showEmptyMsg) {
        var errorHtml = $("<div>").addClass("mt10").addClass("confirmMessage")
            .text(jsonData["empty"]);
        $("#" + listId).replaceWith(errorHtml);
    } else {
        if (onEmpty) {
            onEmpty()
        } else {
            $("#" + listId).remove();
        }
    }
}

/**
 * initialize form data
 */
$.initForm = function(formId, url, params, callback) {
    var postParams = $.getUrlParams();
    postParams["mode"] = "form";
    for (var i in params) {
        postParams[i] = params[i];
    }
    $.ajax({
        type : "POST",
        url : url,
        cache : false,
        dataType : "text",
        data : postParams,
        success : function(result) {
            var jsonData = $.evalJsonCommentFiltered(result)
            var values = jsonData.values;
            if (values && values["redirect"]) {
                $("#" + formId).empty();
                if ($.mobile && $.mobile.changePage) {
                    $.mobile.changePage(values["redirect"], { transition: "slideup"});
                } else {
                    location.href = values["redirect"];
                }
                return;
            }
            if (jsonData["result"] == "success") {
                if (callback) {
                    callback(jsonData);
                } else {
                    $.setFormValues(formId, jsonData);
                }
            } else {
                var error = "Unknown Error";
                if (jsonData["errors"] && jsonData["errors"]["grobal"]) {
                    error = jsonData["errors"]["grobal"];
                }
                var errorHtml = $("<div>").addClass("warningMessage").text(
                    error);
                $("#" + formId).replaceWith(errorHtml);
            }
            $("#" + formId).show();
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            var error = "Request Error: " + errorThrown + ". " + url;
            errorHtml = $("<div>").addClass("warningMessage").text(error);
            $("#" + formId).replaceWith(errorHtml);
            $("#" + formId).show();
        }
    });
};

$.setFormValues = function(formId, jsonData, beforeSubmit, afterSuccess, afterSubmit) {
    var values = jsonData.values;
    var keys = {};
    var sourceValues = values;
    var targetValues = {};
    for (key in sourceValues) {
        var type = $("#" + formId).find("input[name='" + key + "']").attr(
            "type");
        if ((type != 'checkbox') && (type != 'radio')) {
            keys[key] = key;
        }
    }
    $("#" + formId).link(targetValues, keys);

    for (var key in sourceValues) {
        $(targetValues).setField(key, sourceValues[key]);
    }

    // set Checkbox List Fields cause jquery template does not work
    $("#" + formId).find("input[type='checkbox']").each(function() {
        var name = $(this).attr("name");
        if (name.match(/\[\]$/)) {
            key = name.substr(0, name.length - 2);
        } else {
            key = name;
        }
        var value = sourceValues[key];
        if ($.isArray(value)) {
            for (var i in value) {
                if ($(this).val() == value[i].toString()) {
                    $(this).attr("checked", "checked");
                }
            }
        } else {
            try {
                if ($(this).val() == value.toString()) {
                    $(this).attr("checked", "checked");
                }
            } catch (e) {
            }
        }
    });

    // set RadioButton List Fields cause jquery template does not work
    $("#" + formId).find("input[type='radio']").each(function() {
        var name = $(this).attr("name");
        if (name.match(/\[\]$/)) {
            key = name.substr(0, name.length - 2);
        } else {
            key = name;
        }
        var value = sourceValues[key];
        if ($.isArray(value)) {
            for (var i in value) {
                if ($(this).val() == value[i].toString()) {
                    $(this).attr("checked", "checked");
                }
            }
        } else {
            try {
                if ($(this).val() == value.toString()) {
                    $(this).attr("checked", "checked");
                }
            } catch (e) {
            }
        }
    });

    $.tmpl("idHiddenTemplate", jsonData).appendTo("#" + formId);

    if ($("#" + formId).find("input[type='submit']").size() == 0) {
        $.tmpl("submitTemplate", jsonData).appendTo("#" + formId);
    }

    $("#" + formId).bind("submit", function() {
        if (beforeSubmit) {
            beforeSubmit();
        }
        $.submit(formId, afterSuccess, afterSubmit);
        return false;
    });
};

$.submit = function(formId, afterSuccess, afterSubmit) {
    $(".warningMessage").hide();
    $(".warningMessageOne").hide();
    $("#" + formId).find("input[type='submit']").attr('disabled', 'disabled');

    $.ajax({
        type : "POST",
        url : $("#" + formId).attr('action'),
        cache : false,
        dataType : "text",
        data : $("#" + formId).serialize(),
        success : function(result) {
            try {
                var jsonData = $.evalJsonCommentFiltered(result)
                if (jsonData["result"] == "success") {
                    if (afterSuccess) {
                        afterSuccess();
                    }
                    if (jsonData["redirect"]) {
                        if (jsonData["redirect"] == "reload") {
                            location.reload();
                        } else {
                            if ($.mobile && $.mobile.changePage) {
                                $.mobile.changePage(jsonData["redirect"], { transition: "slideup"});
                            } else {
                                location.href = jsonData["redirect"];
                            }
                        }
                    }
                } else {
                    var error = "Unknown Error";
                    if (jsonData["errors"]) {
                        for (var key in jsonData["errors"]) {
                            $("#" + formId).find("#" + key + "Error")
                                .text(jsonData["errors"][key])
                                .slideDown("fast");
                        }
                    } else {
                        $("#" + formId).find("#grobalError")
                            .text(error).slideDown("fast");
                    }

                }
            } catch (e) {
                $("#" + formId).find("#grobalError").text(e.toString())
                    .slideDown("fast");
            }
            $("#" + formId).find("input[type='submit']").removeAttr(
                'disabled');
            if (afterSubmit) {
                afterSubmit();
            }
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            var error = "Request Error: " + errorThrown + ". " + url;
            $("#" + formId).find("#grobalError").text(
                error + "\n" + result).slideDown("fast");
            $("#" + formId).find("input[type='submit']").removeAttr(
                'disabled');
            if (afterSubmit) {
                afterSubmit();
            }
        }
    });
}

/**
 * initialize detail data
 */
$.initDetail = function(detailId, itemTplId, url, params, callback) {
    var postParams = $.getUrlParams();
    postParams["mode"] = "detail";
    for (var i in params) {
        postParams[i] = params[i];
    }
    $.ajax({
        type : "POST",
        url : url,
        cache : false,
        dataType : "text",
        data : postParams,
        success : function(result) {
            var jsonData = $.evalJsonCommentFiltered(result);
            var values = jsonData.values;
            if (values && values["redirect"]) {
                $("#" + detailId).empty();
                if ($.mobile && $.mobile.changePage) {
                    $.mobile.changePage(values["redirect"], { transition: "slideup"});
                } else {
                    location.href = values["redirect"];
                }
                return;
            }

            if (jsonData["result"] == "success") {
                if (callback) {
                    callback(jsonData);
                } else {
                    $.setDetailValues(detailId, itemTplId, jsonData)
                }
            } else {
                var error = "Unknown Error";
                if (jsonData["errors"] && jsonData["errors"]["grobal"]) {
                    error = jsonData["errors"]["grobal"];
                }
                var errorHtml = $("<div>").addClass("mt10").addClass(
                    "warningMessage").text(error);
                $("#" + detailId).replaceWith(errorHtml);
            }
            $("#" + detailId).show();
        },
        error : function(XMLHttpRequest, textStatus, errorThrown) {
            var error = "Request Error: " + errorThrown + ". " + url;
            errorHtml = $("<div>").addClass("mt10").addClass("warningMessage")
                .text(error);
            $("#" + detailId).replaceWith(errorHtml);
            $("#" + detailId).show();
        }
    });
};

$.setDetailValues = function(detailId, itemTplId, jsonData) {
    var values = jsonData.values;
    if (values) {
        if ($("#" + itemTplId).size() > 0) {
            $("#" + itemTplId).tmpl([ values ]).appendTo("#" + detailId);
        } else {
            $.tmpl(itemTplId, [ values ]).appendTo("#" + detailId);
        }
    } else {
        $("#" + detailId).remove();
    }
}

$.template("optionsTemplate", '<option value="${key}">${value}<\/option>');

$.template("idHiddenTemplate", '{{if id}}<input name="id" id="idField" type="hidden" value="${id}" \/>{{/if}}<input name="mode" type="hidden" value="submit" \/>');
$.template("submitTemplate", '<input id="submit" type="submit" value="${submit}" \/>');
