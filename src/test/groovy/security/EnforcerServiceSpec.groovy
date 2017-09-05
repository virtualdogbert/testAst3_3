package security

import ast.virtualdogbert.EnforcerException
import ast.virtualdogbert.MockDomains
import com.security.*
import grails.testing.gorm.DataTest
import grails.testing.services.ServiceUnitTest
import spock.lang.Specification

class EnforcerServiceSpec extends Specification implements ServiceUnitTest<EnforcerService>, DataTest{

    User testUser, testUser2

    @MockDomains
    def setup() {
        mockDomains Role, User, UserRole, DomainRole, Sprocket
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

    //Testing EnforcerService
    void 'test enforce { true }'() {
        when:
            service.enforce({ true })
        then:
            true
    }

    void 'test enforce { false }'() {
        when:
            service.enforce({ false })
        then:
            EnforcerException e = thrown()
            e.message == 'Access Denied'
    }

    void 'test enforce { true }, { throw new EnforcerException("not nice") }'() {
        when:
            service.enforce({ true }, { throw new EnforcerException("not nice") })
        then:
            true
    }

    void 'test enforce { false }, { throw new EnforcerException("nice") }'() {
        when:
            service.enforce({ false }, { throw new EnforcerException("nice") })
        then:
            thrown EnforcerException
    }

    void 'test enforce { true }, { throw new EnforcerException("not nice")}, { println "nice" }'() {
        when:
            service.enforce({ true }, { throw new EnforcerException("not nice") }, { println "nice" })
        then:
            true
    }

    void 'test enforce { false }, { throw new EnforcerException("nice") }, { throw new EnforcerException("not nice") }'() {
        when:
            service.enforce({ false }, { throw new EnforcerException("nice") }, { println("not nice") })
        then:
            thrown EnforcerException
    }

    // For these tests you'll have to sub out the Sprocket domain for one that is in your application
    //Testing DomainRoleTrait
    void 'test enforce hasDomainRole("owner", domainObject, testUser)'() {
        when:
            Sprocket sprocket = new Sprocket(material: 'metal', creator: testUser).save(failOnError: true)
            service.changeDomainRole('owner', sprocket, testUser)
            service.enforce({ hasDomainRole('owner', sprocket, testUser) })
        then:
            true
    }

    void 'test fail enforce hasDomainRole("owner",domainObject, testUser)'() {
        when:
            Sprocket sprocket = new Sprocket(material: 'metal', creator: testUser).save(failOnError: true)
            service.changeDomainRole('owner', sprocket, testUser)
            service.enforce({ hasDomainRole('owner', sprocket, testUser2) })
        then:
            thrown EnforcerException
    }

    //Testing RoleTrait
    void 'test enforce hasRole("ROLE_ADMIN", testUser)'() {
        when:
            service.enforce({ hasRole('ROLE_ADMIN', testUser) })
        then:
            true
    }

    void 'test enforce hasRole("ROLE_USER", testUser)'() {
        when:
            service.enforce({ hasRole('ROLE_USER', testUser) })
        then:
            true
    }

    void 'test enforce hasRole ("ROLE_SUPER_USER", testUser)'() {
        when:
            service.enforce({ hasRole('ROLE_SUPER_USER', testUser) })
        then:
            thrown EnforcerException
    }
}
