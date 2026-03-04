package service;

import model.Employee;

public class PayrollCalculator {

    public static final double PAGIBIG_FIXED = 100.00;

    // BUSINESS RULE: Calculate total gross including allowances
    public double calculateGrossIncome(Employee emp, double hoursWorked) {
        if (emp == null) return 0.0;
        
        double basicPay = emp.getHourlyRate() * hoursWorked;
        return basicPay + emp.getRiceSubsidy() + emp.getPhoneAllowance() + emp.getClothingAllowance();
    }

    // BUSINESS RULE: SSS Table Logic
    public double getSSSDeduction(double salary) {
        if (salary <= 3250) return 135.00;
        if (salary >= 24750) return 1125.00; 

        // Calculation for the steps in the SSS table
        double rangeStart = 3250;
        int steps = (int)((salary - rangeStart) / 500);
        return 135.00 + ((steps + 1) * 22.50);
    }
    
    // BUSINESS RULE: PhilHealth (4% total, divided by 2 for employee share)
    public double getPhilHealthDeduction(double basicSalary) {
        if (basicSalary <= 10000) return 200.00; 
        if (basicSalary >= 60000) return 1200.00;
        
        // Employee share is 2% (half of 4%)
        return basicSalary * 0.02;
    }

    public double getPagIBIGDeduction() {
        return PAGIBIG_FIXED;
    }

    // BUSINESS RULE: BIR Withholding Tax (Monthly Table)
    public double calculateWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20833) return 0.0;
        if (taxableIncome <= 33333) return (taxableIncome - 20833) * 0.20;
        if (taxableIncome <= 66667) return ((taxableIncome - 33333) * 0.25) + 2500.00;
        if (taxableIncome <= 166667) return ((taxableIncome - 66667) * 0.30) + 10833.33;
        if (taxableIncome <= 666667) return ((taxableIncome - 166667) * 0.32) + 40833.33;
        return ((taxableIncome - 666667) * 0.35) + 200833.33;
    }

    /**
     * The "Master" method that the UI calls.
     * It combines all the rules above into one final result.
     */
    public double calculateFullNetPay(Employee emp, double hoursWorked) {
        double gross = calculateGrossIncome(emp, hoursWorked);
        
        // 1. Calculate Government Deductions
        double sss = getSSSDeduction(emp.getBasicSalary());
        double ph = getPhilHealthDeduction(emp.getBasicSalary());
        double pagibig = getPagIBIGDeduction();
        
        double totalGovDeductions = sss + ph + pagibig;
        
        // 2. Taxable Income = Gross - Gov Deductions
        double taxableIncome = gross - totalGovDeductions;
        
        // 3. Subtract Tax
        double tax = calculateWithholdingTax(taxableIncome);
        
        return taxableIncome - tax;
    }

    public double getWithholdingTax(double taxableIncome) {
        if (taxableIncome <= 20833) return 0.0;
        if (taxableIncome <= 33333) return (taxableIncome - 20833) * 0.20;
        if (taxableIncome <= 66667) return ((taxableIncome - 33333) * 0.25) + 2500.00;
        if (taxableIncome <= 166667) return ((taxableIncome - 66667) * 0.30) + 10833.33;
        if (taxableIncome <= 666667) return ((taxableIncome - 166667) * 0.32) + 40833.33;
        return ((taxableIncome - 666667) * 0.35) + 200833.33;
    }
}