package com.thesis.invoice.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class FileDataImage {
    FileData fileData;
    byte[] image;
}
