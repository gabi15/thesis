package com.thesis.invoice.controllers;

import com.thesis.invoice.common.Message;
import com.thesis.invoice.entities.FileData;
import com.thesis.invoice.entities.FileDataImage;
import com.thesis.invoice.entities.InvoiceUpdateForm;
import com.thesis.invoice.exceptions.AppException;
import com.thesis.invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.security.RolesAllowed;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/invoice")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;


    @PostMapping("/fileSystem")
    public ResponseEntity<?> uploadImageToFIleSystem(@RequestParam("image") MultipartFile file, @RequestParam("date") String date, @AuthenticationPrincipal Jwt principal) throws IOException {
        String uploadImage;
        try {
            uploadImage = invoiceService.uploadImageToFileSystem(file, date, principal.getSubject());
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(uploadImage);
    }

    @GetMapping("/fileSystem")
    public ResponseEntity<?> getAllInvoicesDetails() {
        List<FileData> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.status(HttpStatus.OK)
                .body(invoices);
    }

    @GetMapping("/fileSystem/{id}")
    public ResponseEntity<?> getInvoiceImageById(@PathVariable Long id, @AuthenticationPrincipal Jwt principal) {
        byte[] image;
        try {
            image = invoiceService.downloadImageFromFileSystem(id, principal.getSubject());
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("something went wrong with downloading the files");
        } catch (AppException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.OK)
//                .contentType(MediaType.valueOf(fileDataImage.getFileData().getType()))
                .body(image);
    }


//    @GetMapping("/fileSystem/{fileName}")
//    public ResponseEntity<?> downloadImageFromFileSystem(@PathVariable String fileName) throws IOException {
//        byte[] imageData;
//        try {
//            imageData = invoiceService.downloadImageFromFileSystem(fileName);
//        }
//        catch(NoSuchElementException e){
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//        return ResponseEntity.status(HttpStatus.OK)
//                .contentType(MediaType.valueOf("image/png"))
//                .body(imageData);
//
//    }

    @PutMapping(path = "/fileSystem/{invoiceId}")
    public ResponseEntity<String> updateInvoiceProperties(@PathVariable Long invoiceId,
                                                          @RequestBody InvoiceUpdateForm invoiceUpdateForm) {
        String message = invoiceService.updateInvoiceProperties(invoiceId, invoiceUpdateForm);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @GetMapping("/fileSystem/byDate/{dateFrom}/{dateTo}")
    public ResponseEntity<?> downloadImageFromFileSystemByDate(@PathVariable String dateFrom, @PathVariable String dateTo) throws IOException {
        byte[] imageData = invoiceService.downloadImageFromFileSystemByDate(dateFrom, dateTo);
        if (imageData == null) {
            return ResponseEntity.status(HttpStatus.OK).body("No invoice between these dates");
        }
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf("image/png"))
                .body(imageData);
    }

    @DeleteMapping("/fileSystem/{invoiceId}")
    public ResponseEntity<String> deleteInvoiceFromFileSystem(@PathVariable Long invoiceId) {
        Message message = invoiceService.deleteInvoice(invoiceId);
        return new ResponseEntity<>(message.getMessage(), message.getStatus());
    }

    @GetMapping("/")
    @RolesAllowed({"invoice_read"})
    public String test() {
        return "Hello";
    }

}
