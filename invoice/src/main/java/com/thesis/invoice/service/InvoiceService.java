package com.thesis.invoice.service;

import com.thesis.invoice.common.Message;
import com.thesis.invoice.entities.FileData;
import com.thesis.invoice.entities.InvoiceUpdateForm;
import com.thesis.invoice.exceptions.AppException;
import com.thesis.invoice.repositories.FileDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class InvoiceService {

    private final FileDataRepository fileDataRepository;
//    private final String FOLDER_PATH = "/var/lib/invoices/data";
    private final String FOLDER_PATH = "C:/Users/Gabrysia/Invoices/";


    public String uploadImageToFileSystem(MultipartFile file, String dateStr, String userId) throws IOException {
        String fileName = file.getOriginalFilename();
        if (fileName == null || fileName.isEmpty()) {
            throw new AppException("File name is empty", HttpStatus.BAD_REQUEST);
        }
        boolean fileAlreadyExists = fileDataRepository.existsByName(fileName);
        if(fileAlreadyExists){
            throw new AppException("File with this name already exists", HttpStatus.BAD_REQUEST);
        }
        Date date = Date.valueOf(dateStr);
        FileData fileData = fileDataRepository.save(FileData.builder().name(file.getOriginalFilename())
                .type(file.getContentType())
                .date(date)
                .userId(UUID.fromString(userId))
                .build());
        String filePath = FOLDER_PATH + fileData.getId();
        fileData.setFilePath(filePath);
        file.transferTo(new File(filePath));
        return "file uploaded successfully " + filePath;
    }

    public byte[] downloadImageFromFileSystem(Long fileId, String userId) throws IOException {
        boolean isOwner = isUserFileOwner(fileId, UUID.fromString(userId));
        if (!isOwner) {
            throw new AppException("User is not owner of this file", HttpStatus.FORBIDDEN);
        }
        Optional<FileData> fileData = fileDataRepository.findById(fileId);
        String filePath = fileData.get().getFilePath();
        byte[] images = Files.readAllBytes(new File(filePath).toPath());
        return images;
    }

    public boolean isUserFileOwner(Long fileId, UUID userId) {
        Optional<FileData> fileData = fileDataRepository.findById(fileId);
        if (fileData.isPresent()) {
            return fileData.get().getUserId().equals(userId);
        }
        return false;
    }

    public byte[] downloadImageFromFileSystemByDate(String date1, String date2) throws IOException {
        List<FileData> fileData = fileDataRepository.findFileDataByDateBetween(Date.valueOf(date1),Date.valueOf(date2));
        if(fileData.isEmpty()){
            return null;
        }
        String filePath = fileData.get(0).getFilePath();
        byte[] image = Files.readAllBytes(new File(filePath).toPath());

        return image;
    }

    public String updateInvoiceProperties(Long invoiceId, InvoiceUpdateForm invoiceUpdateForm){
        Optional<FileData> fileData = fileDataRepository.findById(invoiceId);
        if(fileData.isEmpty()){
            return "no such invoice";
        }
        FileData invoice = fileData.get();
        Date date = invoiceUpdateForm.getDate();
        String description = invoiceUpdateForm.getDescription();
        if (date!=null){
            invoice.setDate(date);
        }
        if (description!=null){
            invoice.setDescription(description);
        }
        return "Successfully updated invoice";
    }

    public List<FileData> getAllInvoices(String userId){
        return fileDataRepository.findFileDataByUserId(UUID.fromString(userId));
    }

    public Message deleteInvoice(Long id, String userId){
        boolean isOwner = isUserFileOwner(id, UUID.fromString(userId));
        Optional<FileData> invoice = fileDataRepository.findById(id);
        if(invoice.isEmpty()){
            return Message.builder().message("resource not found").status(HttpStatus.NOT_FOUND).build();
        }
        if (!isOwner) {
            throw new AppException("User is not owner of this file", HttpStatus.FORBIDDEN);
        }
        String filePath = invoice.get().getFilePath();
        try {
            Files.delete(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            return Message.builder().message("exception occurred during file deletion").status(HttpStatus.NOT_FOUND).build();
        }
        fileDataRepository.deleteById(id);
        return Message.builder().message("successfully deleted").status(HttpStatus.OK).build();
    }
}
