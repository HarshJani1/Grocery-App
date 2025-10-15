//
//
package entity;
//
public class Product {
    private int id;
    private String name;
    private String description;
    private int like;
    private int dislike;
    private int price;

    // Default constructor required by Jackson
    public Product() {}

    // All-args constructor for convenience
    public Product(int id, String name, String description, int like, int dislike, int price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.like = like;
        this.dislike = dislike;
        this.price = price;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", like=" + like +
                ", dislike=" + dislike +
                ", price=" + price +
                '}';
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public int getLike() { return like; }
    public void setLike(int like) { this.like = like; }
    public int getDislike() { return dislike; }
    public void setDislike(int dislike) { this.dislike = dislike; }
    public int getPrice() { return price; }
    public void setPrice(int price) { this.price = price; }
}

//import lombok.*;
//
//@Data
////@NoArgsConstructor
//@Getter
//@Setter
////@AllArgsConstructor
//public class Product {
//    private int id;
//    private String name;
//    private String description;
//    private int like;
//    private int dislike;
//    private int price;
//
//    public Product(){}
//
//    public Product(int id, String name, String description, int like, int dislike, int price) {
//        this.id = id;
//        this.name = name;
//        this.description = description;
//        this.like = like;
//        this.dislike = dislike;
//        this.price = price;
//
//    }
//}
