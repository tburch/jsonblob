<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="bootstrap"/>
    <title>Welcome to JSON Blob<g:if test="${blobId}"> ${blobId}</g:if></title>

    <r:require module="jsonEditor" />
</head>

<body type="editor">
<div class="row-fluid controls-row">
    <div id="alerts-formatter" class="span5"></div>
    <div class="span2">
        <div class="span12 pagination-centered">
            <p>
                <a ><button id="to-formatter" class="btn"><i class="control icon-caret-left"></i></button></a>
                <a ><button id="to-editor" class="btn"><i class="control icon-caret-right"></i></button></a>
            </p>
        </div>
    </div>
    <div id="alerts-editor" class="span5"></div>
</div>
<div id="jsonSharedModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="modalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h3 id="modalLabel">Your JSON Blob has been created</h3>
    </div>
    <div class="modal-body">
        <p>Your JSON is ready to share with anyone!</p>
        <p>You can share the editor using this url:</p>
        <pre id="jsonEditorUrl"></pre>
        <p>You can share the raw JSON using this url:</p>
        <pre id="rawJsonUrl"></pre>
    </div>
    <div class="modal-footer">
        <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">Got it</button>
    </div>
</div>
<div class="row-fluid">
    <div class="span6">
        <div id="json-formatter" class="editor"></div>
    </div>
    <div class="span6">
        <div id="json-editor" class="editor"></div>
    </div>
</div>

</body>
</html>
