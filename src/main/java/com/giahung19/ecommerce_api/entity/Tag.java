package com.giahung19.ecommerce_api.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "tags")
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    // TODO(human): quan hệ N-N ngược lại tới Product: Set<Product> products, khởi tạo = new HashSet<>().
    //              Tag là inverse side hay owning side? Vì vậy @ManyToMany ở đây cần thuộc tính gì, trỏ tới field nào của Product?
    //              Có nên cascade ở phía này không, vì sao?
    @ManyToMany (mappedBy = "tags")
    private Set<Product> products =new HashSet<>();

    public Tag() {}

    public Tag(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Set<Product> getProducts(){
        return this.products;
    }

    public void setProducts(Set<Product> products){
        this.products=products;
    }


}
