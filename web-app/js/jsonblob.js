$(function () {
    var jsonFormatterId = "json-formatter"
    var jsonEditorId = "json-editor"
    var apiBase = "/api/jsonBlob"
    var blobId = window.location.pathname.substr(1);

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

    var lastChanged = null;
    var editor = null;
    var formatter = null;

    formatter = new JSONFormatter(document.getElementById(jsonFormatterId), {
        change: function () {
            lastChanged = formatter;
        }
    });

    editor = new JSONEditor(document.getElementById(jsonEditorId), {
        change: function () {
            lastChanged = editor;
        }
    });

    if (!blobId) {
        formatter.set(defaultJson)
        editor.set(defaultJson)
    } else {
        var blobApiUrl = [apiBase, blobId].join("/")
        $.getJSON(blobApiUrl, function(data) {
            formatter.set(data);
            editor.set(data);
        });
    }
});
