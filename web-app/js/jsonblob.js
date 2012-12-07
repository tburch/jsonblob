$(function () {
    var jsonFormatterId = "json-formatter"
    var jsonEditorId = "json-editor"
    var apiBase = "/api/jsonBlob"

    var lastChanged = null;
    var editor = null;
    var formatter = null;

    formatter = new JSONFormatter(document.getElementById(jsonFormatterId), {
        change: function () {
            lastChanged = formatter;
        }
    });
    formatter.set(jsonBlob);
    formatter.onError = function (err) {
        console.log(err);
    };
//
    // editor
    editor = new JSONEditor(document.getElementById(jsonEditorId), {
        change: function () {
            lastChanged = editor;
        }
    });
    editor.set(jsonBlob);
});
