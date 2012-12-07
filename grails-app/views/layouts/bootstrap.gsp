<%@ page contentType="text/html;charset=UTF-8" %>
<!DOCTYPE HTML>

<!DOCTYPE HTML>
<html lang="en">
<head>
    <!-- Force latest IE rendering engine or ChromeFrame if installed -->
    <!--[if IE]><meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1"><![endif]-->
    <meta charset="utf-8">
    <title><g:layoutTitle default="JSON Blob"/> | create, edit, view, format, and share JSON</title>
    <meta name="description" content="JSON Blob is a web-based tool to create, edit, view, format, and share JSON. It shows your JSON side by side in a clear, editable tree-view and in formatted plain text. You can save your JSON and share it via URL with anyone">
    <meta name="keywords" content="jsonblob, jsobblob.com, json, editor, formatter, online, format, parser, json editor, jsonblob, json blob, online json editor, javascript, javascript object notation, tools, tool, json tools, tree-view, open source, free, json parser, json parser online, json formatter, json formatter online, online json formatter, online json parser, format json online, jsbin">
    <meta name="author" content="Tristan Burch">

    <r:require modules="icons, jsonBlobTheme, fontAwesome, html5, jquery"/>

    <g:layoutHead/>
    <r:layoutResources/>

    <ga:trackPageview />
</head>
<body>
<div class="navbar navbar-inverse navbar-fixed-top">
    <div class="navbar-inner">
        <div class="container">
            <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
                <span class="icon-bar"></span>
            </a>
            <g:link absolute="true" url="/" class="brand">JSON Blob</g:link>
            <div class="nav-collapse">
                <g:if test="${pageProperty(name:'body.type') ==~ 'editor'}">
                    <ul class="nav">
                        <li><a href="#" id="new"><i class="icon-file"></i> New</a></li>
                        <li class="dropdown">
                            <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                                Open
                                <b class="caret"></b>
                            </a>
                            <ul class="dropdown-menu">
                                <li><a href="#" id="open-file"><i class="icon-upload-alt"></i> File</a></li>
                                <li><a href="#" id="open-url"><i class="icon-link"></i> JSON from URL</a></li>
                            </ul>
                        </li>
                        <li class="dropdown">
                            <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                                Save
                                <b class="caret"></b>
                            </a>
                            <ul class="dropdown-menu">
                                <li><a href="#" id="save-file"><i class="icon-download-alt"></i> File</a></li>
                                <li><a href="#" id="save-url"><i class="icon-share-alt"></i> Sharable URL</a></li>
                            </ul>
                        </li>
                        <li><a href="#" id="clear"><i class="icon-remove"></i> Clear</a></li>
                        <li class="divider-vertical"></li>
                        <li><a href="#" id="raw-json"><i class="icon-external-link"></i> Raw JSON</a></li>
                    </ul>
                </g:if>
                <shiro:notUser>
                    <ul class="nav pull-right">
                        <li class="dropdown">
                            <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                                <i class="icon-user"></i> Sign Up
                                <b class="caret"></b>
                            </a>
                            <ul class="dropdown-menu">
                                <li><a href="#" id="sign-up-github"><i class="icon-github"></i> Github</a></li>
                                <li><a href="#" id="sign-up-twitter"><i class="icon-twitter"></i> Twitter</a></li>
                                <li><a href="#" id="sign-up-google"><i class="icon-google-plus"></i> Google</a></li>
                                <li><a href="#" id="sign-up-facebook"><i class="icon-facebook"></i> Twitter</a></li>
                            </ul>
                        </li>
                        <li class="dropdown">
                            <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                                <i class="icon-signin"></i> Sign In
                                <b class="caret"></b>
                            </a>
                            <ul class="dropdown-menu">
                                <li><a href="#" id="sign-in-github"><i class="icon-github"></i> Github</a></li>
                                <li><a href="#" id="sign-in-twitter"><i class="icon-twitter"></i> Twitter</a></li>
                                <li><a href="#" id="sign-in-google"><i class="icon-google-plus"></i> Google</a></li>
                                <li><a href="#" id="sign-in-facebook"><i class="icon-facebook"></i> Twitter</a></li>
                            </ul>
                        </li>
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
    <g:layoutBody />
</div>

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
                    <li><g:link url="/api">API</g:link></li>
                    <li><g:link url="/about" absolute="true">About</g:link></li>
                    <li><g:link url="http://tristanburch.com" target="_blank">&copy; 2012 Tristan Burch</g:link></li>
                </ul>
            </div>
        </div>
    </div>
</div>

<r:layoutResources />

</body>
</html>
