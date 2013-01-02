$(function () {
    var jsonFormatterId = "json-formatter";
    var jsonEditorId = "json-editor";
    var editorErrors = $("#alerts-editor");
    var formatterErrors = $("#alerts-formatter");
    var toFormatterButton = $("#to-formatter");
    var toEditorButton = $("#to-editor")
    var newJson = $("#new");
    var openFile = $("#open-file");
    var openUrl = $("#open-url");
    var saveFile = $("#save-file");
    var saveUrl = $("#save-url");
    var clearJson = $("#clear");
    var rawUrl = $("#raw-json");
    var modalRawJsonUrl = $("#rawJsonUrl");
    var modalJsonEditorUrl = $("#jsonEditorUrl");
    var jsonSharedModal = $("#jsonSharedModal");
    var fetchUrlModal = $('#fetchUrlModal');

    fetchUrlModal.find('form').submit(function(e){
        e.preventDefault();
    });

    var apiBase = "/api/jsonBlob";
    var blobId = window.location.pathname.substr(1);
    var sawShareModal = false;

    var defaultJson = {
        "name": "John Smith",
        "age": 32,
        "employed": true,
        "address": {
            "street": "701 First Ave.",
            "city": "Sunnyvale, CA 95125",
            "country": "United States"
        },
        "children": [
            {
                "name": "Richard",
                "age": 7
            },
            {
                "name": "Susan",
                "age": 4
            },
            {
                "name": "James",
                "age": 3
            }
        ]
    };

    var saved = true;
    var lastChangeByEditor = null;
    var editor = null;
    var formatter = null;

    // basic functions for the API
    var save = function(callback) {
        var request;
        if (!blobId) {
            request = {
                type: "POST",
                url: apiBase,
                headers: {'Content-Type': 'application/json', 'Accept':'application/json'},
                data: formatter.getText(),
                success: function(data, textStatus, jqXHR) {
                    saved = true;
                    var locationHeader = jqXHR.getResponseHeader("Location");
                    var parts = locationHeader.split("/");
                    blobId = parts[parts.length - 1];
                    rawUrl.removeClass("hidden").show("slow");

                    history.pushState(null, "JSON Blob " + blobId, "/" + blobId);

                    if (callback && typeof(callback) == 'function') {
                        callback(data, textStatus, jqXHR)
                    }
                },
                cache: false
            };
        } else {
            var blobApiUrl = [apiBase, blobId].join("/")
            request = {
                type: "PUT",
                url: blobApiUrl,
                headers: {'Content-Type': 'application/json', 'Accept':'application/json'},
                data: formatter.getText(),
                success: function(data, textStatus, jqXHR) {
                    saved = true;
                    // TODO pushstate url with blob id
                    if (callback && typeof(callback) == 'function') {
                        callback(data, textStatus, jqXHR)
                    }
                },
                cache: false
            };
        }
        if (request) {
            $.ajax(request);
        }
    };

    var reset = function() {
        saved = false;
        sawShareModal = false;
        var json = {};
        formatter.set(json);
        editor.set(json);
        blobId = ""
        rawUrl.addClass("hidden").show();

        history.pushState(null, "JSON Blob", "/");
    }

    var formatterToEditor = function() {
        var error = false
        try {
            formatterErrors.empty();
            editor.set(formatter.get());
            if (blobId) {
                save();
            }
        } catch (err) {
            var msg = err.message.substr(0, err.message.indexOf("<a")) // remove json lint link
            formatterErrors.append('<div class="alert alert-block alert-error fade in"><button type="button" class="close" data-dismiss="alert">&times;</button>' + msg + '</div>');
            formatterErrors.find(".alert").alert();
            error = true;
        }
        return error;
    };

    var editorToFormatter = function() {
        try {
            editorErrors.empty();
            formatter.set(editor.get());
            if (blobId) {
                save();
            }
        } catch (err) {
            editorErrors.append('<div class="alert alert-block alert-error fade in"><button type="button" class="close" data-dismiss="alert">×</button>' + err.message + '</div>');
            editorErrors.find(".alert").alert();
        }
    };

    var init = function() {
        // setup the formatter
        formatter = new JSONFormatter(document.getElementById(jsonFormatterId), {
            change: function () {
                lastChanged = formatter;
                saved = false;
            }
        });

        // setup the editor
        editor = new JSONEditor(document.getElementById(jsonEditorId), {
            change: function () {
                lastChanged = editor;
                saved = false;
            }
        });

        if (!blobId) {
            formatter.set(defaultJson)
            editor.set(defaultJson)
        } else {
            var blobApiUrl = [apiBase, blobId].join("/")
            $.getJSON(blobApiUrl, function(data) {
                saved = true;
                formatter.set(data);
                editor.set(data);
                rawUrl.removeClass("hidden").show();
                sawShareModal = true;
                // TODO pushstate url with blob id
            });
        }
    }

    var saveToDisk = function() {
        var data = formatter.getText();
        var ts = (new Date()).getTime();
        saveFile.attr({
            "href" : "data:application/json;charset=utf-8," + encodeURIComponent(data),
            "download" : (blobId ? blobId : ts) + ".json"
        });
    }

    /* hook up the UI stuff */
    // raw JSON link
    rawUrl.click(function() {
        if (blobId) {
            var blobApiUrl = [apiBase, blobId].join("/")
            window.open(blobApiUrl, "jsonBlob_" + blobId);
        }
    });

    // create blob link
    saveUrl.click(function() {
        var callback = function() {
            if (!sawShareModal) {
                var location = "http://" + document.location.href.split("/")[2];
                modalJsonEditorUrl.append(location + "/" + blobId);
                modalRawJsonUrl.append(location + apiBase + "/" + blobId);
                jsonSharedModal.modal();
                sawShareModal = true;
            }
        }
        if (!lastChangeByEditor) {
            if (!formatterToEditor()) {
                save(callback);
            }
        } else {
            editorToFormatter();
            save(callback);
        }

    });

    // download json file
    saveFile.click(function() {
        if (!lastChangeByEditor) {
            if (!formatterToEditor()) {
                saveToDisk();
            }
        } else {
            editorToFormatter();
            saveToDisk();
        }
    });

    // upload JSON
    openFile.click(function() {
        var modal = $('#uploadFileModal');
        $('#jsonFile').fileupload({
            dataType: 'json',
            add: function (e, data) {
                data.context = $('<button class="btn btn-primary" type="submit"/>').text('Upload')
                    .appendTo($("#jsonFile").parent())
                    .click(function () {
                        $(this).replaceWith($('<p/>').text('Uploading...'));
                        data.submit();
                    });
            },
            done: function (e, data) {
                formatter.set(data.result);
                formatterToEditor();
                modal.modal('hide');
                saved = false;
            }
        });
        modal.modal();
    });

    $("#fetchJSONButton").click(function() {
        var url = $("#jsonUrl").val();
        $.ajax({
            type: "GET",
            url: url,
            dataType: 'json',
            cache: false,
            success: function(data) {
                formatter.set(data);
                formatterToEditor();
                fetchUrlModal.modal('hide');
                saved = false;
            },
            error: function() {
                $.ajax({
                    type: "POST",
                    url: '/file/fetch',
                    dataType: 'json',
                    data: {"url": url},
                    cache: false,
                    success: function(data) {
                        formatter.set(data);
                        formatterToEditor();
                        fetchUrlModal.modal('hide');
                        saved = false;
                    },
                    error: function() {
                        fetchUrlModal.modal('hide');
                        $("#fetchUrlErrorModal").find("pre").text(url);
                        $("#fetchUrlErrorModal").modal();
                    }
                });
            }
        });
    });

    // upload JSON
    openUrl.click(function() {
        fetchUrlModal.modal();
    });

    // clear the editor and formatter with either the clear button
    clearJson.click(function() {
       reset();
    });

    // clear the editor and formatter with either the new button
    newJson.click(function() {
        reset();
    });

    // format pane to editor pane
   toEditorButton.click(function() {
       formatterToEditor();
   });

    //editor pane to format pane
    toFormatterButton.click(function() {
        editorToFormatter();
    });

    var resize = function() {
        var height = $(window).height();
        height -=  $(".navbar-fixed-top").height();
        height -=  $(".navbar-fixed-bottom").height();
        height -=  $(".controls-row").height();
        height -= 20;
        $('.editor').height(height);
    }

    $(document).ready(function(){
        resize();
        init();
    });

    $(window).resize(function() {
        resize();
    });

    $(window).bind('beforeunload', function() {
        if (!saved) {
            return "You have unsaved work, it will be lost if you leave the page.";
        }
    });

});
