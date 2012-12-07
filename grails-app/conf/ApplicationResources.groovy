modules = {
    jsonEditorOnline {
        resource url: 'css/fileretriever.css', disposition: 'head'
        resource url: 'css/app.css', disposition: 'head'
        resource url: 'js/queryparams.js'
        resource url: 'js/ajax.js'
        resource url: 'js/fileretriever.js'
        resource url: 'js/notify.js'
        resource url: 'js/splitter.js'
        resource url: 'js/app.js'

        dependsOn 'jsonEditor'
    }

    jsonEditor {
        resource url: 'css/jsoneditor.css', disposition: 'head'
        resource url: 'js/jsoneditor.js'
        resource url: 'js/lib/jsonlint/jsonlint.js'
    }

    jsonBlob {
        resource url: 'js/jsonblob.js'
    }

    jsonBlobTheme {
        dependsOn 'bootstrap'
        resource url: 'css/theme.css', disposition: 'head'
    }

    icons {
        resource url:'img/favicon.ico', atrs:[rel:"shortcut icon", type:"image/x-icon"], disposition:'head'
    }

    inspiritas {
        dependsOn 'bootstrap'
        resource url: 'css/inspiritas.css'
    }

    fontAwesome {
        dependsOn 'bootstrap'
        resource url:'css/font-awesome.css', disposition: 'head'
        resource url:'css/font-awesome-ie7.css', disposition: 'head', wrapper: {s -> "<!--[if IE 7]>$s<![endif]-->"}
    }

    html5 {
        resource url:'js/html5.js', disposition: 'head', wrapper: {s -> "<!--[if lt IE 9]>$s<![endif]-->"}
    }
}