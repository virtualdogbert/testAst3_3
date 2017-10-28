package testast

import com.security.Role
import com.security.Sprocket
import com.security.User
import com.security.UserRole

class BootStrap {

    def init    = { servletContext ->
        def userRole = new Role('ROLE_USER').save(flush: true, failOnError: true)
        def adminRole = new Role('ADMIN_USER').save(flush: true, failOnError: true)
        User testUser = new User(username: 'me', password: 'password').save(flush: true, failOnError: true)
        User testUser2 = new User(username: 'me2', password: 'password').save(flush: true, failOnError: true)

        UserRole.create testUser, adminRole, true
        UserRole.create testUser, userRole, true

        UserRole.create testUser2, userRole, true
        Sprocket sprocket = new Sprocket(material: 'metal', creator: testUser).save(failOnError: true)

    }
    def destroy = {
    }
}
