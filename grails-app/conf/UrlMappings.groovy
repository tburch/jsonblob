class UrlMappings {

    static excludes = ["/favicon.ico"]

	static mappings = {
        "/$controller/$action?/$id?"{
            constraints {
                // apply constraints here
            }
        }
		"/"(view:"/index")
        "/about"(view: "about")
        "/api"(controller: 'api', action: 'index')
        "/$id?"(controller: 'blob', action: 'load')
		"500"(view:'/error')
	}
}
