package com.isonga.api.dto;

import lombok.Data;

@Data
public class DailrySummarry {
    final String fullname,idNumber,date_recieved;
    final double amount;
}
