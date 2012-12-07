<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="bootstrap"/>
    <title>Welcome to JSON Blob<g:if test="${blobId}"> ${blobId}</g:if></title>

    <r:require module="jsonEditor" />
</head>

<body type="editor">
<div class="row-fluid">
    <div id="alerts-formatter" class="span5"></div>
    <div class="span2">
        <div class="span12 pagination-centered">
            <p>
                <a href="#"><button id="to-text" class="btn"><i class="control icon-caret-left"></i></button></a>
                <a href="#"><button id="to-json" class="btn"><i class="control icon-caret-right"></i></button></a>
            </p>
        </div>
    </div>
    <div id="alerts-editor" class="span5"></div>
</div>
<div class="row-fluid">
    <div class="span6">
        <div id="json-formatter"></div>
    </div>
    <div class="span6">
        <div id="json-editor"></div>
    </div>
</div>

</body>
</html>
