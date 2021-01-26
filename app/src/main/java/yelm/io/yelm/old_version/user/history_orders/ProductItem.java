
package yelm.io.yelm.old_version.user.history_orders;


public class ProductItem {

    private Integer item;

    private String name;

    private String type;

    private String image;

    private Integer price;

    private Integer count;

    private Integer fullPrice;

    public ProductItem(Integer item, String name, String type, Integer price, Integer count, Integer fullPrice, String image ) {
        this.item = item;
        this.name = name;
        this.type = type;
        this.image = image;
        this.price = price;
        this.count = count;
        this.fullPrice = fullPrice;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Integer getItem() {
        return item;
    }

    public void setItem(Integer item) {
        this.item = item;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public Integer getFullPrice() {
        return fullPrice;
    }

    public void setFullPrice(Integer fullPrice) {
        this.fullPrice = fullPrice;
    }

}
