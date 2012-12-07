class UrlMappings {

    static excludes = ["/favicon.ico"]

	static mappings = {
        "/$controller/$action?/$id?"{
            constraints {
                // apply constraints here
            }
        }
		"/"(view:"/index")
        "/bootstrap"(view:"/index-bootstrap")
        "/about"(view: "about")
        "/$id?"(controller: 'blob', action: 'load')
		"500"(view:'/error')
	}
}
