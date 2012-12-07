$(function () {
    var jsonFormatter = $('#json-formatter')
    var jsonEditor = $('#json-editor')
    var apiBase = '/api/jsonBlob'



    var editor = null;
    var formatter = null;

    var formatterToEditor = function() {
        try {
            editor.set(formatter.get());
            var data = formatter.getText();
            if (app.blobId) {
                ajax.put(app.apiBase + "/" + app.blobId, data, {'Content-Type': 'application/json', 'Accept':'application/json'}, function() {
                })
            }
        }
        catch (err) {
            app.notify.showError(err);
        }
    };

    var editorToFormatter = function () {
        try {
            formatter.set(editor.get());
        }
        catch (err) {
            app.notify.showError(err);
        }
    };
});
