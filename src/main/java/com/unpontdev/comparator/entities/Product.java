package com.unpontdev.comparator.entities;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

/**
 * Helds the product object
 */
@Entity(name= "product")
@Table(name="product")
public class Product {


    @Id
    @Column(name="prod_id")
    private String pId;

    private String productSource;

    private String productName;

    private String productUrl;

    private String productId;

    private String productStock;

    private String productSku;

    private String productMainImage;

    private String productDescription;

    private String productBrand;

    private Double price;

    private Double oldPrice;

    private LocalDateTime addedOn;

    private Long termId;

    @OneToMany(mappedBy = "products")
    private Set<ProductImages> productImages;

    /** constructor for product object
     * @param id
     * @param productSource
     * @param productName
     * @param productUrl
     * @param productId
     * @param productStock
     * @param productSku
     * @param productMainImage
     * @param productDescription
     * @param productBrand
     * @param price
     * @param oldPrice
     * @param addedOn
     * @param termId
     */
    public Product(String id, String productSource, String productName, String productUrl,
                   String productId, String productStock, String productSku, String productMainImage,
                   String productDescription, String productBrand, Double price, Double oldPrice,
                   LocalDateTime addedOn, Long termId) {
        this.pId = id;
        this.productSource = productSource;
        this.productName = productName;
        this.productUrl = productUrl;
        this.productId = productId;
        this.productStock = productStock;
        this.productSku = productSku;
        this.productMainImage = productMainImage;
        this.productDescription = productDescription;
        this.productBrand = productBrand;
        this.price = price;
        this.oldPrice = oldPrice;
        this.addedOn = addedOn;
        this.termId = termId;
;
    }
    public Product(){}

    /**Overiding equals and hashCode methods
     * @param o
     * @return
     */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Product)) return false;
        Product product = (Product) o;
        return pId.equals(product.pId) && getProductSource().equals(product.getProductSource()) &&
                getProductName().equals(product.getProductName()) &&
                getProductUrl().equals(product.getProductUrl()) &&
                getProductId().equals(product.getProductId()) &&
                getProductStock().equals(product.getProductStock()) &&
                Objects.equals(getProductSku(), product.getProductSku()) &&
                Objects.equals(getProductMainImage(), product.getProductMainImage()) &&
                Objects.equals(getProductDescription(), product.getProductDescription()) &&
                Objects.equals(getProductBrand(), product.getProductBrand()) &&
                getPrice().equals(product.getPrice()) &&
                Objects.equals(getOldPrice(), product.getOldPrice()) &&
                getAddedOn().equals(product.getAddedOn()) &&
                termId.equals(product.termId) && Objects.equals(getProductImages(), product.getProductImages());
    }

    @Override
    public int hashCode() {
        return Objects.hash(pId, getProductSource(), getProductName(), getProductUrl(),
                getProductId(), getProductStock(), getProductSku(), getProductMainImage(),
                getProductDescription(), getProductBrand(), getPrice(), getOldPrice(),
                getAddedOn(), termId, getProductImages());
    }

    /**
     * Setter and getters for product data
     * @return
     */
    public String getpId() {
        return pId;
    }

    public void setpId(String pId) {
        this.pId = pId;
    }

    public String getProductSource() {
        return productSource;
    }

    public void setProductSource(String productSource) {
        this.productSource = productSource;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductUrl() {
        return productUrl;
    }

    public void setProductUrl(String productUrl) {
        this.productUrl = productUrl;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductStock() {
        return productStock;
    }

    public void setProductStock(String productStock) {
        this.productStock = productStock;
    }

    public String getProductSku() {
        return productSku;
    }

    public void setProductSku(String productSku) {
        this.productSku = productSku;
    }

    public String getProductMainImage() {
        return productMainImage;
    }

    public void setProductMainImage(String productMainImage) {
        this.productMainImage = productMainImage;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public String getProductBrand() {
        return productBrand;
    }

    public void setProductBrand(String productBrand) {
        this.productBrand = productBrand;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(Double oldPrice) {
        this.oldPrice = oldPrice;
    }

    public LocalDateTime getAddedOn() {
        return addedOn;
    }

    public void setAddedOn(LocalDateTime addedOn) {
        this.addedOn = addedOn;
    }

    public Long getTermId() {
        return termId;
    }

    public void setTermId(Long termId) {
        this.termId = termId;
    }

    public Set<ProductImages> getProductImages() {
        return productImages;
    }

    public void setProductImages(Set<ProductImages> productImages) {
        this.productImages = productImages;
    }
}
