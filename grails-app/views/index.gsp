<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="bootstrap"/>
    <title>Welcome to JSON Blob<g:if test="${blobId}"> - blob ${blobId}</g:if></title>

    <r:require modules="jsonEditor, jsonBlob" />
</head>

<body>
<div class="row-fluid">
    <div class="span4"></div>
    <div class="span4">
        <p class="container">
            <a href="#"><button id="to-text" class="btn"><i class="control icon-caret-left"></i></button></a>
            <a href="#"><button id="to-json" class="btn"><i class="control icon-caret-right"></i></button></a>
        </p>
    </div>
    <div class="span4"></div>
</div>
<div class="row-fluid">
    <div class="span6">
        <div id="json-formatter"></div>
    </div>
    <div class="span6">
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
