
package com.grocery.service_product.entity;

import jakarta.persistence.*;
import lombok.Builder;

import java.math.BigDecimal;


@Entity
@Builder
@Table(name = "PRODUCTS")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200,name="NAME")
    private String name;

    @Column(nullable = false,name="DESCRIPTION")
    private String description;

    @Column(name="LIKES")
    private Long likes;

    @Column(name="DISLIKES")
    private Long dislikes;

    @Column(name="PRICE")
    private BigDecimal price;

    @Column(name="IMAGE",length = 16777215)
    private byte[] image;

    // Default constructor required by Jackson
    public Product() {}

    // All-args constructor for convenience
    public Product(Long id, String name, String description, Long like, Long dislike, BigDecimal price,byte[] image) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.likes = like;
        this.dislikes = dislike;
        this.price = price;
        this.image = image;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", like=" + likes +
                ", dislike=" + dislikes +
                ", price=" + price +
                ", image=" + image +
                '}';
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getLike() { return likes; }
    public void setLike(Long like) { this.likes = like; }
    public Long getDislike() { return dislikes; }
    public void setDislike(Long dislike) { this.dislikes = dislike; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public byte[] getImage() { return image; }
    public void setImage(byte[] image) { this.image = image; }
}


