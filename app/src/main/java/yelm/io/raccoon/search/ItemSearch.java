package yelm.io.raccoon.search;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import yelm.io.raccoon.main.model.Modifier;
import yelm.io.raccoon.main.model.Specification;

public class ItemSearch implements Parcelable {

    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("specification")
    @Expose
    private List<Specification> specification = null;
    @SerializedName("modifier")
    @Expose
    private List<Modifier> modifier = null;
    @SerializedName("price")
    @Expose
    private String price;
    @SerializedName("images")
    @Expose
    private List<String> images = null;
    @SerializedName("preview_image")
    @Expose
    private String previewImage;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("unit_type")
    @Expose
    private String unitType;
    @SerializedName("quantity")
    @Expose
    private String quantity;
    @SerializedName("rating")
    @Expose
    private String rating;
    @SerializedName("discount")
    @Expose
    private String discount;
    @SerializedName("main_display")
    @Expose
    private String mainDisplay;
    @SerializedName("category_id")
    @Expose
    private String categoryId;
    @SerializedName("category")
    @Expose
    private String category;
    @SerializedName("subcategory_id")
    @Expose
    private String subcategoryId;
    @SerializedName("subcategory")
    @Expose
    private String subcategory;
    @SerializedName("platform")
    @Expose
    private String platform;


    protected ItemSearch(Parcel in) {
        id = in.readString();
        name = in.readString();
        description = in.readString();
        specification = in.createTypedArrayList(Specification.CREATOR);
        modifier = in.createTypedArrayList(Modifier.CREATOR);
        price = in.readString();
        images = in.createStringArrayList();
        previewImage = in.readString();
        status = in.readString();
        type = in.readString();
        unitType = in.readString();
        quantity = in.readString();
        rating = in.readString();
        discount = in.readString();
        mainDisplay = in.readString();
        categoryId = in.readString();
        category = in.readString();
        subcategoryId = in.readString();
        subcategory = in.readString();
        platform = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeTypedList(specification);
        dest.writeTypedList(modifier);
        dest.writeString(price);
        dest.writeStringList(images);
        dest.writeString(previewImage);
        dest.writeString(status);
        dest.writeString(type);
        dest.writeString(unitType);
        dest.writeString(quantity);
        dest.writeString(rating);
        dest.writeString(discount);
        dest.writeString(mainDisplay);
        dest.writeString(categoryId);
        dest.writeString(category);
        dest.writeString(subcategoryId);
        dest.writeString(subcategory);
        dest.writeString(platform);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<ItemSearch> CREATOR = new Creator<ItemSearch>() {
        @Override
        public ItemSearch createFromParcel(Parcel in) {
            return new ItemSearch(in);
        }

        @Override
        public ItemSearch[] newArray(int size) {
            return new ItemSearch[size];
        }
    };

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Specification> getSpecification() {
        return specification;
    }

    public void setSpecification(List<Specification> specification) {
        this.specification = specification;
    }

    public List<Modifier> getModifier() {
        return modifier;
    }

    public void setModifier(List<Modifier> modifier) {
        this.modifier = modifier;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public String getPreviewImage() {
        return previewImage;
    }

    public void setPreviewImage(String previewImage) {
        this.previewImage = previewImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getDiscount() {
        return discount;
    }

    public void setDiscount(String discount) {
        this.discount = discount;
    }

    public String getMainDisplay() {
        return mainDisplay;
    }

    public void setMainDisplay(String mainDisplay) {
        this.mainDisplay = mainDisplay;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(String subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public String getPlatform() {
        return platform;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }
}
