package com.contact.contactapp.repo;

import com.contact.contactapp.domain.Contact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // This annotation is optional
public interface ContactRepo extends JpaRepository<Contact, String> {
    Optional<Contact> findById(String id); // Using Optional to handle both cases (contact found or not found)
}

// Remember: Basic CRUD is provided by JpaRepository
// If we need custom methods to work with our data, we declare them here. For ex: we declared findById() method