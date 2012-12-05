grails.servlet.version = "3.0" // Change depending on target container compliance (2.5 or 3.0)
grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.6
grails.project.source.level = 1.6
//grails.project.war.file = "target/${appName}-${appVersion}.war"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // specify dependency exclusions here; for example, uncomment this to disable ehcache:
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    checksums true // Whether to verify checksums on resolve

    repositories {
        inherits true // Whether to inherit repository definitions from plugins

        grailsPlugins()
        grailsHome()
        grailsCentral()

        mavenLocal()
        mavenCentral()
    }
    dependencies {
        runtime "org.mongodb:mongo-java-driver:2.9.1"
        compile "org.mongodb:mongo-java-driver:2.9.1"
        runtime "com.gmongo:gmongo:1.0"
        compile "com.fasterxml.jackson.core:jackson-databind:2.1.0"
    }

    plugins {
        runtime ":jquery:1.8.3"
        runtime ":resources:1.2.RC2"
        runtime ":zipped-resources:1.0"
        runtime ":cached-resources:1.0"
        runtime ":yui-minify-resources:0.1.5"
        runtime ":cache-headers:1.1.5"

        compile (":mongodb:1.0.0.GA"){
            excludes 'mongo-java-driver', 'gmongo'
        }

        compile ":jaxrs:0.6"

        build ":tomcat:$grailsVersion"
    }
}
