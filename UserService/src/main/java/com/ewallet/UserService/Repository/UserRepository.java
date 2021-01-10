package com.ewallet.UserService.Repository;

import com.ewallet.UserService.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
}
