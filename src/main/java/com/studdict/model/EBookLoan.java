package com.studdict.model;

import javax.print.attribute.standard.DateTimeAtCompleted;
import javax.print.attribute.standard.DateTimeAtCreation;

public class EBookLoan {
    private String loanId;
    private DateTimeAtCreation startTime;
    private DateTimeAtCompleted endTime;
    private boolean isActive;

    private EBookLicense license;
}