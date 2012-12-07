<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE HTML>

<!DOCTYPE HTML>
<html lang="en">
<head>
    <!-- Force latest IE rendering engine or ChromeFrame if installed -->
    <!--[if IE]><meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"><![endif]-->
    <meta charset="utf-8">
    <title><g:layoutTitle default="JSON Blob"/> | create, edit, view, format, and share JSON</title>
    <meta name="description" content="File Upload widget with multiple file selection, drag&amp;drop support, progress bar and preview images for jQuery. Supports cross-domain, chunked and resumable file uploads. Works with any server-side platform (Google App Engine, PHP, Python, Ruby on Rails, Java, etc.) that supports standard HTML form file uploads.">
    <meta name="viewport" content="width=device-width">
    <meta name="description"
          content="JSON Blob is a web-based tool to create, edit, view, format, and share JSON. It shows your JSON side by side in a clear, editable tree-view and in formatted plain text. You can save your JSON and share it via URL with anyone">
    <meta name="keywords"
          content="jsonblob, jsobblob.com, json, editor, formatter, online, format, parser, json editor, jsonblob, json blob, online json editor, javascript, javascript object notation, tools, tool, json tools, tree-view, open source, free, json parser, json parser online, json formatter, json formatter online, online json formatter, online json parser, format json online, jsbin">
    <meta name="author" content="Tristan Burch">

    <r:require modules="icons, jsonBlobTheme, fontAwesome, html5, jquery"/>

    <g:layoutHead/>
    <r:layoutResources/>

    <style type="text/css">
        /* fixes https://github.com/FortAwesome/Font-Awesome/issues/6*/
    [class^="icon-"] {
        background-image:none;
    }

    body {
        padding-top: 60px;
        padding-bottom: 40px;
    }
    </style>

    <ga:trackPageview />
</head>
<body>
<div class="navbar navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>
            <g:link absolute="true" url="/" class="brand">JSON Blob</g:link>
            <div class="nav-collapse">
                <ul class="nav">
                    <li><a href="#"id="new"><i class="icon-file"></i> New</a></li>
                    <li class="dropdown">
                        <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                            Open
                            <b class="caret"></b>
                        </a>
                        <ul class="dropdown-menu">
                            <li id="open-file"><a href="#"><i class="icon-file"></i> File</a></li>
                            <li id="open-url"><a href="#"><i class="icon-link"></i> JSON from URL</a></li>
                        </ul>
                    </li>
                    <li class="dropdown">
                        <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                            Save
                            <b class="caret"></b>
                        </a>
                        <ul class="dropdown-menu">
                            <li id="save-file"><a href="#"><i class="icon-download-alt"></i> File</a></li>
                            <li id="save-url"><a href="#"><i class="icon-share-alt"></i> Sharable URL</a></li>
                        </ul>
                    </li>
                    <li><a href="#"><i class="icon-remove"></i> Clear</a></li>
                    <li class="divider-vertical"></li>
                    <li><a href="#"><i class="icon-external-link"></i> Raw JSON</a></li>
                </ul>
                <shiro:notUser>
                    <span class="nav pull-right">
                        <g:link controller="signup" action="index" class="btn btn-warning">Sign up</g:link>
                    </span>
                    <ul class="nav pull-right">
                        <li class="${pageProperty(name:'body.section') ==~ 'login' ? 'active' : ""}"><g:link controller="auth" action="login"><i class="icon-signin"></i> Log in</g:link></li>
                    </ul>
                </shiro:notUser>
                <shiro:user>
                    <ul class="nav pull-right">
                        <li class="dropdown">
                            <a class="dropdown-toggle" data-toggle="dropdown" href="#"><shiro:principal/><b class="caret"></b></a>
                            <ul class="dropdown-menu">
                                <li><g:link controller="profile" action="index"><i class="icon-cloud"></i> My blobs</g:link></li>
                                <li class="divider"></li>
                                <li><g:link controller="profile" action="settings"><i class="icon-list"></i> Settings</g:link></li>
                                <li><g:link controller="auth" action="signOut"><i class="icon-signout"></i> Log out</g:link></li>
                            </ul>
                        </li>
                    </ul>
                </shiro:user>
            </div>
        </div>
    </div>
</div>
<div class="container">
    <div class="row-fluid">
        <div class="span5 well">
            Text Editor
        </div>
        <div class="span1">
            Controls
        </div>
        <div class="span6 well">
            JSON Editor
        </div>
    </div>
</div> <!-- /container -->

<div class="navbar navbar-fixed-bottom">
    <div class="navbar-inner">
        <div class="container">
            <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>
            <div class="nav-collapse">
                <ul class="nav">
                    <li><g:link controller="api" action="index">API</g:link></li>
                    <li><g:link url="/about" absolute="true">About</g:link></li>
                    <li><g:link url="http://tristanburch.com">&copy; 2012 Tristan Burch</g:link></li>
                </ul>
            </div>
        </div>
    </div>
</div>

<!-- Le javascript
    ================================================== -->
<!-- Placed at the end of the document so the pages load faster -->

<r:layoutResources />

</body>
</html>
