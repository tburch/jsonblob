class UrlMappings {

    static excludes = ["/favicon.ico"]

	static mappings = {
        "/$controller/$action?/$id?"{
            constraints {
                // apply constraints here
            }
        }
		"/"(view:"/editor")
        "/robots.txt" (view: "/robots")
        "/about"(view: "/about")
        "/api"(controller: 'api', action: 'index')
        "/$id?" {
            controller = 'blob'
            action = 'load'
            constraints {
                id(matches: /[0-9a-f]{24}/)

            }
        }
		"500"(view:'/error')
        "404"(view:'/notfound')
	}
}
