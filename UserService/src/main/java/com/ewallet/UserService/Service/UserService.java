package com.ewallet.UserService.Service;

import com.ewallet.UserService.Exception.UserNotFoundException;
import com.ewallet.UserService.Model.User;
import com.ewallet.UserService.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

@Service
public class UserService {

    private final static Logger LOGGER = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

    @Autowired
    private UserRepository userRepository;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User saveUser(User newUser) {
        return userRepository.save(newUser);
    }

    public User findUserById(int id) {
        LOGGER.info("/users/{id} called with id "+ id);
//         Optional<User> user = userRepository.findById(id);
//         return user;
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
