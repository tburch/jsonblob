class UrlMappings {

    static excludes = ["/favicon.ico"]

	static mappings = {
        "/$id?"(controller: 'blob', action: 'load')

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
