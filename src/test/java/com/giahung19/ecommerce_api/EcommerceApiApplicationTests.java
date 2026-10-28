package com.giahung19.ecommerce_api;

import com.giahung19.ecommerce_api.support.PostgreSqlTestContainerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Import(PostgreSqlTestContainerConfig.class)
class EcommerceApiApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	// 🧪 Test case 1: app khởi động được trên PostgreSQL container trống (Flyway chạy, Hibernate validate khớp schema)
	@Test
	void contextLoads() {
	}

	// 🧪 Test case 2: Flyway đã áp dụng V1 thành công và tạo đủ 8 bảng của entity
	@Test
	void flywayBaseline_ShouldBeAppliedAndCreateAllTables() {
		Integer successfulMigrations = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM flyway_schema_history WHERE version = '1' AND success = true",
				Integer.class);
		assertEquals(1, successfulMigrations);

		Integer entityTables = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = current_schema() "
						+ "AND table_name IN ('categories','users','tags','products','product_tags',"
						+ "'profiles','orders','order_items')",
				Integer.class);
		assertEquals(8, entityTables);
	}

}
