grails.servlet.version = "2.5" // Change depending on target container compliance (2.5 or 3.0)
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

        mavenRepo "http://snapshots.repository.codehaus.org"
        mavenRepo "http://repository.codehaus.org"
        mavenRepo "http://repository.jboss.com/maven2/"
        mavenRepo("http://maven.restlet.org")
    }
    dependencies {
        runtime "org.mongodb:mongo-java-driver:2.9.1"
        compile "org.mongodb:mongo-java-driver:2.9.1"
        runtime "com.gmongo:gmongo:1.0"
        compile "com.fasterxml.jackson.core:jackson-databind:2.1.0"
    }

    plugins {
        runtime ":hibernate:$grailsVersion"

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

        compile ':shiro:1.1.4'

        compile ':heroku:1.0.1'
        compile ':cloud-support:1.0.8'

        build ":tomcat:$grailsVersion"

        runtime ":database-migration:1.1"

        compile ':cache:1.0.0'
    }
}
