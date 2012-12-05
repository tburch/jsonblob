class UrlMappings {

	static mappings = {
        "/$id?"(controller: 'blob', action: 'load')

		"/"(view:"/index")
		"500"(view:'/error')
	}
}
