package com.thesis.invoice.controllers;

import com.thesis.invoice.common.Message;
import com.thesis.invoice.entities.FileData;
import com.thesis.invoice.entities.InvoiceUpdateForm;
import com.thesis.invoice.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/invoice")
@RequiredArgsConstructor
@CrossOrigin
public class InvoiceController {

    private final InvoiceService invoiceService;


    @PostMapping("/fileSystem")
    public ResponseEntity<?> uploadImageToFIleSystem(@RequestParam("image") MultipartFile file, @RequestParam("date") String date) throws IOException {
        String uploadImage = invoiceService.uploadImageToFileSystem(file, date);
        return ResponseEntity.status(HttpStatus.OK)
                .body(uploadImage);
    }

    @GetMapping("/fileSystem")
    public ResponseEntity<?> getAllInvoicesDetails() {
        List<FileData> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.status(HttpStatus.OK)
                .body(invoices);
    }


    @GetMapping("/fileSystem/{fileName}")
    public ResponseEntity<?> downloadImageFromFileSystem(@PathVariable String fileName) throws IOException {
        byte[] imageData;
        try {
            imageData = invoiceService.downloadImageFromFileSystem(fileName);
        }
        catch(NoSuchElementException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf("image/png"))
                .body(imageData);

    }

    @PutMapping(path = "/fileSystem/{invoiceId}")
    public ResponseEntity<String> updateInvoiceProperties(@PathVariable Long invoiceId,
                                                          @RequestBody InvoiceUpdateForm invoiceUpdateForm){
        String message = invoiceService.updateInvoiceProperties(invoiceId,invoiceUpdateForm);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @GetMapping("/fileSystem/byDate/{dateFrom}/{dateTo}")
    public ResponseEntity<?> downloadImageFromFileSystemByDate(@PathVariable String dateFrom, @PathVariable String dateTo ) throws IOException {
        byte[] imageData = invoiceService.downloadImageFromFileSystemByDate(dateFrom, dateTo);
        if(imageData==null){
            return ResponseEntity.status(HttpStatus.OK).body("No invoice between these dates");
        }
        return ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf("image/png"))
                .body(imageData);
    }

    @DeleteMapping("/fileSystem/{invoiceId}")
    public ResponseEntity<String> deleteInvoiceFromFileSystem(@PathVariable Long invoiceId){
            Message message = invoiceService.deleteInvoice(invoiceId);
            return new ResponseEntity<>(message.getMessage(), message.getStatus());
    }

    @GetMapping("/")
    public String test(){
        return "Hello";
    }

}
