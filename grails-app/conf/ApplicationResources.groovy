modules = {
    application {
        resource url:'img/favicon.ico', atrs:[rel:"shortcut icon", type:"image/x-icon"], disposition:'head'

        resource url: 'css/fileretriever.css', disposition: 'head'
        resource url: 'css/app.css', disposition: 'head'
        resource url: 'css/jsoneditor.css', disposition: 'head'

        dependsOn('jquery')

        resource url: 'js/queryparams.js'
        resource url: 'js/ajax.js'
        resource url: 'js/fileretriever.js'
        resource url: 'js/notify.js'
        resource url: 'js/splitter.js'
        resource url: 'js/app.js'
        resource url: 'js/jsoneditor.js'
        resource url: 'js/lib/jsonlint/jsonlint.js'
    }
}