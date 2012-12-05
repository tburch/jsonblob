<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html;charset=utf-8">

    <title><g:layoutTitle default="JSON Editor Online - view, edit and format JSON online"/></title>

    <!--

    @file index.html

    @brief
    JSON Editor Online is a web-based tool to view, edit, and format JSON.
    It shows your data side by side in a clear, editable treeview and in
    formatted plain text.

    Supported browsers: Chrome, Firefox, Safari, Opera, Internet Explorer 8+

    @license
    This json editor is open sourced with the intention to use the editor as
    a component in your own application. Not to just copy and monetize the editor
    as it is.

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy
    of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.

    Copyright (C) 2011-2012 Jos de Jong, http://jsoneditoronline.org

    @author   Jos de Jong, <wjosdejong@gmail.com>
    @date     2012-11-03
    -->

    <meta name="description"
          content="JSON Editor Online is a web-based tool to view, edit, and format JSON. It shows your data side by side in a clear, editable treeview and in formatted plain text.">
    <meta name="keywords"
          content="json, editor, formatter, online, format, parser, json editor, json editor online, online json editor, javascript, javascript object notation, tools, tool, json tools, treeview, open source, free, json parser, json parser online, json formatter, json formatter online, online json formatter, online json parser, format json online">
    <meta name="author" content="Jos de Jong">

    <r:require module="application"/>

    <g:layoutHead/>
    <r:layoutResources/>
</head>

<body>

<div id="header">
    <g:link base="/" class="header">
        <img alt="JSON Editor Online" title="JSON Editor Online" src="/img/logo.png" id="logo">
    </g:link>
    <div id="menu">
        <ul>
            <li>
                <a id="clear" title="Clear contents">Clear</a>
            </li>
            <li>
                <a id="open" title="Open file from disk or url">
                    Open
                    <span id="openMenuButton" title="Open file from disk or url">
                        &#x25BC;
                    </span>
                </a>
                <ul id="openMenu">
                    <li>
                        <a id="menuOpenFile" title="Open JSON file from disk">Open&nbsp;file</a>
                    </li>
                    <li>
                        <a id="menuOpenUrl" title="Open JSON file from url">Open&nbsp;url</a>
                    </li>
                </ul>
            </li>
            <li>
                <a id="save" title="Save JSON file to disk">Save</a>
            </li>
            <li>
                <a id="create" title="Create a shareable URL">Create URL</a>
            </li>
        </ul>
    </div>
</div>

<div id="auto">
    <div id="contents">
        <div id="jsonformatter"></div>

        <div id="splitter"></div>

        <div id="jsoneditor"></div>
    </div>
</div>

<div id="footer">
    <div id="footer-inner">
        <a href="http://jsoneditoronline.org" class="footer">JSON Editor Online 1.6.2</a>
    &bull;
        <a href="changelog.txt" target="_blank" class="footer">Changelog</a>
    &bull;
        <a href="https://github.com/wjosdejong/jsoneditoronline" target="_blank" class="footer">Sourcecode</a>
    &bull;
        <a href="datapolicy.txt" target="_blank" class="footer">Data policy</a>
    &bull;
        <a href="NOTICE" target="_blank" class="footer">Copyright 2011-2012 Jos de Jong</a>
    </div>
</div>

<r:layoutResources/>

<g:layoutBody/>

<script type="text/javascript">
    app.resize();
</script>

</body>
</html>