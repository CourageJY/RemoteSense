package com.remote.changeDetection.repositories;


import com.remote.models.User;
import org.springframework.data.repository.CrudRepository;

public interface UserRepository extends CrudRepository<User,String> {
    User getUsersByEmail(String email);

    User getUsersByName(String name);
}
