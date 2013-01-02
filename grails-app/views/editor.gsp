<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="bootstrap"/>
    <title>Welcome to JSON Blob<g:if test="${blobId}"> ${blobId}</g:if></title>

    <r:require module="jsonEditor" />
</head>

<body type="editor">

<div class="row-fluid controls-row">
    <div id="alerts-formatter" class="span4"></div>
    <div class="span4">
        <div class="span12 pagination-centered">
            <p>
                <a><button id="to-formatter" class="btn"><i class="control icon-caret-left"></i></button></a>
                <a><button id="to-editor" class="btn"><i class="control icon-caret-right"></i></button></a>
            </p>
        </div>
    </div>
    <div id="alerts-editor" class="span4"></div>
</div>

<div class="row-fluid">
    <div class="span6">
        <div id="json-formatter" class="editor"></div>
    </div>
    <div class="span6">
        <div id="json-editor" class="editor"></div>
    </div>
</div>

<div id="jsonSharedModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="shareModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h3 id="shareModalLabel">Your JSON Blob has been created!</h3>
    </div>
    <div class="modal-body">
        <p>You can share this JSON editor using the url:</p>
        <pre id="jsonEditorUrl"></pre>
        <p>You can share the raw JSON using this url:</p>
        <pre id="rawJsonUrl"></pre>
    </div>
    <div class="modal-footer">
        <button class="btn btn-primary" data-dismiss="modal" aria-hidden="true">Got it</button>
    </div>
</div>

<div id="uploadFileModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="uploadModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h3 id="uploadModalLabel">So you want to upload JSON?</h3>
    </div>
    <div class="modal-body">
        <form>
            <fieldset>
                <label>JSON File</label>
                <input id="jsonFile" type="file" data-url="/file/upload" name="file" placeholder="Select a file from your computer">
                <span class="help-block">Select a file from your computer</span>
            </fieldset>
        </form>
    </div>

    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">Never mind</button>
    </div>
</div>

<div id="fetchUrlModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="fetchModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h3 id="fetchModalLabel">So you want to us to get JSON for you?</h3>
    </div>
    <div class="modal-body">
        <form>
            <fieldset>
                <label>JSON URL</label>
                <input id="jsonUrl" type="text" data-url="/file/upload" name="url" placeholder="JSON URL">
                <span class="help-block">Enter a URL that returns JSON</span>
                <button class="btn btn-primary" type="submit" id="fetchJSONButton">I'm Lazy, fetch the JSON</button>
            </fieldset>
        </form>
    </div>

    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">Never mind</button>
    </div>
</div>

<div id="fetchUrlErrorModal" class="modal fade" tabindex="-1" role="dialog" aria-labelledby="fetchErrorModalLabel" aria-hidden="true">
    <div class="modal-header">
        <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
        <h3 id="fetchErrorModalLabel">There was an error getting the JSON!</h3>
    </div>
    <div class="modal-body">
       <p>Sorry, we weren't able to get JSON from the following url:</p>
        <pre></pre>
    </div>

    <div class="modal-footer">
        <button class="btn" data-dismiss="modal" aria-hidden="true">Lame</button>
    </div>
</div>

</body>
</html>
