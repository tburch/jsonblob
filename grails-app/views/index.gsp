<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="bootstrap"/>
    <title>Welcome to JSON Blob<g:if test="${blobId}"> - blob ${blobId}</g:if></title>

    <r:require modules="jsonEditor, jsonBlob" />
</head>

<body>
<div class="row-fluid">
    <div class="span5 well">
        <div id="json-formatter"></div>
    </div>
    <div class="span1">
        <div class="row-fluid">
            <div class="span12 well" id="to-text"><i class="control icon-caret-left icon-large"></i></div>
        </div>
        <div class="row-fluid">
            <div class="span12 well" id="to-json"><i class="control icon-caret-right icon-large"></i></div>
        </div>
    </div>
    <div class="span6 well">
        <div id="json-editor"></div>
    </div>
</div>

<g:if test="${blob}">
    <script type="text/javascript">
        var jsonBlob = ${blob};
    </script>
</g:if>
<g:if test="${!blob}">
    <script type="text/javascript">
        var jsonBlob = {
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
    </script>
</g:if>
</body>
</html>
