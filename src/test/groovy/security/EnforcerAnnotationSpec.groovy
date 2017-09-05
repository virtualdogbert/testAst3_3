package security

import ast.virtualdogbert.Enforce
import ast.virtualdogbert.EnforcerException
import ast.virtualdogbert.MockDomains
import com.security.*
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification
//@Mock([Role, User, UserRole, DomainRole, Sprocket])
class EnforcerAnnotationSpec extends Specification implements ServiceUnitTest<EnforcerService>{

    User testUser, testUser2

    @MockDomains
    def setup() {
        //mockDomains Role, User, UserRole, DomainRole, Sprocket
        def adminRole = new Role('ROLE_ADMIN').save(flush: true, failOnError: true)
                def userRole = new Role('ROLE_USER').save(flush: true, failOnError: true)
                testUser = new User(username: 'me', password: 'password').save(flush: true, failOnError: true)
                testUser2 = new User(username: 'me2', password: 'password').save(flush: true, failOnError: true)

                UserRole.create testUser, adminRole, true
                UserRole.create testUser, userRole, true

                UserRole.create testUser2, userRole, true

                service.springSecurityService = new Expando()
                service.springSecurityService.getCurrentUser = { -> testUser }

                //This enables Enforcer for unit tests because it is turned off by default.
                grailsApplication.config.enforcer.enabled = true
    }


    //Testing Enforce AST transform
    void 'test method closureTrue'() {
        when:
            closureTrue()
        then:
            true
    }

    void 'test method closureTrueWithFailureClosure'() {
        when:
            closureTrueWithFailureClosure()
        then:
            true
    }

    void 'test method closureFalseWithFailureClosure'() {
        when:
            closureFalseWithFailureClosure()
        then:
            thrown EnforcerException
    }

    void 'test method closureTrueWithFailureAndSuccessClosures'() {
        when:
            closureTrueWithFailureAndSuccessClosures()
        then:
            true
    }

    void 'test method closureFalseWithFailureAndSuccessClosures'() {
        when:
            closureFalseWithFailureAndSuccessClosures()
        then:
            thrown EnforcerException
    }

    void 'test method closureTestingParameter'() {
        when:
            closureTestingParameter(5)
        then:
            true
    }

    void 'test method closureFilterParameter'() {
        when:
            def test = closureFilterParameter([1, 2, 3, 4, 5, 6, 7, 8])
        then:
            test == [2, 4, 6, 8]
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

    //Test methods for testing Enforce AST transform
    @Enforce({ true })
    def closureTrue() {
        println 'nice'
    }

    @Enforce(value = { true }, failure = { throw new EnforcerException("not nice") })
    def closureTrueWithFailureClosure() {
        println 'nice'
    }

    @Enforce(value = { false }, failure = { throw new EnforcerException("nice") })
    def closureFalseWithFailureClosure() {
        throw new Exception("this shouldn't happen on closureFalseWithFailureClosure")
    }

    @Enforce(value = { true }, failure = { throw new EnforcerException("not nice") }, success = { println "nice" })
    def closureTrueWithFailureAndSuccessClosures() {

    }

    @Enforce(value = { false }, failure = { throw new EnforcerException("nice") }, success = { println "not nice" })
    def closureFalseWithFailureAndSuccessClosures() {
        throw new Exception("this shouldn't happen on closureFalseWithFailureAndSuccessClosures")
    }

    @Enforce({ number == 5 })
    def closureTestingParameter(number) {
        println 'nice'
    }

    @Enforce(
            {
                numbers = numbers.findResults { it % 2 == 0 ? it : null }
                return true
            }
    )
    def closureFilterParameter(List numbers) {
        return numbers
    }

    @Enforce({ false })
    class TestEnforcer {
        @Enforce(value = { false }, failure = { throw new EnforcerException("nice") })
        def clazzProtectedMethod1() {
            println 'not nice'
        }

        def clazzProtectedMethod2() {
            println 'not nice'
        }

        @Enforce({ true })
        def methodProtectedMethod1() {
            println 'nice'
        }
    }

    // For these tests you'll have to sub out the Sprocket domain for one that is in your application
     //Testing DomainRoleTrait
     void 'test enforce hasDomainRole("owner", domainObject, testUser)'() {
         when:
             Sprocket sprocket = new Sprocket(material: 'metal', creator: testUser).save(failOnError: true)
             service.changeDomainRole('owner', sprocket, testUser)
             testHasDomainRole(sprocket, testUser)
         then:
             true
     }


    @Enforce({ hasDomainRole('owner', sprocket, testUser) })
    def testHasDomainRole(Sprocket sprocket, User testUser) {
        return true
    }
}
