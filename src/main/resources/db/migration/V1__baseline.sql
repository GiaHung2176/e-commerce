-- V1: baseline schema, khớp với các entity tại thời điểm hoàn thành Block C.

CREATE TABLE categories (
    id          BIGINT       NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    PRIMARY KEY (id),
    CONSTRAINT uk_categories_name UNIQUE (name)
) ENGINE = InnoDB;

CREATE TABLE users (
    id       BIGINT                    NOT NULL AUTO_INCREMENT,
    username VARCHAR(255)              NOT NULL,
    password VARCHAR(255)              NOT NULL,
    role     ENUM ('ADMIN', 'CUSTOMER') NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_username UNIQUE (username)
) ENGINE = InnoDB;

CREATE TABLE tags (
    id   BIGINT       NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_tags_name UNIQUE (name)
) ENGINE = InnoDB;

CREATE TABLE products (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    name           VARCHAR(255)   NOT NULL,
    price          DECIMAL(38, 2) NOT NULL,
    stock_quantity INT,
    category_id    BIGINT,
    version        BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id)
) ENGINE = InnoDB;

CREATE TABLE product_tags (
    product_id BIGINT NOT NULL,
    tag_id     BIGINT NOT NULL,
    PRIMARY KEY (product_id, tag_id),
    CONSTRAINT fk_product_tags_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT fk_product_tags_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
) ENGINE = InnoDB;

CREATE TABLE profiles (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(255) NOT NULL,
    phone     VARCHAR(255),
    address   VARCHAR(255),
    user_id   BIGINT       NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_profiles_user UNIQUE (user_id),
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE TABLE orders (
    id         BIGINT NOT NULL AUTO_INCREMENT,
    order_date DATETIME(6),
    status     ENUM ('PENDING', 'COMPLETED', 'CANCELLED'),
    user_id    BIGINT,
    PRIMARY KEY (id),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE = InnoDB;

CREATE TABLE order_items (
    id             BIGINT         NOT NULL AUTO_INCREMENT,
    quantity       INT            NOT NULL,
    price_at_order DECIMAL(38, 2) NOT NULL,
    order_id       BIGINT         NOT NULL,
    product_id     BIGINT         NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id),
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id)
) ENGINE = InnoDB;
