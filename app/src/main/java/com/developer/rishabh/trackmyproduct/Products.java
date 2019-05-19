package com.developer.rishabh.trackmyproduct;

public class Products {

    public Products(String productId, String image, Double oldPrice, Double newPrice, String title, String companyName) {
        this.productId = productId;
        this.image = image;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.title = title;
        this.companyName = companyName;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getImage() {
        return image;
    }

    public Double getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(Double oldPrice) {
        this.oldPrice = oldPrice;
    }

    public Double getNewPrice() {
        return newPrice;
    }

    public void setNewPrice(Double newPrice) {
        this.newPrice = newPrice;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    String productId;
    String image;
    Double oldPrice;
    Double newPrice;
    String title;
    String companyName;
}
