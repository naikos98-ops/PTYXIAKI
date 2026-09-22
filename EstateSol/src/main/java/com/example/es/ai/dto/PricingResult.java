package com.example.es.ai.dto;

public class PricingResult {

    private Double rent;
    private Double sale;

    public PricingResult(Double rent, Double sale) {
        this.rent = rent;
        this.sale = sale;
    }

    public PricingResult() {

    }

    public Double getRent() {
        return rent;
    }

    public void setRent(Double rent) {
        this.rent = rent;
    }

    public Double getSale() {
        return sale;
    }

    public void setSale(Double sale) {
        this.sale = sale;
    }

    private Double renovationCost;

    public Double getRenovationCost() {
        return renovationCost;
    }

    public void setRenovationCost(Double renovationCost) {
        this.renovationCost = renovationCost;
    }
}