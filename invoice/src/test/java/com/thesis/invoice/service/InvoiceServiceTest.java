package com.thesis.invoice.service;

import com.thesis.invoice.common.Message;
import com.thesis.invoice.entities.FileData;
import com.thesis.invoice.exceptions.AppException;
import com.thesis.invoice.repositories.FileDataRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.TestPropertySource;


import java.sql.Date;
import java.util.UUID;


@SpringBootTest
@TestPropertySource(locations = "classpath:application-test.properties")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class InvoiceServiceTest {

    private static final String FOLDER_PATH = "C:/Users/Gabrysia/Invoices/";
    @Autowired
    InvoiceService invoiceService;

    @Autowired
    FileDataRepository fileDataRepository;

    FileData getExampleFileData() {
        return FileData.builder().
                name("invoice").
                type("pdf").
                date(Date.valueOf("2021-05-05")).
                description("invoice").
                filePath(FOLDER_PATH + "1").
                userId(UUID.fromString("e58ed763-928c-4155-bee9-fdbaaadc15f3")).
                id(1L).
                build();
    }

    @Test
    void viewInvoice() {
        FileData invoice = getExampleFileData();
        fileDataRepository.save(invoice);

        try {
            invoiceService.downloadImageFromFileSystem(1L, "e58ed763-928c-4155-bee9-fdbaaadc15f3");
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    @Test
    void viewInvoiceByUnauthorizedUser() {
        FileData invoice = getExampleFileData();
        fileDataRepository.save(invoice);


        Exception exception = Assertions.assertThrows(AppException.class, () -> {
                    invoiceService.downloadImageFromFileSystem(1L, "e58ed763-928c-4155-bee9-fdbaaadc15f1");
                }
        );
        String expectedMessage = "User is not owner of this file";
        String actualMessage = exception.getMessage();
        assert (expectedMessage.equals(actualMessage));

    }

    @Test
    void deleteNotExistingInvoice() {
        FileData invoice = getExampleFileData();
        fileDataRepository.save(invoice);

        Message message = invoiceService.deleteInvoice(4L, "e58ed763-928c-4155-bee9-fdbaaadc15f3");

        String expectedMessage = "resource not found";
        String actualMessage = message.getMessage();
        assert (expectedMessage.equals(actualMessage));
    }

    @Test
    void deleteInvoiceByUnauthorizedUser() {
        FileData invoice = getExampleFileData();
        fileDataRepository.save(invoice);


        Exception exception = Assertions.assertThrows(AppException.class, () -> {
                    invoiceService.deleteInvoice(1L, "e58ed763-928c-4155-bee9-fdbaaadc15f1");
                }
        );
        String expectedMessage = "User is not owner of this file";
        String actualMessage = exception.getMessage();
        assert (expectedMessage.equals(actualMessage));
    }
}
