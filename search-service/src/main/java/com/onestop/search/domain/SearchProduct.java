package com.onestop.search.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/** Denormalized product row used for search. The id is the catalog product id. */
@Entity
@Table(name = "search_product")
@Getter
@Setter
@NoArgsConstructor
public class SearchProduct {

    @Id
    @Column(name = "product_id")
    private Long productId;

    @Column(nullable = false)
    private String name;

    @Column(name = "brand_name")
    private String brandName;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "package_size")
    private String packageSize;

    @Column(name = "selling_price")
    private BigDecimal sellingPrice;

    private BigDecimal mrp;
}
