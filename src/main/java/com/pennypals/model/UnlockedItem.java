package com.pennypals.model;

import jakarta.persistence.*;

@Entity
@Table(name = "unlocked_items")
public class UnlockedItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String itemName;

    @Column(nullable = false)
    private String itemType;

    @Column(nullable = false)
    private String colorHex;

    @Column(nullable = false)
    private String currencyType;

    @Column(nullable = false)
    private Integer cost;

    @Column(nullable = false)
    private Boolean isPurchased = false;

    @Column(nullable = false)
    private Boolean isEquipped = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    public UnlockedItem() {}

    public UnlockedItem(String itemName, String itemType, String colorHex, String currencyType, Integer cost, Boolean isPurchased, Boolean isEquipped, User user) {
        this.itemName = itemName;
        this.itemType = itemType;
        this.colorHex = colorHex;
        this.currencyType = currencyType;
        this.cost = cost;
        if (isPurchased != null) this.isPurchased = isPurchased;
        if (isEquipped != null) this.isEquipped = isEquipped;
        this.user = user;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public String getItemType() { return itemType; }
    public void setItemType(String itemType) { this.itemType = itemType; }
    public String getColorHex() { return colorHex; }
    public void setColorHex(String colorHex) { this.colorHex = colorHex; }
    public String getCurrencyType() { return currencyType; }
    public void setCurrencyType(String currencyType) { this.currencyType = currencyType; }
    public Integer getCost() { return cost; }
    public void setCost(Integer cost) { this.cost = cost; }
    public Boolean getIsPurchased() { return isPurchased; }
    public void setIsPurchased(Boolean isPurchased) { this.isPurchased = isPurchased; }
    public Boolean getIsEquipped() { return isEquipped; }
    public void setIsEquipped(Boolean isEquipped) { this.isEquipped = isEquipped; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}