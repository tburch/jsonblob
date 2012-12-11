<!DOCTYPE html>
<html>
<head>
    <meta name="layout" content="bootstrap"/>
    <title>The JSON Blob API</title>
</head>

<body>
<div class="row-fluid">
    <div class="span3">
        <ul id="apiNav" class="nav nav-list affix">
            <li><a href="#post"><i class="icon-chevron-right"></i> POST</a></li>
            <li><a href="#get"><i class="icon-chevron-right"></i> GET</a></li>
            <li><a href="#put"><i class="icon-chevron-right"></i> GET</a></li>
            <li><a href="#wildcard"><i class="icon-chevron-right"></i> Custom URLs</a></li>
        </ul>
    </div>
    <div class="span9">
        <p id="post" class="lead">/api/jsonBlob POST Requests</p>
        <p>
            Creating a JSON Blob can be accomplished by sending a <code>POST</code> request to <code>/api/jsonBlob</code>.
            The request body should contain valid JSON that the blob stored with.
            Upon successfully storing the JSON blob, a <code>201</code> response will be returned.
            The <code>Location</code> Header in the response will be set to the URL at which the blob can be accessed with a <a href="#get"><code>GET</code></a> request.
            The body of the response is the JSON that was stored in the blob.
        </p>
        <pre class="pre-scrollable">
curl -i -d '{"people":["bill", "steve", "bob"]}' -H "Content-Type: application/json" -H "Accept: application/json" http://jsonblob.com/api/jsonBlob
HTTP/1.1 201 Created
Location: http://jsonblob.com/api/jsonBlob/${demoObjectId}
Content-Type: application/json
Transfer-Encoding: chunked

{"people":["bill","steve","bob"]}
        </pre>
        <p id="get" class="lead">/api/jsonBlob/{blobId} GET Requests</p>
        <p>
            Retrieving a JSON Blob can be accomplished by sending a <code>GET</code> request to <code>/api/jsonBlob/{blobId}</code>, where <code>blobId</code> is the last part of the URL path returned from the <a href="#post"><code>POST</code></a> request.
            Upon successfully Retrieving the JSON blob, a <code>200</code> response will be returned.
            The body of the response is the JSON that was stored in the blob.
        </p>
        <pre class="pre-scrollable">
curl -i -H "Content-Type: application/json" -H "Accept: application/json" http://jsonblob.com/api/jsonBlob/${demoObjectId}
HTTP/1.1 200 OK
Content-Type: application/json
Transfer-Encoding: chunked

{"people":["bill","steve","bob"]}
        </pre>
        <p id="put" class="lead">/api/jsonBlob/{blobId} PUT Requests</p>
        <p>
            Updating a JSON Blob can be accomplished by sending a <code>PUT</code> request to <code>/api/jsonBlob/{blobId}</code>.
            The request body should contain valid JSON that the stored blob will be replaced with.
            Upon successfully storing the new JSON blob, a <code>200</code> response will be returned.
            The body of the response is the JSON that was stored in the blob.
        </p>
        <pre class="pre-scrollable">
curl -i -X "PUT" -d '{"people":["fred", "mark", "andrew"]}' -H "Content-Type: application/json" -H "Accept: application/json" http://jsonblob.com/api/jsonBlob/${demoObjectId}
HTTP/1.1 200 OK
Content-Type: application/json
Transfer-Encoding: chunked

{"people":["fred","mark","andrew"]}
        </pre>
        <p id="wildcard" class="lead">Custom Urls</p>
        <p>
            <a href="#get"><code>GET</code></a> and <a href="#put"><code>PUT</code></a> requests can be customized to support any url path scheme.
            The only requirements are that the first path part is <code>/api</code> and that the <code>blobId</code> is present somewhere as a URL path part following <code>/api</code>.
            Upon successfully Retrieving the JSON blob, a <code>200</code> response will be returned.
            The body of the response is the JSON that was stored in the blob.
        </p>
        <p>As an example, if we were trying to mimic a RESTful url structure for getting the names of the employees in with a particular role, we may use want to use a url like <a href="/api/company/${demoObjectId}/employees/engineers"><code>/api/company/${demoObjectId}/employees/engineers</code></a> where <code>${demoObjectId}</code> is the <code>blobId</code> that represents the data the client is expecting.</p>
        <pre class="pre-scrollable">
curl -i  -H "Content-Type: application/json" -H "Accept: application/json" http://jsonblob.com/api/company/${demoObjectId}/employees/engineers
HTTP/1.1 200 OK
Content-Type: application/json
Transfer-Encoding: chunked

{"people":["bill","steve","bob"]}
        </pre>
    </div>
</div>
</body>
</html>
