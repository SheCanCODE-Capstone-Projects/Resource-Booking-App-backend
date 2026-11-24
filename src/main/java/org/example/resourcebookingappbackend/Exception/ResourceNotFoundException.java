package org.example.resourcebookingappbackend.Exception; // folder (package) where this file lives

// error used when a resource id does not exist
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(Long id) { // constructor that receives the missing id
        super("Resource with id " + id + " was not found"); // build the error message
    }
}