package com.isonga.api.services;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.isonga.api.models.Savings;
import com.isonga.api.repositories.SavingsRepository;
import com.isonga.api.repositories.UserRepository;

@Service
public class ExcelImportService {

    @Autowired
    private SavingsRepository savingsRepository;

    @Autowired
    private UserRepository userRepository;

    LocalDate recordDate;

    @Transactional
    public void importHistoricalSavings(InputStream is) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue;
                if (row.getRowNum() == 1) {

                    if (row.getCell(2) == null) {
                        throw new IllegalArgumentException("No date was specified on this sheet");
                    }

                    recordDate = row.getCell(2).getLocalDateTimeCellValue().toLocalDate();

                }
                // Validate that the cells are not null before processing
                if (row.getCell(0) == null || row.getCell(1) == null || row.getCell(2) == null) {
                    continue; 
                }

                // Col 0: User ID (String), Col 1: Total Amount (Numeric), Col 2: Date (Excel Date)
                String userId = row.getCell(0).getStringCellValue();
                BigDecimal totalAmount = BigDecimal.valueOf(row.getCell(1).getNumericCellValue());
                
                // Historical Date Processing
                LocalDate historicalDate = row.getCell(2).getLocalDateTimeCellValue().toLocalDate();
                int weekNum = historicalDate.get(WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear());
                int year = historicalDate.getYear();

                BigDecimal ingoboka = new BigDecimal("200.00");
                BigDecimal netAmount = totalAmount.subtract(ingoboka);

                // Check if user exists before saving to maintain data integrity
                if (userRepository.findByIdNumber(userId).isPresent()) {
                    Savings savings = Savings.builder()
                            .userIdNumber(userId)
                            .amount(netAmount)
                            .ingoboka(ingoboka)
                            .dateReceived(historicalDate)
                            .weekNumber(weekNum)
                            .year(year)
                            .createdAt(LocalDateTime.now())
                            .build();

                    savingsRepository.save(savings);
                }
            }
        }
    }
}