package com.contact.contactapp.service;

import com.contact.contactapp.domain.Contact;
import com.contact.contactapp.repo.ContactRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

import static com.contact.contactapp.constant.Constant.PHOTO_DIRECTORY;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

@Service
@Slf4j // Automatically generate a logger field in the class - used for logging purposes
@Transactional(rollbackOn = Exception.class) // Transaction should be rolled back if any unchecked exception occurs during the method execution.
@RequiredArgsConstructor // Constructor injection
public class ContactService {
    private final ContactRepo contactRepo;

    public Page<Contact> getAllContacts(int page, int size) {
        return contactRepo.findAll(PageRequest.of(page, size, Sort.by("name")));
    }

    public Contact getContact(String id) {
        return contactRepo.findById(id).orElseThrow(() -> new RuntimeException("Contact not found!"));
    }

    public Contact createContact(Contact contact) {
        return contactRepo.save(contact);
    }

    public String deleteContact(Contact contact) {
        contactRepo.delete(contact);
        return("Contact deleted!");
    }

    public String uploadPhoto(String id, MultipartFile file) { // MultipartFile is an interface used for file uploads
        log.info("Saving picture for user ID: {}" + id);
        Contact contact = getContact(id); // Gets the contact or throws error if don't exist
        String photoUrl = photoFunction.apply(id, file);
        contact.setPhotoUrl(photoUrl);
        contactRepo.save(contact);
        return photoUrl;
    }

    // Function Interface that takes String ("file name") and returns String ("file extension")
    private final Function<String, String> fileExtension = fileName -> Optional.of(fileName)
            .filter(name -> name.contains(".")) // Check if the file name contains "."
            .map(name -> "." + name.substring(fileName.lastIndexOf(".") + 1)) // Substring for the file extension
            .orElse(".png"); // Default value - if the file name does not contain "." or the file name is empty

    // BiFunction Interface that takes String ("id") and MultipartFile ("image file") as parameters
    // and returns String ("url of the saved image file")
    private final BiFunction<String, MultipartFile, String> photoFunction = (id, image) -> {
        String filename = id + fileExtension.apply(image.getOriginalFilename());
        try {
            Path fileStorageLocation = Paths.get(PHOTO_DIRECTORY).toAbsolutePath().normalize();
            if (!Files.exists(fileStorageLocation)) {
                Files.createDirectories(fileStorageLocation);
            }
            // Copy image to the file storage location
            Files.copy(image.getInputStream(), fileStorageLocation.resolve(filename), REPLACE_EXISTING);
            return ServletUriComponentsBuilder // Build image url for the saved image file
                    .fromCurrentContextPath()
                    .path("/contacts/image/" + filename).toUriString();
        }
        catch (Exception e) {
            throw new RuntimeException("Unable to save image!");
        }
    };
}
