package de.adorsys.multibanking.web;

import de.adorsys.multibanking.domain.UserEntity;
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
@CrossOrigin(origins = "*")
@RequestMapping(path = "api/v1/users")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    @Autowired
    UserRepository userRepository;

    @RequestMapping(value = "/{userId}", method = RequestMethod.GET)
    public Resource<UserEntity> getUser(@PathVariable(value = "userId") String userId) {

        UserEntity userEntry = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(UserEntity.class, userId));

        return new Resource<>(userEntry);
    }

    @RequestMapping(method = RequestMethod.POST)
    public HttpEntity<Void> createUser(@RequestBody UserEntity user) {

        UserEntity persistedUser = userRepository.save(user);
        log.info("Neuen UserEntity [{}] angelegt.", persistedUser);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(linkTo(methodOn(UserController.class).getUser(persistedUser.getId())).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }
}
