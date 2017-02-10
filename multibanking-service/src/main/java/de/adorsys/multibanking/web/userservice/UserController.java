package de.adorsys.multibanking.web.userservice;

import de.adorsys.multibanking.domain.User;
import de.adorsys.multibanking.exception.ResourceNotFoundException;
import de.adorsys.multibanking.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

/**
 * Created by alexg on 07.02.17.
 */
@RestController
@RequestMapping(path = "api/v1/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserRepository userRepository;

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public Resource<User> getUser(@PathVariable(value = "id") String id) {

        User userEntry = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, id));

        return new Resource<>(userEntry);
    }

    @RequestMapping(method = RequestMethod.POST)
    public HttpEntity<Void> createUser(@RequestBody User user) {

        User persistedUser = userRepository.save(user);
        log.info("Neuen User [{}] angelegt.", persistedUser);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(linkTo(methodOn(UserController.class).getUser(persistedUser.getId())).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }
}
