package com.ewallet.UserService.Controller;

import com.ewallet.UserService.Model.User;
import com.ewallet.UserService.Service.UserService;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.logging.Logger;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @ApiOperation(value = "Find all the User")
    @GetMapping("/users")
    List<User> findAll() {
        return userService.findAll();
    }

    @ApiOperation(value = "Register New User")
    @PostMapping("/createUser")
    //return 201 instead of 200
    @ResponseStatus(HttpStatus.CREATED)
    User newUser(@RequestBody User newUser) {
        return userService.saveUser(newUser);
    }

    // Find a given user
    @ApiOperation(value = "Find User by Id ")
    @GetMapping("/users/{id}")
    User findOne(@PathVariable int id) {
        return userService.findUserById(id);
    }
}
