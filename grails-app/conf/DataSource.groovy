dataSource {
    pooled = true
    driverClassName = "org.h2.Driver"
    username = "sa"
    password = ""
}
hibernate {
    cache.use_second_level_cache = true
    cache.use_query_cache = false
    cache.region.factory_class = 'net.sf.ehcache.hibernate.EhCacheRegionFactory'
}
// environment specific settings
environments {
    development {
        dataSource {
            dbCreate = "create-drop" // one of 'create', 'create-drop', 'update', 'validate', ''
            url = "jdbc:h2:mem:devDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }

        mongo {
            host = "localhost"
            port = 27017
            username = "admin"
            password = "admin"
            databaseName = "jsonblob"
        }
    }
    test {
        dataSource {
            dbCreate = "update"
            url = "jdbc:h2:mem:testDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
        }

        mongo {
            host = "localhost"
            port = 27017
            username = "admin"
            password = "admin"
            databaseName = "jsonblob"
        }
    }
    production {
        dataSource {
            dbCreate = "update"
            url = "jdbc:h2:prodDb;MVCC=TRUE;LOCK_TIMEOUT=10000"
            pooled = true
            properties {
               maxActive = -1
               minEvictableIdleTimeMillis=1800000
               timeBetweenEvictionRunsMillis=1800000
               numTestsPerEvictionRun=3
               testOnBorrow=true
               testWhileIdle=true
               testOnReturn=true
               validationQuery="SELECT 1"
            }
        }
        def mongoHqUrl = System.properties.get("MONGOHQ_URL") as String
        def mongoMatcher = mongoHqUrl.trim() =~ /mongodb:\/\/(.*):(.*)@(.*):(\d+)\/(.*)/
        mongo {
            host = mongoMatcher[0][3]
            port = mongoMatcher[0][4]
            username = mongoMatcher[0][1]
            password = mongoMatcher[0][2]
            databaseName = mongoMatcher[0][5]
        }
    }
}
