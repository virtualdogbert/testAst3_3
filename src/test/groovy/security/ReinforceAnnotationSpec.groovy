package security

import ast.virtualdogbert.EnforcerException
import ast.virtualdogbert.Reinforce
import ast.virtualdogbert.ReinforceFilter
import com.security.DomainRole
import com.security.EnforcerService
import com.security.Role
import com.security.Sprocket
import com.security.User
import com.security.UserRole
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class ReinforceAnnotationSpec extends Specification implements ServiceUnitTest<EnforcerService>, DataTest{

    EnforcerService enforcerService

    def setup() {
        mockDomains Role, User, UserRole, DomainRole, Sprocket
        //This enables Enforcer for unit tests because it is turned off by default.
        grailsApplication.config.enforcer.enabled = true
        enforcerService = service
    }

    void 'test method reinforceClosureTrue'() {
        when:
            reinforceClosureTrue()
        then:
            true
    }

    void 'test method reinforceClosureTrueWithFailureClosure'() {
        when:
            reinforceClosureTrueWithFailureClosure()
        then:
            true
    }

    void 'test method reinforceClosureFalseWithFailureClosure'() {
        when:
            reinforceClosureFalseWithFailureClosure()
        then:
            thrown EnforcerException
    }

    void 'test method reinforceClosureTrueWithFailureAndSuccessClosures'() {
        when:
            reinforceClosureTrueWithFailureAndSuccessClosures()
        then:
            true
    }

    void 'test method reinforceClosureFalseWithFailureAndSuccessClosures'() {
        when:
            reinforceClosureFalseWithFailureAndSuccessClosures()
        then:
            thrown EnforcerException
    }

    void 'test method reinforceClosureTestingParameter'() {
        when:
            reinforceClosureTestingParameter(5)
        then:
            true
    }

    void 'test class protection'() {
        setup:
            TestEnforcer t = new TestEnforcer()
        when:
            t.clazzProtectedMethod1()
        then:
            thrown EnforcerException
        when:
            t.clazzProtectedMethod2()
        then:
            thrown EnforcerException
        when:
            t.methodProtectedMethod1()
        then:
            true
    }

    void 'test method reinforceFilter'() {
        when:
            def returnedList = reinforceFilter()
        then:
            returnedList == [2, 4, 6, 8]
    }

    @Reinforce({ true })
    def reinforceClosureTrue() {
        println 'nice'
    }

    @Reinforce({ false })
    def reinforceClosureFalse() {
        println 'nice'
    }

    @Reinforce(value = { true }, failure = { throw new EnforcerException("not nice") })
    def reinforceClosureTrueWithFailureClosure() {
        println 'nice'
    }

    @Reinforce(value = { false }, failure = { throw new EnforcerException("nice") })
    def reinforceClosureFalseWithFailureClosure() {
        throw new Exception("this shouldn't happen on closureFalseWithFailureClosure")
    }

    @Reinforce(value = { true }, failure = { throw new EnforcerException("not nice") }, success = { println "nice" })
    def reinforceClosureTrueWithFailureAndSuccessClosures() {

    }

    @Reinforce(value = { false }, failure = { throw new EnforcerException("nice") }, success = { println "not nice" })
    def reinforceClosureFalseWithFailureAndSuccessClosures() {
        throw new Exception("this shouldn't happen on closureFalseWithFailureAndSuccessClosures")
    }

    @Reinforce({ number == 5 })
    def reinforceClosureTestingParameter(number) {
        println 'nice'
    }

    @Reinforce({ false })
    class TestEnforcer {
        @Reinforce(value = { false }, failure = { throw new EnforcerException("nice") })
        def clazzProtectedMethod1() {
            println 'not nice'
        }

        def clazzProtectedMethod2() {
            println 'not nice'
        }

        @Reinforce({ true })
        def methodProtectedMethod1() {
            println 'nice'
        }
    }

    @ReinforceFilter({ Object o -> (o as List).findResults { it % 2 == 0 ? it : null } })
    List<Integer> reinforceFilter() {
        [1, 2, 3, 4, 5, 6, 7, 8, 9]
    }
}
