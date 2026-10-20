package com.giahung19.ecommerce_api.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer quantity;

    // Giá của sản phẩm tại thời điểm đặt hàng (snapshot), không đọc lại Product.price
    @Column(nullable = false)
    private BigDecimal priceAtOrder;

    // TODO(human): quan hệ N-1 tới Order. Khóa ngoại nằm ở bảng nào? Cột tên gì (@JoinColumn)?
    //              Nên để fetch LAZY hay EAGER, và vì sao? 
    @ManyToOne (fetch = FetchType.LAZY)
    @JoinColumn (name = "order_id",nullable = false)
    private Order order;

    // TODO(human): quan hệ N-1 tới Product, cùng câu hỏi về @JoinColumn và fetch.
    @ManyToOne (fetch= FetchType.LAZY)
    @JoinColumn (name ="product_id",nullable = false)
    private Product product;

    public OrderItem() {}

    public OrderItem(Integer quantity, BigDecimal priceAtOrder) {
        this.quantity = quantity;
        this.priceAtOrder = priceAtOrder;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getPriceAtOrder() { return priceAtOrder; }
    public void setPriceAtOrder(BigDecimal priceAtOrder) { this.priceAtOrder = priceAtOrder; }

    public Product getProduct(){
        return this.product;
    }
    public void setProduct(Product product){
        this.product=product;
    }

    public Order getOrder(){
        return this.order;
    }
    public void setOrder(Order order){
        this.order=order;
    }

}
