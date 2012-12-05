// environment specific settings
environments {
    development {
        mongo {
            host = "localhost"
            port = 27017
            username = "admin"
            password = "admin"
            databaseName = "jsonblob"
        }
    }
    test {
        mongo {
            host = "localhost"
            port = 27017
            username = "admin"
            password = "admin"
            databaseName = "jsonblob"
        }
    }
    production {
//        mongo {
//            uri = new URI(System.env.MONGOHQ_URL)
//            host = uri.host
//            port = uri.port
//            username = uri.userInfo.split(":")[0]
//            password = uri.userInfo.split(":")[1]
//            databaseName = uri.path.substring(1)
//        }
    }
}
