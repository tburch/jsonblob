package jsonblob

import grails.converters.JSON

class HealthCheckController {
    def status() {
        render(contentType: 'text/json') {
            [status: "OK",
            timestamp: new Date(),
            random: Math.random()]
        }
    }
}
