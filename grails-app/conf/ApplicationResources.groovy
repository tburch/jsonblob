modules = {

    jsonEditor {
        defaultBundle 'app'
        resource url: 'css/jsoneditor.css', disposition: 'head'
        resource url: 'js/jsoneditor.js'
        resource url: 'js/lib/jsonlint/jsonlint.js'
        resource url: 'js/jsonblob.js'
        resource url: 'js/lib/jquery/jquery.ui.widget.js'
        resource url: 'js/lib/jquery/jquery.iframe-transport.js'
        resource url: 'js/lib/jquery/jquery.fileupload.js'
    }

    jsonBlob {
        defaultBundle 'app'
        dependsOn('jquery', 'bootstrap')
        resource url: 'css/theme.css', disposition: 'head'
    }

    jquery {
        defaultBundle 'app'
        resource url: 'js/lib/jquery/jquery-1.8.3.js'
    }

    bootstrap {
        defaultBundle 'app'
        dependsOn('jquery', 'html5')
        resource url: 'js/lib/bootstrap/bootstrap.js'

        resource url: 'css/bootstrap.css', disposition: 'head'
        resource url: 'css/bootstrap-responsive.css', disposition: 'head'
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