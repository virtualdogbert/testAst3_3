package testast

import ast.virtualdogbert.Config
import grails.converters.JSON
import grails.plugin.springsecurity.annotation.Secured

@Secured("permitAll")
class TestController {
    @Config(value = 'test.number', stat = true)
    Integer test = 6

    @Config('test.number')
    Integer test2 = 5

    @Config('test.list')
        List test3 = [1,2,3]

    @Config('test.map')
    Map test4 = [one:1,two:2,three:3]

    //@ErrorsHandler
    def index(int i) {
        render ([
                i:i,
                test1: test,
                test2: test2,
                test3: test3,
                test4: test4

        ] as JSON)
    }
}
