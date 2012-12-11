modules = {

    jsonEditor {
        defaultBundle 'app'
        resource url: 'css/jsoneditor.css', disposition: 'head'
        resource url: 'js/jsoneditor.js'
        resource url: 'js/lib/jsonlint/jsonlint.js'
        resource url: 'js/jsonblob.js'
        resource url: 'js/jquery.ui.widget.js'
        resource url: 'js/jquery.iframe-transport.js'
        resource url: 'js/jquery.fileupload.js'
        dependsOn 'jquery'
    }

    jsonBlob {
        defaultBundle 'app'
        resource url: 'css/theme.css', disposition: 'head'
        dependsOn 'bootstrap'
    }

    icons {
        defaultBundle 'app'
        resource url:'img/favicon.ico', atrs:[rel:"shortcut icon", type:"image/x-icon"], disposition:'head'
    }

    fontAwesome {
        defaultBundle 'app'
        dependsOn 'bootstrap'
        resource url:'css/font-awesome.css', disposition: 'head'
        resource url:'css/font-awesome-ie7.css', disposition: 'head', wrapper: {s -> "<!--[if IE 7]>$s<![endif]-->"}
    }

    html5 {
        defaultBundle false
        resource url:'js/html5.js', disposition: 'head', wrapper: {s -> "<!--[if lt IE 9]>$s<![endif]-->"}
    }
}